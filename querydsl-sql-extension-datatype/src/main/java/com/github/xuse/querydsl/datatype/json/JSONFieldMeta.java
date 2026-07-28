package com.github.xuse.querydsl.datatype.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * {@code io.github.xuse.fastjson.adapter.JSONField} 注解的属性快照。
 * <p>
 * 该注解属于可选依赖，本类通过反射读取其属性并统一为普通值对象，
 * 使调用方无需直接依赖注解类型。若类路径上不存在该注解，本类静默失效。
 */
final class JSONFieldMeta {

	/** 注解类型，类路径上不存在时为 null */
	private static final Class<? extends Annotation> ANNOTATION_CLASS;

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
			// 注解不在类路径上，相关能力不生效
		} catch (NoSuchMethodException ignored) {
			// 注解结构不匹配，安全降级
			clz = null;
		}
		ANNOTATION_CLASS = clz;
		M_NAME = mName;
		M_ALTERNATE_NAMES = mAlternateNames;
		M_FORMAT = mFormat;
		M_SERIALIZE = mSerialize;
		M_DESERIALIZE = mDeserialize;
	}

	final String name;
	final String[] alternateNames;
	final String format;
	final boolean serialize;
	final boolean deserialize;

	private JSONFieldMeta(Annotation ann) {
		String name0 = invoke(M_NAME, ann);
		String[] alternateNames0 = invoke(M_ALTERNATE_NAMES, ann);
		String format0 = invoke(M_FORMAT, ann);
		Boolean serialize0 = invoke(M_SERIALIZE, ann);
		Boolean deserialize0 = invoke(M_DESERIALIZE, ann);
		this.name = name0 == null ? "" : name0;
		this.alternateNames = alternateNames0 == null ? new String[0] : alternateNames0;
		this.format = format0 == null ? "" : format0;
		this.serialize = serialize0 == null || serialize0;
		this.deserialize = deserialize0 == null || deserialize0;
	}

	/**
	 * 判断注解是否在类路径上可用。
	 *
	 * @return true 表示可用
	 */
	static boolean isAvailable() {
		return ANNOTATION_CLASS != null;
	}

	/**
	 * 将注解实例转为属性快照。
	 *
	 * @param ann 注解实例，可为 null
	 * @return 属性快照，入参为 null 时返回 null
	 */
	static JSONFieldMeta of(Annotation ann) {
		return ann == null ? null : new JSONFieldMeta(ann);
	}

	/** 注解类型，未加载时为 null */
	static Class<? extends Annotation> annotationClass() {
		return ANNOTATION_CLASS;
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
