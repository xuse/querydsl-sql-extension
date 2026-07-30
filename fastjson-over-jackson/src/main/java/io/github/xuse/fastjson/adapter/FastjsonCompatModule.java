package io.github.xuse.fastjson.adapter;

import java.util.Date;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Module 提供 fastjson 兼容能力。
 * <p>
 * 注册内容：
 * <ul>
 * <li>{@link Date} 的宽松反序列化器 {@link FlexibleDateDeserializer}</li>
 * <li>java.time (JSR-310) 类型的 fastjson 兼容编解码，见 {@link JavaTimeCodec}。
 * 无需引入 jackson-datatype-jsr310</li>
 * </ul>
 * {@link JSONField} 的字段名、别名、日期格式、serialize/deserialize 可见性
 * 均由 {@link FastjsonAnnotationIntrospector} 处理。
 */
public class FastjsonCompatModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public FastjsonCompatModule() {
        super("FastjsonCompat", Version.unknownVersion());
        addDeserializer(Date.class, new FlexibleDateDeserializer());
        JavaTimeCodec.register(this);
        // 无类型场景（Object、裸 List/Map）还原为 JSONObject/JSONArray
        addDeserializer(Object.class, UntypedJSONDeserializer.INSTANCE);
        // JSONObject/JSONArray 作为字段值时按 JSON 内容输出，避免被当作 bean 内省
        addSerializer(JSONObject.class, JSONContainerSerializer.OBJECT);
        addSerializer(JSONArray.class, JSONContainerSerializer.ARRAY);
    }
}
