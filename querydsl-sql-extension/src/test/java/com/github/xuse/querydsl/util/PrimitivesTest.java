package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TreeMap;

import org.junit.Test;

@SuppressWarnings("unused")
public class PrimitivesTest {
	Class<?>[] pClazz= {
			int.class,
			long.class,
			short.class,
			double.class,
			float.class,
			char.class,
			boolean.class,
			byte.class
	};
	
	Class<?>[] wClazz= {
			Integer.class,
			Long.class,
			Short.class,
			Double.class,
			Float.class,
			Character.class,
			Boolean.class,
			Byte.class
	};
	
	Class<?>[] strClazz= {
			String.class,
	};
	
	@Test
	public void testPrimitives() {
		for(Class<?> clz:pClazz) {
			Object o=Primitives.defaultValueOfPrimitive(clz);
			assertNotNull(o);
			
			o=Primitives.defaultValueForBasicType(clz);
			assertNotNull(o);
			
			Class<?> clz2=Primitives.toPrimitiveClass(clz);
			assertEquals(clz, clz2);
			
			clz2=Primitives.toWrapperClass(clz);
			assertNotNull(clz2);
			assertNotEquals(clz, clz2);
			
		}
		for(Class<?> clz:wClazz) {
			Object o=Primitives.defaultValueForBasicType(clz);
			assertNull(o);
			
			Class<?> clz2=Primitives.toPrimitiveClass(clz);
			assertNotEquals(clz, clz2);
			
			clz2=Primitives.toWrapperClass(clz);
			assertEquals(clz, clz2);
		}
		
		for(Class<?> clz:strClazz) {
			Object o=Primitives.defaultValueForBasicType(clz);
			assertEquals("",o);
		}
	}
	
	@Test
	public void testUnbox() {
		assertEquals(false,Primitives.unbox((Boolean)null, false));
		assertEquals((byte)0,Primitives.unbox((Byte)null, (byte)0));
		assertEquals('a',Primitives.unbox((Character)null, 'a'));
		assertEquals(0L,Primitives.unbox((Long)null, 0L));
		assertEquals(0,Primitives.unbox((Integer)null, 0));
		assertEquals((short)0,Primitives.unbox((Short)null, (short)0));
		assertEquals(0d,Primitives.unbox((Double)null, 0d),0);
		assertEquals(0f,Primitives.unbox((Float)null, 0f),0);
		
		
		assertEquals(false,Primitives.unbox(Boolean.FALSE, false));
		assertEquals((byte)0,Primitives.unbox((byte)0, (byte)0));
		assertEquals('a',Primitives.unbox('a', 'a'));
		assertEquals(0L,Primitives.unbox(0L, 0L));
		assertEquals(0,Primitives.unbox(0, 0));
		assertEquals((short)0,Primitives.unbox(Short.valueOf((short)0), (short)0));
		assertEquals(0d,Primitives.unbox(Double.valueOf(0d), 0d),0);
		assertEquals(0f,Primitives.unbox(0f, 0f),0);
		
		
		assertEquals(0L,Primitives.unbox((Date)null, 0L));
		assertEquals(1L,Primitives.unbox(new Date(1), 0L));
	}
	
	@Test
	public void testOddEven() {
		assertTrue(Primitives.isEven(2));
		assertTrue(Primitives.isEven(100000));
		assertTrue(Primitives.isEven(0));
		assertTrue(Primitives.isEven(-10));
		assertTrue(Primitives.isEven(2L));
		assertTrue(Primitives.isEven(100000L));
		assertTrue(Primitives.isEven(0L));
		assertTrue(Primitives.isEven(-10L));
		assertFalse(Primitives.isEven(1));
		assertFalse(Primitives.isEven(-1));
		assertFalse(Primitives.isEven(-999));
		assertFalse(Primitives.isEven(1L));
		assertFalse(Primitives.isEven(-1L));
		assertFalse(Primitives.isEven(-999L));
		
		
		assertFalse(Primitives.isOdd(2));
		assertFalse(Primitives.isOdd(100000));
		assertFalse(Primitives.isOdd(0));
		assertFalse(Primitives.isOdd(-10));
		assertFalse(Primitives.isOdd(2L));
		assertFalse(Primitives.isOdd(100000L));
		assertFalse(Primitives.isOdd(0L));
		assertFalse(Primitives.isOdd(-10L));
		
		assertTrue(Primitives.isOdd(1));
		assertTrue(Primitives.isOdd(-1));
		assertTrue(Primitives.isOdd(-999));
		assertTrue(Primitives.isOdd(1L));
		assertTrue(Primitives.isOdd(-1L));
		assertTrue(Primitives.isOdd(-999L));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSafty1() {
		long a=100;
		int b=Primitives.toIntSafely(a);
		assertEquals(b, a);
		
		a=100000000000L;
		b=Primitives.toIntSafely(a);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSafty3() {
		long a=-100000000000L;
		int b=Primitives.toIntSafely(a);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSafty2() {
		int a=100;
		int b=Primitives.toShortSafely(a);
		assertEquals(b, a);
		
		a=10000000;
		b = Primitives.toShortSafely(a);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSafty4() {
		int a=-10000000;
		short b = Primitives.toShortSafely(a);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void exception1() {
		for(Class<?> clz:wClazz) {
			Primitives.defaultValueOfPrimitive(clz);	
		}
	}
	
	@Test
	public void testPrimitiveTypeNameDistribution() {
		String[] ss = new String[] { "byte", "boolean", "char", "int", "short", "long", "float", "double" };

		TreeMap<Integer, String> map = new TreeMap<>();
		for (String s : ss) {
			int v = s.length() + s.charAt(0);
			map.put(v, s);
		}
		int min = map.firstKey();
		int max = map.lastKey();
		if (map.size() < 8) {
			System.out.println("发生冲突!");
		} else {
			System.out.println("分布宽度" + (max - min + 1));
		}
		System.out.println(map);
	}
}
