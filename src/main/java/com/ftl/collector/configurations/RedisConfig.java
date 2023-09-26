//package com.ftl.collector.configurations;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.connection.RedisPassword;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Slf4j
//@Configuration
//@ConditionalOnProperty(name = "spring.redis.type", havingValue = "single", matchIfMissing = false)
//@RequiredArgsConstructor
//public class RedisConfig {
//
//    private final RedisProperties properties;
//
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
//        redisStandaloneConfiguration.setHostName(properties.getHost());
//        redisStandaloneConfiguration.setPort(properties.getPort());
//        redisStandaloneConfiguration.setUsername(properties.getUsername());
//        redisStandaloneConfiguration.setPassword(RedisPassword.of(properties.getPassword()));
//
//        LettuceConnectionFactory lcf = new LettuceConnectionFactory(redisStandaloneConfiguration);
//
//        lcf.setShareNativeConnection(false);
//        lcf.afterPropertiesSet();
//        return lcf;
//    }
//
//    @Bean
//    @Primary
//    public RedisTemplate<String, String> redisTemplate() {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new StringRedisSerializer());
//        template.afterPropertiesSet();
//        return template;
//    }
//
//}
//
