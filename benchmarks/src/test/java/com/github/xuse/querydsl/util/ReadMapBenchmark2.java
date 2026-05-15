package com.github.xuse.querydsl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.github.xuse.querydsl.util.collection.CacheMap;

import io.github.xuse.querydsl.sql.extension.BenchmarkRunner;

/**
 * JMH benchmark comparing pure read performance of CacheMap vs HashMap vs ConcurrentHashMap.
 * Simulates the QBeanExWithConverter cache scenario: pre-populated map, read-only access.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@RunWith(BenchmarkRunner.class)
public class ReadMapBenchmark2 {

    private static final int MAP_SIZE = 64;

    /** Keys used for lookup (simulating ConverterCacheKey) */
    private static final Object[] KEYS = new Object[MAP_SIZE];

    private final Map<Object, Object> cacheMap = new CacheMap<>(128);
    private final Map<Object, Object> hashMap = new HashMap<>(128);
    private final Map<Object, Object> concurrentHashMap = new ConcurrentHashMap<>(64);

    static {
        for (int i = 0; i < MAP_SIZE; i++) {
            KEYS[i] = new CacheKey(i);
        }
    }

    public ReadMapBenchmark2() {
        for (int i = 0; i < MAP_SIZE; i++) {
            Object value = "value_" + i;
            cacheMap.put(KEYS[i], value);
            hashMap.put(KEYS[i], value);
            concurrentHashMap.put(KEYS[i], value);
        }
    }

    /**
     * CacheMap: Unsafe volatile 数组访问，无锁读，严格 happens-before
     */
    @Benchmark
    public void readCacheMap(Blackhole bh) {
        for (int i = 0; i < MAP_SIZE; i++) {
            bh.consume(cacheMap.get(KEYS[i]));
        }
    }

    /**
     * HashMap: JDK 内置，无并发保护，纯读基准
     */
    @Benchmark
    public void readHashMap(Blackhole bh) {
        for (int i = 0; i < MAP_SIZE; i++) {
            bh.consume(hashMap.get(KEYS[i]));
        }
    }

    /**
     * ConcurrentHashMap: JDK 内置，Unsafe volatile + 分段锁，JVM 内部类优化
     */
    @Benchmark
    public void readConcurrentHashMap(Blackhole bh) {
        for (int i = 0; i < MAP_SIZE; i++) {
            bh.consume(concurrentHashMap.get(KEYS[i]));
        }
    }

    /**
     * Simulates the ConverterCacheKey used in ProjectionsAlter.
     */
    private static final class CacheKey {
        private final int id;
        private final int hash;

        CacheKey(int id) {
            this.id = id;
            this.hash = Integer.hashCode(id) * 31 + 17;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CacheKey) {
                return this.id == ((CacheKey) obj).id;
            }
            return false;
        }
    }
}
