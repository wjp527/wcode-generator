package com.wjp.web.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存管理器 【Caffeine + Redis 缓存】
 * @author wjp
 */
@Component
public class CacheManager {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 操作缓存的客户端【Caffeine 缓存】
    // 缓存过期时间是100分钟，最大缓存数量是10_000,超过10_000[10000]就删除最旧的缓存
    Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    /**
     * 向缓存中插入数据
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        // 但是都不要设置，因为会耗时
        // redisTemplate.setKeySerializer(new StringRedisSerializer());
        // redisTemplate.setValueSerializer(redisTemplate.getValueSerializer());
        // 本地缓存插入数据
        localCache.put(key, value);
        // 向 Redis 中插入数据 100 分钟过期
        redisTemplate.opsForValue().set(key, value, 10, TimeUnit.MINUTES);
    }

    /**
     * 从缓存中获取数据
     * @param key
     * @return
     */
    public Object get(String key) {
        // 先从本地缓存中获取
        Object value = localCache.getIfPresent(key);
        if(value!= null) {
            return value;
        }

        // 本地缓存未命中，尝试从Redis中获取
        value = redisTemplate.opsForValue().get(key);
        if(value!= null) {
            // 向本地缓存中写入数据
            localCache.put(key, value);
            return value;

        }
        return value;
    }

    /**
     * 删除缓存
     * @param key
     */
    public void delete(String key) {
        // 本地缓存删除缓存
        localCache.invalidate(key);
        // redis 中删除缓存
        redisTemplate.delete(key);
    }
}
