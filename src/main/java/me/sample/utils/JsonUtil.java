package me.sample.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class JsonUtil {

    private static ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String json(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Map<String, Object> marshallToMap(Object val) {
        return MAPPER.convertValue(val, Map.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> unmarshallMapFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return MAPPER.readValue(json, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Can not unmarshall message", e);
        }
    }
}
