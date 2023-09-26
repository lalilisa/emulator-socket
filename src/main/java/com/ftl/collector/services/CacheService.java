package com.ftl.collector.services;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
//    @PostConstruct
//    public void initRedis(){
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
//        hashOperations = redisTemplate.opsForHash();
//    }


    public String getHashKey(String key, String hashKey, String defaultValue) {
        var hashOperations = redisTemplate.opsForHash();
        hashOperations = redisTemplate.opsForHash();
        Object o = hashOperations.get(key, hashKey);
        if (o == null) {
            hashOperations.put(key, hashKey, defaultValue);
            return defaultValue;
        }
        return o.toString();

    }
}
