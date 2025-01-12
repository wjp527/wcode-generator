package com.wjp.web;

import com.wjp.web.codegeneratorzmq.MyMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MqInitMainTest {

    @Resource
    private MyMessageProducer myMessageProducer;
    @Test
    void main() {

        myMessageProducer.sendMessage("code_exchange", "my_routingKey", "hi");

    }
}