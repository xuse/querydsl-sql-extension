package io.github.xuse.fastjson.adapter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * {@link JSONObject} / {@link JSONArray} 的序列化器。
 * <p>
 * 两者是包装 Jackson 节点的普通类，若不显式注册序列化器，Jackson 会按 bean 内省，
 * 把 {@code getInner()} / {@code getInnerMap()} / {@code isEmpty()} 当作属性输出，
 * 得到 {@code {"empty":false,"innerMap":{...},"inner":{...}}} 这类错误结果。
 * <p>
 * 此处直接写出底层节点，保证 {@code JSONObject} 作为字段值或嵌套元素时
 * 输出其真实 JSON 内容。
 */
final class JSONContainerSerializer {

    private JSONContainerSerializer() {}

    static final JsonSerializer<JSONObject> OBJECT = new JsonSerializer<JSONObject>() {
        @Override
        public void serialize(JSONObject value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeTree(value.getInner());
            }
        }
    };

    static final JsonSerializer<JSONArray> ARRAY = new JsonSerializer<JSONArray>() {
        @Override
        public void serialize(JSONArray value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeTree(value.getInner());
            }
        }
    };
}
