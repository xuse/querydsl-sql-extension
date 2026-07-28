package com.github.xuse.querydsl.datatype.json;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * 通过反射识别 {@code io.github.xuse.fastjson.adapter.JSONField} 注解的 Jackson AnnotationIntrospector。
 * <p>
 * 本类不直接依赖 fastjson-over-jackson 模块，注解属性由 {@link JSONFieldMeta} 反射读取。
 * 如果类路径上不存在该注解，本 Introspector 静默失效。
 * <p>
 * 支持的注解属性：{@code name}、{@code alternateNames}、{@code format}、
 * {@code serialize}、{@code deserialize}。
 * <p>
 * Jackson 原生注解的优先级高于本 Introspector，由 {@code AnnotationIntrospectorPair} 保证。
 */
public class JSONFieldAnnotationIntrospector extends NopAnnotationIntrospector {

	private static final long serialVersionUID = 1L;

	/**
	 * 判断 @JSONField 注解是否在类路径上可用。
	 *
	 * @return true 表示可用，本 Introspector 会生效
	 */
	public static boolean isAvailable() {
		return JSONFieldMeta.isAvailable();
	}

	@Override
	public Version version() {
		return Version.unknownVersion();
	}

	@Override
	public PropertyName findNameForSerialization(Annotated a) {
		JSONFieldMeta meta = findMeta(a);
		if (meta != null && meta.serialize && !meta.name.isEmpty()) {
			return PropertyName.construct(meta.name);
		}
		return null;
	}

	@Override
	public PropertyName findNameForDeserialization(Annotated a) {
		JSONFieldMeta meta = findMeta(a);
		if (meta != null && meta.deserialize && !meta.name.isEmpty()) {
			return PropertyName.construct(meta.name);
		}
		return null;
	}

	@Override
	public List<PropertyName> findPropertyAliases(Annotated a) {
		JSONFieldMeta meta = findMeta(a);
		if (meta == null || !meta.deserialize) {
			return null;
		}
		List<PropertyName> aliases = new ArrayList<PropertyName>();
		for (String alt : meta.alternateNames) {
			if (alt != null && !alt.isEmpty()) {
				aliases.add(PropertyName.construct(alt));
			}
		}
		return aliases.isEmpty() ? null : aliases;
	}

	@Override
	public boolean hasIgnoreMarker(AnnotatedMember m) {
		JSONFieldMeta meta = findMeta(m);
		// serialize=false && deserialize=false → 完全忽略
		return meta != null && !meta.serialize && !meta.deserialize;
	}

	@Override
	public Boolean hasAsValue(Annotated a) {
		return null;
	}

	/**
	 * 将 {@code serialize=false} / {@code deserialize=false} 映射为 Jackson 的单向可见性。
	 * <p>
	 * 走 Jackson 合并后的注解表，字段与 getter/setter 上的注解都能识别。
	 */
	@Override
	public JsonProperty.Access findPropertyAccess(Annotated a) {
		JSONFieldMeta meta = findMeta(a);
		if (meta == null) {
			return null;
		}
		if (!meta.serialize && !meta.deserialize) {
			// 两者都 false 由 hasIgnoreMarker 整体忽略
			return null;
		}
		if (!meta.serialize) {
			// 只写：不参与序列化输出
			return JsonProperty.Access.WRITE_ONLY;
		}
		if (!meta.deserialize) {
			// 只读：不参与反序列化写入
			return JsonProperty.Access.READ_ONLY;
		}
		return null;
	}

	@Override
	public JsonFormat.Value findFormat(Annotated a) {
		JSONFieldMeta meta = findMeta(a);
		if (meta != null && !meta.format.isEmpty()) {
			return JsonFormat.Value.forPattern(meta.format)
					.withTimeZone(java.util.TimeZone.getDefault());
		}
		return null;
	}

	/**
	 * 从 Jackson 合并后的注解表中读取注解属性（涵盖 field/getter/setter）。
	 */
	private static JSONFieldMeta findMeta(Annotated a) {
		Class<? extends Annotation> type = JSONFieldMeta.annotationClass();
		if (type == null) {
			return null;
		}
		if (!(a instanceof AnnotatedField) && !(a instanceof AnnotatedMethod)) {
			return null;
		}
		return JSONFieldMeta.of(a.getAnnotation(type));
	}
}
