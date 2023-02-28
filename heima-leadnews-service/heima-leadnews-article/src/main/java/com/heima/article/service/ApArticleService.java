package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.wemedia.pojos.WmNews;

public interface ApArticleService extends IService<ApArticle> {
    ResponseResult loadApArticle(ArticleDto dto, int type);

    /**
     * 保存App文章（新增或修改）
     */
    public Long saveApArticle(ApArticleDto dto);

    ResponseResult loadApArticle2(ArticleDto dto, int i);
}
