package com.heima.article.service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.utils.common.JsonUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {
    @Autowired
    private Configuration configuration;
    @Autowired
    private MinIOFileStorageService storageService;
    @Autowired
    private ApArticleMapper apArticleMapper;

    @Override
    public void buildArticleToMinIO(ApArticle apArticle, String content) {

        try {
            if(content!=null && StringUtils.isNotEmpty(content)){
                //准备和读取模板
                Template template = configuration.getTemplate("article.ftl");

                //准备文章内容数据
                Map<String,Object> data = new HashMap<>();
                List<Map> list = JsonUtils.toList(content,Map.class);
                data.put("content",list);

                //填充数据，生成页面
                StringWriter writer = new StringWriter();
                template.process(data,writer);//生成静态页，写入StringWriter缓存对象

                //把页面存储到MinIO
                InputStream inputStream = new ByteArrayInputStream(writer.toString().getBytes());
                String url = storageService.uploadHtmlFile("",apArticle.getId()+".html",inputStream);

                //更新url地址
                apArticle.setStaticUrl(url);
                apArticleMapper.updateById(apArticle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("生成静态页失败，{}",e.getMessage());
        }

    }
}
