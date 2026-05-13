package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.collection.CacheMap;

/**
 * Unit tests for {@link CacheMap}.
 */
class CacheMapTest {

    // ==================== Basic Operations ====================

    @Test
    void testPutAndGet() {
        CacheMap<String, Integer> map = new CacheMap<>(16);
        assertNull(map.put("a", 1));
        assertNull(map.put("b", 2));
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertNull(map.get("c"));
        assertEquals(2, map.size());
    }

    @Test
    void testPutOverwrite() {
        CacheMap<String, String> map = new CacheMap<>(16);
        assertNull(map.put("key", "v1"));
        assertEquals("v1", map.put("key", "v2"));
        assertEquals("v2", map.get("key"));
        assertEquals(1, map.size());
    }

    @Test
    void testPutIfAbsent() {
        CacheMap<String, String> map = new CacheMap<>(16);
        assertNull(map.putIfAbsent("a", "first"));
        assertEquals("first", map.putIfAbsent("a", "second"));
        assertEquals("first", map.get("a"));
        assertEquals(1, map.size());
    }

    @Test
    void testComputeIfAbsent() {
        CacheMap<String, String> map = new CacheMap<>(16);
        AtomicInteger callCount = new AtomicInteger(0);
        String result = map.computeIfAbsent("key", k -> {
            callCount.incrementAndGet();
            return "computed_" + k;
        });
        assertEquals("computed_key", result);
        assertEquals(1, callCount.get());

        // Second call should not invoke function
        String result2 = map.computeIfAbsent("key", k -> {
            callCount.incrementAndGet();
            return "should_not_happen";
        });
        assertEquals("computed_key", result2);
        assertEquals(1, callCount.get());
    }

    @Test
    void testContainsKey() {
        CacheMap<String, String> map = new CacheMap<>(16);
        map.put("exists", "value");
        assertTrue(map.containsKey("exists"));
        assertFalse(map.containsKey("missing"));
    }

    @Test
    void testIsEmpty() {
        CacheMap<String, String> map = new CacheMap<>(16);
        assertTrue(map.isEmpty());
        map.put("a", "b");
        assertFalse(map.isEmpty());
    }

    // ==================== Capacity & Error Handling ====================

    @Test
    void testCapacityOverflow() {
        // capacity=4 -> table size=4, maxSize = 4*0.75 = 3
        CacheMap<String, String> map = new CacheMap<>(8);
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        map.put("4", "c");
        map.put("5", "c");
        map.put("6", "c");
        assertThrows(IllegalStateException.class, () -> map.put("7", "d"));
        assertEquals(6, map.size());
        // Existing keys can still be overwritten
        assertEquals("a", map.put("1", "aa"));
        assertEquals("aa", map.get("1"));
        assertEquals(6, map.size());
    }

    @Test
    void testNullKeyThrows() {
        CacheMap<String, String> map = new CacheMap<>(16);
        assertThrows(NullPointerException.class, () -> map.put(null, "v"));
    }

    @Test
    void testNullValueThrows() {
        CacheMap<String, String> map = new CacheMap<>(16);
        assertThrows(NullPointerException.class, () -> map.put("k", null));
    }

    @Test
    void testRemoveUnsupported() {
        CacheMap<String, String> map = new CacheMap<>(16);
        map.put("a", "b");
        assertThrows(UnsupportedOperationException.class, () -> map.remove("a"));
    }

    @Test
    void testClearUnsupported() {
        CacheMap<String, String> map = new CacheMap<>(16);
        map.put("a", "b");
        assertThrows(UnsupportedOperationException.class, () -> map.clear());
    }

    // ==================== Hash Collision ====================

    @Test
    void testHashCollision() {
        // Use keys with same hash to force collision chain
        CacheMap<CollidingKey, String> map = new CacheMap<>(16);
        CollidingKey k1 = new CollidingKey(1, 42);
        CollidingKey k2 = new CollidingKey(2, 42);
        CollidingKey k3 = new CollidingKey(3, 42);

        map.put(k1, "v1");
        map.put(k2, "v2");
        map.put(k3, "v3");

        assertEquals("v1", map.get(k1));
        assertEquals("v2", map.get(k2));
        assertEquals("v3", map.get(k3));
        assertNull(map.get(new CollidingKey(4, 42)));
        assertEquals(3, map.size());

        // Overwrite in collision chain
        assertEquals("v2", map.put(k2, "v2_new"));
        assertEquals("v2_new", map.get(k2));
        assertEquals(3, map.size());
    }

    // ==================== Views ====================

    @Test
    void testEntrySet() {
        CacheMap<String, Integer> map = new CacheMap<>(16);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        assertEquals(3, entries.size());
    }

    @Test
    void testKeySet() {
        CacheMap<String, Integer> map = new CacheMap<>(16);
        map.put("x", 10);
        map.put("y", 20);

        Set<String> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("x"));
        assertTrue(keys.contains("y"));
    }

    @Test
    void testValues() {
        CacheMap<String, Integer> map = new CacheMap<>(16);
        map.put("a", 100);
        map.put("b", 200);

        Collection<Integer> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(100));
        assertTrue(values.contains(200));
    }

    // ==================== Concurrency ====================

    @Test
    void testConcurrentReadsAndWrites() throws InterruptedException {
        CacheMap<Integer, String> map = new CacheMap<>(256);
        int writerCount = 2;
        int readerCount = 4;
        int itemsPerWriter = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(writerCount + readerCount);
        AtomicInteger errors = new AtomicInteger(0);

        // Pre-populate some entries
        for (int i = 0; i < 20; i++) {
            map.put(i, "init_" + i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(writerCount + readerCount);

        // Writers
        for (int w = 0; w < writerCount; w++) {
            final int writerId = w;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < itemsPerWriter; i++) {
                        int key = 20 + writerId * itemsPerWriter + i;
                        map.put(key, "w" + writerId + "_" + i);
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Readers
        for (int r = 0; r < readerCount; r++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 1000; i++) {
                        // Read pre-populated entries
                        int key = i % 20;
                        String val = map.get(key);
                        if (val == null) {
                            errors.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(0, errors.get(), "Concurrent reads should not fail");
        // All written entries should be visible after writers complete
        assertEquals(20 + writerCount * itemsPerWriter, map.size());
        for (int i = 0; i < 20; i++) {
            assertEquals("init_" + i, map.get(i));
        }
    }

    @Test
    void testComputeIfAbsentConcurrent() throws InterruptedException {
        CacheMap<String, String> map = new CacheMap<>(64);
        int threadCount = 8;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger computeCount = new AtomicInteger(0);
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String val = map.computeIfAbsent("shared_key", k -> {
                        computeCount.incrementAndGet();
                        return "computed";
                    });
                    results.put(Thread.currentThread().getName(), val);
                } catch (Exception e) {
                    // ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Function should be called exactly once (double-check in synchronized)
        assertEquals(1, computeCount.get());
        // All threads should see the same value
        for (String val : results.values()) {
            assertEquals("computed", val);
        }
    }

    // ==================== Helper ====================

    private static final class CollidingKey {
        private final int id;
        private final int hash;

        CollidingKey(int id, int hash) {
            this.id = id;
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CollidingKey) {
                return this.id == ((CollidingKey) obj).id;
            }
            return false;
        }
    }
}
