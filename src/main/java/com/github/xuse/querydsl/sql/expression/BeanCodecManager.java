package com.github.xuse.querydsl.sql.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.util.Radix;
import com.github.xuse.querydsl.util.TypeUtils;
import com.mysema.commons.lang.Assert;

public class BeanCodecManager {
	private static final Logger log = LoggerFactory.getLogger(BeanCodecManager.class);

	private static final BeanCodecManager INSTANCE = new BeanCodecManager();
	
	private final Set<Class<?>> normalClassess=new HashSet<>();

	private final Map<CacheKey, BeanCodec> beanCodecs = new ConcurrentHashMap<CacheKey, BeanCodec>();

	private final ClassLoaderAccessor cl;

	private BeanCodecManager() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null)
			cl = BeanCodecManager.class.getClassLoader();
		this.cl = new ClassLoaderAccessor(cl);
	}

	static class CacheKey {

		final Class<?> targetClass;

		final List<String> fieldNames;

		private final int hash;

		static CacheKey of(Class<?> target, List<String> fieldNames) {
			Assert.notNull(target, "targetClass");
			Assert.notNull(fieldNames, "fieldName");
			return new CacheKey(target, fieldNames);
		}
		
		private CacheKey(Class<?> targetClass, List<String> fieldNames) {
			this.targetClass = targetClass;
			this.fieldNames = fieldNames;
			this.hash = fieldNames.hashCode();
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
		BeanCodec result = beanCodecs.get(key);
		if (result != null) {
			return result;
		}
		synchronized (this) {
			result = beanCodecs.get(key);
			if (result == null) {
				try {
					result = generateAccessor(key, bindings);
					beanCodecs.putIfAbsent(key, result);
				} catch (RuntimeException e) {
					result = beanCodecs.get(key);
					if (result != null) {
						log.error("", e);
						return result;
					}
					throw e;
				}
			}
			return result;
		}
	}

	private BeanCodec generateAccessor(CacheKey key, BindingProvider bindings){
		BeanCodecProvider provider;
		if (normalClassess.contains(key.targetClass)) {
			provider = BeanCodecDefaultProvider.INSTANCE;
		} else {
			boolean isRecord = TypeUtils.isRecord(key.targetClass);
			if (isRecord) {
				provider = BeanCodecRecordProvider.INSTANCE;
			} else {
				normalClassess.add(key.targetClass);
				provider = BeanCodecDefaultProvider.INSTANCE;
			}
		}
		return provider.generateAccessor(key, bindings, cl);
	}
	


	public static BeanCodecManager getInstance() {
		return INSTANCE;
	}
}
