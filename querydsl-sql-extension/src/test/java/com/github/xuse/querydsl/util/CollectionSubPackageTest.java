package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.collection.ArrayListMap;
import com.github.xuse.querydsl.util.collection.CacheMap;
import com.github.xuse.querydsl.util.collection.FastHashtable;

/**
 * Tests for custom collection implementations in the util.collection sub-package.
 * Validates: Requirements 9.1, 9.2
 */
@DisplayName("Collection Sub-Package Tests")
class CollectionSubPackageTest {

    private static final int ENTRY_COUNT = 15;

    // ==================== ArrayListMap Tests ====================

    @Nested
    @DisplayName("ArrayListMap")
    class ArrayListMapTests {

        @Test
        @DisplayName("put, get, size on 10+ entries")
        void testPutGetSize() {
            ArrayListMap<String, Integer> map = new ArrayListMap<>();
            for (int i = 0; i < ENTRY_COUNT; i++) {
                map.put("key" + i, i);
            }
            assertEquals(ENTRY_COUNT, map.size());
            for (int i = 0; i < ENTRY_COUNT; i++) {
                assertEquals(i, map.get("key" + i));
            }
        }

        @Test
        @DisplayName("put overwrites existing key")
        void testPutOverwrite() {
            ArrayListMap<String, String> map = new ArrayListMap<>();
            for (int i = 0; i < ENTRY_COUNT; i++) {
                map.put("key" + i, "val" + i);
            }
            // Overwrite an existing key
            map.put("key5", "updated");
            assertEquals("updated", map.get("key5"));
            // Size should remain unchanged
            assertEquals(ENTRY_COUNT, map.size());
        }

        @Test
        @DisplayName("iterate over 10+ entries via entrySet")
        void testIterate() {
            ArrayListMap<String, Integer> map = new ArrayListMap<>();
            for (int i = 0; i < ENTRY_COUNT; i++) {
                map.put("key" + i, i);
            }
            Set<String> visitedKeys = new HashSet<>();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                assertNotNull(entry.getKey());
                assertNotNull(entry.getValue());
                visitedKeys.add(entry.getKey());
            }
            assertEquals(ENTRY_COUNT, visitedKeys.size());
        }

        @Test
        @DisplayName("add does not check duplicates")
        void testAddWithoutDuplicateCheck() {
            ArrayListMap<String, String> map = new ArrayListMap<>();
            map.add("dup", "first");
            map.add("dup", "second");
            // add does not deduplicate, so size is 2
            assertEquals(2, map.size());
        }

        @Test
        @DisplayName("clear removes all entries")
        void testClear() {
            ArrayListMap<String, Integer> map = new ArrayListMap<>();
            for (int i = 0; i < ENTRY_COUNT; i++) {
                map.put("key" + i, i);
            }
            map.clear();
            assertEquals(0, map.size());
            assertNull(map.get("key0"));
        }

        @Test
        @DisplayName("wrap creates map from existing list")
        void testWrap() {
            List<Entry<String, Integer>> entries = new ArrayList<>();
            for (int i = 0; i < ENTRY_COUNT; i++) {
                entries.add(new Entry<>("key" + i, i));
            }
            ArrayListMap<String, Integer> map = ArrayListMap.wrap(entries);
            assertEquals(ENTRY_COUNT, map.size());
            for (int i = 0; i < ENTRY_COUNT; i++) {
                assertEquals(i, map.get("key" + i));
            }
        }

