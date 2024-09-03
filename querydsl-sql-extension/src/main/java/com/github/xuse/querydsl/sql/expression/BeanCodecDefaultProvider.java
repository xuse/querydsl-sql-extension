package com.github.xuse.querydsl.sql.expression;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.sql.expression.BeanCodecManager.CacheKey;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.JDKEnvironment;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.TypeUtils;
import com.querydsl.core.util.ReflectionUtils;
import com.querydsl.sql.Column;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanCodecDefaultProvider implements BeanCodecProvider {
	public static final BeanCodecDefaultProvider INSTANCE = new BeanCodecDefaultProvider();

	@Override
	public BeanCodec generateAccessor(CacheKey key, BindingProvider bindings, ClassLoaderAccessor cl) {
		String clzName = key.getClassName();
		List<FieldProperty> properties = initMethods(key, bindings);
		// For Android and graalvm
		if (JDKEnvironment.ANDROID || JDKEnvironment.GRAAL_NATIVE || JDKEnvironment.DISABLE_ASM) {
			return inputFields(new ReflectCodec(key.targetClass, properties),key.targetClass, properties);
		}
		// Normal JDK
		CodecClassGenerator g = new CodecClassGenerator(cl);
		Class<?> clz = g.generate(key.targetClass, properties, clzName,false);
		BeanCodec bc;
		if (clz == null) {
			// 无法生成类
			try {
				clz = cl.loadClass(clzName);
			} catch (ClassNotFoundException e) {
				log.error("无法使用ASM加速类{}，退化为反射", key, e);
			}
		}
		if (clz == null) {
			bc = new ReflectCodec(key.targetClass, properties);
		} else {
			bc = (BeanCodec) TypeUtils.newInstance(clz);
		}
		return inputFields(bc,key.targetClass,properties);
	}


	protected static void propertyNotFound(String property) {
		// do nothing
	}

	/**
	 * @since 20240524,支持按@Column注解的字段名来返回结果
	 * @param key      key
	 * @param bindings bindings
	 * @return List&lt;FieldProperty&gt;
	 */
	private static List<FieldProperty> initMethods(CacheKey key, BindingProvider bindings) {
		Class<?> targetClz = key.targetClass;
		int count = 0;
		int len = bindings.size();
		try {
			targetClz.getDeclaredConstructor();
		} catch (NoSuchMethodException | SecurityException e1) {
			throw Exceptions.illegalArgument("Class {} must has a constructor with empty parameter.",
					targetClz.getName());
		}
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(targetClz);
			PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
			Map<String, FieldProperty> maps = new HashMap<>();
			for (PropertyDescriptor p : props) {
				if (p.getReadMethod().getDeclaringClass() == Object.class) {
					continue;
				}
				String name = p.getName();
				Method setter = p.getWriteMethod();
				Field field = ReflectionUtils.getFieldOrNull(targetClz, name);
				Column c;
				if (field != null && (c = field.getAnnotation(Column.class)) != null) {
					String alias = c.value();
					if (StringUtils.isNotEmpty(alias)) {
						name = alias;
					}
				} else if (setter != null && (c = setter.getAnnotation(Column.class)) != null) {
					String alias = c.value();
					if (StringUtils.isNotEmpty(alias)) {
						name = alias;
					}
				}
				maps.put(name, new FieldProperty(p.getReadMethod(), p.getWriteMethod(), field));
			}
			
			List<FieldProperty> properties = new ArrayList<>(len);
			for (String property : bindings.names(maps.keySet())) {
				FieldProperty prop = maps.get(property);
				if (prop != null) {
					count++;
				} else {
					propertyNotFound(property);
					prop = new FieldProperty(null, null, null);
				}
				prop.setBindingType(bindings.getType(property,prop));
				properties.add(prop);
			}
			if (count == 0) {
				throw new IllegalArgumentException("There is no property match between the bean ["
						+ key.targetClass.getName() + "] and select expression :" + key.fieldNames);
			}
			return properties;
		} catch (IntrospectionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
