package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * FastHashtable 单元测试类
 * Unit test class for FastHashtable
 */
public class FastHashtableTest {

    private FastHashtable<String> hashtable;

    @BeforeEach
    public void setUp() {
        // 初始化测试对象
        // Initialize test object
        hashtable = new FastHashtable<>(10);
    }

    @Test
    public void testPutAndGet() {
        // 测试插入和获取存在的键值对
        // Test inserting and retrieving an existing key-value pair
        String key = "testKey";
        String value = "testValue";
        hashtable.put(key, value);
        assertEquals(value, hashtable.get(key),"获取值应与插入值一致");
        assertTrue(hashtable.containsKey(key),"containsKey 应返回 true");
    }

    @Test
    public void testPut_Overwrite() {
        // 测试覆盖已有键值对
        // Test overwriting an existing key-value pair
        String key = "overwriteKey";
        String oldValue = "oldValue";
        String newValue = "newValue";
        hashtable.put(key, oldValue);
        hashtable.put(key, newValue);
        assertEquals(newValue, hashtable.get(key));
    }


    @Test
    public void testRemove() {
    	Assertions.assertThrows(UnsupportedOperationException.class, () -> {
    		hashtable.remove("");
    	});
    }
    
    @Test
    public void testComputeIfAbsent() {
    	for(int i=0;i<64;i++) {
    		String s=String.valueOf(i);
    		hashtable.computeIfAbsent(s,(e)->e);	
    	}
    	assertEquals(64,hashtable.size());
    	hashtable.computeIfAbsent("1",(e)->e);
    	assertEquals(64,hashtable.size());
    	
    	
    	hashtable.first();
    	assertEquals("0",hashtable.firstKey());
    }
    
    @Test
    public void testCompute() {
    	for(int i=0;i<128;i++) {
    		String s=String.valueOf(i);
    		hashtable.compute(s,(e,f)->e);	
    	}
    	assertEquals(128,hashtable.size());
    	hashtable.compute("1",(e,f)->e);
    	assertEquals(128,hashtable.size());
    }
    
    
    @Test
    public void testPutAll() {
    	Map<String,String> map=new HashMap<>();
    	for(int i=0;i<128;i++) {
    		String s=String.valueOf(i);
    		map.put(s, s);
    	}
    	hashtable.putAll(map);
    	assertEquals(map.size(),hashtable.size());
    }
    

    @Test
    public void testSize() {
        // 测试 size 方法的正确性
        // Test the correctness of the size method
        assertEquals( 0, hashtable.size());
        hashtable.put("key1", "value1");
        assertEquals( 1, hashtable.size());
    }

    @Test
    public void testClear() {
        // 测试清空哈希表
        // Test clearing the hash table
        hashtable.put("key1", "value1");
        hashtable.put("key2", "value2");
        hashtable.clear();
        assertEquals(0, hashtable.size());
        assertNull( hashtable.get("key1"));
    }
}