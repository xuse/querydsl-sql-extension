package com.github.xuse.querydsl.sql.expression;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.util.ReflectionUtils;

public class BeanCodecManager {

	private static BeanCodecManager INSTANCE = new BeanCodecManager();

	private static final Logger log = LoggerFactory.getLogger(BeanCodecManager.class);

	private final Map<CacheKey, BeanCodec> populators = new ConcurrentHashMap<CacheKey, BeanCodec>();

	private final ClassLoaderAccessor cl;

	public BeanCodecManager() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null)
			cl = BeanCodecManager.class.getClassLoader();
		this.cl = new ClassLoaderAccessor(cl);
	}

	private static class CacheKey {
		private Class<?> targetClass;
		private List<String> fieldNames;

		static CacheKey of(Class<?> target, List<String> fieldNames) {
			Assert.notNull(target, "targetClass");
			Assert.notNull(fieldNames, "fieldName");
			CacheKey ck = new CacheKey();
			ck.targetClass = target;
			ck.fieldNames = fieldNames;
			return ck;
		}

		@Override
		public String toString() {
			return "CacheKey [targetClass=" + targetClass + ", fieldNames=" + fieldNames + "]";
		}

		@Override
		public int hashCode() {
			return targetClass.hashCode() * 37 + fieldNames.hashCode();
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
			return targetClass.getName() + "_" + fieldNames.hashCode();
		}
	}

	public BeanCodec getPopulator(Class<?> target, List<String> fieldNames) {
		CacheKey key = CacheKey.of(target, fieldNames);
		BeanCodec result = populators.get(key);
		if (result == null) {
			try {
				result = generateAccessor(key);
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

	protected static void propertyNotFound(String property) {
		// do nothing
	}

	private synchronized BeanCodec generateAccessor(CacheKey key)
			throws InstantiationException, IllegalAccessException {
		String clzName = key.getClassName();
		Class<?> clz;
		try {
			clz = cl.loadClass(clzName);
			return (BeanCodec) clz.newInstance();
		} catch (ClassNotFoundException e) {
			// do nothing..
		}
		List<FieldProperty> methods = initMethods(key);
		CodecClassGenerator g = new CodecClassGenerator(cl);
		clz = g.generate(key.targetClass, methods, clzName);
		BeanCodec bc= (BeanCodec) clz.newInstance();
		Field[] fields=new Field[methods.size()];
		for(int i=0;i<methods.size();i++) {
			fields[i] = methods.get(i).getField();
		}
		bc.setFields(fields);
		 return bc;
	}

	private static List<FieldProperty> initMethods(CacheKey key) {
		List<String> args = key.fieldNames;
		Class<?> targetClz = key.targetClass;
		int count = 0;
		int len = args.size();
		try {
			List<FieldProperty> propereties = new ArrayList<>(len);
			BeanInfo beanInfo = Introspector.getBeanInfo(targetClz);
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (String property : args) {
				Method setter = null, getter = null;
				Field field = null;
				for (PropertyDescriptor prop : propertyDescriptors) {
					if (prop.getName().equals(property)) {
						setter = prop.getWriteMethod();
						getter = prop.getReadMethod();
						field = ReflectionUtils.getFieldOrNull(targetClz, property);
						break;
					}
				}
				if (setter == null || getter == null) {
					propertyNotFound(property);
				}else {
					count++;
				}
				propereties.add(new FieldProperty(getter, setter, field));
			}
			if (count == 0) {
				throw new IllegalArgumentException("There is no property match between the bean ["
						+ key.targetClass.getName() + "] and select expression :" + key.fieldNames);
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
