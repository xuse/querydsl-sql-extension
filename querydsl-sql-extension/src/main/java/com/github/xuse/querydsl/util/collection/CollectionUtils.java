/*
 * querydsl-sql-extension - Copyright 2017-2024 Joey (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.util.collection;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.xuse.querydsl.util.Assert;

/**
 * 集合操作工具类 v2.0
 * @author Joey
 */
public class CollectionUtils {
	@SuppressWarnings("rawtypes")
	private static final List ALL_NULL_LIST = new NullsList<>();
	
	
	private static final class NullsList<E> extends AbstractList<E>{
		@Override
		public E get(int index) {
			return null;
		}
		@Override
		public int size() {
			return 0;
		}
	};
	
	/**
	 * 返回一个size()为零。但可以获取任意标号元素的List。其每个元素都是null.
	 * @param <T> type
	 * @return nullElementsList
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> nullElementsList(){
		return ALL_NULL_LIST;
	}
	
	protected CollectionUtils() {
	}

	/**
	 * 集合转换为Map
	 * @param c collection
	 * @param keyExt key extractor
	 * @param valueExt value extractor
	 * @return array
	 * @param <E> The type of target object.
	 * @param <K> The type of target object.
	 * @param <V> The type of target object.
	 */
	public static <E, K, V> Map<K, List<V>> bucket(Collection<E> c, Function<E, K> keyExt, Function<E, V> valueExt) {
		Map<K, List<V>> buckets = new HashMap<>();
		if (c != null) {
			for (Iterator<E> it = c.iterator(); it.hasNext(); ) {
				E value = it.next();
				K key = keyExt.apply(value);
				List<V> bucket = buckets.get(key);
				if (bucket == null) {
					buckets.put(key, bucket = new ArrayList<>());
				}
				bucket.add(valueExt.apply(value));
			}
		}
		return buckets;
	}

	public static <K, V> Map<K, List<V>> bucket(Collection<V> c, Function<V, K> keyExt) {
		if (c != null) {
			Map<K, List<V>> buckets = new HashMap<>(c.size());
			for (V value:c) {
				K key = keyExt.apply(value);
				List<V> bucket = buckets.get(key);
				if (bucket == null) {
					buckets.put(key, bucket = new ArrayList<>());
				}
				bucket.add(value);
			}
			return buckets;
		}
		return Collections.emptyMap();
	}

	/**
	 * 将Collection转换为Map。（Map不保证顺序）
	 * @param collection   集合
	 * @param keyExtractor 键值提取函数
	 * @return 在每个元素中提取键值后，形成Map。相同键值的记录将发生叠加（仅保留最后的一个）
	 * @param <K> The type of target object.
	 * @param <V> The type of target object.
	 */
	public static <K, V> Map<K, V> group(Collection<V> collection, Function<V, K> keyExtractor) {
		if (collection == null || collection.isEmpty())
			return Collections.emptyMap();
		Map<K, V> result = new HashMap<K, V>(collection.size());
		for (V value : collection) {
			K key = keyExtractor.apply(value);
			result.put(key, value);
		}
		return result;
	}
	
	/**
	 * 将数组转换为Map。（Map不保证顺序）
	 * @param array        数组
	 * @param keyExtractor 键值提取函数
	 * @return 在每个元素中提取键值后，形成Map，如果多个对象返回相同的key，那么会互相覆盖。如果不希望互相覆盖，请使用
	 *         {@linkplain #bucket(Collection, Function, Function)}
	 * @param <K> The type of target object.
	 * @param <V> The type of target object.
	 */
	public static <K, V> Map<K, V> group(V[] array, Function<V, K> keyExtractor) {
		if (array == null || array.length == 0)
			return Collections.emptyMap();
		Map<K, V> result = new HashMap<K, V>(array.length);
		for (V value : array) {
			K key = keyExtractor.apply(value);
			result.put(key, value);
		}
		return result;
	}

	/**
	 * nullsafe的集合
	 * @param value list
	 * @return not null list
	 * @param <E> The type of target object.
	 */
	public static <E> List<E> nullSafe(List<E> value) {
		return value == null ? Collections.emptyList() : value;
	}

	/**
	 * nullsafe的集合
	 * @param value set
	 * @return not null set
	 * @param <E> The type of target object.
	 */
	public static <E> Set<E> nullSafe(Set<E> value) {
		return value == null ? Collections.emptySet() : value;
	}

	/**
	 * nullsafe的集合
	 * @param value  map
	 * @return not null map
	 * @param <K> The type of target object.
	 * @param <V> The type of target object.
	 */
	public static <K, V> Map<K, V> nullSafe(Map<K, V> value) {
		return value == null ? Collections.emptyMap() : value;
	}

