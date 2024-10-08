package com.github.xuse.querydsl.util;

import java.lang.reflect.Field;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

import sun.misc.Unsafe;

/**
 * LinkedHashMap的自定义实现，目标是得到一个更高性能的Map。
 * @implSpec
 * 不支持删除元素 2 以写入顺序形成链表 3
 * 时间复杂度为O(1)的查找
 * 冲突后会形成一个小型的二级hash table.
 * 再冲突后使用链表存储.
 * @implNote 
 * 比LinkedHashMap快15~20%。
 * 不支持扩容。大幅超过预定容量后会逐渐退化为链表，性能急剧下降。
 * @param <V> value type
 */
@SuppressWarnings("restriction")
public final class FastHashtable<V> implements Map<String, V> {

	private final Object[] table;

	private int hashTableMask;

	/**
	 * The head (eldest) of the doubly linked list.
	 */
	private transient Node<V> head;

	/**
	 * The tail (youngest) of the doubly linked list.
	 */
	private transient Node<V> tail;

	private int size;

	private static final Unsafe U = unsafe();

	private static Unsafe unsafe() {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return (Unsafe) theUnsafe.get(Unsafe.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static final int GRID_MASK = 7;
	private static final int ABASE = U.arrayBaseOffset(Object[].class);
	private static final int ASHIFT;
	static {
		int scale = U.arrayIndexScale(Node[].class);
		if ((scale & (scale - 1)) != 0)
			throw new ExceptionInInitializerError("array index scale not a power of two");
		ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
	}

	static final Object tabAt(Object tab, int i) {
		return U.getObject(tab, ((long) i << ASHIFT) + ABASE);
	}
	static final void tabSet(Object tab, int i, Object obj) {
		U.putObject(tab, ((long) i << ASHIFT) + ABASE, obj);
	}

	@SuppressWarnings("rawtypes")
	static final void gridSet(Node[] tab, int i, Node node) {
		U.putObject(tab, ((long) i << ASHIFT) + ABASE, node);
	}

	public FastHashtable(int initSize) {
		initSize = tableSizeFor(initSize);
		this.table = new Object[initSize];
		this.hashTableMask = initSize - 1;
	}

	static final int tableSizeFor(int cap) {
		int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
		return (n < 0) ? 1 : (n >= 16384) ? 16384 : n + 1;
	}

	public V get(String key) {
		return innerGet(hash(key), key);
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public V computeIfAbsent(String key, Function<String, V> func) {
		int hash = hash(key);
		int slotPos = (hash >>> 3) & hashTableMask;
		Object obj = tabAt(table, slotPos);
		Node<V> acc;
		if (obj == null) {
			tabSet(table, slotPos, tail(new Node<V>(hash, key, func.apply(key))));
			return null;
		}
		Node[] grids = null;
		if (obj instanceof Node[]) {
			grids = (Node[]) obj;
			acc = (Node)tabAt(grids, hash & GRID_MASK);
		} else {
			// acc is not null.
			acc = (Node<V>) obj;
		}
		if (acc == null) {
			gridSet(grids, hash & GRID_MASK, tail(new Node<V>(hash, key, func.apply(key))));
			return null;
		}
		// 已有
		if (acc.hash == hash && acc.key.equals(key)) {
			return acc.value;
		}
		// 转换节点类型
		if (obj == acc) {
			tabSet(table, slotPos, grids = new Node[8]);
			gridSet(grids, acc.hash & GRID_MASK, acc);

			int index = hash & GRID_MASK;
			acc = (Node)tabAt(grids, index);
			if (acc == null) {
				gridSet(grids, index, tail(new Node<V>(hash, key, func.apply(key))));
				return null;
			}
		}
		// 冲突，还是需要用链表
		while (acc.conflictNext != null) {
			acc = acc.conflictNext;
			if (acc.hash == hash && acc.key.equals(key)) {
				return acc.value;
			}
		}
		// 挂在旧节点下
		Node<V> newNode = tail(new Node<V>(hash, key, func.apply(key)));
		acc.conflictNext = newNode;
		return newNode.value;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public V put(String key, V value) {
		int hash = hash(key);
		int slotPos = (hash >>> 3) & hashTableMask;
		Object obj = tabAt(table, slotPos);
		if (obj == null) {
			tabSet(table, slotPos, tail(new Node<V>(hash, key, value)));
			return null;
		}

		Node<V> acc;
		Node[] grids = null;
		if (obj instanceof Node[]) {
			grids = (Node[]) obj;
			acc = (Node)tabAt(grids, hash & GRID_MASK);
		} else {
			acc = (Node<V>) obj;
		}
		if (acc == null) {
			gridSet(grids,hash & GRID_MASK,tail(new Node<V>(hash, key, value)));
			return null;
		}
		// 已有，覆盖
		if (acc.hash == hash && acc.key.equals(key)) {
			V old = acc.value;
			acc.value = value;
			return old;
		}
		// 转换节点类型
		if (obj == acc) {
			tabSet(table, slotPos, grids = new Node[8]);
			gridSet(grids,acc.hash & GRID_MASK, acc);

			int index = hash & GRID_MASK;
			acc = (Node)tabAt(grids,index);
			if (acc == null) {
				gridSet(grids, index,tail(new Node<V>(hash, key, value)));
				return null;
			}
		}
		while (acc.conflictNext != null) {
			acc = acc.conflictNext;
			if (acc.hash == hash && acc.key.equals(key)) {
				V old = acc.value;
				acc.value = value;
				return old;
			}
		}
		// 新节点，挂在最后一个节点后面
		Node<V> newNode = tail(new Node<V>(hash, key, value));
		acc.conflictNext = newNode;
		return null;
	}

	private Node<V> tail(Node<V> node) {
		size++;
		if (head == null) {
			tail = head = node;
		} else {
			tail.next = node;
			tail = node;
		}
		return node;
	}

	@SuppressWarnings("unchecked")
	private V innerGet(int hash, String key) {
		int slotPos = (hash >>> 3) & hashTableMask;
		Object obj = tabAt(table, slotPos);
		if (obj != null) {
			Node<V> acc;
			if (obj instanceof Node[]) {
				acc = (Node)tabAt(obj, hash & GRID_MASK);
			} else {
				acc = (Node<V>) obj;
			}
			if (acc != null) {
				if (acc.hash == hash && acc.key.equals(key)) {
					return acc.value;
				}
				while (acc.conflictNext != null) {
					acc = acc.conflictNext;
					if (acc.hash == hash && acc.key.equals(key)) {
						return acc.value;
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Node<V> innerGetNode(Object key) {
		if (key instanceof String) {
			int hash = hash((String) key);
			int slotPos = (hash >>> 3) & hashTableMask;
			Object obj = tabAt(table, slotPos);
			if (obj != null) {
				Node<V> acc;
				if (obj instanceof Node[]) {
					Node<V>[] grids = (Node<V>[]) obj;
					acc = (Node)tabAt(grids,hash & GRID_MASK);
				} else {
					acc = (Node<V>) obj;
				}
				if (acc != null) {
					if (acc.hash == hash && acc.key.equals(key)) {
						return acc;
					}
					while (acc.conflictNext != null) {
						acc = acc.conflictNext;
						if (acc.hash == hash && acc.key.equals(key)) {
							return acc;
						}
					}
				}
			}
		}
		return null;
	}

	private int hash(String key) {
		// return FnvHash.fnv1a32(key);
		return key.hashCode();
	}

	static final class Node<V> implements Map.Entry<String, V> {
		final int hash;
		final String key;

		V value;
		// 按写入顺序记录下一个节点
		Node<V> next;
		// 如果是冲突节点，就往下挂
		Node<V> conflictNext;

		Node(int hash, String key, V value) {
			this.hash = hash;
			this.key = key;
			this.value = value;
		}

		public final String getKey() {
			return key;
		}

		public final V getValue() {
			return value;
		}

		public final String toString() {
			return key + "=" + value;
		}

		public final int hashCode() {
			return Objects.hashCode(key) ^ Objects.hashCode(value);
		}

		public final V setValue(V newValue) {
			V oldValue = value;
			value = newValue;
			return oldValue;
		}

		public final boolean equals(Object o) {
			if (o == this)
				return true;
			if (o instanceof Map.Entry) {
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
			}
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		for (int i = 0; i <= this.hashTableMask; i++) {
			tabSet(table, i, null);
		}
		size = 0;
		head = tail = null;
	}

	private LinkedEntrySet entrySet;
	transient Set<String> keySet;
	transient Collection<V> values;

	public Set<Map.Entry<String, V>> entrySet() {
		Set<Map.Entry<String, V>> es;
		return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
	}

	final <T> T[] valuesToArray(T[] a) {
		Object[] r = a;
		int idx = 0;
		for (FastHashtable.Node<V> e = head; e != null; e = e.next) {
			r[idx++] = e.value;
		}
		return a;
	}

	final <T> T[] keysToArray(T[] a) {
		Object[] r = a;
		int idx = 0;
		for (Node<V> e = head; e != null; e = e.next) {
			r[idx++] = e.key;
		}
		return a;
	}

	@SuppressWarnings("unchecked")
	final <T> T[] prepareArray(T[] a) {
		int size = this.size;
		if (a.length < size) {
			return (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		}
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	final class LinkedKeySet extends AbstractSet<String> {
		public final int size() {
			return size;
		}

		public final void clear() {
			FastHashtable.this.clear();
		}

		public final Iterator<String> iterator() {
			return new LinkedKeyIterator();
		}

		public final boolean contains(Object o) {
			return containsKey(o);
		}

		public final boolean remove(Object key) {
			// return removeNode(hash(key), key, null, false, true) != null;
			throw new UnsupportedOperationException();
		}

		public final Spliterator<String> spliterator() {
			return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.DISTINCT);
		}

		public Object[] toArray() {
			return keysToArray(new Object[size]);
		}

		public <T> T[] toArray(T[] a) {
			return keysToArray(prepareArray(a));
		}

		public final void forEach(Consumer<? super String> action) {
			if (action == null)
				throw new NullPointerException();
			for (Node<V> e = head; e != null; e = e.next)
				action.accept(e.key);
		}
	}

	final class LinkedValues extends AbstractCollection<V> {
		public final int size() {
			return size;
		}

		public final void clear() {
			FastHashtable.this.clear();
		}

		public final Iterator<V> iterator() {
			return new LinkedValueIterator();
		}

		public final boolean contains(Object o) {
			return containsValue(o);
		}

		public final Spliterator<V> spliterator() {
			return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED);
		}

		public Object[] toArray() {
			return valuesToArray(new Object[size]);
		}

		public <T> T[] toArray(T[] a) {
			return valuesToArray(prepareArray(a));
		}

		public final void forEach(Consumer<? super V> action) {
			if (action == null)
				throw new NullPointerException();
			for (Node<V> e = head; e != null; e = e.next)
				action.accept(e.value);
		}
	}

	final class LinkedEntrySet extends AbstractSet<Map.Entry<String, V>> {
		public final int size() {
			return size;
		}

		public final void clear() {
			FastHashtable.this.clear();
		}

		public final Iterator<Map.Entry<String, V>> iterator() {
			return new LinkedEntryIterator();
		}

		public final boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			Object key = (String) e.getKey();
			Node<V> candidate = innerGetNode(key);
			return candidate != null && candidate.equals(e);
		}

		public final boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		public final Spliterator<Map.Entry<String, V>> spliterator() {
			return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.DISTINCT);
		}

		public final void forEach(Consumer<? super Map.Entry<String, V>> action) {
			if (action == null)
				throw new NullPointerException();
			for (Node<V> e = head; e != null; e = e.next)
				action.accept(e);
		}
	}

	abstract class LinkedHashIterator {
		Node<V> next;

		LinkedHashIterator() {
			next = head;
		}

		public final boolean hasNext() {
			return next != null;
		}

		final Node<V> nextNode() {
			Node<V> e = next;
			if (e == null)
				throw new NoSuchElementException();
			next = e.next;
			return e;
		}
	}

	final class LinkedKeyIterator extends LinkedHashIterator implements Iterator<String> {
		public final String next() {
			return nextNode().getKey();
		}
	}

	final class LinkedValueIterator extends LinkedHashIterator implements Iterator<V> {
		public final V next() {
			return nextNode().value;
		}
	}

	final class LinkedEntryIterator extends LinkedHashIterator implements Iterator<Map.Entry<String, V>> {
		public final Node<V> next() {
			return nextNode();
		}
	}

	public int size() {
		return size;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Node<V> node = head;
		sb.append('{');
		if (node != null) {
			sb.append(node.key).append('=').append(node.value);
			node = node.next;
		}
		while (node != null) {
			sb.append(", ").append(node.key).append('=').append(node.value);
			node = node.next;
		}
		sb.append('}');
		return sb.toString();
	}

	public int getMaxDepth() {
		int maxLevel = 0;
		for (int i = 0; i <= this.hashTableMask; i++) {
			Object obj = tabAt(table, i);
			if (obj != null) {
				if (obj instanceof Node[]) {
					@SuppressWarnings("rawtypes")
					Node[] grids = (Node[]) obj;
					for (int j = 0; j < grids.length; j++) {
						Node<?> node = (Node)tabAt(grids,j);
						if (node != null) {
							int level = 1;
							node = node.conflictNext;
							while (node != null) {
								level++;
								node = node.conflictNext;
							}
							if (level > maxLevel) {
								maxLevel = level;
							}
						}
					}
				} else {
				}
			}
		}
		return maxLevel;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return innerGetNode(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(Object key) {
		if (key instanceof String) {
			return get((String) key);
		}
		return null;
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		for (Map.Entry<? extends String, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public Set<String> keySet() {
		Set<String> ks;
		return (ks = keySet) == null ? (keySet = new LinkedKeySet()) : ks;
	}

	@Override
	public Collection<V> values() {
		Collection<V> vs = values;
		if (vs == null) {
			vs = new LinkedValues();
			values = vs;
		}
		return vs;
	}

	public String firstKey() {
		if(head!=null) {
			return head.key;
		}
		return null;
	}
	
	public Map.Entry<String, V> first(){
		return head;
	}
}
