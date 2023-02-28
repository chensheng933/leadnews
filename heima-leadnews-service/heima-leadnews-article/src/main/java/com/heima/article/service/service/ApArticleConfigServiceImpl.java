package com.heima.article.service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.utils.common.BeanHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {

    @Override
    public void updateArticleConfig(Map msg) {
        //取出参数
        Long articleId = (Long)msg.get("articleId");
        Integer enable = (Integer) msg.get("enable");

        UpdateWrapper<ApArticleConfig> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("article_id",articleId);//条件
        updateWrapper.set("is_down",enable==1?0:1);//数据修改
        update(updateWrapper);
    }
}
