package com.heima.article.config;

/**
 * 流数据的监听消费者实现的接口类，系统自动会通过
 * KafkaStreamListenerFactory类扫描项目中实现该接口的类
 * 并注册为流数据的消费端
 * <p>
 * 其中泛型可是KStream或KTable
 *
 * @param <T>
 */
public interface KafkaStreamListener<T> {

    // 流式处理的时候需要监听的主题是什么  INPUTTOPIC
    String listenerTopic();

    //流式处理完成之后继续发送到的主题是什么 OUTTOPIC
    String sendTopic();

    // 流式业务的对象处理逻辑
    T getService(T stream);

}