	/**
	 * List to map
	 * @param list list
	 * @return Map&lt;,Integer&gt;
	 * @param <V> The type of target object.
	 */
	public static <V> Map<V, Integer> getIndexMap(List<V> list) {
		if (isEmpty(list)) {
			return Collections.emptyMap();
		}
		Map<V, Integer> indexes = new HashMap<>();
		int index = 0;
		for (Iterator<V> it = list.iterator(); it.hasNext(); ) {
			indexes.put(it.next(), Integer.valueOf(index++));
		}
		return indexes;
	}

	/**
	 * 将一个数组的每个元素进行函数处理后重新组成一个集合
	 * @param array      数组
	 * @param extractor  提取函数
	 * @return 提取后形成的列表
	 * @param <F> The type of target object.
	 * @param <T> The type of target object.
	 */
	public static <F, T> List<T> extract(F[] array, Function<F, T> extractor) {
		return extract(Arrays.asList(array), extractor, false);
	}

	/**
	 * 将一个集合对象的每个元素进行函数处理后重新组成一个集合
	 * @param collection 集合对象
	 * @param extractor  提取函数
	 * @return 提取后形成的列表
	 * @param <F> The type of target object.
	 * @param <T> The type of target object.
	 */
	public static <F, T> List<T> extract(Collection<F> collection, Function<F, T> extractor) {
		return extract(collection, extractor, false);
	}

	/**
	 * 将一个集合对象的每个元素进行函数处理后重新组成一个集合
	 * @param collection 集合对象
	 * @param extractor  提取函数
	 * @param ignoreNull 如果为true，那么提出后的null值会被忽略
	 * @return 提取后形成的列表
	 * @param <F> The type of target object.
	 * @param <T> The type of target object.
	 */
	public static <F, T> List<T> extract(Collection<F> collection, Function<F, T> extractor, boolean ignoreNull) {
		List<T> result = new ArrayList<T>(collection.size());
		if (collection != null) {
			for (F a : collection) {
				T t = extractor.apply(a);
				if (ignoreNull && t == null) {
					continue;
				}
				result.add(t);
			}
		}
		return result;
	}

