package com.ftl.collector.configurations;

import com.ftl.common.utils.FtlDateUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConf {
    private static final Logger log = LoggerFactory.getLogger(AppConf.class);

    private String clusterId;
    private String kafkaUrl;
    private Topics topics;
    private Uris uris;
    private String instanceId;
    private Integer kafkaTimeout;
    private Integer kafkaPartitions;
    private String coreSocketUri;
    private Integer reconnectionDelay;
    private String env;
    private String priceChannel;

    private Schedule schedulers;

    private String urlSymbolStatic;
    private int maxRetryQuery = 5;
    private int delayRetry = 3000;
    private SocketClusterConf socketCluster;
    private FileUploads fileUploads;
    private Minio minio;
    private TimeIgnoreBidAskChange timeIgnoreBidAskChange;
    private Channels channels;
    private List<String> holidays;
    private Map<String, Integer> highlightMap = new HashMap<>();
    private int defaultHighlightNumber = 1000;


    @PostConstruct
    public void init() {
        log.info("123appConf: {}", this);
    }

    @Data
    public static class TimeIgnoreBidAskChange {
        private String startTime;
        private String endTime;

        public Date getStart() {
            Date today = new Date();
            String time = FtlDateUtils.DATE_FORMAT().format(today) + startTime;
            try {
                return FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmss").parse(time);
            } catch (Exception ex) {
                log.error("error while parse startTime (TimeIgnoreBidAskChange): {}, {}", startTime, ex);
                return new Date();
            }
        }

        public Date getEnd() {
            Date today = new Date();
            String time = FtlDateUtils.DATE_FORMAT().format(today) + endTime;
            try {
                return FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmss").parse(time);
            } catch (Exception ex) {
                log.error("error while parse endTime (TimeIgnoreBidAskChange): {}, {}", endTime, ex);
                return new Date();
            }
        }
    }

    @Data
    public static class FileUploads {
        private String symbolStaticFile = "symbol.json";
        private String expectPrice = "symbol-expected-price.json";
        private String fiinStatic = "fiin_static.json";
    }

    @Data
    public static class Schedule {
        private String wsAggr;
        private boolean wsAggrEnabled;
    }

    @Data
    public static class Minio {
        private String endpoint;
        private String port;
        private String bucketName;
        private String accessKey;
        private String secretKey;
        private boolean useSSL;
        private String bucketPolicy;

        public String getBaseUrl() {
            String prefixUrl = this.isUseSSL() ? "https://" : "http://";
            String baseUrl = prefixUrl + this.getEndpoint() + ":" + this.getPort() + "/";
            log.info("minio baseUrl: {}", baseUrl);
            return baseUrl;
        }
    }


    @Data
    public static class Channels {
        private String quote;
        private String bidoffer;
        private String extra;
        private String marketStatus;
        private String marketInit;
    }

    @Data
    public static class Uris {
        private String marketPtDeal;
    }

    @Data
    public static class Topics {
        private String corePrice;
        private String quote;
        private String bidoffer;
        private String extra;
        private String marketStatus;
        private String marketQuery;
        private String marketInit;
        private String discover;
        private String stockDetailService;
        private String naviBridge;
        private String commonApi;
    }

    @Data
    public static class SocketClusterConf {
        public static final String CODEC_MIN_BIN = "codecMinBin";
        private boolean enable = true;
        private String codec = CODEC_MIN_BIN;
        private boolean autoReconnection = true;
        private boolean logMessage = false;
        private boolean secure = true;
        private String hostname = "localhost";
        private int port = 8000;
        private String path = "ws/socketcluster/";
    }

    public String getKafkaBootstraps() {
        return this.kafkaUrl.replace(";", ",");
    }


    @Data
    public static class TimeRangeConf {
        protected String startTime;
        protected String endTime;

        public Date getStart() {
            Date today = new Date();
            String time = FtlDateUtils.DATE_FORMAT().format(today) + startTime;
            try {
                return FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmss").parse(time);
            } catch (Exception ex) {
                log.error("error while parse startTime {}: {}, {}", this.getClass().getSimpleName(), startTime, ex);
                return new Date();
            }
        }

        public Date getEnd() {
            Date today = new Date();
            String time = FtlDateUtils.DATE_FORMAT().format(today) + endTime;
            try {
                return FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmss").parse(time);
            } catch (Exception ex) {
                log.error("error while parse endTime {}: {}, {}", this.getClass().getSimpleName(), endTime, ex);
                return new Date();
            }
        }

        public boolean isContain(Date date) {
            return date.getTime() - this.getStart().getTime() >= 0 && date.getTime() - this.getEnd().getTime() <= 0;
        }
    }
}
