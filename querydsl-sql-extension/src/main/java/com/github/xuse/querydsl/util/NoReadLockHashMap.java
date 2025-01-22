package com.github.xuse.querydsl.util;

import java.util.HashMap;

/**
 * A thread-safe HashMap with lock-free reads, but loses the ability to resize.
 * 
 */
public final class NoReadLockHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = -2739427832065080348L;
	private final int actualThreshold;

    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
    
	public NoReadLockHashMap() {
		this(12);
	}

	public NoReadLockHashMap(int capacity) {
		super(tableSizeFor(capacity * 4 / 3));
		actualThreshold = (int) (tableSizeFor(capacity * 4 / 3) * 0.75f);
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