	public static <T> boolean removeFirst(Collection<T> collection, Predicate<T> filter) {
		if (collection == null || collection.isEmpty())
			return false;
		for(Iterator<T> iter=collection.iterator();iter.hasNext();) {
			if (filter.test(iter.next())) {
				iter.remove();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 在集合中查找符合条件的首个元素
	 * @param collection 集合
	 * @param filter     过滤器
	 * @return find result.
	 * @param <T> The type of target object.
	 */
	public static <T> T findFirst(Collection<T> collection, Predicate<T> filter) {
		if (collection == null || collection.isEmpty())
			return null;
		for (T obj : collection) {
			if (filter.test(obj)) {
				return obj;
			}
		}
		return null;
	}

	/**
	 * 根据字段名称和字段值查找第一个记录
	 * @param collection 集合
	 * @param f f
	 * @param value      查找值
	 * @return 查找到的元素
	 * @param <T> The type of target object.
	 * @param <V> The type of target object.
	 */
	public static <T, V> T findFirst(Collection<T> collection, Function<T, V> f, V value) {
		if (collection == null || collection.isEmpty())
			return null;
		return findFirst(collection, t -> Objects.equals(f.apply(t), value));
	}

	/**
	 * 根据字段名称和字段值查找所有记录
	 * @param            <T> 泛型
	 * @param collection 集合
	 * @param f function
	 * @param value      值
	 * @return 过滤后的集合
	 * @param <V> The type of target object.
	 */
	public static <T, V> List<T> find(Collection<T> collection, Function<T, V> f, V value) {
		if (collection == null || collection.isEmpty())
			return Collections.emptyList();
		return filter(collection, t -> Objects.equals(f.apply(t), value));
	}

	/**
	 *  在集合中查找符合条件的元素
	 *
	 *  @param            <T> 泛型
	 *  @param collection 集合
	 *  @param filter     过滤器
	 *  @return result
	 */
	public static <T> List<T> filter(Collection<T> collection, Predicate<T> filter) {
		if (collection == null || collection.isEmpty())
			return Collections.emptyList();
		List<T> list = new ArrayList<T>(collection.size());
		for (T obj : collection) {
			if (filter.test(obj)) {
				list.add(obj);
			}
		}
		return list;
	}

	/**
	 * 对Map进行过滤，获得一个新的Map. 如果传入的是有序Map，新Map会保留原来的顺序。
	 * @param map    要处理的Map
	 * @param filter 过滤器
	 * @return 过滤后的新Map
	 * @param <K> The type of target object.
	 * @param <V> The type of target object.
	 */
	public static <K, V> Map<K, V> filter(Map<K, V> map, BiPredicate<K, V> filter) {
		if (map == null) {
			return Collections.emptyMap();
		}
		Map<K, V> result;
		if (map instanceof SortedMap) {
			result = new TreeMap<K, V>(((SortedMap<K, V>) map).comparator());
		} else {
			result = new HashMap<K, V>(map.size());
		}
		for (Map.Entry<K, V> e : map.entrySet()) {
			boolean applied = filter.test(e.getKey(),e.getValue());
			if (applied) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}

	/**
	 *  从集合中去除不需要的元素（精炼，提炼） 注意，如果传入的Collection类型不支持Iterator.remove()方式移除元素，将抛出异常。(
	 *  一般是UnsupportedOperationException)
	 *
	 *  @param            <T> 泛型
	 *  @param collection 集合
	 *  @param filter filter
	 */
	public static <T> void refine(Collection<T> collection, Predicate<T> filter) {
		if (collection != null) {
			for (Iterator<T> iter = collection.iterator(); iter.hasNext(); ) {
				T e = iter.next();
				if (!filter.test(e)) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * 从集合中去除不需要的元素（精炼，提炼），返回true时元素被保留。反之被删除。<br>
	 * 注意，如果传入的Map类型不支持Iterator.remove()方式移除元素，将抛出异常。(
	 * 一般是UnsupportedOperationException)
	 * @param map    要处理的Map
	 * @param filter Function，用于指定哪些元素要保留
	 * @throws UnsupportedOperationException If encounter UnsupportedOperationException
	 * @param <K> The type of target object.
	 * @param <V> The type of target object.
	 */
	public static <K, V> void refine(Map<K, V> map, BiPredicate<K, V> filter) {
		if (map != null) {
			for (Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<K, V> e = iter.next();
				if (!filter.test(e.getKey(),e.getValue())) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * 将传入的对象转换为可遍历的对象。
	 * Enumeration直到Java9才提供.asIterator()方法。在Java8下可用此代替。
	 * @param data data
	 * @return Iterator
	 * @param <E> The type of target object.
	 * 
	 * 
	 * 
	 */
	public static <E> Iterator<E> iterator(Enumeration<E> data) {
		Assert.notNull(data);
		return new EnumerationIterator<E>(data);
	}

	/**
	 * 将Enumeration转换为一个新的List
	 * @param data data
	 * @return 转换后的List
	 * @param <E> The type of target object.
	 */
	public static <E> List<E> toList(Enumeration<E> data) {
		if (data == null) {
			return Collections.emptyList();
		}
		List<E> result = new ArrayList<E>();
		for (; data.hasMoreElements(); ) {
			result.add(data.nextElement());
		}
		return result;
	}

	/**
	 * 将Iterable转换为List
	 * @param data data
	 * @return 转换后的List
	 * @param <E> The type of target object.
	 */
	public static <E> List<E> toList(Iterable<E> data) {
		if (data == null) {
			return Collections.emptyList();
		}
		List<E> result = new ArrayList<E>();
		for (Iterator<E> iter = data.iterator(); iter.hasNext(); ) {
			result.add(iter.next());
		}
		return result;
	}

	/**
	 * 得到数组或集合类型的长度
	 * @param obj obj
	 * @return length
	 * @throws IllegalArgumentException 如果传入参数不是一个数组或者一个集合抛出
	 */
	@SuppressWarnings("rawtypes")
	public static int length(Object obj) {
		if (obj.getClass().isArray()) {
			return Array.getLength(obj);
		}
		Assert.isTrue(obj instanceof Collection);
		return ((Collection) obj).size();
	}

	/**
	 * 两个集合对象的合并，并去除重复对象
	 * @param <T> type of collection
	 * @param a 集合A
	 * @param b 集合B
	 * @return Collection
	 */
	public static <T> Collection<T> union(Collection<T> a, Collection<T> b) {
		if (a == null && b == null) {
			return Collections.emptySet();
		}
		if (a == null)
			a = Collections.emptyList();
		if (b == null)
			b = Collections.emptyList();
		HashSet<T> s = new HashSet<T>(a.size() + b.size());
		s.addAll(a);
		s.addAll(b);
		return s;
	}

	/**
	 *  Return <code>true</code> if the supplied Collection is <code>null</code> or
	 *  empty. Otherwise, return <code>false</code>.
	 *
	 *  @param collection the Collection to check
	 *  @return whether the given Collection is empty
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return (collection == null || collection.isEmpty());
	}

	/**
	 *  Return <code>true</code> if the supplied Collection is NOT<code>null</code>
	 *  or empty. Otherwise, return <code>false</code>.
	 *
	 *  @param collection the Collection to check
	 *  @return whether the given Collection is empty
	 */
	public static boolean isNotEmpty(Collection<?> collection) {
		return !(collection == null || collection.isEmpty());
	}

	/**
	 *  Return <code>true</code> if the supplied Map is <code>null</code> or empty.
	 *  Otherwise, return <code>false</code>.
	 *
	 *  @param map the Map to check
	 *  @return whether the given Map is empty
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}

	/**
	 *  Return <code>true</code> if the supplied Map is NOT <code>null</code> or
	 *  empty. Otherwise, return <code>false</code>.
	 *
	 *  @param map the Map to check
	 *  @return whether the given Map is empty
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !(map == null || map.isEmpty());
	}

	/**
	 *  Check whether the given Iterator contains the given element.
	 *
	 *  @param iterable the Iterator to check
	 *  @param element  the element to look for
	 *  @return <code>true</code> if found, <code>false</code> else
	 */
	public static boolean contains(Iterable<?> iterable, Object element) {
		if (iterable != null) {
			Iterator<?> iterator = iterable.iterator();
			while (iterator.hasNext()) {
				Object candidate = iterator.next();
				if (Objects.equals(candidate, element)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *  Check whether the given Enumeration contains the given element.
	 *
	 *  @param enumeration the Enumeration to check
	 *  @param element     the element to look for
	 *  @return <code>true</code> if found, <code>false</code> else
	 */
	public static boolean contains(Enumeration<?> enumeration, Object element) {
		if (enumeration != null) {
			while (enumeration.hasMoreElements()) {
				Object candidate = enumeration.nextElement();
				if (Objects.equals(candidate, element)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *  Check whether the given Collection contains the given element instance.
	 *  <p>
	 *  Enforces the given instance to be present, rather than returning
	 *  <code>true</code> for an equal element as well.
	 *
	 *  @param collection the Collection to check
	 *  @param element    the element to look for
	 *  @return <code>true</code> if found, <code>false</code> else
	 */
	public static boolean fastContains(Collection<?> collection, Object element) {
		if (collection != null) {
			for (Object candidate : collection) {
				if (candidate == element) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *  Return <code>true</code> if any element in '<code>candidates</code>' is
	 *  contained in '<code>source</code>'; otherwise returns <code>false</code>.
	 *
	 *  @param source     the source Collection
	 *  @param candidates the candidates to search for
	 *  @return whether any of the candidates has been found
	 */
	public static boolean containsAny(Collection<?> source, Collection<?> candidates) {
		if (isEmpty(candidates)) {
			return true;
		}
		if (isEmpty(source)) {
			return false;
		}
		for (Object candidate : candidates) {
			if (source.contains(candidate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *  Return <code>true</code> if all elements in '<code>candidates</code>' is
	 *  contained in '<code>source</code>'; otherwise returns <code>false</code>.
	 *
	 *  @param source     the source Collection
	 *  @param candidates the candidates to search for
	 *  @return whether any of the candidates has been found
	 */
	public static boolean containsAll(Collection<?> source, Collection<?> candidates) {
		if (isEmpty(candidates)) {
			return true;
		}
		if (isEmpty(source)) {
			return false;
		}
		for (Object candidate : candidates) {
			if (!source.contains(candidate)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create a new identityHashSet.
	 * @return set
	 * @param <E> The type of target object.
	 */
	public static <E> Set<E> identityHashSet() {
		return Collections.newSetFromMap(new IdentityHashMap<E, Boolean>());
	}

	/**
	 * 获得集合的最后一个元素
	 * @param collection collection
	 * @return last
	 * @param <T> The type of target object.
	 */
	public static <T> T last(List<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		return collection.get(collection.size() - 1);
	}
	
	/**
	 * 在List中的指定位置插入元素。如果超出当前长度，则将list扩展到指定长度。
	 * @param list  List
	 * @param index 序号
	 * @param value 值
	 * @param <T> The type of target object.
	 */
	public static <T> void setElement(List<T> list, int index, T value) {
		Assert.notNull(list);
		if (index == list.size()) {
			list.add(value);
		} else if (index > list.size()) {
			for (int i = list.size(); i < index; i++) {
				list.add(null);
			}
			list.add(value);
		} else {
			list.set(index, value);
		}
	}

	/**
	 * Iterator wrapping an Enumeration.
	 * @param <E> The type of target object.
	 */
	static class EnumerationIterator<E> implements Iterator<E> {
		private Enumeration<E> enumeration;

		public EnumerationIterator(Enumeration<E> enumeration) {
			this.enumeration = enumeration;
		}

		public boolean hasNext() {
			return this.enumeration.hasMoreElements();
		}

		public E next() {
			return this.enumeration.nextElement();
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Not supported");
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> cast(List<? extends T> source) {
		return (List<T>) source;
	}
}
