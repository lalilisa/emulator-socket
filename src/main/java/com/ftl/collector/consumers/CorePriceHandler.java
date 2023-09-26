//package com.ftl.collector.consumers;
//
//import blazing.chain.LZSEncoding;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ftl.collector.configurations.AppConf;
//import com.ftl.collector.model.MessageSocket;
//import com.ftl.collector.producers.RequestSender;
//import com.ftl.collector.services.SocketClusterService;
//import com.ftl.collector.utils.MsgUtils;
//import com.ftl.common.kafka.KafkaProducer;
//import com.ftl.common.kafka.KafkaRequestHandler;
//import com.ftl.common.kafka.ThreadedKafkaConsumer;
//import com.ftl.common.model.Message;
//import com.ftl.common.model.market.Advertised;
//import com.ftl.common.model.market.BidOffer;
//import com.ftl.common.model.market.DealNotice;
//import com.ftl.common.model.market.ExtraQuote;
//import com.ftl.common.model.market.MarketStatus;
//import com.ftl.common.model.market.Quote;
//import com.ftl.common.model.market.QuoteOddLot;
//import com.ftl.common.model.market.SymbolInfo;
//import com.ftl.common.model.market.SymbolStatic;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Properties;
//import java.util.concurrent.CompletableFuture;
//
//import static com.ftl.collector.boardsocket.MyClientEndpoint.requestKeyMap;
//
//@Service
//@Slf4j
//public class CorePriceHandler extends KafkaRequestHandler {
//
//    private final SocketClusterService socketClusterService;
//    private final RequestSender requestSender;
//    private final ObjectMapper om;
//    private final AppConf appConf;
//
//
//    @Autowired
//    public CorePriceHandler(
//            ObjectMapper objectMapper,
//            AppConf appConf,
//            RequestSender requestSender,
//            SocketClusterService socketClusterService
//    ) {
//        super();
//        this.requestSender = requestSender;
//        this.om = objectMapper;
//        this.appConf = appConf;
//        this.socketClusterService = socketClusterService;
//
//        if (!"socket".equals(appConf.getPriceChannel()))
//            this.init(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), List.of(appConf.getTopics().getCorePrice()), 1, null);
//    }
//
//
//    @Override
//    protected Object handle(Message message) throws Exception {
//        return true;
//    }
//
//
//    protected void init(ObjectMapper om, String bootStrapServer, String clusterId, List<String> topics, int maxThread, KafkaProducer<String, String> producer) {
//
//        this.producer = producer == null ? new KafkaProducer<>(bootStrapServer, null, new Properties()) : producer;
//        this.sendOut = pair -> {
//        };
//        this.handler = record -> {
//            try {
////                log.info("receive msg: {} with key {}", record.value(), record.key());
//                String message = record.value();
//
//                handleMessage(message);
//            } catch (Exception e) {
//                log.error("fail to handle message {}", record, e);
//            } finally {
//            }
//        };
//        List<String> consumeTopics = topics == null ? Arrays.asList(clusterId) : topics;
//        Properties prop = new Properties();
////        prop.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(1000));
////        prop.setProperty(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(1000));
////        prop.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(1000));
//        this.consumer = new ThreadedKafkaConsumer<>(bootStrapServer, clusterId, consumeTopics, prop, handler, maxThread);
//    }
//
//    private void scPublish(String channel, Object data) {
//      //  this.socketClusterService.publish(channel, data);
//    }
//
//    private void kafkaPublish(String topic, Object symbolData) {
//       // this.requestSender.sendMiniMessageSafeNoResponse(topic, "Update", symbolData);
//    }
//
//    private void kafkaPublish(String topic, Object symbolData, String symbol) {
//        int partition = 0;
//        try {
//            partition = getPartition(symbol);
//            requestSender.sendMiniMessageSafeNoResponseWithPartition(topic, partition, "Update", symbolData);
//        } catch (IOException e) {
//            log.error("Error occured while send to partition " + partition + " with symbol " + symbol, e);
//        }
//    }
//
//    public int getPartition(String symbol) {
//        int partition = appConf.getKafkaPartitions();
//        symbol = symbol.toLowerCase();
//        int hash = toInt(symbol, 0) + toInt(symbol, 1) + toInt(symbol, 2);
//        return hash % partition;
//    }
//
//    static int toInt(String str, int pos) {
//        try {
//            return str.charAt(pos);
//        } catch (Exception e) {
//            return 0;
//        }
//    }
//
//    public void handleMessage(String message) throws IOException {
//        String decompressedBase64 = LZSEncoding.decompressFromBase64(message);
////        saveToFile(decompressedBase64);
//        MessageSocket messageSocket = om.readValue(decompressedBase64, MessageSocket.class);
////        log.info("messageJson: {}", messageSocket);
//        if (messageSocket.getMsgType() - 1 == 0) { // thoi gian server
//            log.info("ignore msgType = 1");
//            return;
//        }
//        if (messageSocket.getMsgType() - 2 == 0) { // extra
//            SymbolStatic symbolInfo = MsgUtils.parseMsg2(messageSocket.getMessage());
//            if (symbolInfo != null) {
//                kafkaPublish("market.extra", symbolInfo, symbolInfo.getS());
//                scPublish("market.bidoffer." + symbolInfo.getS(), symbolInfo);
//                log.info("extraQuote: {}", symbolInfo);
//            } else {
//                log.warn("parseMsg2 return null");
//            }
//            return;
//        }
//        if (messageSocket.getMsgType() - 3 == 0) { // index extra/quote
//            Object object = MsgUtils.parseMsg3(messageSocket.getMessage());
//            if (object instanceof Quote) {
//                log.info("indexQuote: {}", object);
//                kafkaPublish("market.index", object, ((Quote) object).getS());
////                scPublish("market.quote." + ((Quote) object).getS(), object);
//            } else if (object instanceof ExtraQuote) {
//                kafkaPublish("market.extra", object, ((ExtraQuote) object).getS());
//                scPublish("market.bidoffer." + ((ExtraQuote) object).getS(), object);
//                log.info("extraQuote: {}", object);
//            }
//            return;
//        }
//        if (messageSocket.getMsgType() - 4 == 0) { // init stock
//            List<SymbolInfo> symbolInfoList = MsgUtils.parseListMsg4(messageSocket.getMessage());
//            CompletableFuture completableFuture = requestKeyMap.getOrDefault(2, null);
//            if (completableFuture != null) {
//                completableFuture.complete(symbolInfoList);
//
//            } else {
//                log.info("no handle response");
//            }
//        }
//        if (messageSocket.getMsgType() - 5 == 0) { // init index
//            List<SymbolInfo> symbolInfoList = MsgUtils.parseListMsg5(messageSocket.getMessage());
//            CompletableFuture completableFuture = requestKeyMap.getOrDefault(3, null);
//            if (completableFuture != null) {
//                completableFuture.complete(symbolInfoList);
//            } else {
//                log.info("no handle response");
//            }
//        }
//        if (messageSocket.getMsgType() - 6 == 0) { // market status change
//            MarketStatus marketStatus = MsgUtils.parseMsg6(messageSocket.getMessage());
//            if (marketStatus != null) {
//                scPublish("market.status", marketStatus);
//                kafkaPublish("market.status", marketStatus);
//            }
//        }
//        if (messageSocket.getMsgType() - 7 == 0) { // init market
//            List<MarketStatus> symbolInfoList = MsgUtils.parseListMsg7(messageSocket.getMessage());
//            CompletableFuture completableFuture = requestKeyMap.getOrDefault(4, null);
//            if (completableFuture != null) {
//                completableFuture.complete(symbolInfoList);
//            } else {
//                log.info("no handle response");
//            }
//        }
//        if (messageSocket.getMsgType() - 8 == 0) { // top
//            List<BidOffer> bidOfferList = MsgUtils.parseListMsg8(messageSocket.getMessage());
//            for (BidOffer bidOffer : bidOfferList) {
//                log.info("bidOffer: {}", bidOffer);
//                if ("E".equals(bidOffer.getOe())) {
//                    kafkaPublish("market.bidoffer", bidOffer, bidOffer.getS());
//                    scPublish("market.bidoffer." + bidOffer.getS(), bidOffer);
//                } else {
//                    bidOffer.setBoOd(bidOffer.getBo());
//                    bidOffer.setBbOd(bidOffer.getBb());
//                    bidOffer.setBo(null);
//                    bidOffer.setBb(null);
//                    kafkaPublish("market.bidoffer.oddlot", bidOffer);
//                    scPublish("market.bidoffer.oddlot." + bidOffer.getS(), bidOffer);
//                }
//            }
//            return;
//        }
//        if (messageSocket.getMsgType() - 9 == 0) { // quote
//            Object object = MsgUtils.parseMsg9(messageSocket.getMessage());
//            if (object instanceof Quote) {  // khớp
//                log.info("stockQuote: {}", object);
//                kafkaPublish("market.quote", object, ((Quote) object).getS());
////                scPublish("market.quote." + ((Quote) object).getS(), object);
//            } else if (object instanceof ExtraQuote) { // tạm khớp
//                kafkaPublish("market.extra", object, ((ExtraQuote) object).getS());
//                scPublish("market.bidoffer." + ((ExtraQuote) object).getS(), object);
//                log.info("extraQuote: {}", object);
//            } else if (object instanceof QuoteOddLot) { // lô lẻ
//                kafkaPublish("market.quote.oddlot", object, ((QuoteOddLot) object).getS());
//                scPublish("market.quote.oddlot." + ((QuoteOddLot) object).getS(), object);
//                log.info("quoteOddlot: {}", object);
//            }
//        }
//        if (messageSocket.getMsgType() - 10 == 0) { // advertised/dealNotice
//            Object object = MsgUtils.parseMsg10(messageSocket.getMessage());
//            if (object instanceof DealNotice) {
//                log.info("dealNotice: {}", object);
//                kafkaPublish("market.dealNotice", object);
//                scPublish("market.dealNotice." + ((DealNotice) object).getM(), object);
//            } else if (object instanceof Advertised) {
//                log.info("advertised: {}", object);
//                kafkaPublish("market.advertised", object);
//                scPublish("market.advertised." + ((Advertised) object).getM(), object);
//            }
//        }
//    }
//
//
////    synchronized static void saveToFile(String data) {
////        String filePath = "history-kafka.txt";
////        try {
////            File file = new File(filePath);
////            boolean fileExists = file.exists();
////
////            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
////            if (!fileExists) {
////                file.createNewFile();
////            } else {
////                writer.newLine(); // Thêm một dòng trống trước khi thêm nội dung mới
////            }
////            writer.write(data);
////            writer.close();
////        } catch (IOException e) {
////            System.out.println("Xảy ra lỗi khi thêm nội dung vào file: " + e.getMessage());
////        }
////    }
//}
