package com.ftl.collector.producers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftl.collector.configurations.AppConf;
import com.ftl.collector.constants.Constants;
import com.ftl.common.constants.MessageTypeEnum;
import com.ftl.common.kafka.KafkaRequestSender;
import com.ftl.common.model.Message;
import com.ftl.common.model.Pair;
import com.ftl.common.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RequestSender extends KafkaRequestSender {
    public static final Logger log = LoggerFactory.getLogger(RequestSender.class);
    private AppConf appConf;
    private ObjectMapper objectMapper;
    private RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOperations;

    @Autowired
    public RequestSender(
            ObjectMapper objectMapper,
            AppConf appConf,
            RedisTemplate<String, String> redisTemplate) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), appConf.getInstanceId());
        this.setDefaultTimeout(appConf.getKafkaTimeout());
        this.appConf = appConf;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @PostConstruct
    void init() {
//        putCommonParams("3");
    }

    public void sendMiniMessageSafeNoResponseWithPartition(String topic, Integer partitionNumber, String uri, Object content) throws IOException {

        Message message = this.createMessage((MessageTypeEnum) null, uri, (String) null, (String) null, this.sourceId, content, false, (String) null, (Integer) partitionNumber);
        this.sendOut.accept(new Pair(this.getTopic(topic), message));


    }



}
