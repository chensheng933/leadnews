package com.heima.article.listener;

import com.heima.article.service.HotArticleService;
import com.heima.common.constants.MQConstants;
import com.heima.model.article.dtos.ArticleVisitStreamMsg;
import com.heima.utils.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 用户行为数据实时更新
 */
@Component
@Slf4j
public class HotArticleHandlerListener {
    @Autowired
    private HotArticleService hotArticleService;

    @KafkaListener(topics = MQConstants.HOT_ARTICLE_OUTPUT_TOPIC)
    public void handleHotArticle(String value){
        log.info("用户行为数据实时更新");
        if(StringUtils.isNotEmpty(value)){
            ArticleVisitStreamMsg streamMsg = JsonUtils.toBean(value, ArticleVisitStreamMsg.class);
            hotArticleService.updateHotArticle(streamMsg);
        }
    }
}
