package com.github.xuse.querydsl.datatype.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Module，配合 {@link JSONFieldAnnotationIntrospector} 支持
 * {@code io.github.xuse.fastjson.adapter.JSONField} 注解。
 * <p>
 * 注解的字段名、别名、日期格式，以及 {@code serialize=false} / {@code deserialize=false}
 * 的可见性控制均由 Introspector 处理（映射为 Jackson 的 {@code JsonProperty.Access}），
 * 本 Module 当前不再需要额外的属性过滤逻辑。
 */
public class JSONFieldCompatModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	public JSONFieldCompatModule() {
		super("JSONFieldCompat", Version.unknownVersion());
	}

	/**
	 * 判断是否可用（类路径上存在 @JSONField）。
	 *
	 * @return true 表示可用
	 */
	public static boolean isAvailable() {
		return JSONFieldMeta.isAvailable();
	}
}
