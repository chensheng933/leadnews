package com.heima.search.listener;

import com.heima.common.constants.MQConstants;
import com.heima.search.service.ApArticleSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 监听文章发布
 */
@Component
@Slf4j
public class ArticleDocumentListener {
    @Autowired
    private ApArticleSearchService articleSearchService;

    /**
     * 文章上架
     */
    @KafkaListener(topics = MQConstants.WM_NEW_UP_ES_TOPIC)
    public void handleUpES(String articleId){
        log.info("进行文章上架索引同步");

        if(StringUtils.isNotEmpty(articleId)){
            articleSearchService.saveToES(Long.valueOf(articleId));
        }
    }

    /**
     * 文章下架
     */
    @KafkaListener(topics = MQConstants.WM_NEW_DOWN_ES_TOPIC)
    public void handleDownES(String articleId){
        log.info("进行文章下架索引同步");

        if(StringUtils.isNotEmpty(articleId)){
            articleSearchService.removeFromES(Long.valueOf(articleId));
        }
    }
}
