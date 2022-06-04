package com.ranranx.aolie.monitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ranranx.aolie.monitor.service.IMqService;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * 监控信息发送服务
 *
 * @author xxl
 * @version V0.0.1
 * @date 2022/6/3 0003 10:01
 **/

@Component
@RocketMQMessageListener(topic = "${timing.monitor.topic:monitor}", selectorExpression = "",
        consumerGroup = "${timing.monitor.consumer:monitor}")
public class RocketMqConsumerService implements RocketMQListener<MessageExt> {
    @Autowired
    private LogService logService;


    @Override
    public void onMessage(MessageExt message) {
        byte[] body = message.getBody();
        String msg = new String(body);
        logService.saveLog(JSON.parseObject(msg));
    }
}

