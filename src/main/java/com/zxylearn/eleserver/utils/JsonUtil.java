package com.zxylearn.eleserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JsonUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public <K, V> String toJson(Map<K, V> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }
}