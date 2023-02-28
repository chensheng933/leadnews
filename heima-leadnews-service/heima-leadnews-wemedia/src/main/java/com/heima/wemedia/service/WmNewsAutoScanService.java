package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsAutoScanService {

    /**
     * 自媒体文章自动审核
     */
    public void autoScanWmNews(Integer id);

    /**
     * 保存App文章
     * @param wmNews
     */
    public void publishApArticle(WmNews wmNews);
}
