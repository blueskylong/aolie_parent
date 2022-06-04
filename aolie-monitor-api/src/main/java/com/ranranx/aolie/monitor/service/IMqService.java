package com.ranranx.aolie.monitor.service;

import java.util.function.Function;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/6/3 0003 10:20
 **/
public interface IMqService {
    /**
     * 发送消息
     *
     * @param msgBody
     */
    void sendMsg(String msgBody);

    /**
     * 发送异步消息在SendCallback中可处理相关成功失败时的逻辑
     */
    void sendAsyncMsg(String msgBody, Function onSuccess, Function onFailure);

    /**
     * 发送延时消息<br/>
     * 在start版本中延时消息⼀共分为18个等级分别为：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h<br/>
     */
    void sendDelayMsg(String msgBody, Integer delayLevel);

    /**
     * 发送带tag的消息,直接在topic后⾯加上":tag"
     */
    void sendTagMsg(String msgBody);
}
