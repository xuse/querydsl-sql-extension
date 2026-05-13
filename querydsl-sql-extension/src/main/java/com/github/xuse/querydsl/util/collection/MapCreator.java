package com.github.xuse.querydsl.util.collection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.xuse.querydsl.util.lang.JDKEnvironment;

/**
 * Map 工厂类，统一管理 {@link FastHashtable} 和 {@link CacheMap} 的创建。
 *
 * <p>这两个 Map 实现均依赖 {@code sun.misc.Unsafe} 来绕过数组边界检查以获得更高性能。
 * 在某些 JVM 环境下（如 Android、GraalVM Native Image、或未来移除 Unsafe 的 JDK 版本），
 * Unsafe 可能不可用。本类提供 escape gate：当 Unsafe 不可用或用户通过系统属性
 * {@code querydsl.extension.useUnsafeMap=false} 显式禁用时，自动降级为 JDK 内置 Map 实现。</p>
 *
 * <h2>降级策略</h2>
 * <ul>
 *   <li>{@link FastHashtable} → {@link LinkedHashMap}（保持插入顺序）</li>
 *   <li>{@link CacheMap} → {@link ConcurrentHashMap}（保持线程安全）</li>
 * </ul>
 *
 * <h2>配置方式</h2>
 * <p>设置系统属性 {@code -Dquerydsl.extension.useUnsafeMap=false} 可强制使用 JDK 内置 Map。</p>
 */
public final class MapCreator {

    private static final boolean USE_UNSAFE_MAP;

    static {
        boolean useUnsafe = JDKEnvironment.UNSAFE != null;
        if (useUnsafe) {
            String prop = System.getProperty("querydsl.extension.useUnsafeMap");
            if ("false".equalsIgnoreCase(prop)) {
                useUnsafe = false;
            }
        }
        USE_UNSAFE_MAP = useUnsafe;
    }

    private MapCreator() {
    }

    /**
     * 创建一个高性能的 {@code Map<String, V>}，适用于"写入一次、高频随机读取"场景。
     * <p>Unsafe 可用时返回 {@link FastHashtable}，否则返回 {@link LinkedHashMap}。</p>
     *
     * @param expectedSize 预期元素数量
     * @param <V>          value type
     * @return Map 实例
     */
    public static <V> Map<String, V> createFastMap(int expectedSize) {
        if (USE_UNSAFE_MAP) {
            return new FastHashtable<>(expectedSize);
        }
        return new LinkedHashMap<>(expectedSize * 4 / 3 + 1);
    }

    /**
     * 创建一个线程安全的 {@code Map<K, V>}，适用于"少量并发写、高频无锁读"场景。
     * <p>Unsafe 可用时返回 {@link CacheMap}，否则返回 {@link ConcurrentHashMap}。</p>
     *
     * @param expectedSize 预期元素数量
     * @param <K>          key type
     * @param <V>          value type
     * @return Map 实例
     */
    public static <K, V> Map<K, V> createConcurrentMap(int expectedSize) {
        if (USE_UNSAFE_MAP) {
            return new CacheMap<>(expectedSize);
        }
        return new ConcurrentHashMap<>(expectedSize);
    }

    /**
     * 返回当前是否使用 Unsafe 优化的 Map 实现。
     */
    public static boolean isUsingUnsafeMap() {
        return USE_UNSAFE_MAP;
    }
}
