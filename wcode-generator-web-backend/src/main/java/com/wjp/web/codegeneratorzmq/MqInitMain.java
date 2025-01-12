package com.wjp.web.codegeneratorzmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 初始化消息队列
 * 只需要手动执行一次
 */
public class MqInitMain {
    public static void main(String[] args) {

        try {
            // 创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();

            // 设置 RabbitMQ 服务器地址
            factory.setHost("localhost");
            // 从工厂中创建连接
            Connection connection = factory.newConnection();

            // 创建通道
            // 和 Rabbit MQ 进行通信,这里需要channel的ack，nack【消息确认机制】，手动确认和拒绝消息
            Channel channel = connection.createChannel();

            // 定义交换机名称
            String EXCHANGE_NAME = "code_exchange";

            // 声明交换机 类型为 direct
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列
            String queueName = "code_queue";
            // 创建队列 可持久化存储 消息
            channel.queueDeclare(queueName, true, false, false, null);
            // 绑定队列到交换机
            channel.queueBind(queueName, EXCHANGE_NAME, "my_routingKey");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
