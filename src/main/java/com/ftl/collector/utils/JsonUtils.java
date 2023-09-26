package com.ftl.collector.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return "";
        }
    }

    public static <T> T fromString(String s, Class<T> clazz) {
        try {
            objectMapper.readValue(s, new TypeReference<T>() {
            });
            return objectMapper.readValue(s, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T fromStrWithTypeReference(String s, TypeReference<T> tr) {
        try {
            return objectMapper.readValue(s, tr);
        } catch (Exception e) {
            return null;
        }
    }
}
