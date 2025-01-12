package com.wjp.web.codegeneratorzmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 生产者代码
 */
@Component
public class MyMessageProducer {

    // 获取RabbitTemplate对象
    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 发送消息到指定队列
     * @param exchange 发送到哪个交换机
     * @param routingKey 发送到哪个路由键
     * @param message 发送的消息
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        // exchange: 交换机名称
        // routingKey: 路由键
        // message: 消息内容
        // 发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

}
