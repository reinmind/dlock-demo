package org.zx.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MessageConsumer {

    @RabbitListener(queues = {"ORDER-PAYMENT","ORDER-CREATE","ORDER-CLOSED"})
    public void receiveOrderPayment(String message, @Headers Map<String,Object> headers){
        final Object queueName = headers.get("amqp_receivedRoutingKey");
        log.info("receive order queueName={},msg={}",queueName,message);
    }
}
