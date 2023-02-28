package com.heima.article;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.utils.common.JsonUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ArticleApplication.class)
public class ArticleTest {
    @Autowired
    private Configuration configuration;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private MinIOFileStorageService storageService;
    @Autowired
    private ApArticleMapper apArticleMapper;
    /**
     * 为文章生成静态详情页
     */
    @Test
    public void testCreateStaticPage() throws Exception {
        Long id = 1383827787629252610L;

        //根据id查询文章内容
        QueryWrapper<ApArticleContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id",id);
        ApArticleContent articleContent = apArticleContentMapper.selectOne(queryWrapper);

        if(articleContent!=null && StringUtils.isNotEmpty(articleContent.getContent())){
            //准备和读取模板
            Template template = configuration.getTemplate("article.ftl");

            //准备文章内容数据
            Map<String,Object> data = new HashMap<>();
            //取出文章内容
            String contentText = articleContent.getContent();
            List<Map> content = JsonUtils.toList(contentText,Map.class);
            data.put("content",content);

            //填充数据，生成页面
            StringWriter writer = new StringWriter();
            template.process(data,writer);//生成静态页，写入StringWriter缓存对象

            //把页面存储到MinIO
            InputStream inputStream = new ByteArrayInputStream(writer.toString().getBytes());
            String url = storageService.uploadHtmlFile("",id+".html",inputStream);

            //更新url地址
            ApArticle apArticle = new ApArticle();
            apArticle.setId(id);
            apArticle.setStaticUrl(url);
            apArticleMapper.updateById(apArticle);
        }
    }
}
