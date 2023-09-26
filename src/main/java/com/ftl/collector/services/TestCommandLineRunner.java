package com.ftl.collector.services;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TestCommandLineRunner implements CommandLineRunner {
    private final SendEmulatorData sendEmulatorData;
    @Override
    public void run(String... args) throws Exception {
        //sendEmulatorData.testSendData();
    }
}
