package com.github.xuse.querydsl.util;

import java.lang.reflect.Field;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NoReadLockHashMap<K, V> extends HashMap<K, V> {
	private final int actualThreshold;
	static Field threshold;
	static {
		try {
			threshold = HashMap.class.getDeclaredField("threshold");
			threshold.setAccessible(true);
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
			throw new IllegalArgumentException("Too many elements. maxium="+this.getThreshold());
		}
		return super.put(key, value);
	}

	public int getThreshold() {
		return actualThreshold;
	}
}
