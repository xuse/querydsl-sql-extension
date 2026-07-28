package com.github.xuse.querydsl.datatype.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * Jackson Module，通过反射识别 {@code io.github.xuse.fastjson.adapter.JSONField} 注解的
 * {@code serialize=false} 和 {@code deserialize=false} 属性，实现属性级别的序列化/反序列化过滤。
 * <p>
 * 与 {@link JSONFieldAnnotationIntrospector} 配合使用：Introspector 处理字段名映射和别名，
 * 本 Module 处理 serialize/deserialize 的排除逻辑。
 * <p>
 * 如果类路径上不存在 {@code @JSONField} 注解，本 Module 不产生任何效果。
 */
public class JSONFieldCompatModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	private static final Class<? extends Annotation> JSON_FIELD_CLASS;
	private static final Method M_SERIALIZE;
	private static final Method M_DESERIALIZE;

	static {
		Class<? extends Annotation> clz = null;
		Method mSerialize = null, mDeserialize = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Annotation> c = (Class<? extends Annotation>)
					Class.forName("io.github.xuse.fastjson.adapter.JSONField");
			clz = c;
			mSerialize = clz.getMethod("serialize");
			mDeserialize = clz.getMethod("deserialize");
		} catch (ClassNotFoundException ignored) {
		} catch (NoSuchMethodException ignored) {
			clz = null;
		}
		JSON_FIELD_CLASS = clz;
		M_SERIALIZE = mSerialize;
		M_DESERIALIZE = mDeserialize;
	}

	public JSONFieldCompatModule() {
		super("JSONFieldCompat", Version.unknownVersion());
		if (JSON_FIELD_CLASS != null) {
			setSerializerModifier(new SerializeFilterModifier());
			setDeserializerModifier(new DeserializeFilterModifier());
		}
	}

	/**
	 * 判断是否可用（类路径上存在 @JSONField）。
	 */
	public static boolean isAvailable() {
		return JSON_FIELD_CLASS != null;
	}

	private static class SerializeFilterModifier extends BeanSerializerModifier {
		@Override
		public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
				BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
			Iterator<BeanPropertyWriter> it = beanProperties.iterator();
			while (it.hasNext()) {
				BeanPropertyWriter writer = it.next();
				Annotation ann = getFieldAnnotation(beanDesc.getBeanClass(), writer.getName());
				if (ann != null && !getSerialize(ann)) {
					it.remove();
				}
			}
			return beanProperties;
		}
	}

	private static class DeserializeFilterModifier extends BeanDeserializerModifier {
		@Override
		public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
				BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
			List<BeanPropertyDefinition> result = new ArrayList<BeanPropertyDefinition>(propDefs.size());
			for (BeanPropertyDefinition prop : propDefs) {
				Annotation ann = getFieldAnnotation(beanDesc.getBeanClass(), prop.getInternalName());
				if (ann != null && !getDeserialize(ann)) {
					continue;
				}
				result.add(prop);
			}
			return result;
		}
	}

	private static Annotation getFieldAnnotation(Class<?> clazz, String fieldName) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			try {
				Field field = current.getDeclaredField(fieldName);
				return field.getAnnotation(JSON_FIELD_CLASS);
			} catch (NoSuchFieldException e) {
				current = current.getSuperclass();
			}
		}
		return null;
	}

	private static boolean getSerialize(Annotation ann) {
		try {
			return (Boolean) M_SERIALIZE.invoke(ann);
		} catch (Exception e) {
			return true;
		}
	}

	private static boolean getDeserialize(Annotation ann) {
		try {
			return (Boolean) M_DESERIALIZE.invoke(ann);
		} catch (Exception e) {
			return true;
		}
	}
}
