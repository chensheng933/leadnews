package com.heima.article.service.service;

import ch.qos.logback.core.pattern.color.RedCompositeConverter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.RedisConstants;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.HotArticleVo;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public ResponseResult loadApArticle(ArticleDto dto, int type) {
        if(dto.getMinBehotTime()==null) dto.setMinBehotTime(new Date());
        if(dto.getMaxBehotTime()==null) dto.setMaxBehotTime(new Date());
        if(dto.getSize()==null) dto.setSize(10);
        if(dto.getTag()==null) dto.setTag("__all__");

        List<ApArticle> articleList = apArticleMapper.loadApArticle(dto,type);
        return ResponseResult.okResult(articleList);
    }

    @Override
    public Long saveApArticle(ApArticleDto dto) {
        ApArticle apArticle = BeanHelper.copyProperties(dto,ApArticle.class);

        //判断新增还是修改
        if(dto.getId()==null){
            //新增
            //新增ap_article表
            save(apArticle);

            //新增ap_article_config表
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(apArticle.getId());
            apArticleConfig.setIsDelete(false);
            apArticleConfig.setIsDown(false);
            apArticleConfig.setIsComment(true);
            apArticleConfig.setIsForward(true);
            apArticleConfigMapper.insert(apArticleConfig);

            //新增ap_article_content表
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);

        }else{
            //修改

            //修改ap_article表
            updateById(apArticle);

            //修改ap_article_content表
            QueryWrapper<ApArticleContent> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("article_id",dto.getId());
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(queryWrapper);
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }

        //不管新增还是修改，都需要重新生成静态页
        articleFreemarkerService.buildArticleToMinIO(apArticle,dto.getContent());

        return apArticle.getId();
    }

    @Override
    public ResponseResult loadApArticle2(ArticleDto dto, int type) {
        //查询Redis数据
        String key = RedisConstants.HOT_ARTICLE_FIRST_PAGE+dto.getTag();
        String redisData = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotEmpty(redisData)){
            List<HotArticleVo> hotArticleVoList = JsonUtils.toList(redisData, HotArticleVo.class);
            List<ApArticle> articleList = BeanHelper.copyWithCollection(hotArticleVoList,ApArticle.class);
            return ResponseResult.okResult(articleList);
        }

        //查询数据库
        return loadApArticle(dto,type);
    }
}
