package com.ftl.collector.configurations;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "spring")
@Data
public class SpringConf {
    private static final Logger log = LoggerFactory.getLogger(SpringConf.class);

    private Application application;


    @Data
    public static class Application {
        private String name;
    }

    @PostConstruct
    public void init() {
        log.info("springConf: {}", this);
    }

}
