package com.ftl.collector;

import com.ftl.collector.configurations.AppConf;
//import com.ftl.collector.configurations.RedisConf;
import com.ftl.collector.configurations.RedisConf;
import com.ftl.collector.configurations.SpringConf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties({AppConf.class, SpringConf.class})
//@EnableScheduling
@EnableAsync
@EnableCaching
public class EmulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmulatorApplication.class, args);
    }

}
