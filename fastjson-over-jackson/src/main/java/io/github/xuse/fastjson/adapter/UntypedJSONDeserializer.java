package io.github.xuse.fastjson.adapter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

/**
 * 无类型场景下产出 {@link JSONObject} / {@link JSONArray}，对齐 fastjson 行为。
 * <p>
 * 当目标类型为 {@code Object}、裸 {@code List}、裸 {@code Map}（即缺少泛型参数）时，
 * Jackson 默认还原为 {@code LinkedHashMap} / {@code ArrayList}，而 fastjson 还原为
 * {@code JSONObject} / {@code JSONArray}。迁移代码中常见的
 * {@code (JSONObject) rawList.get(0)} 强转在前者下编译期无错、运行时抛
 * {@code ClassCastException}，故此处对齐 fastjson。
 * <p>
 * 仅接管容器的构造：标量值（字符串、数字、布尔）仍由父类处理，
 * 因此 {@code UseBigDecimal} 等数值语义不受影响。
 * <p>
 * 声明了泛型的集合（如 {@code List<Map<String,Object>>}）按声明类型还原，
 * 不走本反序列化器，与 fastjson 一致。
 */
class UntypedJSONDeserializer extends UntypedObjectDeserializer {

    private static final long serialVersionUID = 1L;

    static final UntypedJSONDeserializer INSTANCE = new UntypedJSONDeserializer();

    UntypedJSONDeserializer() {
        super(null, null);
    }

    /**
     * 对象 → JSONObject。
     * <p>
     * 父类返回 {@code Map}，此处按其内容构建 ObjectNode 后包装。
     */
    @Override
    protected Object mapObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object result = super.mapObject(p, ctxt);
        return result instanceof Map ? toJSONObject((Map<?, ?>) result) : result;
    }

    @Override
    protected Object mapArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object result = super.mapArray(p, ctxt);
        return result instanceof List ? toJSONArray((List<?>) result) : result;
    }

    private static JSONObject toJSONObject(Map<?, ?> map) {
        JSONObject obj = new JSONObject();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            obj.put(String.valueOf(e.getKey()), e.getValue());
        }
        return obj;
    }

    private static JSONArray toJSONArray(List<?> list) {
        JSONArray arr = new JSONArray();
        for (Object v : list) {
            arr.add(v);
        }
        return arr;
    }

}
