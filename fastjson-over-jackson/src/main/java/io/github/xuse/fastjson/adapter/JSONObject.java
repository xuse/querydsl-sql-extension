package io.github.xuse.fastjson.adapter;

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
 * 底层使用 Jackson 的 ObjectNode。
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

    public ObjectNode getInner() {
        return node;
    }

    public static JSONObject parseObject(String text) {
        return JSON.parseObject(text);
    }

    public static String toJSONString(Object obj) {
        return JSON.toJSONString(obj);
    }

    // ==================== get 方法族 ====================

    public Object get(String key) {
        return nodeToValue(node.get(key));
    }

    public String getString(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.isTextual() ? child.asText() : child.toString();
    }

    public Integer getInteger(String key) {
        return TypeCast.toInteger(node.get(key));
    }

    public int getIntValue(String key) {
        return TypeCast.intValue(node.get(key));
    }

    public Long getLong(String key) {
        return TypeCast.toLong(node.get(key));
    }

    public long getLongValue(String key) {
        return TypeCast.longValue(node.get(key));
    }

    public Double getDouble(String key) {
        return TypeCast.toDouble(node.get(key));
    }

    public double getDoubleValue(String key) {
        return TypeCast.doubleValue(node.get(key));
    }

    public Float getFloat(String key) {
        return TypeCast.toFloat(node.get(key));
    }

    public float getFloatValue(String key) {
        return TypeCast.floatValue(node.get(key));
    }

    public Boolean getBoolean(String key) {
        return TypeCast.toBoolean(node.get(key));
    }

    public boolean getBooleanValue(String key) {
        return TypeCast.booleanValue(node.get(key));
    }

    public BigDecimal getBigDecimal(String key) {
        return TypeCast.toBigDecimal(node.get(key));
    }

    public BigInteger getBigInteger(String key) {
        return TypeCast.toBigInteger(node.get(key));
    }

    public JSONObject getJSONObject(String key) {
        JsonNode child = node.get(key);
        if (child == null || child.isNull()) {
            return null;
        }
        if (child.isObject()) {
            return new JSONObject((ObjectNode) child);
        }
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

    /**
     * 返回所有键值对的 entrySet 视图。
     * <p>
     * 兼容 fastjson 的 JSONObject.entrySet() 遍历模式。
     */
    public Set<Map.Entry<String, Object>> entrySet() {
        Map<String, Object> map = getInnerMap();
        return map.entrySet();
    }

    public Map<String, Object> getInnerMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            map.put(entry.getKey(), nodeToValue(entry.getValue()));
        }
        return map;
    }

    public <T> T toJavaObject(Class<T> clazz) {
        return JSON.toJavaObject(this, clazz);
    }

    @Override
    public String toString() {
        return node.toString();
    }

    private static Object nodeToValue(JsonNode n) {
        return JSONArray.nodeToValue(n);
    }
}
