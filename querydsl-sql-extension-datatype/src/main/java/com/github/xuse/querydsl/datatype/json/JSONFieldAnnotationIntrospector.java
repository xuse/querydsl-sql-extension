package com.github.xuse.querydsl.datatype.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 本类不直接依赖 fastjson-over-jackson 模块，而是在运行时通过 {@code Class.forName} 尝试加载注解类。
 * 如果类路径上不存在该注解，本 Introspector 静默失效，不影响正常使用。
 * <p>
 * 支持的注解属性：
 * <ul>
 *   <li>{@code name()} — 指定 JSON 字段名</li>
 *   <li>{@code alternateNames()} — 反序列化时可识别的别名列表</li>
 *   <li>{@code format()} — 日期格式</li>
 *   <li>{@code serialize()} — 是否参与序列化</li>
 *   <li>{@code deserialize()} — 是否参与反序列化</li>
 * </ul>
 */
public class JSONFieldAnnotationIntrospector extends NopAnnotationIntrospector {

	private static final long serialVersionUID = 1L;

	/** 缓存的 @JSONField 注解 Class，加载失败则为 null */
	private static final Class<? extends Annotation> JSON_FIELD_CLASS;

	/** 各属性方法缓存 */
	private static final Method M_NAME;
	private static final Method M_ALTERNATE_NAMES;
	private static final Method M_FORMAT;
	private static final Method M_SERIALIZE;
	private static final Method M_DESERIALIZE;

	static {
		Class<? extends Annotation> clz = null;
		Method mName = null, mAlternateNames = null, mFormat = null, mSerialize = null, mDeserialize = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Annotation> c = (Class<? extends Annotation>)
					Class.forName("io.github.xuse.fastjson.adapter.JSONField");
			clz = c;
			mName = clz.getMethod("name");
			mAlternateNames = clz.getMethod("alternateNames");
			mFormat = clz.getMethod("format");
			mSerialize = clz.getMethod("serialize");
			mDeserialize = clz.getMethod("deserialize");
		} catch (ClassNotFoundException ignored) {
			// fastjson-over-jackson 不在类路径上，本 Introspector 不生效
		} catch (NoSuchMethodException ignored) {
			// 注解结构不匹配，安全降级
			clz = null;
		}
		JSON_FIELD_CLASS = clz;
		M_NAME = mName;
		M_ALTERNATE_NAMES = mAlternateNames;
		M_FORMAT = mFormat;
		M_SERIALIZE = mSerialize;
		M_DESERIALIZE = mDeserialize;
	}

	/**
	 * 判断 @JSONField 注解是否在类路径上可用。
	 *
	 * @return true 表示可用，本 Introspector 会生效
	 */
	public static boolean isAvailable() {
		return JSON_FIELD_CLASS != null;
	}

	@Override
	public Version version() {
		return Version.unknownVersion();
	}

	@Override
	public PropertyName findNameForSerialization(Annotated a) {
		Annotation ann = findAnnotation(a);
		if (ann == null) {
			return null;
		}
		if (!getSerialize(ann)) {
			return null;
		}
		String name = getName(ann);
		if (name != null && !name.isEmpty()) {
			return PropertyName.construct(name);
		}
		return null;
	}

	@Override
	public PropertyName findNameForDeserialization(Annotated a) {
		Annotation ann = findAnnotation(a);
		if (ann == null) {
			return null;
		}
		if (!getDeserialize(ann)) {
			return null;
		}
		String name = getName(ann);
		if (name != null && !name.isEmpty()) {
			return PropertyName.construct(name);
		}
		return null;
	}

	@Override
	public List<PropertyName> findPropertyAliases(Annotated a) {
		Annotation ann = findAnnotation(a);
		if (ann == null || !getDeserialize(ann)) {
			return null;
		}
		String[] alternates = getAlternateNames(ann);
		if (alternates == null || alternates.length == 0) {
			return null;
		}
		List<PropertyName> aliases = new ArrayList<PropertyName>(alternates.length);
		for (String alt : alternates) {
			if (alt != null && !alt.isEmpty()) {
				aliases.add(PropertyName.construct(alt));
			}
		}
		return aliases.isEmpty() ? null : aliases;
	}

	@Override
	public boolean hasIgnoreMarker(AnnotatedMember m) {
		if (JSON_FIELD_CLASS == null) {
			return false;
		}
		Annotation ann = m.getAnnotation(JSON_FIELD_CLASS);
		if (ann == null) {
			return false;
		}
		// serialize=false && deserialize=false → 完全忽略
		return !getSerialize(ann) && !getDeserialize(ann);
	}

	@Override
	public Boolean hasAsValue(Annotated a) {
		return null;
	}

	@Override
	public JsonFormat.Value findFormat(Annotated a) {
		Annotation ann = findAnnotation(a);
		if (ann != null) {
			String format = getFormat(ann);
			if (format != null && !format.isEmpty()) {
				return JsonFormat.Value.forPattern(format)
						.withTimeZone(java.util.TimeZone.getDefault());
			}
		}
		return null;
	}

	// ==================== 内部方法 ====================

	private static Annotation findAnnotation(Annotated a) {
		if (JSON_FIELD_CLASS == null) {
			return null;
		}
		if (a instanceof AnnotatedField || a instanceof AnnotatedMethod) {
			return a.getAnnotation(JSON_FIELD_CLASS);
		}
		return null;
	}

	private static String getName(Annotation ann) {
		return invoke(M_NAME, ann);
	}

	private static String[] getAlternateNames(Annotation ann) {
		return invoke(M_ALTERNATE_NAMES, ann);
	}

	private static String getFormat(Annotation ann) {
		return invoke(M_FORMAT, ann);
	}

	private static boolean getSerialize(Annotation ann) {
		Boolean val = invoke(M_SERIALIZE, ann);
		return val == null || val;
	}

	private static boolean getDeserialize(Annotation ann) {
		Boolean val = invoke(M_DESERIALIZE, ann);
		return val == null || val;
	}

	@SuppressWarnings("unchecked")
	private static <T> T invoke(Method method, Annotation ann) {
		if (method == null) {
			return null;
		}
		try {
			return (T) method.invoke(ann);
		} catch (Exception e) {
			return null;
		}
	}
}
