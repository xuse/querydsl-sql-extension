package com.github.xuse.querydsl.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 用List实现的最简单的Map，目标是占用内存最小，不考虑性能，事实上元素不多的情况下性能不是什么问题。
 * @param <K>
 * @param <V>
 */
public final class ArrayListMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Serializable {
	private static final long serialVersionUID = -4930667933312037159L;

	//@XmlElement(nillable = false, name = "entry")
	private List<com.github.xuse.querydsl.util.Entry<K, V>> entries;

	public List<com.github.xuse.querydsl.util.Entry<K, V>> getEntries() {
		return entries;
	}
	

	/**
	 * 包装指定的列表为Map
	 * @param <K>
	 * @param <V>
	 * @param entries
	 * @return
	 */
	public static <K, V> ArrayListMap<K, V> wrap(List<com.github.xuse.querydsl.util.Entry<K, V>> entries) {
		return new ArrayListMap<>(entries);
	}

	/**
	 * 包装指定的列表为Map
	 * @param <K>
	 * @param <V>
	 * @param entries
	 * @return
	 */
	public static <K, V> ArrayListMap<K, V> immutableMap(com.github.xuse.querydsl.util.Entry<K, V>[] entries) {
		return new ArrayListMap<>(Arrays.asList(entries));
	}

	private ArrayListMap(List<com.github.xuse.querydsl.util.Entry<K, V>> data) {
		this.entries = data;
	}

	public ArrayListMap() {
		this(16);
	}

	public ArrayListMap(int size) {
		this.entries = new ArrayList<com.github.xuse.querydsl.util.Entry<K, V>>(size);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayListMap(Map<K, V> map) {
		if (map instanceof ArrayListMap) {
			entries = ((ArrayListMap) map).entries;
		} else {
			entries = new ArrayList<com.github.xuse.querydsl.util.Entry<K, V>>(map.size());
			for (Entry<K, V> e : map.entrySet()) {
				entries.add(new com.github.xuse.querydsl.util.Entry<K, V>(e.getKey(), e.getValue()));
			}
		}
	}

	/**
	 * 在Map中添加元素，不检查重复与否
	 * 
	 * @param key
	 * @param value
	 */
	public void add(K key, V value) {
		entries.add(new com.github.xuse.querydsl.util.Entry<K, V>(key, value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.xuse.querydsl.util.AbstractMap#put(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public V put(K key, V value) {
		int index = -1;
		for (int i = 0; i < entries.size(); i++) {
			if (Objects.equals(entries.get(i).getKey(), key)) {
				index = i;
				break;
			}
		}
		if (index > -1) {
			entries.set(index, new com.github.xuse.querydsl.util.Entry<K, V>(key, value));
		} else {
			entries.add(new com.github.xuse.querydsl.util.Entry<K, V>(key, value));
		}
		return value;
	}

	private class EntriesIterator implements Iterator<java.util.Map.Entry<K, V>> {
		private int n = 0;

		public boolean hasNext() {
			return n < entries.size();
		}

		public java.util.Map.Entry<K, V> next() {
			java.util.Map.Entry<K, V> result = entries.get(n);
			n++;
			return result;
		}

		public void remove() {
			n--;
			entries.remove(n);
		}
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new AbstractSet<java.util.Map.Entry<K, V>>() {
			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new EntriesIterator();
			}

			@Override
			public int size() {
				return entries.size();
			}
		};
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public void clear() {
		entries.clear();
	}
}
