package io.github.xuse.fastjson.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.Iterator;

/**
 * 兼容 com.alibaba.fastjson.JSONArray 的 API。
 * <p>
 * 底层使用 Jackson 的 ArrayNode。
 */
public class JSONArray implements Serializable, Iterable<Object> {

    private static final long serialVersionUID = 1L;

    private final ArrayNode node;

    public JSONArray() {
        this.node = JSON.getObjectMapper().createArrayNode();
    }

    public JSONArray(ArrayNode node) {
        this.node = node != null ? node : JSON.getObjectMapper().createArrayNode();
    }

    public ArrayNode getInner() {
        return node;
    }

    public int size() {
        return node.size();
    }

    public boolean isEmpty() {
        return node.isEmpty();
    }

    // ==================== get 方法族 ====================

    public Object get(int index) {
        JsonNode child = node.get(index);
        return nodeToValue(child);
    }

    public JSONObject getJSONObject(int index) {
        JsonNode child = node.get(index);
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

    public JSONArray getJSONArray(int index) {
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return null;
        }
        if (child.isArray()) {
            return new JSONArray((ArrayNode) child);
        }
        return null;
    }

    public String getString(int index) {
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.isTextual() ? child.asText() : child.toString();
    }

    public Integer getInteger(int index) {
        return TypeCast.toInteger(node.get(index));
    }

    public int getIntValue(int index) {
        return TypeCast.intValue(node.get(index));
    }

    public Long getLong(int index) {
        return TypeCast.toLong(node.get(index));
    }

    public long getLongValue(int index) {
        return TypeCast.longValue(node.get(index));
    }

    public Double getDouble(int index) {
        return TypeCast.toDouble(node.get(index));
    }

    public double getDoubleValue(int index) {
        return TypeCast.doubleValue(node.get(index));
    }

    public Float getFloat(int index) {
        return TypeCast.toFloat(node.get(index));
    }

    public float getFloatValue(int index) {
        return TypeCast.floatValue(node.get(index));
    }

    public Boolean getBoolean(int index) {
        return TypeCast.toBoolean(node.get(index));
    }

    public boolean getBooleanValue(int index) {
        return TypeCast.booleanValue(node.get(index));
    }

    public java.math.BigDecimal getBigDecimal(int index) {
        return TypeCast.toBigDecimal(node.get(index));
    }

    public java.math.BigInteger getBigInteger(int index) {
        return TypeCast.toBigInteger(node.get(index));
    }

    // ==================== add ====================

    public JSONArray add(Object value) {
        if (value == null) {
            node.addNull();
        } else if (value instanceof String) {
            node.add((String) value);
        } else if (value instanceof Integer) {
            node.add((Integer) value);
        } else if (value instanceof Long) {
            node.add((Long) value);
        } else if (value instanceof Double) {
            node.add((Double) value);
        } else if (value instanceof Float) {
            node.add((Float) value);
        } else if (value instanceof Boolean) {
            node.add((Boolean) value);
        } else if (value instanceof JSONObject) {
            node.add(((JSONObject) value).getInner());
        } else if (value instanceof JSONArray) {
            node.add(((JSONArray) value).getInner());
        } else {
            node.add(JSON.getObjectMapper().valueToTree(value));
        }
        return this;
    }

    public <T> java.util.List<T> toJavaList(Class<T> clazz) {
        return JSON.getObjectMapper().convertValue(node,
                JSON.getObjectMapper().getTypeFactory().constructCollectionType(java.util.List.class, clazz));
    }

    /**
     * 移除指定下标的元素并返回被移除的值。
     * <p>
     * 兼容 fastjson 的 JSONArray.remove(int index)
     */
    public Object remove(int index) {
        Object old = get(index);
        node.remove(index);
        return old;
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < node.size();
            }
            @Override
            public Object next() {
                return get(index++);
            }
        };
    }

    @Override
    public String toString() {
        return node.toString();
    }

    static Object nodeToValue(JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (n.isTextual()) return n.asText();
        if (n.isInt()) return n.intValue();
        if (n.isLong()) return n.longValue();
        // fastjson 默认 UseBigDecimal：浮点数以 BigDecimal 呈现
        if (n.isFloatingPointNumber()) return n.decimalValue();
        if (n.isBigInteger()) return n.bigIntegerValue();
        if (n.isBoolean()) return n.booleanValue();
        if (n.isObject()) return new JSONObject((ObjectNode) n);
        if (n.isArray()) return new JSONArray((ArrayNode) n);
        return n.toString();
    }
}
