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
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asInt();
    }

    public int getIntValue(int index) {
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return 0;
        }
        return child.asInt();
    }

    public Long getLong(int index) {
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asLong();
    }

    public long getLongValue(int index) {
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return 0L;
        }
        return child.asLong();
    }

    public Double getDouble(int index) {
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asDouble();
    }

    public Boolean getBoolean(int index) {
        JsonNode child = node.get(index);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asBoolean();
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
