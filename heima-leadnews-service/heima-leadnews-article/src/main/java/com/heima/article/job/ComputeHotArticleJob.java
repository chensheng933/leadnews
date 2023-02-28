package com.heima.article.job;

import com.heima.article.service.HotArticleService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 定时更新首页热点文章
 */
@Component
@Slf4j
public class ComputeHotArticleJob {
    @Autowired
    private HotArticleService hotArticleService;

    /**
     * 定时更新首页热点文章
     */
    @XxlJob("hotArticleComputeJob")
    public void hotArticleComputeJob(){
        log.info("定时更新首页热点文章");
        hotArticleService.computeHotArticle();
    }
}
