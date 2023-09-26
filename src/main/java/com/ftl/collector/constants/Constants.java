package com.ftl.collector.constants;

public interface Constants {
    int DEFAULT_FETCH_COUNT = 100;


    public final class Redis {
        public static final String TOPIC_SUBSCRIBE = "MC_INTERNAL_EVENT";
        public static final String COMMON_PARAMS = "COMMON_PARAMS";
        public static final String DEPLAY_TIME = "DEPLAY_TIME";
        public static final String NUMBER_OF_PACKET = "NUMBER_OF_PACKET";
        public static final String ENABLE_EMULATOR = "ENABLE_EMULATOR";
//        public static final String SYMBOL_MAP_KEY = "MC_SYMBOL_MAP";
//        public static final String AGGR_QUOTES_CACHE = "MC_AGGR_QUOTES";
//        public static final String USING_EXPECTED_PRICE_KEY  = "USING_EXPECTED_PRICE";
//        public static final String ENABLE_CRON_EXPECTED_PRICE  = "ENABLE_CRON_EXPECTED_PRICE";
//        public static final String CAN_TRANSFER  = "CAN_TRANSFER";

    }
}
