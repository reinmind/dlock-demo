package org.example.dao;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PaymentDAO {
    @Resource
    RedisTemplate<String,String> redisTemplate;

    public boolean setPaymentStatus(String paymentId,String status){
        redisTemplate.opsForValue().set(paymentId,status);
        return true;
    }

    public String getPaymentStatus(String paymentId){
        return redisTemplate.opsForValue().get(paymentId);
    }
}
