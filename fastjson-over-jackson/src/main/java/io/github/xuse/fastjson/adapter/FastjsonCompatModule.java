package io.github.xuse.fastjson.adapter;

import java.util.Date;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Module 提供 fastjson 兼容能力。
 * <p>
 * 目前仅注册宽松的日期反序列化器；{@link JSONField} 的字段名、别名、日期格式、
 * serialize/deserialize 可见性均由 {@link FastjsonAnnotationIntrospector} 处理。
 */
public class FastjsonCompatModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public FastjsonCompatModule() {
        super("FastjsonCompat", Version.unknownVersion());
        addDeserializer(Date.class, new FlexibleDateDeserializer());
    }
}
