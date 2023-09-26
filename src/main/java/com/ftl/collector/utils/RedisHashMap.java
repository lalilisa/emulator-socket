package com.ftl.collector.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class RedisHashMap<V> implements Map<String, V> {

    private final HashOperations op;
    private final String redisKey;
    private final ObjectMapper om;
//    private final TypeReference<K> typeKey;
    private final TypeReference<V> typeVlue;

    public RedisHashMap(RedisTemplate<String, String> redisTemplate, String keyName) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        this.op = redisTemplate.opsForHash();
        this.redisKey = keyName;
        this.om = new ObjectMapper();
        Type[] genericTypes = ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments();
        assert genericTypes.length == 1;
//        this.typeKey = new CustomTypeRef<>(genericTypes[0]);
        this.typeVlue = new CustomTypeRef<>(genericTypes[0]);

        this.om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    /*
    * Use this constructor when type of key or value is parameterized type.
    * Ex: var r = new RedisHashMap<String, List<Banner>>(redisTemplate, "redis_hash_key",
                new TypeReference<String>() {
                },
                new TypeReference<List<Banner>>() {
                }
        );
    * Thanks to stupid generic
    */
    public RedisHashMap(RedisTemplate<String, String> redisTemplate, String keyName, TypeReference<V> typeVlue) {
        this.op = redisTemplate.opsForHash();
        this.redisKey = keyName;
        this.om = new ObjectMapper();
        this.typeVlue = typeVlue;

        this.om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public int size() {
        Long size = op.size(redisKey);
        if (size == null || size < 1) return 0;
        return size.intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() < 1;
    }

    @Override
    public boolean containsKey(Object key) {
        return op.hasKey(redisKey, toString(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return false; // To reduce redis call
    }

    @Override
    public V get(Object key) {
        var redisValue = op.get(redisKey, toString(key));
        if (redisValue != null) {
            return fromString(redisValue.toString(), typeVlue);
        }
        return null;
    }

    @Override
    public V put(String key, V value) {
//        log.info("Put {} with key {} value {}", redisKey, toString(key), toString(value));
        op.put(redisKey, key, toString(value));
        return null; // To reduce redis call
    }

    @Override
    public V remove(Object key) {
        op.delete(redisKey, key);
        return null; // To reduce redis call
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        var mapCache = map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> toString(e.getValue())
                ));
        op.putAll(redisKey, mapCache);
    }

    @Override
    public void clear() {
        op.getOperations().delete(redisKey);
    }

    @Override
    public Set<String> keySet() {
        Set<String> res = new HashSet<>();
        op.keys(redisKey)
                .stream()
                .forEach(e -> {
                    res.add(e.toString());
                });
        return res;
    }

    @Override
    public Collection<V> values() {
        List<V> res = new ArrayList<>();
        op.values(redisKey)
                .stream()
                .forEach(e -> {
                    V v = fromString(e.toString(), typeVlue);
                    res.add(v);
                });
        return res;
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        Map map = op.entries(redisKey);
        Set<Entry<String, V>>  res = new HashSet<>();
        if (map != null) {
            for (Object key : map.keySet()) {
                String hashKey = key.toString();
                V value = fromString(map.get(key).toString(), typeVlue);
                res.add(new AbstractMap.SimpleEntry<>(hashKey, value));
            }
        }
        return res;
    }



    <T> T fromString(String s, TypeReference<T> tr) {
        try {
            return om.readValue(s, tr);
        } catch (Exception e) {
            log.warn("Deserialize error. Data: " + s, e);
            return null;
        }
    }

    public String toString(Object obj) {
        try {
            return om.writeValueAsString(obj);
        } catch (Throwable e) {
            log.error("Serialize error. Data: " + obj, e.getMessage());
            return "";
        }
    }

    static class CustomTypeRef<T> extends TypeReference<T> {

        Type customType;

        public CustomTypeRef(Type customType) {
            this.customType = customType;
        }
        public Type getType() {
            if (customType != null) return customType;
            return _type;
        }
    }
}
