//package com.ftl.collector.boardsocket;
//
//import blazing.chain.LZSEncoding;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ftl.collector.configurations.AppConf;
//import com.ftl.collector.model.MessageSocket;
//import com.ftl.collector.producers.RequestSender;
//import com.ftl.collector.services.SocketClusterService;
//import com.ftl.collector.utils.MsgUtils;
//import com.ftl.common.model.market.Advertised;
//import com.ftl.common.model.market.BidOffer;
//import com.ftl.common.model.market.DealNotice;
//import com.ftl.common.model.market.ExtraQuote;
//import com.ftl.common.model.market.MarketStatus;
//import com.ftl.common.model.market.Quote;
//import com.ftl.common.model.market.QuoteOddLot;
//import com.ftl.common.model.market.SymbolInfo;
//import com.ftl.common.model.market.SymbolStatic;
//import com.ftl.common.utils.FtlDateUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import java.util.concurrent.*;
//import javax.websocket.ClientEndpoint;
//import javax.websocket.CloseReason;
//import javax.websocket.ContainerProvider;
//import javax.websocket.OnClose;
//import javax.websocket.OnError;
//import javax.websocket.OnMessage;
//import javax.websocket.OnOpen;
//import javax.websocket.Session;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.net.URI;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//
//
//@ClientEndpoint
//@Component
//public class MyClientEndpoint {
//    private static final Logger log = LoggerFactory.getLogger(MyClientEndpoint.class);
//    public static ConcurrentHashMap<Integer, CompletableFuture> requestKeyMap = new ConcurrentHashMap<>();
//    private final AppConf appConf;
//    private final ObjectMapper objectMapper;
//    private final SocketClusterService socketClusterService;
//    private final RequestSender requestSender;
//    private Session session = null;
//    private volatile boolean receivedAuto = true;
//    private volatile boolean connected = true;
//
//
//    @Autowired
//    public MyClientEndpoint(ObjectMapper objectMapper,
//                            AppConf appConf,
//                            SocketClusterService socketClusterService,
//                            RequestSender requestSender
//    ) {
//        this.appConf = appConf;
//        this.objectMapper = objectMapper;
//        this.socketClusterService = socketClusterService;
//        this.requestSender = requestSender;
//
//
//        connectCoreSocket();
//
//    }
//
//    public synchronized static void saveToFile(String data) {
//        String filePath = "history-socket.txt";
//        try {
//            File file = new File(filePath);
//            boolean fileExists = file.exists();
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
//            if (!fileExists) {
//                file.createNewFile();
//            } else {
//                writer.newLine(); // Thêm một dòng trống trước khi thêm nội dung mới
//            }
//
//            writer.write(data);
//            writer.close();
//        } catch (IOException e) {
//            System.out.println("Xảy ra lỗi khi thêm nội dung vào file: " + e.getMessage());
//        }
//    }
//
//    @OnOpen
//    public void handleOpen(Session session) {
//        this.connected = true;
//        log.info("Connected to Server with id {}", session.getId());
//    }
//
//    @OnMessage
//    public void handleMessage(String message) throws IOException {
//        String decompressedBase64 = LZSEncoding.decompressFromBase64(message);
//        MessageSocket messageSocket = objectMapper.readValue(decompressedBase64, MessageSocket.class);
//
//        if (messageSocket.getMsgType() - 1 == 0) { // thoi gian server
////            log.info("ignore msgType = 1");
//            return;
//        }
//
//
//        if (messageSocket.getMsgType() == 4 || messageSocket.getMsgType() == 5 || messageSocket.getMsgType() == 7) {
//            log.info("messageJson: {}", messageSocket);
//        }
//
//        if (messageSocket.getMsgType() - 4 == 0) { // init stock
//            List<SymbolInfo> symbolInfoList = MsgUtils.parseListMsg4(messageSocket.getMessage());
//            CompletableFuture completableFuture = requestKeyMap.getOrDefault(2, null);
//            if (completableFuture != null) {
//                completableFuture.complete(symbolInfoList);
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
//
//        if (messageSocket.getMsgType() - 7 == 0) { // init market
//            List<MarketStatus> symbolInfoList = MsgUtils.parseListMsg7(messageSocket.getMessage());
//            CompletableFuture completableFuture = requestKeyMap.getOrDefault(4, null);
//            if (completableFuture != null) {
//                completableFuture.complete(symbolInfoList);
//            } else {
//                log.info("no handle response");
//            }
//        }
//        if ("socket".equals(appConf.getPriceChannel())) {
//            if (messageSocket.getMsgType() - 2 == 0) { // extra
//                receivedAuto = true;
//                SymbolStatic symbolInfo = MsgUtils.parseMsg2(messageSocket.getMessage());
//                if (symbolInfo != null) {
//                    kafkaPublish("market.extra", symbolInfo);
//                    scPublish("market.bidoffer." + symbolInfo.getS(), symbolInfo);
//                    log.info("extraQuote: {}", symbolInfo);
//                } else {
//                    log.warn("parseMsg2 return null");
//                }
//                return;
//            }
//            if (messageSocket.getMsgType() - 3 == 0) { // index extra/quote
//                receivedAuto = true;
//                Object object = MsgUtils.parseMsg3(messageSocket.getMessage());
//                if (object instanceof Quote) {
//                    log.info("indexQuote: {}", object);
//                    kafkaPublish("market.index", object);
//                    scPublish("market.quote." + ((Quote) object).getS(), object);
//                } else if (object instanceof ExtraQuote) {
//                    kafkaPublish("market.extra", object);
//                    scPublish("market.bidoffer." + ((ExtraQuote) object).getS(), object);
//                    log.info("extraQuote: {}", object);
//                }
//                return;
//            }
//            if (messageSocket.getMsgType() - 6 == 0) { // market status change
//                receivedAuto = true;
//                MarketStatus marketStatus = MsgUtils.parseMsg6(messageSocket.getMessage());
//                if (marketStatus != null) {
//                    scPublish("market.status", marketStatus);
//                    kafkaPublish("market.status", marketStatus);
//                }
//                return;
//            }
//            if (messageSocket.getMsgType() - 8 == 0) { // top
//                receivedAuto = true;
//                List<BidOffer> bidOfferList = MsgUtils.parseListMsg8(messageSocket.getMessage());
//                for (BidOffer bidOffer : bidOfferList) {
//                    log.info("bidOffer: {}", bidOffer);
//                    if ("E".equals(bidOffer.getOe())) {
//                        kafkaPublish("market.bidoffer", bidOffer);
//                        scPublish("market.bidoffer." + bidOffer.getS(), bidOffer);
//                    } else {
//                        bidOffer.setBoOd(bidOffer.getBo());
//                        bidOffer.setBbOd(bidOffer.getBb());
//                        bidOffer.setBo(null);
//                        bidOffer.setBb(null);
//                        kafkaPublish("market.bidoffer.oddlot", bidOffer);
//                        scPublish("market.bidoffer.oddlot." + bidOffer.getS(), bidOffer);
//                    }
//                }
//                return;
//            }
//            if (messageSocket.getMsgType() - 9 == 0) { // quote
//                receivedAuto = true;
//                Object object = MsgUtils.parseMsg9(messageSocket.getMessage());
//                if (object instanceof Quote) {
//                    log.info("stockQuote: {}", object);
//                    kafkaPublish("market.quote", object);
//                    scPublish("market.quote." + ((Quote) object).getS(), object);
//                } else if (object instanceof ExtraQuote) {
//                    kafkaPublish("market.extra", object);
//                    scPublish("market.bidoffer." + ((ExtraQuote) object).getS(), object);
//                    log.info("extraQuote: {}", object);
//                } else if (object instanceof QuoteOddLot) {
//                    kafkaPublish("market.quote.oddlot", object);
//                    scPublish("market.quote.oddlot." + ((QuoteOddLot) object).getS(), object);
//                    log.info("quoteOddlot: {}", object);
//                }
//                return;
//            }
//            if (messageSocket.getMsgType() - 10 == 0) { // advertised/dealNotice
//                receivedAuto = true;
//                Object object = MsgUtils.parseMsg10(messageSocket.getMessage());
//                if (object instanceof DealNotice) {
//                    log.info("dealNotice: {}", object);
//                    kafkaPublish("market.dealNotice", object);
//                    scPublish("market.dealNotice." + ((DealNotice) object).getM(), object);
//                } else if (object instanceof Advertised) {
//                    log.info("advertised: {}", object);
//                    kafkaPublish("market.advertised", object);
//                    scPublish("market.advertised." + ((Advertised) object).getM(), object);
//                }
//                return;
//            }
//        }
////
//    }
//
//    @OnClose
//    public void handleClose(Session session, CloseReason closeReason) {
//        log.warn("closeReason: {}", closeReason);
//        this.connected = false;
//        if (this.session.getId().equals(session.getId())) {
//            connectCoreSocket();
//        }
//    }
//
//    public boolean isConnected() {
//        return connected;
//    }
//
//    @OnError
//    public void handleError(Throwable t) {
//        log.error("Encounter error ", t);
//    }
//
//    public void sendMessage(String message) throws IOException {
//        this.session.getBasicRemote().sendText(message);
//    }
//
//    public CompletableFuture sendAsync(int msgType) throws IOException {
//        log.info("send {}", msgType);
//        if (this.requestKeyMap.containsKey(msgType)) {
//            log.info("already contain msgTyp in map -> return");
//            return this.requestKeyMap.get(msgType);
//        }
//        CompletableFuture completableFuture = new CompletableFuture<>();
//        this.requestKeyMap.put(msgType, completableFuture);
//        MessageSocket msgSend = new MessageSocket();
//        msgSend.setMsgType(msgType);
//        sendMessage(objectMapper.writeValueAsString(msgSend));
//        return completableFuture;
//    }
//
////    public Object sendSync(int msgType, int maxRetry) throws Exception {
////        log.info("sendSync, msgType: {}, maxRetry: {}", msgType, maxRetry);
////        try {
////            Object res = this.sendAsync(msgType).get(10, TimeUnit.SECONDS);
////            requestKeyMap.remove(msgType);
////            return res;
////        } catch (Exception ex) {
////            log.error("ex: ", ex);
////            requestKeyMap.remove(msgType);
////            if (maxRetry > 1) {
////                FtlDateUtils.sleep(1000);
////                return sendSync(msgType, --maxRetry);
////            } else {
////                throw ex;
////            }
////        }
////    }
//
//    private void connectCoreSocket() {
//        try {
//            if (this.session != null && this.session.isOpen()) {
//                log.info("session is open -> close");
//                session.close();
//            }
//
//            URI coreSocketUri = new URI(appConf.getCoreSocketUri());
//            log.info("coreSocketUri: {}", coreSocketUri);
//            this.session = ContainerProvider.getWebSocketContainer().connectToServer(this, coreSocketUri);
//            this.session.setMaxTextMessageBufferSize(500000);
////            this.session.setMaxBinaryMessageBufferSize(Integer.MAX_VALUE);
//        } catch (Exception ex) {
//            log.error("error while connectWebsocket: ", ex);
//            FtlDateUtils.sleep(appConf.getReconnectionDelay());
//            connectCoreSocket();
//        }
//    }
//
//
//    @Scheduled(cron = "${app.schedulers.checkReceiveAuto1}")
//    @Scheduled(cron = "${app.schedulers.checkReceiveAuto2}")
//    @Scheduled(cron = "${app.schedulers.checkReceiveAuto3}")
//    @Scheduled(cron = "${app.schedulers.checkReceiveAuto4}")
//    @Scheduled(cron = "${app.schedulers.checkReceiveAuto5}")
//    public void checkReceiveAuto() {
//        if ("socket".equals(appConf.getPriceChannel())) {
//            log.info("--------------------------------");
//            Date current = new Date();
//            if (!FtlDateUtils.isTradingDate(current, appConf.getHolidays())) {
//                return;
//            }
//            log.info("checkReceiveAuto ---------------------------------------");
//            if (!receivedAuto) {
//                log.warn("receivedAuto = false -> restart service");
//                System.exit(0);
//            }
//            receivedAuto = false;
//        }
//    }
//
//
//    private void scPublish(String channel, Object data) {
//        this.socketClusterService.publish(channel, data);
//    }
//
//    private void kafkaPublish(String topic, Object symbolData) {
//        this.requestSender.sendMiniMessageSafeNoResponse(topic, "Update", symbolData);
//    }
//
//}
