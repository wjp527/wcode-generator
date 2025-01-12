package com.wjp.web.codegeneratorzmq;

import com.rabbitmq.client.Channel;
import com.wjp.maker.generator.main.GenerateTemplate;
import com.wjp.maker.generator.main.ZipGenerator;
import com.wjp.maker.meta.Meta;
import com.wjp.web.common.ErrorCode;
import com.wjp.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 消费者代码
 */
@Component
@Slf4j
public class CodeGeneratorMessageConsumer {

    /**
     * 接收消息
     * @param message  消息内容
     * @param channel  和 Rabbit MQ 进行通信,这里需要channel的ack，nack【消息确认机制】，手动确认和拒绝消息
     * @param deliveryTag  指定要 拒绝/接受 哪条消息
     */
    // queues = {}: 监听所有队列 ❌
    // queues = "code_queue": 监听指定队列 ✔️
    @RabbitListener(queues = {CodeGeneratorConstant.CODE_GENERATOR_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(Map<String, Object> message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        // 从消息中提取 meta 和 outputPath
        if (message == null) {
            // 消息为空，拒绝消息
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");

        }
        Object result = message.get("meta");
        Meta meta = (Meta) result;
        String outputPath = (String) message.get("outputPath");

        // 5）调用 maker 方法制作生成器
        GenerateTemplate generateTemplate = new ZipGenerator();
        try {
            generateTemplate.doGenerate(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "制作失败");
        }

        log.info("ReceivedMessage message: {}", message);
        // 只要消息被消费，就ack
        channel.basicAck(deliveryTag, false);
    }

}
