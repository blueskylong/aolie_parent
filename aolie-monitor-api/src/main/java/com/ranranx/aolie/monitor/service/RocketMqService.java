package com.ranranx.aolie.monitor.service;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * 监控信息发送服务
 *
 * @author xxl
 * @version V0.0.1
 * @date 2022/6/3 0003 10:01
 **/
@Service
public class RocketMqService implements IMqService {
    @Value("${timing.monitor.topic:monitor}")
    private String topic;
    @Value("${timing.monitor.tag:core}")
    private String tag;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Value("${rocketmq.producer.send-message-timeout:100}")
    private Integer messageTimeOut;


    @Override
    public void sendMsg(String msgBody) {
        rocketMQTemplate.syncSend(topic + ":" + tag, MessageBuilder.withPayload(msgBody).build());
    }

    /**
     * 发送异步消息在SendCallback中可处理相关成功失败时的逻辑
     */
    @Override
    public void sendAsyncMsg(String msgBody, Function onSuccess, Function onFailure) {
        rocketMQTemplate.asyncSend(topic + ":" + tag, MessageBuilder.withPayload(msgBody).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                // 处理消息发送成功逻辑
                if (onSuccess != null) {
                    onSuccess.apply(sendResult);
                }

            }

            @Override
            public void onException(Throwable e) {
                // 处理消息发送异常逻辑
                if (onFailure != null) {
                    onFailure.apply(e);
                }
            }
        });
    }

    /**
     * 发送延时消息<br/>
     * 在start版本中延时消息⼀共分为18个等级分别为：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h<br/>
     */
    @Override
    public void sendDelayMsg(String msgBody, Integer delayLevel) {
        rocketMQTemplate.syncSend(topic + ":" + tag, MessageBuilder.withPayload(msgBody).build(),
                messageTimeOut, delayLevel);
    }

    /**
     * 发送带tag的消息,直接在topic后⾯加上":tag"
     */
    @Override
    public void sendTagMsg(String msgBody) {
        rocketMQTemplate.syncSend(topic + ":" + tag, MessageBuilder.withPayload(msgBody).build());
    }
}
