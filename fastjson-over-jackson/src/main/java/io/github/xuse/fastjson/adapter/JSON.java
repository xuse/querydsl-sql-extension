package io.github.xuse.fastjson.adapter;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Fastjson API 兼容层 — 基于 Jackson 实现。
 * <p>
 * 迁移方式：将 import com.alibaba.fastjson.JSON 替换为 import io.github.xuse.fastjson.adapter.JSON
 */
public final class JSON {
    /** 全局共享 ObjectMapper（线程安全，不可修改配置） */
    private static final ObjectMapper MAPPER = createMapper();

    /**
     * 兼容 fastjson 的 DEFFAULT_DATE_FORMAT 字段。
     */
    public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private JSON() {}

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new FastjsonCompatModule());
        AnnotationIntrospector defaultAi = mapper.getSerializationConfig().getAnnotationIntrospector();
        AnnotationIntrospector pair = AnnotationIntrospectorPair.pair(defaultAi, new FastjsonAnnotationIntrospector());
        mapper.setAnnotationIntrospector(pair);
        return mapper;
    }

    /** 获取内部 ObjectMapper，供高级场景使用 */
    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    // ==================== toJSONString ====================

    public static String toJSONString(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JSONException("Serialize failed", e);
        }
    }

    public static String toJSONString(Object object, boolean prettyFormat) {
        if (object == null) {
            return "null";
        }
        try {
            if (prettyFormat) {
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            }
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JSONException("Serialize failed", e);
        }
    }

    public static String toJSONStringWithDateFormat(Object object, String dateFormat) {
        if (object == null) {
            return "null";
        }
        try {
            ObjectMapper copy = MAPPER.copy();
            copy.setDateFormat(new SimpleDateFormat(dateFormat));
            return copy.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JSONException("Serialize failed", e);
        }
    }

    // ==================== parseObject ====================

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text, clazz);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize failed: " + truncate(text), e);
        }
    }

    public static <T> T parseObject(String text, Type type) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text, MAPPER.getTypeFactory().constructType(type));
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize failed: " + truncate(text), e);
        }
    }

    /**
     * 泛型反序列化。
     * <p>用法: JSON.parseObject(text, new TypeReference&lt;Map&lt;String, Object&gt;&gt;() {})
     */
    public static <T> T parseObject(String text, TypeReference<T> typeRef) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text, typeRef);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize failed: " + truncate(text), e);
        }
    }

    /** 解析为 JSONObject（兼容 fastjson 的 JSON.parseObject(str) 无 class 参数的用法） */
    public static JSONObject parseObject(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            ObjectNode node = (ObjectNode) MAPPER.readTree(text);
            return new JSONObject(node);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize to JSONObject failed: " + truncate(text), e);
        }
    }

    // ==================== parseArray ====================

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize array failed: " + truncate(text), e);
        }
    }

    public static JSONArray parseArray(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            ArrayNode node = (ArrayNode) MAPPER.readTree(text);
            return new JSONArray(node);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize to JSONArray failed: " + truncate(text), e);
        }
    }

    // ==================== parse ====================

    /** 兼容 fastjson 的 JSON.parse(str) — 返回 JSONObject 或 JSONArray 或基本类型 */
    public static Object parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(text);
            if (node.isObject()) {
                return new JSONObject((ObjectNode) node);
            } else if (node.isArray()) {
                return new JSONArray((ArrayNode) node);
            } else if (node.isTextual()) {
                return node.asText();
            } else if (node.isInt()) {
                return node.intValue();
            } else if (node.isLong()) {
                return node.longValue();
            } else if (node.isDouble() || node.isFloat()) {
                return node.doubleValue();
            } else if (node.isBoolean()) {
                return node.booleanValue();
            } else if (node.isNull()) {
                return null;
            }
            return node.toString();
        } catch (JsonProcessingException e) {
            throw new JSONException("Parse failed: " + truncate(text), e);
        }
    }

    // ==================== toJavaObject ====================

    /** 兼容 fastjson 的 JSON.toJavaObject(jsonObject, Class) */
    public static <T> T toJavaObject(JSONObject jsonObject, Class<T> clazz) {
        if (jsonObject == null) {
            return null;
        }
        try {
            return MAPPER.treeToValue(jsonObject.getInner(), clazz);
        } catch (JsonProcessingException e) {
            throw new JSONException("Convert JSONObject to JavaObject failed", e);
        }
    }

    // ==================== 内部工具 ====================

    private static String truncate(String text) {
        if (text == null) return "null";
        return text.length() > 64 ? text.substring(0, 64) + "..." : text;
    }
}
