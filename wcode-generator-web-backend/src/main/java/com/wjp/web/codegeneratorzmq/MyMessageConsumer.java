package com.wjp.web.codegeneratorzmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 消费者代码
 */
@Component
@Slf4j
public class MyMessageConsumer {

    /**
     * 接收消息
     * @param message  消息内容
     * @param channel  和 Rabbit MQ 进行通信,这里需要channel的ack，nack【消息确认机制】，手动确认和拒绝消息
     * @param deliveryTag  指定要 拒绝/接受 哪条消息
     */
    // queues = {}: 监听所有队列 ❌
    // queues = "code_queue": 监听指定队列 ✔️
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("ReceivedMessage message: {}", message);
        // 只要消息被消费，就ack
        channel.basicAck(deliveryTag, false);
    }

}