        @Test
        @DisplayName("constructor from existing Map")
        void testConstructFromMap() {
            ArrayListMap<String, Integer> original = new ArrayListMap<>();
            for (int i = 0; i < ENTRY_COUNT; i++) {
                original.put("key" + i, i);
            }
            ArrayListMap<String, Integer> copy = new ArrayListMap<>(original);
            assertEquals(ENTRY_COUNT, copy.size());
            for (int i = 0; i < ENTRY_COUNT; i++) {
                assertEquals(i, copy.get("key" + i));
            }
        }
    }

    // ==================== CacheMap Tests ====================

    @Nested
    @DisplayName("CacheMap")
    class CacheMapTests {

        @Test
        @DisplayName("put, get, size on 10+ entries")
        void testPutGetSize() {
            CacheMap<String, Integer> map = new CacheMap<>(64);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                map.put("key" + i, i);
            }
            assertEquals(ENTRY_COUNT, map.size());
            for (int i = 0; i < ENTRY_COUNT; i++) {
                assertEquals(i, map.get("key" + i));
            }
        }

        @Test
        @DisplayName("iterate over 10+ entries via entrySet")
        void testIterate() {
            CacheMap<String, Integer> map = new CacheMap<>(64);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                map.put("key" + i, i);
            }
            Set<String> visitedKeys = new HashSet<>();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                assertNotNull(entry.getKey());
                assertNotNull(entry.getValue());
                visitedKeys.add(entry.getKey());
            }
            assertEquals(ENTRY_COUNT, visitedKeys.size());
        }

        @Test
        @DisplayName("at capacity: put throws IllegalStateException")
        void testCapacityOverflowThrows() {
            // CacheMap with expectedSize=4 -> table size=4, maxSize = 4*0.75 = 3
            CacheMap<String, String> map = new CacheMap<>(4);
            int maxSize = map.getMaxSize();
            // Fill to capacity
            for (int i = 0; i < maxSize; i++) {
                map.put("key" + i, "val" + i);
            }
            assertEquals(maxSize, map.size());
            // Next put should throw
            assertThrows(IllegalStateException.class, () -> map.put("overflow", "boom"));
        }

        @Test
        @DisplayName("at capacity: existing entries remain accessible")
        void testCapacityOverflowExistingAccessible() {
            CacheMap<String, String> map = new CacheMap<>(4);
            int maxSize = map.getMaxSize();
            for (int i = 0; i < maxSize; i++) {
                map.put("key" + i, "val" + i);
            }
            // Trigger overflow
            try {
                map.put("overflow", "boom");
            } catch (IllegalStateException e) {
                // expected
            }
            // All existing entries should still be accessible
            for (int i = 0; i < maxSize; i++) {
                assertEquals("val" + i, map.get("key" + i));
            }
            assertEquals(maxSize, map.size());
        }

        @Test
        @DisplayName("at capacity: overwriting existing key still works")
        void testCapacityOverwriteStillWorks() {
            CacheMap<String, String> map = new CacheMap<>(4);
            int maxSize = map.getMaxSize();
            for (int i = 0; i < maxSize; i++) {
                map.put("key" + i, "val" + i);
            }
            // Overwrite existing key should succeed even at capacity
            assertEquals("val0", map.put("key0", "updated"));
            assertEquals("updated", map.get("key0"));
            assertEquals(maxSize, map.size());
        }

        @Test
        @DisplayName("containsKey and get for missing key")
        void testMissingKey() {
            CacheMap<String, Integer> map = new CacheMap<>(64);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                map.put("key" + i, i);
            }
            assertNull(map.get("nonexistent"));
            assertTrue(!map.containsKey("nonexistent"));
        }
    }

    // ==================== FastHashtable Tests ====================

    @Nested
    @DisplayName("FastHashtable")
    class FastHashtableTests {

        @Test
        @DisplayName("put, get, size on 10+ entries")
        void testPutGetSize() {
            FastHashtable<Integer> table = new FastHashtable<>(32);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                table.put("key" + i, i);
            }
            assertEquals(ENTRY_COUNT, table.size());
            for (int i = 0; i < ENTRY_COUNT; i++) {
                assertEquals(i, table.get("key" + i));
            }
        }

        @Test
        @DisplayName("iterate over 10+ entries via entrySet preserves insertion order")
        void testIterate() {
            FastHashtable<Integer> table = new FastHashtable<>(32);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                table.put("key" + i, i);
            }
            List<String> iteratedKeys = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : table.entrySet()) {
                assertNotNull(entry.getKey());
                assertNotNull(entry.getValue());
                iteratedKeys.add(entry.getKey());
            }
            assertEquals(ENTRY_COUNT, iteratedKeys.size());
            // FastHashtable preserves insertion order
            for (int i = 0; i < ENTRY_COUNT; i++) {
                assertEquals("key" + i, iteratedKeys.get(i));
            }
        }

        @Test
        @DisplayName("keySet and values on 10+ entries")
        void testKeySetAndValues() {
            FastHashtable<String> table = new FastHashtable<>(32);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                table.put("k" + i, "v" + i);
            }
            Set<String> keys = table.keySet();
            assertEquals(ENTRY_COUNT, keys.size());
            for (int i = 0; i < ENTRY_COUNT; i++) {
                assertTrue(keys.contains("k" + i));
            }
            assertEquals(ENTRY_COUNT, table.values().size());
        }

        @Test
        @DisplayName("put overwrites existing key and returns old value")
        void testPutOverwrite() {
            FastHashtable<String> table = new FastHashtable<>(32);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                table.put("key" + i, "val" + i);
            }
            String old = table.put("key5", "updated");
            assertEquals("val5", old);
            assertEquals("updated", table.get("key5"));
            assertEquals(ENTRY_COUNT, table.size());
        }

        @Test
        @DisplayName("get returns null for missing key")
        void testGetMissing() {
            FastHashtable<String> table = new FastHashtable<>(32);
            for (int i = 0; i < ENTRY_COUNT; i++) {
                table.put("key" + i, "val" + i);
            }
            assertNull(table.get("nonexistent"));
            assertNull(table.get((Object) null));
            assertNull(table.get((Object) Integer.valueOf(42)));
        }

        @Test
        @DisplayName("isEmpty and containsKey")
        void testIsEmptyAndContainsKey() {
            FastHashtable<String> table = new FastHashtable<>(32);
            assertTrue(table.isEmpty());
            table.put("a", "1");
            assertTrue(!table.isEmpty());
            assertTrue(table.containsKey("a"));
            assertTrue(!table.containsKey("b"));
        }

        @Test
        @DisplayName("remove throws UnsupportedOperationException")
        void testRemoveUnsupported() {
            FastHashtable<String> table = new FastHashtable<>(32);
            table.put("a", "1");
            assertThrows(UnsupportedOperationException.class, () -> table.remove("a"));
        }
    }
}
