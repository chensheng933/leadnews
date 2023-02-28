package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;

import java.util.Map;

public interface ApArticleConfigService extends IService<ApArticleConfig> {
    void updateArticleConfig(Map msg);
}
