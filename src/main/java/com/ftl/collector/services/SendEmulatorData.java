package com.ftl.collector.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftl.collector.constants.Constants;
import com.ftl.collector.model.MessageSocket;
import com.ftl.collector.model.event.EmulatorEvent;
import com.ftl.collector.model.event.EventType;
import com.ftl.collector.utils.MsgUtils;
import com.ftl.common.model.market.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
@Slf4j
public class SendEmulatorData {

    private final SocketClusterService socketClusterService;

    private final ObjectMapper objectMapper;
//    private final CacheService cacheService;

    private final ApplicationEventPublisher eventPublisher;

    private final MessagePublisherService messagePublisher;
    private final RedisTemplate<String, String> redisTemplate;
    public static ConcurrentHashMap<Integer, CompletableFuture> requestKeyMap = new ConcurrentHashMap<>();


    //    @Scheduled(cron = "*/20 * * * * *")

    @EventListener
    @Async
    public void sendFakeData(EmulatorEvent emulatorEvent) throws IOException, InterruptedException {
        File file = new File("message-data.txt");
        boolean isExist = file.exists();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (!checkEnableEmulator())
                return;
            String delay = getHashKey(Constants.Redis.COMMON_PARAMS, Constants.Redis.DEPLAY_TIME, "20");
            String numberOfPacketStr = getHashKey(Constants.Redis.COMMON_PARAMS, Constants.Redis.NUMBER_OF_PACKET, "1");
            Thread.sleep(Integer.parseInt(delay));
            int numberOfPacket = Integer.parseInt(numberOfPacketStr);
            for (int i = 1; i <= numberOfPacket; i++) {
                MessageSocket messageSocket = objectMapper.readValue(line.trim(), MessageSocket.class);
                line = bufferedReader.readLine();
                //  handlerMessage(messageSocket);
                handlerMessageCore(messageSocket);
            }
        }
        eventPublisher.publishEvent(new EmulatorEvent(this));
        //  sendFakeData(new EmulatorEvent(this));
    }


    public void handlerMessageCore(MessageSocket messageSocket) {
        log.info("messageJson: {}", messageSocket);

        if (messageSocket.getMsgType() - 1 == 0) { // thoi gian server
            log.info("ignore msgType = 1");
        }
        if (messageSocket.getMsgType() - 2 == 0) { // extra
            SymbolStatic symbolInfo = MsgUtils.parseMsg2(messageSocket.getMessage());
            if (symbolInfo != null) {
                scPublish("market.bidoffer." + symbolInfo.getS(), symbolInfo);
                log.info("extraQuote: {}", symbolInfo);
            } else {
                log.warn("parseMsg2 return null");
            }
        }
        if (messageSocket.getMsgType() - 3 == 0) { // index extra/quote
            Object object = MsgUtils.parseMsg3(messageSocket.getMessage());
            if (object instanceof Quote) {
                Quote quote = (Quote) object;
                quote.setTi(System.currentTimeMillis());
                log.info("indexQuote: {}", quote);
                //  scPublish("market.quote." + ((Quote) object).getS(), object);
            } else if (object instanceof ExtraQuote) {
                ExtraQuote extraQuote = (ExtraQuote) object ;
                extraQuote.setTi(System.currentTimeMillis());
                scPublish("market.bidoffer." + ((ExtraQuote) object).getS(), object);
                log.info("extraQuote: {}", extraQuote);
            }
        }
        if (messageSocket.getMsgType() - 4 == 0) { // init stock
            List<SymbolInfo> symbolInfoList = MsgUtils.parseListMsg4(messageSocket.getMessage());
            CompletableFuture completableFuture = requestKeyMap.getOrDefault(2, null);
            if (completableFuture != null) {
                completableFuture.complete(symbolInfoList);
                messagePublisher.publish(EventType.INIT_STOCK_4_2, messageSocket.getMessage());
            } else {
                log.info("no handle response");
            }
        }
        if (messageSocket.getMsgType() - 5 == 0) { // init index
            List<SymbolInfo> symbolInfoList = MsgUtils.parseListMsg5(messageSocket.getMessage());
            CompletableFuture completableFuture = requestKeyMap.getOrDefault(3, null);
            if (completableFuture != null) {
                completableFuture.complete(symbolInfoList);
                messagePublisher.publish(EventType.INIT_INDEX_5_3, messageSocket.getMessage());
            } else {
                log.info("no handle response");
            }
        }
        if (messageSocket.getMsgType() - 6 == 0) { // market status change
            MarketStatus marketStatus = MsgUtils.parseMsg6(messageSocket.getMessage());
            if (marketStatus != null)
                marketStatus.setTi(System.currentTimeMillis());
            if (marketStatus != null) {
                scPublish("market.status", marketStatus);
            }
        }
        if (messageSocket.getMsgType() - 7 == 0) { // init market
            List<MarketStatus> symbolInfoList = MsgUtils.parseListMsg7(messageSocket.getMessage());
            CompletableFuture completableFuture = requestKeyMap.getOrDefault(4, null);
            if (completableFuture != null) {
                completableFuture.complete(symbolInfoList);
                messagePublisher.publish(EventType.INIT_MARKET_7_4, messageSocket.getMessage());
            } else {
                log.info("no handle response");
            }
        }
        if (messageSocket.getMsgType() - 8 == 0) { // top
            List<BidOffer> bidOfferList = MsgUtils.parseListMsg8(messageSocket.getMessage());
            for (BidOffer bidOffer : bidOfferList) {
                bidOffer.setTi(System.currentTimeMillis());
                log.info("bidOffer: {}", bidOffer);
                if ("E".equals(bidOffer.getOe())) {
                    scPublish("market.bidoffer." + bidOffer.getS(), bidOffer);
                } else {
                    bidOffer.setBoOd(bidOffer.getBo());
                    bidOffer.setBbOd(bidOffer.getBb());
                    bidOffer.setBo(null);
                    bidOffer.setBb(null);
                    scPublish("market.bidoffer.oddlot." + bidOffer.getS(), bidOffer);
                }
            }
        }
        if (messageSocket.getMsgType() - 9 == 0) { // quote
            Object object = MsgUtils.parseMsg9(messageSocket.getMessage());
            if (object instanceof Quote) {  // khớp
                Quote quote = (Quote) object;
                quote.setTi(System.currentTimeMillis());
                log.info("stockQuote: {}", quote);
//                scPublish("market.quote." + ((Quote) object).getS(), object);
            } else if (object instanceof ExtraQuote) { // tạm khớp
                ExtraQuote extraQuote = (ExtraQuote) object;
                extraQuote.setTi(System.currentTimeMillis());
                scPublish("market.bidoffer." + extraQuote.getS(), extraQuote);
                log.info("extraQuote: {}", object);
            } else if (object instanceof QuoteOddLot) { // lô lẻ
                QuoteOddLot quoteOddLot = (QuoteOddLot) object;
                quoteOddLot.setTi(System.currentTimeMillis());
                scPublish("market.quote.oddlot." + quoteOddLot.getS(), object);
                log.info("quoteOddlot: {}", quoteOddLot);
            }
        }
        if (messageSocket.getMsgType() - 10 == 0) { // advertised/dealNotice
            Object object = MsgUtils.parseMsg10(messageSocket.getMessage());
            if (object instanceof DealNotice) {
                DealNotice dealNotice = (DealNotice) object;
                dealNotice.setTi(System.currentTimeMillis());
                log.info("dealNotice: {}", object);
                scPublish("market.dealNotice." + dealNotice.getM(), dealNotice);
            } else if (object instanceof Advertised) {
                Advertised advertised = (Advertised) object;
                advertised.setTi(System.currentTimeMillis());
                log.info("advertised: {}", object);
                scPublish("market.advertised." + advertised.getM(),advertised);
            }
        }
    }

    private void scPublish(String channel, Object data) {
        // socketClusterService.publish(channel, data);
    }

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

    public boolean checkEnableEmulator() {
        String o = getHashKey(Constants.Redis.COMMON_PARAMS, Constants.Redis.ENABLE_EMULATOR, "0");
        return "1".equals(o);
    }
}
