package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

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
	public void testNoLockHashMap() {
		NoReadLockHashMap<String,String> map=new NoReadLockHashMap<>();
		map=new NoReadLockHashMap<>(5);
		System.out.println("Actually size:"+map.getThreshold());
		map.put("a", "a");
		map.put("b", "c");
		map.put("b", "b");
		assertEquals(2, map.size());
		
		NoReadLockHashMap<String,String> map2=new NoReadLockHashMap<>(5);
		map2.putIfAbsent("a", "a");
		assertEquals(map.getThreshold(),map2.getThreshold());
		
		map.put("1", "b");
		map.put("2", "b");
		map.put("3", "b");
		map.put("4", "b");
		try {
			map.put("5", "b");
		}catch(RuntimeException e) {
		}
		try {
			map.putIfAbsent("5", "b");
		}catch(RuntimeException e) {
		}
		assertFalse(map.containsKey("5"));
		
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
