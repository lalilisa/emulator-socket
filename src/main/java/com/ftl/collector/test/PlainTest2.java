package com.ftl.collector.test;

import java.time.Instant;

public class PlainTest2 {
    public static void main(String[] args) {
        String stringFromJavaScript = "2022-09-05T10:49:33.5925260+07:00";
        Instant inst = Instant.parse(stringFromJavaScript);
        System.out.println(inst);

    }
}
