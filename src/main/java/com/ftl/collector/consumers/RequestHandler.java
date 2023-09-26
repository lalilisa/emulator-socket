package com.ftl.collector.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftl.collector.configurations.AppConf;
import com.ftl.collector.constants.Constants;
import com.ftl.collector.model.event.EmulatorEvent;
import com.ftl.common.kafka.KafkaRequestHandler;
import com.ftl.common.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.EventListener;

@Service
public class RequestHandler extends KafkaRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public RequestHandler(
            AppConf appConf,
            ObjectMapper objectMapper,
            RedisTemplate<String, String> redisTemplate,
            ApplicationEventPublisher eventPublisher
    ) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), 10);
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.eventPublisher =eventPublisher;
    }

    @Override
    protected Object handle(Message message) throws Exception {
        switch (message.getUri()){
            case "/start-emulator":{
                redisTemplate.opsForHash().put(Constants.Redis.COMMON_PARAMS,Constants.Redis.ENABLE_EMULATOR,"1");
                eventPublisher.publishEvent(new EmulatorEvent(this));
                return true;
            }
            case "/stop-emulator":{
                redisTemplate.opsForHash().put(Constants.Redis.COMMON_PARAMS,Constants.Redis.ENABLE_EMULATOR,"0");
                return true;
            }
        }
        return false;
    }
}
