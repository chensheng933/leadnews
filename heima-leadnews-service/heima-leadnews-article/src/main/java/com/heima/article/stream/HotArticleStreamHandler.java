package com.heima.article.stream;

import com.heima.article.config.KafkaStreamListener;
import com.heima.common.constants.MQConstants;
import com.heima.model.article.dtos.ArticleVisitStreamMsg;
import com.heima.model.article.dtos.UpdateArticleMsg;
import com.heima.utils.common.JsonUtils;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

/**
 * 实时流式处理逻辑
 */
@Component
public class HotArticleStreamHandler implements KafkaStreamListener<KStream<String,String>> {
    @Override
    public String listenerTopic() {
        return MQConstants.HOT_ARTICLE_INPUT_TOPIC;
    }

    @Override
    public String sendTopic() {
        return MQConstants.HOT_ARTICLE_OUTPUT_TOPIC;
    }

    @Override
    public KStream<String, String> getService(KStream<String, String> stream) {

        /**
         * 原始数据
         *     key      value
         *          {"articleId":1,"type":"LIKES"}
         *          {"articleId":2,"type":"LIKES"}
         *          {"articleId":2,"type":"COMMENT"}
         *          .....
         * 1） 对value值进行处理
         *     key      value
         *           1_LIKES
         *          2_LIKES
         *          2_COMMENT
         *
         *  2)把value值赋值给key
         *     key            value
         *  1_LIKES         1_LIKES
         *  2_LIKES        2_LIKES
         *  2_COMMENT      2_COMMENT
         *
         * 3) 分组+统计
         *     key            value
         *  1_LIKES         400
         *  2_LIKES         200
         *  2_COMMENT      100
         *
         *
         * 目标数据：
         *     key     value
         *          {""articleId":1","like":400}
         *          {""articleId":2","like":200}
         *          {""articleId":2","comment":100}
         *          ....
         */
        KTable<Windowed<Object>, Long> kTable = stream.flatMapValues(new ValueMapper<String, Iterable<?>>() {
            @Override
            public Iterable<?> apply(String value) {  //value: {"articleId":1,"type":"LIKES"}
                UpdateArticleMsg msg = JsonUtils.toBean(value, UpdateArticleMsg.class);
                String valueStr = msg.getArticleId() + "_" + msg.getType().name();  //1_LIKES
                return Arrays.asList(valueStr);
            }
        }).map(new KeyValueMapper<String, Object, KeyValue<?, ?>>() {

            @Override
            public KeyValue<?, ?> apply(String key, Object value) {
                return new KeyValue<>(value, value);
            }
        }).groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(5)))
                .count(Materialized.as("total"));

        KStream kStream = kTable.toStream().map(new KeyValueMapper<Windowed<Object>, Long, KeyValue<?, ?>>() {
            @Override
            public KeyValue<?, ?> apply(Windowed<Object> windowed, Long value) {
                String key = (String)windowed.key(); // 1_LIKES

                String[] array = key.split("_");

                ArticleVisitStreamMsg streamMsg = new ArticleVisitStreamMsg();
                streamMsg.setArticleId(Long.valueOf(array[0]));

                switch (UpdateArticleMsg.UpdateArticleType.valueOf(array[1])){
                    case LIKES:
                        streamMsg.setLike(value);
                        break;
                    case COMMENT:
                        streamMsg.setComment(value);
                        break;
                    case COLLECTION:
                        streamMsg.setCollect(value);
                        break;
                    case VIEWS:
                        streamMsg.setView(value);
                        break;
                }

                String json = JsonUtils.toString(streamMsg);
                return new KeyValue<>(null,json);
            }
        });

        return kStream;
    }
}
