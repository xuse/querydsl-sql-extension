package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;

public class ArrayUtilsTest {
	private static final String[] STRING_ARRAY = { "a", "b", "c" };
	private static final int[] INT_ARRAY = { 1, 2, 3 };
	private static final short[] SHORT_ARRAY = { 1, 2, 3 };
	private static final long[] LONG_ARRAY = { 1L, 2L, 3L };
	private static final float[] FLOAT_ARRAY = { 1f, 2f, 3f };
	private static final double[] DOUBLE_ARRAY = { 1d, 2d, 3d };
	private static final byte[] BYTE_ARRAY = { 0x01, 0x02, 0x03 };
	private static final boolean[] BOOLEAN_ARRAY = { true, false, true };
	private static final char[] CHAR_ARRAY = { 'a', 'b', 'c' };

	@Test
	public void testArrays() {
		assertFalse(ArrayUtils.isEmpty(STRING_ARRAY));
		assertFalse(ArrayUtils.isEmpty(INT_ARRAY));
		assertFalse(ArrayUtils.isEmpty(SHORT_ARRAY));
		assertFalse(ArrayUtils.isEmpty(LONG_ARRAY));
		assertFalse(ArrayUtils.isEmpty(FLOAT_ARRAY));
		assertFalse(ArrayUtils.isEmpty(DOUBLE_ARRAY));
		assertFalse(ArrayUtils.isEmpty(BYTE_ARRAY));
		assertFalse(ArrayUtils.isEmpty(BOOLEAN_ARRAY));
		assertFalse(ArrayUtils.isEmpty(CHAR_ARRAY));
		
		String[] strings=ArrayUtils.addAllElement(STRING_ARRAY, STRING_ARRAY);
		assertEquals(6, strings.length);
		
		strings=ArrayUtils.addElement(STRING_ARRAY,"d");
		assertEquals(4, strings.length);
		
		strings = ArrayUtils.addElement(null, "d", String.class);
		strings = ArrayUtils.addElement(strings, "d", String.class);
		assertEquals(2, strings.length);
		
		
		CharSequence[] cs=ArrayUtils.cast(STRING_ARRAY, CharSequence.class);
		assertEquals(3, cs.length);
		
		
		assertTrue(ArrayUtils.contains(BYTE_ARRAY, (byte)0x01));
		assertTrue(ArrayUtils.contains(INT_ARRAY, (int)0x01));
		assertTrue(ArrayUtils.contains(SHORT_ARRAY, (short)1));
		assertTrue(ArrayUtils.contains(LONG_ARRAY, 1L));
		assertTrue(ArrayUtils.contains(DOUBLE_ARRAY, 1d));
		assertTrue(ArrayUtils.contains(FLOAT_ARRAY, 1f));
		assertTrue(ArrayUtils.contains(BOOLEAN_ARRAY, false));
		assertTrue(ArrayUtils.contains(CHAR_ARRAY, 'a'));
		assertTrue(ArrayUtils.contains(STRING_ARRAY, "a"));
		
		
		assertFalse(ArrayUtils.contains(BYTE_ARRAY, (byte)4));
		assertFalse(ArrayUtils.contains(INT_ARRAY, (int)4));
		assertFalse(ArrayUtils.contains(SHORT_ARRAY, (short)4));
		assertFalse(ArrayUtils.contains(LONG_ARRAY, 4L));
		assertFalse(ArrayUtils.contains(DOUBLE_ARRAY, 4d));
		assertFalse(ArrayUtils.contains(FLOAT_ARRAY, 4f));
		assertFalse(ArrayUtils.contains(new boolean[] {true,true}, false));
		assertFalse(ArrayUtils.contains(CHAR_ARRAY, 'd'));
		assertFalse(ArrayUtils.contains(STRING_ARRAY, "d"));
		
		assertFalse(ArrayUtils.contains((byte[])null, (byte)4));
		assertFalse(ArrayUtils.contains((int[])null, (int)4));
		assertFalse(ArrayUtils.contains((short[])null, (short)4));
		assertFalse(ArrayUtils.contains((long[])null, 4L));
		assertFalse(ArrayUtils.contains((double[])null, 4d));
		assertFalse(ArrayUtils.contains((float[])null, 4f));
		assertFalse(ArrayUtils.contains((boolean[])null, false));
		assertFalse(ArrayUtils.contains((char[])null, 'd'));
		assertFalse(ArrayUtils.contains(null, "d"));
		
		assertTrue(ArrayUtils.containsAny(STRING_ARRAY, new String[] {"c","b"}));
		assertFalse(ArrayUtils.containsAny(STRING_ARRAY, new String[] {}));
		//assertFalse(ArrayUtils.containsAll(STRING_ARRAY, new String[] {"a","b"}));
		assertTrue(ArrayUtils.containsIgnoreCase(STRING_ARRAY, "A"));
		
		assertTrue(ArrayUtils.containsIgnoreCase(new String[] {"b",null,"a"}, "A"));
		assertFalse(ArrayUtils.containsIgnoreCase(null, "A"));
		assertFalse(ArrayUtils.containsIgnoreCase(new String[0], "A"));
		assertTrue(ArrayUtils.containsIgnoreCase(new String[] {"a",null}, null));
		assertArrayEquals(STRING_ARRAY, (String[])ArrayUtils.toFixLength(STRING_ARRAY,3));
		
		
		List<Integer> ints = ArrayUtils.convert(STRING_ARRAY, String::hashCode);
		assertEquals(3, ints.size());
		
		strings = ArrayUtils.copyOf(STRING_ARRAY, 6);
		assertEquals(6,strings.length);
		byte[] bytes = ArrayUtils.copyOf(BYTE_ARRAY, 6);
		assertEquals(6,bytes.length);
		
		char[] chars = ArrayUtils.copyOf(CHAR_ARRAY, 6);
		assertEquals(6,chars.length);
		
		assertTrue(ArrayUtils.equals(null, null));
		assertFalse(ArrayUtils.equals(null, INT_ARRAY));
		
		
		assertFalse(ArrayUtils.equals(STRING_ARRAY, null));
		assertFalse(ArrayUtils.equals(STRING_ARRAY, INT_ARRAY));
		
		assertTrue(ArrayUtils.equals(INT_ARRAY, INT_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(SHORT_ARRAY, SHORT_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(LONG_ARRAY, LONG_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(FLOAT_ARRAY, FLOAT_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(DOUBLE_ARRAY, DOUBLE_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(BYTE_ARRAY, BYTE_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(CHAR_ARRAY, CHAR_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(BOOLEAN_ARRAY, BOOLEAN_ARRAY.clone()));
		assertFalse(ArrayUtils.equals(INT_ARRAY, new int[] {1,2}));
		assertTrue(ArrayUtils.equals(INT_ARRAY, INT_ARRAY));
		
		assertTrue(ArrayUtils.equals(STRING_ARRAY, STRING_ARRAY.clone()));
		assertTrue(ArrayUtils.equals(STRING_ARRAY, STRING_ARRAY));
		assertTrue(ArrayUtils.equals((Object)STRING_ARRAY, (Object)STRING_ARRAY.clone()));
		
		assertTrue(ArrayUtils.equals(null, null));
		assertTrue(ArrayUtils.equals((Object)null, (Object)null));
		assertFalse(ArrayUtils.equals(STRING_ARRAY, (Object)null));
		assertFalse(ArrayUtils.equals((Object)null, STRING_ARRAY));
		assertFalse(ArrayUtils.equals(null, STRING_ARRAY));
		assertFalse(ArrayUtils.equals(new String[] {"a"}, STRING_ARRAY));
		assertFalse(ArrayUtils.equals(STRING_ARRAY, null));
		
		
		assertFalse(ArrayUtils.equals(STRING_ARRAY, new String[] {"a","c","b"}));
		
		assertTrue(ArrayUtils.equalsIgnoreOrder(STRING_ARRAY, new String[] {"a","c","b"}));
		assertTrue(ArrayUtils.equalsIgnoreOrder(null, null));
		assertFalse(ArrayUtils.equalsIgnoreOrder(null, new String[] {"a","c","b"}));
		assertFalse(ArrayUtils.equalsIgnoreOrder(STRING_ARRAY, null));
		assertFalse(ArrayUtils.equalsIgnoreOrder(STRING_ARRAY, new String[] {"a","c"}));
		assertFalse(ArrayUtils.equalsIgnoreOrder(STRING_ARRAY, new String[] {"a","c","d"}));
		
		assertTrue(ArrayUtils.equalsIgnoreOrder(INT_ARRAY, new int[] {3,2,1}));
		
		assertFalse(ArrayUtils.fastContains((String[])null, "1"));
		assertFalse(ArrayUtils.fastContains(STRING_ARRAY, "1"));
		assertTrue(ArrayUtils.fastContains(STRING_ARRAY, "b"));
		
		assertFalse(ArrayUtils.fastContains((Collection<String>)null, "1"));
		assertFalse(ArrayUtils.fastContains(Arrays.asList(STRING_ARRAY), "1"));
		assertTrue(ArrayUtils.fastContains(Arrays.asList(STRING_ARRAY), "b"));
		
		assertFalse(ArrayUtils.fastContainsAny((String[])null, new String[]{"1","2"}));
		assertFalse(ArrayUtils.fastContainsAny(STRING_ARRAY, new String[]{"1","2"}));
		assertTrue(ArrayUtils.fastContainsAny(STRING_ARRAY, new String[] {"1","a"}));
		
		assertFalse(ArrayUtils.fastContainsAny((Collection<String>)null, new String[]{"1","2"}));
		assertFalse(ArrayUtils.fastContainsAny(Arrays.asList(STRING_ARRAY), new String[]{"1","2"}));
		assertTrue(ArrayUtils.fastContainsAny(Arrays.asList(STRING_ARRAY), new String[] {"1","a"}));
		
		assertEquals(3,ArrayUtils.get(INT_ARRAY, -1));
		assertEquals(3,ArrayUtils.get(INT_ARRAY, 2));
		
		assertEquals(-1,ArrayUtils.indexOf((byte[])null, (byte)0x01));
		assertEquals(0,ArrayUtils.indexOf(BYTE_ARRAY, (byte)0x01));
		assertEquals(-1,ArrayUtils.indexOf(BYTE_ARRAY, (byte)0x05));
		assertEquals(-1, ArrayUtils.indexOf(BYTE_ARRAY, (byte)0x01, 1));
		assertEquals(1, ArrayUtils.indexOf(BYTE_ARRAY, (byte)0x02, 1));
		assertEquals(0 ,ArrayUtils.indexOf(BYTE_ARRAY, (byte)0x01),-1);
		assertEquals(-1, ArrayUtils.indexOf(BYTE_ARRAY, (byte) 0x05, -1));
		
		
		assertEquals(0,ArrayUtils.indexOf(SHORT_ARRAY, (short)1));
		assertEquals(-1,ArrayUtils.indexOf(SHORT_ARRAY, (short)4));
		assertEquals(-1, ArrayUtils.indexOf(SHORT_ARRAY, (short) 1, 1));
		assertEquals(1, ArrayUtils.indexOf(SHORT_ARRAY, (short) 2, 1));
		assertEquals(-1, ArrayUtils.indexOf((short[])null, (short) 1, 1));
		assertEquals(0, ArrayUtils.indexOf(SHORT_ARRAY, (short) 1, -1));
		
		
		assertEquals(-1,ArrayUtils.indexOf(null, "a"));
		assertEquals(0,ArrayUtils.indexOf(STRING_ARRAY, "a"));
		assertEquals(-1,ArrayUtils.indexOf(STRING_ARRAY, "d"));
		assertEquals(-1, ArrayUtils.indexOf(STRING_ARRAY, "a", 1));
		assertEquals(1, ArrayUtils.indexOf(new String[] {"a",null}, null, 0));
		
		assertEquals(1, ArrayUtils.indexOf(STRING_ARRAY, "b", 1));
		assertEquals(1, ArrayUtils.indexOf(STRING_ARRAY, "b", -1));
		assertEquals(-1, ArrayUtils.indexOf(STRING_ARRAY, null, -1));
		
		
		Object[] objs=ArrayUtils.intersect(STRING_ARRAY, new String[] {"a","c","d"});
		assertEquals(2, objs.length);
		
		assertFalse(ArrayUtils.isEmpty(INT_ARRAY));
		assertFalse(ArrayUtils.isEmpty(SHORT_ARRAY));
		assertFalse(ArrayUtils.isEmpty(LONG_ARRAY));
		assertFalse(ArrayUtils.isEmpty(DOUBLE_ARRAY));
		assertFalse(ArrayUtils.isEmpty(FLOAT_ARRAY));
		assertFalse(ArrayUtils.isEmpty(BOOLEAN_ARRAY));
		assertFalse(ArrayUtils.isEmpty(BYTE_ARRAY));
		assertFalse(ArrayUtils.isEmpty(CHAR_ARRAY));
		assertFalse(ArrayUtils.isEmpty(STRING_ARRAY));
		assertTrue(ArrayUtils.isEmpty(new int[0]));
		assertTrue(ArrayUtils.isEmpty(new short[0]));
		assertTrue(ArrayUtils.isEmpty(new long[0]));
		assertTrue(ArrayUtils.isEmpty(new double[0]));
		assertTrue(ArrayUtils.isEmpty(new float[0]));
		assertTrue(ArrayUtils.isEmpty(new boolean[0]));
		assertTrue(ArrayUtils.isEmpty(new byte[0]));
		assertTrue(ArrayUtils.isEmpty(new char[0]));
		assertTrue(ArrayUtils.isEmpty(new String[0]));
		
		assertTrue(ArrayUtils.isEmpty((int[])null));
		assertTrue(ArrayUtils.isEmpty((short[])null));
		assertTrue(ArrayUtils.isEmpty((long[])null));
		assertTrue(ArrayUtils.isEmpty((double[])null));
		assertTrue(ArrayUtils.isEmpty((float[])null));
		assertTrue(ArrayUtils.isEmpty((boolean[])null));
		assertTrue(ArrayUtils.isEmpty((byte[])null));
		assertTrue(ArrayUtils.isEmpty((char[])null));
		assertTrue(ArrayUtils.isEmpty((String[])null));
		
		
		assertFalse(ArrayUtils.isIndexValid(INT_ARRAY, 3));
		assertFalse(ArrayUtils.isIndexValid(INT_ARRAY, -4));
		assertTrue(ArrayUtils.isIndexValid(INT_ARRAY, 2));
		assertTrue(ArrayUtils.isIndexValid(INT_ARRAY, -3));
		assertTrue(ArrayUtils.isIndexValid(INT_ARRAY, 0));
		
		
		objs = ArrayUtils.union(STRING_ARRAY, new String[] {"a","d"});
		System.out.println(Arrays.toString(objs));
		assertEquals(4, objs.length);
		
		strings = ArrayUtils.merge(STRING_ARRAY, new String[] {"a","d"});
		assertEquals(4, strings.length);
		
		strings= ArrayUtils.minus(STRING_ARRAY, new String[] {"a","d"});
		assertEquals(2, strings.length);
		
		assertNull(ArrayUtils.minus(null, new String[] {"a","d"}));
		
		strings=ArrayUtils.removeDups(new String[] {"a","b","a"});
		assertEquals(2, strings.length);
		
		strings=ArrayUtils.removeDups(new String[] {"a","b","c"});
		assertEquals(3, strings.length);
		
		strings=ArrayUtils.removeNull(new String[] {null, null});
		assertEquals(0, strings.length);
		
		strings=ArrayUtils.removeNull(new String[] {"a", null});
		assertEquals(1, strings.length);
		
		
		ArrayUtils.set(INT_ARRAY, -1, 4);
		assertEquals(4, INT_ARRAY[2]);
		ArrayUtils.set(INT_ARRAY, 2, 3);
		assertEquals(3, INT_ARRAY[2]);
		
		Object arrs=ArrayUtils.setValueAndExpandArray(INT_ARRAY, 3, 10);
		assertEquals(4, Array.getLength(arrs));
		arrs=ArrayUtils.setValueAndExpandArray(INT_ARRAY, -4, 10);
		assertEquals(4, Array.getLength(arrs));
		
		assertEquals(3,ArrayUtils.stream(DOUBLE_ARRAY).toArray().clone().length);
		assertEquals(3,ArrayUtils.stream(LONG_ARRAY).toArray().length);
		assertEquals(3,ArrayUtils.stream(INT_ARRAY).toArray().length);

		bytes=ArrayUtils.subArray(BYTE_ARRAY, 2);
		assertEquals(2,bytes.length);
		bytes=ArrayUtils.subArray(BYTE_ARRAY, 4);
		assertEquals(3,bytes.length);
		bytes=ArrayUtils.subArray(BYTE_ARRAY, 1,2);
		assertEquals(1,bytes.length);
		chars=ArrayUtils.subArray(CHAR_ARRAY, 2);
		assertEquals(2,chars.length);
		chars=ArrayUtils.subArray(CHAR_ARRAY, 4);
		assertEquals(3,chars.length);
		
		strings=ArrayUtils.subArray(STRING_ARRAY, 1,2);
		assertEquals(1,strings.length);
		
		
		bytes=ArrayUtils.subArray(BYTE_ARRAY, 3);
		assertEquals(3,bytes.length);
		assertNull(ArrayUtils.subArray((byte[])null, 1,3));
		assertArrayEquals(new byte[] {0x01,0x02,0x03},ArrayUtils.subArray(BYTE_ARRAY, -1,4));
		assertArrayEquals(new byte[0],ArrayUtils.subArray(BYTE_ARRAY, 2,1));
		
		
		
		chars=ArrayUtils.subArray(CHAR_ARRAY, 3);
		assertEquals(3,chars.length);
		
		strings=ArrayUtils.subArray(STRING_ARRAY, 1,3);
		assertEquals(2,strings.length);
		
		assertNull(ArrayUtils.subArray((String[])null, -1,3));
		assertArrayEquals(new String[] {"a","b","c"},ArrayUtils.subArray(STRING_ARRAY, -1,4));
		assertArrayEquals(new String[] {},ArrayUtils.subArray(STRING_ARRAY, 3,2));
		//ArrayUtils.
		
		StringTokenizer st=new StringTokenizer("a b c");
		objs  = ArrayUtils.toArray(st,Object.class);
		assertEquals(3, objs.length);
		
		int index=0;
		for(@SuppressWarnings("unused") Character c:ArrayUtils.toIterable("sdfsdsdd")) {
			index++;
		}
		assertEquals(8, index);
		
		
		objs=ArrayUtils.toObject(INT_ARRAY, Object.class);
		assertEquals(3, objs.length);
		
		objs=ArrayUtils.toObject(STRING_ARRAY, String.class);
		assertEquals(3, objs.length);
		
		{
			objs=ArrayUtils.toWrapped(SHORT_ARRAY);
			short[] array=(short[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(INT_ARRAY);
			int[] array=(int[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(LONG_ARRAY);
			long[] array=(long[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(DOUBLE_ARRAY);
			double[] array=(double[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(FLOAT_ARRAY);
			float[] array=(float[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(BYTE_ARRAY);
			byte[] array=(byte[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(CHAR_ARRAY);
			char[] array=(char[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(BOOLEAN_ARRAY);
			boolean[] array=(boolean[])ArrayUtils.toPrimitive(objs);
			assertEquals(3, array.length);			
		}
		{
			objs=ArrayUtils.toWrapped(STRING_ARRAY);
			assertEquals(3, objs.length);		
			
		}
		String[] stringArray=ArrayUtils.toStringArray(Arrays.asList("a","c"));
		assertEquals(2,stringArray.length);
		
		
		objs=ArrayUtils.xor(Arrays.asList(1,2,3,4).toArray(), Arrays.asList(2,3,4,5).toArray());
		assertArrayEquals(new Object[] {1,5}, objs);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testException1() {
		Iterator<Character> iter=ArrayUtils.toIterable("abc").iterator();
		iter.next();
		iter.remove();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testToPrimitive() {
		ArrayUtils.toPrimitive(new String[] {"a"});
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testObjectEquals() {
		ArrayUtils.equals(new Object(), new Object());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testObjectEquals2() {
		ArrayUtils.equals(STRING_ARRAY, new Object());
	}
	
}
