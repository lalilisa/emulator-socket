package com.ftl.collector.configurations.params;


import com.ftl.collector.constants.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisParams {
    private final RedisTemplate<String, String> redisTemplate;

    @Bean
    public void initParams(){
        checkAndInsertKey(Constants.Redis.COMMON_PARAMS,Constants.Redis.DEPLAY_TIME,"20");
        checkAndInsertKey(Constants.Redis.COMMON_PARAMS,Constants.Redis.NUMBER_OF_PACKET,"1");
        checkAndInsertKey(Constants.Redis.COMMON_PARAMS,Constants.Redis.ENABLE_EMULATOR,"0");

    }
    private void checkAndInsertKey(String key, String hashKey,String value) {
        var o = redisTemplate.opsForHash().get(key, hashKey);
        if (o == null) {
            redisTemplate.opsForHash().put(key,hashKey,value);
        }
    }
}
