package com.ftl.collector.services;

import com.ftl.collector.model.event.InternalRedisEvent;
import com.ftl.collector.utils.JsonUtils;
import com.ftl.collector.utils.MsgUtils;
import com.ftl.common.model.market.MarketStatus;
import com.ftl.common.model.market.SymbolInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ftl.collector.services.SendEmulatorData.requestKeyMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageSubscriberService implements MessageListener {

    public void onMessage(final Message _message, final byte[] pattern) {
        try {
            String message = new String(_message.getBody());
            InternalRedisEvent event = JsonUtils.fromString(message, InternalRedisEvent.class);
            switch (event.getType()) {
                case INIT_MARKET_7_4: {
                    List<MarketStatus> symbolInfoList = MsgUtils.parseListMsg7(event.getPayload());
                    CompletableFuture completableFuture = requestKeyMap.getOrDefault(4, null);
                    if (completableFuture != null) {
                        completableFuture.complete(symbolInfoList);
                    } else {
                        log.info("no handle response");
                    }
                    return;
                }
                case INIT_INDEX_5_3: {
                    List<SymbolInfo> symbolInfoList = MsgUtils.parseListMsg5(event.getPayload());
                    CompletableFuture completableFuture = requestKeyMap.getOrDefault(3, null);
                    if (completableFuture != null) {
                        completableFuture.complete(symbolInfoList);
                    } else {
                        log.info("no handle response");
                    }
                    return;
                }
                case INIT_STOCK_4_2: {
                    List<SymbolInfo> symbolInfoList = MsgUtils.parseListMsg4(event.getPayload());
                    CompletableFuture completableFuture = requestKeyMap.getOrDefault(2, null);
                    if (completableFuture != null) {
                        completableFuture.complete(symbolInfoList);
                    } else {
                        log.info("no handle response");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to process internal publised message", e);
        }
    }
}
