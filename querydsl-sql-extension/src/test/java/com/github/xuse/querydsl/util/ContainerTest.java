package com.github.xuse.querydsl.util;



import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.collection.CacheMap;

@SuppressWarnings("unused")
public class ContainerTest {
	private String hello="Hello";
	
	@Test
	public void testHolder() {
		Holder<Boolean> holder=new Holder<>(Boolean.FALSE);
		assertEquals(Boolean.FALSE,holder.get());
		
		holder=new Holder<>();
		assertEquals(null,holder.get());
		
		holder.value=Boolean.TRUE;
		assertEquals(Boolean.TRUE,holder.get());
	}
	
	@Test
	public void testCacheMap() {
		CacheMap<String,String> map=new CacheMap<>(8);
		map.put("a", "a");
		map.put("b", "c");
		map.put("b", "b");
		assertEquals(2, map.size());
		
		CacheMap<String,String> map2=new CacheMap<>(8);
		map2.putIfAbsent("a", "a");
		assertEquals("a", map2.get("a"));
		
		map.put("1", "b");
		map.put("2", "b");
		map.put("3", "b");
		map.put("4", "b");
		map.put("5", "b");
		map.put("6", "b");
		try {
			// CacheMap has capacity limit (75% of 8 = 6), 7th element should throw
			map.put("7", "b");
			fail("Should have thrown");
		}catch(IllegalStateException e) {
		}
		assertFalse(map.containsKey("7"));
	}
	
	@Test
	public void testEntry(){
		Entry<String,String> e=new Entry<>("a","b");
		assertNotNull(e.toString());
		e.setKey("a");
		String s=e.getKey();
		s=e.getValue();
		assertEquals("b",e.setValue("c"));
		Entry<String,String> e1=new Entry<>();
		assertEquals(0,e1.hashCode());
		assertNotEquals(e1, e);
		assertNotEquals(e1.hashCode(), e.hashCode());
		
		
		List<Entry<String,String>> list=Entry.fromMap(Collections.singletonMap("a", "c"));
		assertEquals(e, list.get(0));
		
		assertNotEquals(e, new Object());
		assertNotEquals(e, new Entry<String,String>("a","b"));
		assertNotEquals(e, new Entry<String,String>("b","c"));
	}
	

	@Test
	public void testReader() throws IOException {
		try(StringReader reader=new StringReader(hello)){
			try(InputStream in=new ReaderInputStream(reader,StandardCharsets.UTF_8)){
				byte[] data=IOUtils.toByteArray(in);
				assertArrayEquals(hello.getBytes(StandardCharsets.UTF_8),data);
			}
		};
		
		
		try(StringReader reader=new StringReader(hello)){
			ByteBuffer bufer=ByteBuffer.allocate(2048);
			
			try(InputStream in=new ReaderInputStream(reader)){
				assertTrue(in.available()>0);
				int b;
				while((b=in.read())!=-1){
					bufer.put((byte)b);
				}
				assertTrue(in.available()==0);
			}
		};
	}
}
