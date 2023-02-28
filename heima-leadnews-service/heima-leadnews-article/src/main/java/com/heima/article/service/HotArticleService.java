package com.heima.article.service;

import com.heima.model.article.dtos.ArticleVisitStreamMsg;

public interface HotArticleService {

    /**
     * 定时更新首页热点文章
     */
    public void computeHotArticle();

    void updateHotArticle(ArticleVisitStreamMsg streamMsg);
}
