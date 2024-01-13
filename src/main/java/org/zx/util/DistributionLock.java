package org.zx.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DistributionLock {
    /**
     * Tair
     * @param key
     * @return
     */
    @Resource
    RedisTemplate<String, Long> redisTemplate;

    /**
     * Redisson 自动续期
     * @param key
     * @return
     */
    private boolean lock(String key) {
        // 1. 判断临界资源有没有被占用
        final Long value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return false;
        }
        // 2.占用锁
        final boolean lockSuccess = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, 1L, 10, TimeUnit.SECONDS));

        return lockSuccess;
    }

    // 突然机器挂了，unlock没执行
    public boolean unlock(String key) {
        final Boolean unlockValue = redisTemplate.delete(key);
        log.info("unlockValue={}",unlockValue);
        return true;
    }

    public boolean tryLock(String key) {
        try {
            // 自旋
            for (int i = 0; i < 3; i++) {
                Thread.sleep(10);
                if (lock(key)) {
                    log.info("lockSuccess={}",true);
                    return true;
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("lockSuccess={}",false);
        return false;
    }
}
