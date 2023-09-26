package com.ftl.collector.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.ftl.collector.configurations.AppConf;
import com.ftl.common.socketcluster.Ack;
import com.ftl.common.socketcluster.BasicListener;
import com.ftl.common.socketcluster.ReconnectStrategy;
import com.ftl.common.socketcluster.Socket;
import com.ftl.common.socketcluster.codec.SocketMinBinCodec;
import com.ftl.common.utils.FtlStringUtils;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service
@RequiredArgsConstructor
public class SocketClusterService {
    private static final Logger log = LoggerFactory.getLogger(SocketClusterService.class);

    private final AppConf appConf;
    private List<Socket> sockets = new ArrayList<>();
    private final ObjectMapper objectMapper;

    private Ack publishAck = (String name, Object error, Object data) -> {
        if (error != null && !(error instanceof NullNode)) {
            if (error instanceof Throwable) {
                log.error("error on publishing to {}, {}", name, error);
            } else {
                log.error("error on publishing to {} with error {} and type {}", name, error, error.getClass());
            }
        }
    };

    @PostConstruct
    public void init() {
        if (!appConf.getSocketCluster().isEnable()) {
            return;
        }

        String prefixUrl = appConf.getSocketCluster().isSecure() ? "wss://" : "ws://";

//        String port = ":" + appConf.getSocketCluster().getPort();
//        if (appConf.getSocketCluster().isSecure() && appConf.getSocketCluster().getPort() - 443 == 0) {
//            port = "";
//        }

        for (String host : appConf.getSocketCluster().getHostname().split(",")) {
            String socketUrl = prefixUrl + host +
//                    port +
                    appConf.getSocketCluster().getPath();
            log.info("Init SC with url: {}", socketUrl);
            Socket socket = new Socket(socketUrl, this.objectMapper);

            if (appConf.getSocketCluster().isAutoReconnection()) {
                socket.setReconnection(new ReconnectStrategy().setDelay(600));
            }

            if (!appConf.getSocketCluster().isLogMessage()) {
                socket.disableLogging();
            }

            if (StringUtils.isEmpty(appConf.getSocketCluster().getCodec())
                    || appConf.getSocketCluster().getCodec().equals(AppConf.SocketClusterConf.CODEC_MIN_BIN)) {
                socket.setCodec(new SocketMinBinCodec());
            }

            socket.setListener(new BasicListener() {
                private long disconnectedTime = 0;
                private final long maxWarningDisconnectedTime = 15000;
                private Timer timer = null;

                @Override
                public void onConnected(Socket socket, Map<String, List<String>> headers) {
                    long time = System.currentTimeMillis();
                    if (disconnectedTime > 0 && time - disconnectedTime >= maxWarningDisconnectedTime) {
                        log.info("SC {} connected", host);
                    }
                    timer.cancel();
                    timer = null;
                    disconnectedTime = 0;
                }

                @Override
                public void onDisconnected(Socket socket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                    if (timer != null) return;
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            log.warn("SC {} disconnected for long time {}", host, disconnectedTime);
                        }
                    }, maxWarningDisconnectedTime);
                    disconnectedTime = System.currentTimeMillis();
                }

                @Override
                public void onConnectError(Socket socket, WebSocketException exception) {
                    log.warn("SC " + host + " connect error", exception);
                }

                @Override
                public void onAuthentication(Socket socket, Boolean status) {
                    log.warn("SC {} authentication state changed {}", host, status);
                }

                @Override
                public void onSetAuthToken(String token, Socket socket) {
                    log.warn("SC {} authentication data changed {}", host, token);
                }
            });
            socket.connect();

            sockets.add(socket);
        }



    }

    public void publish(String channel, Object data) {
        if (!appConf.getSocketCluster().isEnable()) {
            log.warn("socketCluster is disabled, cannot publish");
            return;
        }
        if (appConf.getSocketCluster().isLogMessage()) {
            log.info("scPublish_ chanel: {}, data: {}", channel, FtlStringUtils.convertObjToString(data));
        }
        if (!StringUtils.isEmpty(channel)) {
            Map<String, Object> map = objectMapper.convertValue(data, Map.class); // to ignore null field
            for (Socket socket : sockets) {
                socket.publish(channel, map, publishAck);
            }
        }
    }
}