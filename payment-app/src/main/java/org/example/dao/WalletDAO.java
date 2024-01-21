package org.example.dao;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class WalletDAO {

    @Resource
    RedisTemplate<Long,Integer> redisTemplate;

    public Integer getBalance(Long userId){
        return redisTemplate.opsForValue().get(userId);
    }

    public Integer setBalace(Long userId,Integer amount){
        redisTemplate.opsForValue().set(userId,amount);
        return amount;
    }

    public Long increaseBalance(Long userId,Long amount){
         return redisTemplate.opsForValue().increment(userId, amount);
    }

    public Long decreaseBalace(Long userId,Integer amount){
        final Integer balance = getBalance(userId);
        redisTemplate.opsForValue().set(userId, balance - amount);
        return 1L;
    }
}
