package com.ftl.collector.services;


import com.ftl.collector.model.event.EventType;
import com.ftl.collector.model.event.InternalRedisEvent;
import com.ftl.collector.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagePublisherService {

    private final RedisTemplate redisTemplate;

    private final ChannelTopic topic;

    public void publish(EventType type, Object message) {
        InternalRedisEvent event = new InternalRedisEvent(type, JsonUtils.toString(message));
        redisTemplate.convertAndSend(topic.getTopic(), JsonUtils.toString(event));
    }
}
