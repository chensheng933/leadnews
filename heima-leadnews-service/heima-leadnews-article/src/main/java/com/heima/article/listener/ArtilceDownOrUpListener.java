package com.heima.article.listener;

import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.MQConstants;
import com.heima.utils.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听App端文章上下架业务
 */
@Component
@Slf4j
public class ArtilceDownOrUpListener {
    @Autowired
    private ApArticleConfigService apArticleConfigService;

    @KafkaListener(topics = MQConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void handlerDownOrUp(String value){
        log.info("触发App端文章上下架业务");
        if(StringUtils.isNotEmpty(value)){
            Map msg = JsonUtils.toBean(value, Map.class);
            apArticleConfigService.updateArticleConfig(msg);
        }
    }
}
