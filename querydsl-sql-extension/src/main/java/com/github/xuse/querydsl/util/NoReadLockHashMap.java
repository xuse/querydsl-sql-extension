package com.github.xuse.querydsl.util;

import java.lang.reflect.Field;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * 线程安全的HashMap，读取无锁，但失去扩容能力。
 * 
 */
@Slf4j
public final class NoReadLockHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = -2739427832065080348L;
	private final int actualThreshold;
	static Field threshold;
	static {
		try {
			Field threshold = HashMap.class.getDeclaredField("threshold");
			threshold.setAccessible(true);
			threshold.getInt(new HashMap<String, String>());
			NoReadLockHashMap.threshold = threshold;
		} catch (Exception ex) {
			log.error("Field java.util.HashMap.threshold is not accessiable.");
		}
	}

	public NoReadLockHashMap() {
		this(12);
	}

	public NoReadLockHashMap(int capacity) {
		super(capacity * 4 / 3);
		if (threshold != null) {
			try {
				this.actualThreshold = (int) (threshold.getInt(this) * 0.75f);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			this.actualThreshold = (int) (capacity * 0.75f);
		}
	}

	@Override
	public synchronized V put(K key, V value) {
		if (size() >= actualThreshold) {
			throw new IllegalArgumentException("Too many elements. maximum=" + getThreshold());
		}
		return super.put(key, value);
	}

	@Override
	public synchronized V putIfAbsent(K key, V value) {
		if (size() >= actualThreshold) {
			throw new IllegalArgumentException("Too many elements. maximum=" + this.getThreshold());
		}
		return super.putIfAbsent(key, value);
	}

	public int getThreshold() {
		return actualThreshold;
	}
}
