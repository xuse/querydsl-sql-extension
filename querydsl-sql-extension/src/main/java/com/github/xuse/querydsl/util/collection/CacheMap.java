package com.github.xuse.querydsl.util.collection;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.xuse.querydsl.util.lang.UnsafeAccess;

/**
 * 高性能的线程安全 Map，针对"少量并发写、高频无锁读"场景优化，对标 {@link java.util.concurrent.ConcurrentHashMap}。
 *
 * <h2>设计原理</h2>
 * <p>核心思路：通过禁止扩容 + volatile 数组槽位访问，使读操作完全无锁，同时保证读线程
 * 始终看到完整的节点状态（不会读到半初始化节点）。</p>
 * <ul>
 *   <li><b>读路径</b>：通过 {@code Unsafe.getObjectVolatile} 读取数组槽位，无任何锁或 CAS，
 *       happens-before 由 volatile 语义保证——写线程 {@code putObjectVolatile} 发布节点后，
 *       读线程一定能看到完整的节点内容。</li>
 *   <li><b>写路径</b>：{@code synchronized} 互斥，防止并发写破坏链表结构；
 *       写入前检查容量上限，绝不触发 HashMap 的 resize。</li>
 *   <li><b>冲突处理</b>：同一槽位冲突时以链表形式挂载（{@code Node.next}），
 *       链表节点通过 {@code putObjectVolatile} 发布，读线程安全可见。</li>
 * </ul>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>不支持扩容，容量在构造时固定（内部数组大小为 2 的幂次）</li>
 *   <li>超过容量上限时 {@code put} 抛出 {@link IllegalStateException}</li>
 *   <li>不支持 {@code remove}、{@code clear}（抛出 {@link UnsupportedOperationException}）</li>
 *   <li>Key/Value 均不允许为 null</li>
 * </ul>
 *
 * <h2>与传统方案的区别</h2>
 * <p>传统的 {@code NoReadLockHashMap}（已废弃）继承自 {@code HashMap}，读操作依赖 HashMap 内部实现细节
 * （不扩容时链表只追加不断链），没有 volatile 语义保证，存在可见性延迟风险。
 * 本类通过 {@code Unsafe} volatile 数组访问提供严格的 happens-before 保证，
 * 同时绕过数组边界检查，读性能优于 {@code ConcurrentHashMap}。</p>
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class CacheMap<K, V> extends AbstractMap<K, V> {

    private static final int MAXIMUM_CAPACITY = 1 << 16;

    /** 哈希桶数组，槽位通过 Unsafe volatile 访问 */
    private final Object[] table;
    private final int mask;
    private final int maxSize;
    private int size;

    public CacheMap() {
        this(64);
    }

    public CacheMap(int expectedSize) {
        int cap = tableSizeFor(expectedSize);
        this.table = new Object[cap];
        this.mask = cap - 1;
        // 负载因子 0.75，与 HashMap 一致
        this.maxSize = (int) (cap * 0.75f);
    }

    private static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    // ==================== 读路径（无锁）====================

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        int hash = spread(key.hashCode());
        int slot = hash & mask;
        // volatile 读，保证 happens-before
        Node<K, V> node = (Node<K, V>) UnsafeAccess.getArrayObjectVolatile(table, slot);
        while (node != null) {
            if (node.hash == hash && node.key.equals(key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V existing = get(key);
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            // double-check
            existing = get(key);
            if (existing != null) {
                return existing;
            }
            V value = mappingFunction.apply(key);
            if (value != null) {
                putInternal(key, value);
            }
            return value;
        }
    }

    // ==================== 写路径（synchronized）====================

    @Override
    public synchronized V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("CacheMap does not permit null keys or values");
        }
        return putInternal(key, value);
    }

    @Override
    public synchronized V putIfAbsent(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("CacheMap does not permit null keys or values");
        }
        V existing = get(key);
        if (existing != null) {
            return existing;
        }
        putInternal(key, value);
        return null;
    }

    @SuppressWarnings("unchecked")
    private V putInternal(K key, V value) {
        int hash = spread(key.hashCode());
        int slot = hash & mask;
        Node<K, V> head = (Node<K, V>) UnsafeAccess.getArrayObjectVolatile(table, slot);
        // 检查是否已存在，覆盖旧值
        Node<K, V> cur = head;
        while (cur != null) {
            if (cur.hash == hash && cur.key.equals(key)) {
                V old = cur.value;
                cur.value = value;
                return old;
            }
            cur = cur.next;
        }
        if (size >= maxSize) {
            throw new IllegalStateException("CacheMap is full, maximum capacity=" + maxSize);
        }
        // 新节点头插，通过 volatile 写发布
        Node<K, V> newNode = new Node<>(hash, key, value, head);
        UnsafeAccess.putArrayObjectVolatile(table, slot, newNode);
        size++;
        return null;
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("CacheMap does not support remove");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("CacheMap does not support clear");
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 返回当前容量上限。
     */
    public int getMaxSize() {
        return maxSize;
    }

    // ==================== 内部节点 ====================

    private static final class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        volatile V value;
        /** 冲突链表，写入后不修改（头插法），读线程安全 */
        final Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override public K getKey()   { return key; }
        @Override public V getValue() { return value; }
        @Override public V setValue(V v) {
            V old = value; value = v; return old;
        }
    }

    /**
     * 扰动函数，与 HashMap 一致，减少高位 hash 冲突。
     */
    private static int spread(int h) {
        return h ^ (h >>> 16);
    }

    // ==================== 视图（只读遍历）====================

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        // 简单实现，用于遍历场景（非热路径）
        java.util.LinkedHashMap<K, V> snapshot = new java.util.LinkedHashMap<>(size * 2);
        for (int i = 0; i < table.length; i++) {
            @SuppressWarnings("unchecked")
            Node<K, V> node = (Node<K, V>) UnsafeAccess.getArrayObjectVolatile(table, i);
            while (node != null) {
                snapshot.put(node.key, node.value);
                node = node.next;
            }
        }
        return snapshot.entrySet();
    }

    @Override
    public Set<K> keySet() {
        java.util.LinkedHashSet<K> keys = new java.util.LinkedHashSet<>(size * 2);
        for (int i = 0; i < table.length; i++) {
            @SuppressWarnings("unchecked")
            Node<K, V> node = (Node<K, V>) UnsafeAccess.getArrayObjectVolatile(table, i);
            while (node != null) {
                keys.add(node.key);
                node = node.next;
            }
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        java.util.ArrayList<V> vals = new java.util.ArrayList<>(size);
        for (int i = 0; i < table.length; i++) {
            @SuppressWarnings("unchecked")
            Node<K, V> node = (Node<K, V>) UnsafeAccess.getArrayObjectVolatile(table, i);
            while (node != null) {
                vals.add(node.value);
                node = node.next;
            }
        }
        return vals;
    }
}
