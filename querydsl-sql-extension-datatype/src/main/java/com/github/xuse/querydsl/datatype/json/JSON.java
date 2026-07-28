package com.github.xuse.querydsl.datatype.json;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

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
 * 迁移方式：将 import com.alibaba.fastjson.JSON 替换为 import com.ezviz.vas.simcard.carrier.json.JSON
 */
public final class JSON {
    /** 全局共享 ObjectMapper（线程安全，不可修改配置） */
    private static final ObjectMapper MAPPER = createMapper();

    /**
     * 兼容 fastjson 的 DEFFAULT_DATE_FORMAT 字段。
     * 注意：修改此字段不会影响已创建的 MAPPER，仅影响 toJSONStringWithDateFormat。
     */
    public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private JSON() {}

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 忽略未知属性（兼容 fastjson 默认行为）
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 空对象不报错
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // Date 序列化为时间戳毫秒（与 fastjson 默认行为一致）
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        // 注册兼容模块（Date 灵活反序列化 + serialize/deserialize 过滤）
        mapper.registerModule(new FastjsonCompatModule());
        // 注册 @JSONField 注解支持（追加在 Jackson 默认注解之后，不覆盖 @JsonProperty 等）
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

    /**
     * 兼容 fastjson 的 toJSONString(obj, prettyFormat) 和 toJSONString(obj, SerializerFeature...)
     * 当 prettyFormat=true 时格式化输出
     */
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

    /**
     * 兼容 fastjson 的 toJSONString(obj, SerializerFeature.WriteDateUseDateFormat)
     */
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
