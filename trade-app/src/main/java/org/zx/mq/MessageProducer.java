package org.zx.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class MessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        // exchangeName
        // routingKey
        rabbitTemplate.convertAndSend("TRADE-ORDER", "CLOSED", message);
        log.info("message sent! msg={}",message);
    }

}
