package com.ftl.collector.configurations;

import com.ftl.collector.constants.Constants;
import com.ftl.collector.services.MessageSubscriberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "redis")
@RequiredArgsConstructor
@Data
@Configuration
public class RedisConf {
    private static final Logger log = LoggerFactory.getLogger(RedisConf.class);

    @Autowired
    private final MessageSubscriberService messageSubscriber;

    private String host;
    private int port;
    private String password;
    private boolean sentinelEnable;
    private String sentinelMasterName;
    private String sentinelUrls;
    private String sentinelPassword;

    @Bean
    public RedisConnectionFactory initRedisConnectionFactory() {
        if (sentinelEnable) {
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
            sentinelConfig.master(sentinelMasterName);
            for (String node : sentinelUrls.split(",")) {
                String split[] = node.split(":");
                sentinelConfig.sentinel(split[0], Integer.parseInt(split[1]));
            }
            sentinelConfig.setSentinelPassword(sentinelPassword);
            sentinelConfig.setPassword(password);
            LettuceConnectionFactory lcf = new LettuceConnectionFactory(sentinelConfig);
            lcf.afterPropertiesSet();
            return lcf;
        }

        RedisStandaloneConfiguration standaloneConfig  = new RedisStandaloneConfiguration(host, port);
        standaloneConfig.setPassword(password);
        LettuceConnectionFactory lcf = new LettuceConnectionFactory(standaloneConfig);
        lcf.afterPropertiesSet();
        return lcf;
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> initRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(initRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @PostConstruct
    public void init() {
        log.info("redisConf: {}", this);
    }

    @Bean
    MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(messageSubscriber);
    }

    @Bean
    ChannelTopic topic() {
        return ChannelTopic.of(Constants.Redis.TOPIC_SUBSCRIBE);
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(initRedisConnectionFactory());
        container.addMessageListener(messageListener(), topic());
        return container;
    }

}
