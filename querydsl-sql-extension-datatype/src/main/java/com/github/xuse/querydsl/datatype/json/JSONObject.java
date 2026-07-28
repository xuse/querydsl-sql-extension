package com.github.xuse.querydsl.datatype.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 兼容 com.alibaba.fastjson.JSONObject 的 API。
 * <p>
 * 底层使用 Jackson 的 ObjectNode，对外暴露与 fastjson JSONObject 一致的方法签名。
 */
public class JSONObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ObjectNode node;

    public JSONObject() {
        this.node = JSON.getObjectMapper().createObjectNode();
    }

    public JSONObject(ObjectNode node) {
        this.node = node != null ? node : JSON.getObjectMapper().createObjectNode();
    }

    /** 获取底层 Jackson ObjectNode */
    public ObjectNode getInner() {
        return node;
    }

    // ==================== 静态工厂 ====================

    /** 兼容 JSONObject.parseObject(str) */
    public static JSONObject parseObject(String text) {
        return JSON.parseObject(text);
    }

    /** 兼容 JSONObject.toJSONString(obj) */
    public static String toJSONString(Object obj) {
        return JSON.toJSONString(obj);
    }

    // ==================== get 方法族 ====================

    public Object get(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        if (child.isObject()) {
            return new JSONObject((ObjectNode) child);
        }
        if (child.isArray()) {
            return new JSONArray((ArrayNode) child);
        }
        if (child.isTextual()) {
            return child.asText();
        }
        if (child.isInt()) {
            return child.intValue();
        }
        if (child.isLong()) {
            return child.longValue();
        }
        if (child.isDouble() || child.isFloat()) {
            return child.doubleValue();
        }
        if (child.isBoolean()) {
            return child.booleanValue();
        }
        return child.toString();
    }

    public String getString(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.isTextual() ? child.asText() : child.toString();
    }

    public Integer getInteger(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asInt();
    }

    public int getIntValue(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return 0;
        }
        return child.asInt();
    }

    public Long getLong(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asLong();
    }

    public long getLongValue(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return 0L;
        }
        return child.asLong();
    }

    public Double getDouble(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asDouble();
    }

    public double getDoubleValue(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return 0.0;
        }
        return child.asDouble();
    }

    public Boolean getBoolean(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asBoolean();
    }

    public boolean getBooleanValue(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return false;
        }
        return child.asBoolean();
    }

    public BigDecimal getBigDecimal(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.decimalValue();
    }

    public BigInteger getBigInteger(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.bigIntegerValue();
    }

    public JSONObject getJSONObject(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        if (child.isObject()) {
            return new JSONObject((ObjectNode) child);
        }
        // 如果是字符串，尝试解析
        if (child.isTextual()) {
            return JSON.parseObject(child.asText());
        }
        return null;
    }

    public JSONArray getJSONArray(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        if (child.isArray()) {
            return new JSONArray((ArrayNode) child);
        }
        return null;
    }

    // ==================== put / remove ====================

    public JSONObject put(String key, Object value) {
        if (value == null) {
            node.putNull(key);
        } else if (value instanceof String) {
            node.put(key, (String) value);
        } else if (value instanceof Integer) {
            node.put(key, (Integer) value);
        } else if (value instanceof Long) {
            node.put(key, (Long) value);
        } else if (value instanceof Double) {
            node.put(key, (Double) value);
        } else if (value instanceof Float) {
            node.put(key, (Float) value);
        } else if (value instanceof Boolean) {
            node.put(key, (Boolean) value);
        } else if (value instanceof BigDecimal) {
            node.put(key, (BigDecimal) value);
        } else if (value instanceof JSONObject) {
            node.set(key, ((JSONObject) value).getInner());
        } else if (value instanceof JSONArray) {
            node.set(key, ((JSONArray) value).getInner());
        } else {
            // 将任意对象转为 JsonNode
            node.set(key, JSON.getObjectMapper().valueToTree(value));
        }
        return this;
    }

    public Object remove(String key) {
        Object old = get(key);
        node.remove(key);
        return old;
    }

    // ==================== 容器方法 ====================

    public boolean containsKey(String key) {
        return node.has(key);
    }

    public int size() {
        return node.size();
    }

    public boolean isEmpty() {
        return node.isEmpty();
    }

    public Set<String> keySet() {
        Set<String> keys = new java.util.LinkedHashSet<>();
        node.fieldNames().forEachRemaining(keys::add);
        return keys;
    }

    /** 转为 Map（浅层） */
    public Map<String, Object> getInnerMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            map.put(entry.getKey(), nodeToValue(entry.getValue()));
        }
        return map;
    }

    // ==================== toJavaObject ====================

    /** 兼容 jsonObject.toJavaObject(Class) */
    public <T> T toJavaObject(Class<T> clazz) {
        return JSON.toJavaObject(this, clazz);
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return node.toString();
    }

    // ==================== 内部工具 ====================

    private static Object nodeToValue(JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (n.isTextual()) return n.asText();
        if (n.isInt()) return n.intValue();
        if (n.isLong()) return n.longValue();
        if (n.isDouble()) return n.doubleValue();
        if (n.isBoolean()) return n.booleanValue();
        if (n.isObject()) return new JSONObject((ObjectNode) n);
        if (n.isArray()) return new JSONArray((ArrayNode) n);
        return n.toString();
    }
}
