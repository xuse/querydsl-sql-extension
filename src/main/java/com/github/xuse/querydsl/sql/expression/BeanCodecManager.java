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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.Radix;
import com.github.xuse.querydsl.util.TypeUtils;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Expression;
import com.querydsl.core.util.ReflectionUtils;
import com.querydsl.sql.Column;

public class BeanCodecManager {

	private static BeanCodecManager INSTANCE = new BeanCodecManager();

	private static final Logger log = LoggerFactory.getLogger(BeanCodecManager.class);

	private final Map<CacheKey, BeanCodec> populators = new ConcurrentHashMap<CacheKey, BeanCodec>();

	private final ClassLoaderAccessor cl;

	private BeanCodecManager() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null)
			cl = BeanCodecManager.class.getClassLoader();
		this.cl = new ClassLoaderAccessor(cl);
	}

	private static class CacheKey {
		private Class<?> targetClass;
		private List<String> fieldNames;
		private int hash;

		static CacheKey of(Class<?> target, List<String> fieldNames) {
			Assert.notNull(target, "targetClass");
			Assert.notNull(fieldNames, "fieldName");
			CacheKey ck = new CacheKey();
			ck.targetClass = target;
			ck.fieldNames = fieldNames;
			ck.hash = fieldNames.hashCode();
			return ck;
		}

		@Override
		public String toString() {
			return targetClass + ", fieldNames=" + fieldNames;
		}

		@Override
		public int hashCode() {
			return targetClass.hashCode() * 37 + hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CacheKey) {
				CacheKey rhs = (CacheKey) obj;
				return this.targetClass.equals(rhs.targetClass) && this.fieldNames.equals(rhs.fieldNames);
			}
			return false;
		}

		public String getClassName() {
			return targetClass.getName() + "_" + Radix.D64.encodeInt(hash);
		}
	}

	public BeanCodec getPopulator(Class<?> target, BindingProvider bindings) {
		List<String> fieldNames = bindings.fieldNames();
		CacheKey key = CacheKey.of(target, fieldNames);
		BeanCodec result = populators.get(key);
		if (result != null) {
			return result;
		}
		synchronized (this) {
			result = populators.get(key);
			//使用双重检查锁定来提高并发安全性
			if(result==null) {
				try {
					result = generateAccessor(key, bindings);
					populators.putIfAbsent(key, result);
				} catch (RuntimeException e) {
					result = populators.get(key);
					if (result != null) {
						log.error("", e);
						return result;
					}
					throw e;
				} catch (InstantiationException e) {
					throw Exceptions.illegalState(e);
				} catch (IllegalAccessException e) {
					throw Exceptions.illegalState(e);
				}	
			}
			return result;
		}
	}

	protected static void propertyNotFound(String property) {
		// do nothing
	}

	private BeanCodec generateAccessor(CacheKey key, BindingProvider bindings) throws InstantiationException, IllegalAccessException {
		String clzName = key.getClassName();
		Class<?> clz;
		try {
			clz = cl.loadClass(clzName);
			return (BeanCodec) TypeUtils.newInstance(clz);
		} catch (ClassNotFoundException e) {
			// do nothing..
		}
		List<FieldProperty> properties = initMethods(key, bindings);
		CodecClassGenerator g = new CodecClassGenerator(cl);
		clz = g.generate(key.targetClass, properties, clzName);
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
		}else{
			bc = (BeanCodec) TypeUtils.newInstance(clz);
		}
		Field[] fields = new Field[properties.size()];
		for (int i = 0; i < properties.size(); i++) {
			fields[i] = properties.get(i).getField();
		}
		bc.setFields(fields);
		return bc;
	}
	
	/**
	 * @since 20240524,支持按@Column注解的字段名来返回结果
	 * @param key
	 * @param bindings
	 * @return
	 */
	private static List<FieldProperty> initMethods(CacheKey key,BindingProvider bindings) {
		Class<?> targetClz = key.targetClass;
		int count = 0;
		int len = bindings.size();
		try {
			targetClz.getDeclaredConstructor();
		} catch (NoSuchMethodException|SecurityException e1) {
			throw Exceptions.illegalArgument("Class {} must has a constructor with empry parameter.",targetClz.getName());
		}
		try {
			List<FieldProperty> propereties = new ArrayList<>(len);
			
			BeanInfo beanInfo = Introspector.getBeanInfo(targetClz);
			PropertyDescriptor[] props=beanInfo.getPropertyDescriptors();
			
			Map<String, FieldProperty> maps = new HashMap<>();
			for(PropertyDescriptor p:props) {
				if (p.getReadMethod().getDeclaringClass() == Object.class) {
					continue;
				}
				String name=p.getName();
				Method setter=p.getWriteMethod();
				Field field = ReflectionUtils.getFieldOrNull(targetClz, name);
				Column c;
				if(field!=null && (c=field.getAnnotation(Column.class))!=null) {
					String alias=c.value();
					if(StringUtils.isNotEmpty(alias)) {
						name=alias;
					}
				}else if(setter!=null && (c=setter.getAnnotation(Column.class))!=null) {
					String alias=c.value();
					if(StringUtils.isNotEmpty(alias)) {
						name=alias;
					}
				}
				maps.put(name, new FieldProperty(p.getReadMethod(), p.getWriteMethod(), field));
			}
			
			for (String property : bindings.names(maps)) {
				FieldProperty prop = maps.get(property);
				if(prop!=null) {
					count++;
				}else {
					propertyNotFound(property);
					continue;
				}
				Expression<?> expression=bindings.get(property);
				prop.setBindingType(expression.getType());
				propereties.add(prop);
			}
			if (count == 0) {
				throw new IllegalArgumentException(
						"There is no property match between the bean [" + key.targetClass.getName() + "] and select expression :" + key.fieldNames);
			}
			return propereties;
		} catch (IntrospectionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static BeanCodecManager getInstance() {
		return INSTANCE;
	}
}
