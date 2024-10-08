/*
 * querydsl-sql-extension - Copyright 2017-2024 Joey (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 数组工具
 *
 * @author Administrator
 */
public class ArrayUtils {

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/**
	 *   数组对象遍历执行toString方法，获得String数组对象。
	 *
	 *   @param list source list.
	 *   @return String数组
	 */
	public static String[] toStringArray(List<? extends Object> list) {
		List<String> result = new ArrayList<String>();
		for (Object f : list) {
			result.add(f.toString());
		}
		return result.toArray(new String[list.size()]);
	}

	/**
	 *  @param source source list
	 *  @param function function.
	 *  @return 将数组进行转换
	 *  @param <K> The type of target object.
	 *  @param <V> The type of target object.
	 */
	public static <K, V> List<V> convert(K[] source, Function<K, V> function) {
		List<V> result = new ArrayList<V>(source.length);
		for (int i = 0; i < source.length; i++) {
			result.add(function.apply(source[i]));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Enumeration<T> e, Class<T> type) {
		List<T> result = new ArrayList<T>();
		for (; e.hasMoreElements(); ) {
			result.add(e.nextElement());
		}
		return result.toArray((T[]) Array.newInstance(type, result.size()));
	}

	/**
	 *   这样就可以对CharBuffer,StringBuilder,Stringbuffer等对象进行Iterator模式的遍历了。
	 *   @param e CharSequence
	 *   @return  将CharSequence变为可遍历的char对象
	 */
	public static Iterable<Character> toIterable(final CharSequence e) {
		return new Iterable<Character>() {

			public Iterator<Character> iterator() {
				return new Iterator<Character>() {

					int n = 0;

					public boolean hasNext() {
						return n < e.length();
					}

					public Character next() {
						return e.charAt(n++);
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 *  @param from from
	 *  @param to to
	 *  @return 将一个子类的数组转换为父类的数组
	 *  @param <S> The type of target object.
	 *  @param <T> The type of target object.
	 */
	@SuppressWarnings("unchecked")
	public static <S, T> T[] cast(S[] from, Class<T> to) {
		T[] result = (T[]) Array.newInstance(to, from.length);
		for (int i = 0; i < result.length; i++) {
			result[i] = (T) from[i];
		}
		return result;
	}

	/**
	 *   @param <T> type
	 *   @param arr1 array
	 *   @return 从数组中移除null的元素
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeNull(T[] arr1) {
		List<T> list = new ArrayList<T>(arr1.length);
		for (T e : arr1) {
			if (e != null) {
				list.add(e);
			}
		}
		if (list.size() == arr1.length)
			return arr1;
		// 此处不能使用 list.toArray(arr1);
		// 因为ArrayList.toArray[]的实现是将List元素拷贝到给出的容器中，如果容器大于List的空间，则在超出部分补上null.
		// 因此不能起到消除null元素的作用。
		T[] t = (T[]) Array.newInstance(arr1.getClass().getComponentType(), list.size());
		return list.toArray(t);
	}

	/**
	 *  @param obj obj
	 *  @return 将原生八类型的数组容器转换为对象八类型数组
	 */
	public static Object[] toWrapped(Object obj) {
		Class<?> c = obj.getClass();
		Assert.isTrue(c.isArray());
		Class<?> priType = c.getComponentType();
		if (!priType.isPrimitive())
			return (Object[]) obj;
		
		String s = priType.getName();
		int len=Array.getLength(obj);
		//进行转换
		Object[] result;
		int index=0;
		switch (s.length() + s.charAt(0)) {
		case 108://int
			result = new Integer[len];
			for (int e : (int[]) obj) {
				result[index++] = Integer.valueOf(e);
			}
			break;
		case 112://long
			result = new Long[len];
			for (long e : (long[]) obj) {
				result[index++] = Long.valueOf(e);
			}
			break;
		case 106:
			result = new Double[len];
			for (double e : (double[]) obj) {
				result[index++] = Double.valueOf(e);
			}
			break;
		case 105:
			result=new Boolean[len];
			for(boolean e:(boolean[])obj) {
				result[index++]=Boolean.valueOf(e);
			}
			break;
		case 107:
			result=new Float[len];
			for(float e:(float[])obj) {
				result[index++]=Float.valueOf(e);
			}
			break;
		case 103:
			result=new Character[len];
			for(char e:(char[])obj) {
				result[index++]=Character.valueOf(e);
			}
			break;
		case 102:
			result=new Byte[len];
			for(byte e:(byte[])obj) {
				result[index++]=Byte.valueOf(e);
			}
			break;
		default:
			result=new Short[len];
			for(short e:(short[])obj) {
				result[index++]=Short.valueOf(e);
			}
		}
		return result;
	}

	/**
	 *  @param obj obj
	 *  @param containerType containerType
	 *  @return 将数组转换为指定类型的数组容器
	 *  @param <T> The type of target object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toObject(Object obj, Class<T> containerType) {
		Class<?> c = obj.getClass();
		Assert.isTrue(c.isArray());
		Class<?> priType = c.getComponentType();
		if (priType == containerType) {
			return (T[]) obj;
		}
		return cast(toWrapped(obj), containerType);
	}

	/**
	 *  将由非原生八类型的数组转换为原生八类型的数组
	 *  @param obj obj
	 *  @return Object
	 */
	public static Object toPrimitive(Object[] obj) {
		Class<?> c = obj.getClass();
		Assert.isTrue(c.isArray());
		Class<?> objType = c.getComponentType();
		Object result;
		int len=obj.length;
		int index=0;
		if (objType == Boolean.class) {
			boolean[] bools = new boolean[len];
			result = bools;
			for (Boolean b : (Boolean[]) obj) {
				bools[index++] = Primitives.unbox(b, false);
			}
		} else if (objType == Byte.class) {
			byte[] bools = new byte[len];
			result = bools;
			for (Byte b : (Byte[]) obj) {
				bools[index++] = Primitives.unbox(b,(byte)0);
			}
		} else if (objType == Character.class) {
			char[] bools = new char[len];
			result = bools;
			for (Character b : (Character[]) obj) {
				bools[index++] = Primitives.unbox(b,(char)0);
			}
		} else if (objType == Integer.class) {
			int[] bools = new int[len];
			result = bools;
			for (Integer b : (Integer[]) obj) {
				bools[index++] = Primitives.unbox(b,0);
			}
		} else if (objType == Long.class) {
			long[] bools = new long[len];
			result = bools;
			for (Long b : (Long[]) obj) {
				bools[index++] = Primitives.unbox(b,0L);
			}
		} else if (objType == Float.class) {
			float[] bools = new float[len];
			result = bools;
			for (Float b : (Float[]) obj) {
				bools[index++] = Primitives.unbox(b,0f);
			}
		} else if (objType == Double.class) {
			double[] bools = new double[len];
			result = bools;
			for (Double b : (Double[]) obj) {
				bools[index++] = Primitives.unbox(b,0d);
			}
		} else if (objType == Short.class) {
			short[] bools = new short[len];
			result = bools;
			for (Short b : (Short[]) obj) {
				bools[index++] = Primitives.unbox(b,(short)0);
			}
		} else {
			throw new IllegalArgumentException(c+" is not a primitive type.");
		}
		return result;
	}

	/**
	 *  算法效率不高，仅限于少量元素合并。
	 *  @param array1 array1
	 *  @param array2 array2
	 *  @return 合并两个数组，消除重复的元素。
	 *  @param <T> The type of target object.
	 */
	public static <T> T[] merge(T[] array1, T[] array2) {
		List<T> list = new ArrayList<T>();
		for (T str : array1) {
			list.add(str);
		}
		for (T str : array2) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		return list.toArray(array1);
	}

	/**
	 * 去掉重复数据，过滤重复数据有两个算法， 1是简单算法（即遍历查找重复），例如 List&lt;&gt; list=new ArrayList&lt;&gt;();
	 * for (T obj : array) { if (!list.contains(obj)) list.add(obj); } return
	 * list.toArray();
	 * 2是复杂算法，使用hashset查找重复 Set&lt;&gt; set=new LinkedHashSet&lt;&gt;();
	 * //因为前一算法保证了元素的顺序，为对相同功能比较，此处用linkedhashset for (T obj : array) {
	 * set.add(obj); } return set.toArray();
	 * 根据测试当元素数量少于50个时，前一算法要比后一算法快。当元素数量激增时，后者要更快 后者——更通用 前者——极限情况下针对特定场景的优化。
	 * @param array array
	 * @return 去掉重复数据
	 * @param <T> The type of target object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeDups(T[] array) {
		List<T> list = new ArrayList<T>();
		for (T obj : array) {
			if (!list.contains(obj))
				list.add(obj);
		}
		if (list.size() == array.length)
			return array;
		return list.toArray((T[]) Array.newInstance(array.getClass().getComponentType(), list.size()));
	}

	/**
	 *  @param otherContains otherContains
	 *  @param formats formats
	 *  @return 包含任意一个元素
	 *  @param <T> The type of target object.
	 */
	public static <T> boolean containsAny(T[] otherContains, T[] formats) {
		for (T obj1 : otherContains) {
			for (T obj2 : formats) {
				if (obj1.equals(obj2))
					return true;
			}
		}
		return false;
	}

	public static <T> boolean contains(T[] array, T obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (Objects.equals(array[i], obj)) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(long[] array, long obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(short[] array, short obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(char[] array, char obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(double[] array, double obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(float[] array, float obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(byte[] array, byte obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean contains(boolean[] array, boolean obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(int[] array, int obj) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 *  泛型的addArray
	 *  @param array array
	 *  @param data data
	 *  @param componentType componentType
	 *  @return result
	 *  @param <T> The type of target object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] addElement(T[] array, T data, Class<T> componentType) {
		if (data == null)
			return array;
		T[] newArray;
		if (array == null) {
			Assert.notNull(componentType, "The componentType shoule be assigned when the array is null.");
			newArray = (T[]) Array.newInstance(componentType, 1);
			newArray[0] = data;
		} else {
			Class<?> containerType = array.getClass().getComponentType();
			if (!containerType.isAssignableFrom(data.getClass())) {
				// error.
				throw new ArrayStoreException("The new element which typed " + data.getClass().getName() + " can not be put into a array whoes type is " + containerType.getName());
			}
			newArray = (T[]) Array.newInstance(containerType, array.length + 1);
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[array.length] = data;
		}
		return newArray;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] addElement(T[] array, T element) {
		if (element == null)
			return array;
		return addElement(array, element, (Class<T>) element.getClass());
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] addAllElement(T[] array, T[] data) {
		if (data == null || data.length == 0)
			return array;
		T[] newArray;
		if (array == null) {
			return data;
		} else {
			newArray = (T[]) Array.newInstance(data.getClass().getComponentType(), array.length + data.length);
			System.arraycopy(array, 0, newArray, 0, array.length);
			System.arraycopy(data, 0, newArray, array.length, data.length);
		}
		return newArray;
	}

	/**
	 *  <p>Produces a new {@code byte} array containing the elements
	 *  between the start and end indices.
	 *
	 *  <p>The start index is inclusive, the end index exclusive.
	 *  Null array input produces null output.
	 *
	 *  @param array  the array
	 *  @param startIndexInclusive  the starting index. Undervalue (&lt;0)
	 *       is promoted to 0, overvalue (&gt;array.length) results
	 *       in an empty array.
	 *  @param endIndexExclusive  elements up to endIndex-1 are present in the
	 *       returned subarray. Undervalue (&lt; startIndex) produces
	 *       empty array, overvalue (&gt;array.length) is demoted to
	 *       array length.
	 *  @return a new array containing the elements between
	 *       the start and end indices.
	 *  @since 2.1
	 *  @see Arrays#copyOfRange(byte[], int, int)
	 */
	public static byte[] subArray(final byte[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) {
			return null;
		}
		if (startIndexInclusive < 0) {
			startIndexInclusive = 0;
		}
		if (endIndexExclusive > array.length) {
			endIndexExclusive = array.length;
		}
		final int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) {
			return EMPTY_BYTE_ARRAY;
		}
		final byte[] subarray = new byte[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 *  获取子数组
	 *  @param array array
	 *  @param len 截取前n个元素
	 *  @return 子数组
	 */
	public static byte[] subArray(byte[] array, int len) {
		if (array.length == len) {
			return array;
		} else if (len > array.length) {
			len = array.length;
		}
		byte[] data = new byte[len];
		System.arraycopy(array, 0, data, 0, len);
		return data;
	}

	public static char[] subArray(char[] array, int len) {
		if (array.length == len) {
			return array;
		} else if (len > array.length) {
			len = array.length;
		}
		char[] data = new char[len];
		System.arraycopy(array, 0, data, 0, len);
		return data;
	}

	/**
	 *  泛型的subArray.如果使用非泛型的方法，a小写
	 *  @param <T> type of target
	 *  @param array array
	 *  @param startIndexInclusive startIndexInclusive
	 *  @param endIndexExclusive endIndexExclusive
	 *  @return 子数组
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T[] subArray(T[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) {
			return null;
		}
		if (startIndexInclusive < 0) {
			startIndexInclusive = 0;
		}
		if (endIndexExclusive > array.length) {
			endIndexExclusive = array.length;
		}
		int newSize = endIndexExclusive - startIndexInclusive;
		Class type = array.getClass().getComponentType();
		if (newSize <= 0) {
			return (T[]) Array.newInstance(type, 0);
		}
		T[] subarray = (T[]) Array.newInstance(type, newSize);
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 *  判断列表中是否包含指定的对象,和Collection.contains方法比起来，前者是用obj1.equals(obj2)，
	 *  这里用==直接判断是否<B>同一对象</B>，速度更快，但是不能比较出两个值完全相同的对象来。
	 *  @param <T> type of target
	 *  @param list list
	 *  @param obj obj
	 *  @return is contains
	 */
	public static <T> boolean fastContains(T[] list, T obj) {
		if (list != null){
			for (T e : list) {
				if (e == obj)
					return true;
			}			
		}
		return false;
	}

	/**
	 *  判断列表中是否包含指定的对象,和Collection.contains方法比起来，前者是用obj1.equals(obj2)，
	 *  这里用==直接判断是否<B>同一对象</B>，速度更快，但是不能比较出两个值完全相同的对象来。
	 *  @param <T> type of target
	 *  @param list list
	 *  @param obj obj
	 *  @return true if contains
	 */
	public static <T> boolean fastContains(Collection<T> list, T obj) {
		if(list!= null) {
			for (T e : list) {
				if (e == obj)
					return true;
			}	
		}
		return false;
	}

	/**
	 *  判断列表中是否包含指定的对象,和Collection.contains方法比起来，前者是用obj1.equals(obj2)，
	 *  这里用==直接判断是否<B>同一对象</B>，速度更快，但是不能比较出两个值完全相同的对象来。
	 *  @param list list
	 *  @param keys keys
	 *  @return true if contains.
	 *  @param <T> The type of target object.
	 */
	public static <T> boolean fastContainsAny(Collection<T> list, T[] keys) {
		if (list != null) {
			for (T e : list) {
				for (T obj : keys) {
					if (e == obj)
						return true;
				}
			}	
		}
		return false;
	}

	/**
	 *  判断列表中是否包含指定的对象,和Collection.contains方法比起来，前者是用obj1.equals(obj2)，
	 *  这里用==直接判断是否<B>同一对象</B>，速度更快，但是不能比较出两个值完全相同的对象来。
	 *  @param list list
	 *  @param keys keys
	 *  @return true if contains.
	 *  @param <T> The type of target object.
	 */
	public static <T> boolean fastContainsAny(T[] list, T[] keys) {
		if (list != null) {
			for (T e : list) {
				for (T obj : keys) {
					if (e == obj)
						return true;
				}
			}			
		}
		return false;
	}

	/**
	 *  计算是否 包含目标子串，忽略大小写
	 *  @param values values
	 *  @param str str
	 *  @return true if contains.
	 */
	public static boolean containsIgnoreCase(String[] values, String str) {
		if(values!=null) {
			for (String v : values) {
				if (v == null) {
					if (str == null) {
						return true;
					} else {
						continue;
					}
				}
				if (v.equalsIgnoreCase(str)) {
					return true;
				}
			}	
		}
		return false;
	}

	/**
	 *  @param obj obj
	 *  @param len len
	 *  @return 转换为指定长度的数组,超过则截断，不足则补null
	 */
	public static Object toFixLength(Object obj, int len) {
		int length = Array.getLength(obj);
		if (length == len)
			return obj;
		Object result = Array.newInstance(obj.getClass().getComponentType(), len);
		System.arraycopy(obj, 0, result, 0, Math.min(length, len));
		return result;
	}

	/*
	 * @since JDK 1.6
	 */
	public static final byte[] copyOf(byte[] original, int newLength) {
		byte[] copy = new byte[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	/*
	 * @since JDK 1.6
	 */
	public static final char[] copyOf(char[] original, int newLength) {
		char[] copy = new char[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	/*
	 * @since JDK 1.6
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[] copyOf(T[] original, int newLength) {
		return (T[]) copyOf(original, newLength, original.getClass());
	}

	/*
	 * @since JDK 1.6
	 */
	@SuppressWarnings("unchecked")
	public static final <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
		T[] copy = ((Object) newType == (Object) Object[].class) ? (T[]) new Object[newLength] : (T[]) Array.newInstance(newType.getComponentType(), newLength);
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	/**
	 *  判断两个数组，忽略其顺序
	 *  @param a1 a1
	 *  @param a2 a2
	 *  @return boolean
	 *  @param <T> The type of target object.
	 */
	public static <T> boolean equalsIgnoreOrder(T[] a1, T[] a2) {
		if (a1 == null && a2 == null)
			return true;
		if (a1 == null || a2 == null)
			return false;
		if (a1.length != a2.length)
			return false;
		for (int n = 0; n < a1.length; n++) {
			T o = a1[n];
			if (!contains(a2, o)) {
				return false;
			}
		}
		return true;
	}

	/**
	 *  判断两个数组的元素是否相等。忽略其元素的顺序
	 *  @param array1 array1
	 *  @param array2 array2
	 *  @return true if equals
	 */
	public static boolean equalsIgnoreOrder(Object array1, Object array2) {
		Object[] obj1 = toWrapped(array1);
		Object[] obj2 = toWrapped(array2);
		HashSet<Object> set1 = new HashSet<Object>(Arrays.asList(obj1));
		HashSet<Object> set2 = new HashSet<Object>(Arrays.asList(obj2));
		return set1.equals(set2);
	}


	/*
	 * 两个对象数组的比较
	 */
	public static boolean equals(Object[] a1, Object[] a2) {
		if (a1 == null && a2 == null)
			return true;
		if (a1 == null || a2 == null)
			return false;
		if (a1.length != a2.length)
			return false;
		for (int n = 0; n < a1.length; n++) {
			if (!Objects.equals(a1[n], a2[n])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 *  判断两个数组的元素和顺序是否相等。
	 *  由于java.util.Arrays中提供的数组元素比较都是在已知数组类型的前提下的，而数组可以归结为9种类型，当不确定数组类型时，可以用此方法。
	 *  @param a1 a1
	 *             Object，必须是数组
	 *  @param a2 a2
	 *             Object,必须是数组
	 *  @return true if equals
	 *  @see java.util.Arrays#equals(boolean[], boolean[])
	 *  @see java.util.Arrays#equals(byte[], byte[])
	 *  @see java.util.Arrays#equals(char[], char[])
	 *  @see java.util.Arrays#equals(double[], double[])
	 *  @see java.util.Arrays#equals(float[], float[])
	 *  @see java.util.Arrays#equals(int[], int[])
	 *  @see java.util.Arrays#equals(long[], long[])
	 *  @see java.util.Arrays#equals(short[], short[])
	 *  @see java.util.Arrays#equals(Object[], Object[])
	 *  @throws IllegalArgumentException If encounter IllegalArgumentException
	 *              如果输入对象不是数组，抛出
	 */
	public static boolean equals(Object a1, Object a2) {
		if (a1 == a2)
			return true;
		if (a1 == null || a2 == null)
			return false;
		Class<?> clz1 = a1.getClass();
		Class<?> clz2 = a2.getClass();
		if (!clz1.isArray() || !clz2.isArray()) {
			throw new IllegalArgumentException("must comapre between two Array.");
		}
		clz1 = clz1.getComponentType();
		clz2 = clz2.getComponentType();
		if (clz1.isPrimitive() != clz2.isPrimitive()) {
			return false;
		}
		if (clz1 == int.class) {
			return Arrays.equals((int[]) a1, (int[]) a2);
		} else if (clz1 == short.class) {
			return Arrays.equals((short[]) a1, (short[]) a2);
		} else if (clz1 == long.class) {
			return Arrays.equals((long[]) a1, (long[]) a2);
		} else if (clz1 == float.class) {
			return Arrays.equals((float[]) a1, (float[]) a2);
		} else if (clz1 == double.class) {
			return Arrays.equals((double[]) a1, (double[]) a2);
		} else if (clz1 == boolean.class) {
			return Arrays.equals((boolean[]) a1, (boolean[]) a2);
		} else if (clz1 == byte.class) {
			return Arrays.equals((byte[]) a1, (byte[]) a2);
		} else if (clz1 == char.class) {
			return Arrays.equals((char[]) a1, (char[]) a2);
		} else {
			return Arrays.equals((Object[]) a1, (Object[]) a2);
		}
	}

	/**
	 *  操作未知类型的数组:set 当序号为负数时，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 *  和set方法的区别在于，此方法如果发现数组大小不够，会自动扩大数组。
	 *  @param obj obj
	 *  @param index index
	 *  @param value value
	 *  @return 操作未知类型的数组
	 */
	public static Object setValueAndExpandArray(Object obj, int index, Object value) {
		int length = Array.getLength(obj);
		Object result = obj;
		if (index < 0 && index + length >= 0) {
			index += length;
		} else if (index < 0) {
			// 需要扩张
			result = toFixLength(obj, -index);
		} else if (index >= length) {
			// 扩张
			result = toFixLength(obj, index + 1);
		}
		set(result, index, value);
		return result;
	}

	/**
	 *  @param obj obj
	 *  @param index index
	 *  @return 检测索引是否有效 当序号为负数时，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 */
	public static boolean isIndexValid(Object obj, int index) {
		int length = Array.getLength(obj);
		if (index < 0)
			index += length;
		return index >= 0 && index < length;
	}

	/*
	 * 操作未知类型的数组:get
	 * 
	 * @param obj
	 *            数组对象
	 * @param index
	 *            序号 当序号为负数时，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 */
	public static final Object get(Object obj, int index) {
		if (index >= 0) {
			return Array.get(obj, index);
		} else {
			return Array.get(obj, Array.getLength(obj) + index);
		}
	}

	/*
	 * 操作未知类型的数组:set
	 * 
	 * @param: index
	 *             当序号为负数时，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 */
	public static final void set(Object obj, int index, Object value) {
		if (index >= 0) {
			Array.set(obj, index, value);
		} else {
			Array.set(obj, Array.getLength(obj) + index, value);
		}
	}

	/**
	 *  取交集
	 *  @param ls ls
	 *  @param ls2 ls2
	 *  @return 交集
	 */
	public static Object[] intersect(Object[] ls, Object[] ls2) {
		HashSet<Object> set = new HashSet<Object>(Arrays.asList(ls));
		set.retainAll(Arrays.asList(ls2));
		return set.toArray();
	}

	/**
	 *  取并集
	 *  @param ls ls
	 *  @param ls2 ls2
	 *  @return 并集
	 */
	public static <T> T[] union(T[] ls, T[] ls2) {
		HashSet<Object> set = new HashSet<>(Arrays.asList(ls));
		for (Object o : ls2) {
			set.add(o);
		}
		@SuppressWarnings("unchecked")
		T[] empty=(T[])Array.newInstance(ls.getClass().getComponentType(), 0);
		return set.toArray(empty);
	}

	/**
	 *  取差集(即包含在ls，但不包含在ls2中的元素) 可以理解为集合ls减去集合ls2
	 *  @param ls ls
	 *  @param ls2 ls2
	 *  @return 差集
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] minus(T[] ls, T[] ls2) {
		if(ls==null) {
			return null;
		}
		HashSet<Object> set = new HashSet<Object>(Arrays.asList(ls));
		set.removeAll(Arrays.asList(ls2));
		Class<?> type=ls.getClass().getComponentType();
		return set.toArray((T[]) Array.newInstance(type, 0));
	}

	/**
	 *  取两个集合，各自没有被对方包含的部分的集合。 即从 并集中挖去交集。
	 *  @param ls ls
	 *  @param ls2 ls2
	 *  @return 亦或集
	 */
	public static Object[] xor(Object[] ls, Object[] ls2) {
		// 计算全集
		Set<Object> setAll = new HashSet<Object>(Arrays.asList(ls));
		for (Object o : ls2) {
			setAll.add(o);
		}
		// 交集
		HashSet<Object> setInter = new HashSet<Object>(Arrays.asList(ls));
		setInter.retainAll(Arrays.asList(ls2));
		// 取差
		setAll.removeAll(setInter);
		return setAll.toArray();
	}

	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	/**
	 *  The index value when an element is not found in a list or array: {@code -1}.
	 *  This value is returned by methods in this class and can also be used in comparisons with values returned by
	 *  various method from {@link java.util.List}.
	 */
	public static final int INDEX_NOT_FOUND = -1;

	// byte IndexOf
	// -----------------------------------------------------------------------
	/**
	 *  <p>Finds the index of the given value in the array.
	 *
	 *  <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 *  @param array  the array to search through for the object, may be {@code null}
	 *  @param valueToFind  the value to find
	 *  @return the index of the value within the array,
	 *   {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(final byte[] array, final byte valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 *  <p>Finds the index of the given value in the array starting at the given index.
	 *
	 *  <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 *  <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 *  length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 *  @param array  the array to search through for the object, may be {@code null}
	 *  @param valueToFind  the value to find
	 *  @param startIndex  the index to start searching at
	 *  @return the index of the value within the array,
	 *   {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(final byte[] array, final byte valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	// short IndexOf
	// -----------------------------------------------------------------------
	/**
	 *  <p>Finds the index of the given value in the array.
	 *
	 *  <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 *  @param array  the array to search through for the object, may be {@code null}
	 *  @param valueToFind  the value to find
	 *  @return the index of the value within the array,
	 *   {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(final short[] array, final short valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 *  <p>Finds the index of the given value in the array starting at the given index.
	 *
	 *  <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 *  <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 *  length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 *  @param array  the array to search through for the object, may be {@code null}
	 *  @param valueToFind  the value to find
	 *  @param startIndex  the index to start searching at
	 *  @return the index of the value within the array,
	 *   {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(final short[] array, final short valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	/**
	 *  <p>Finds the index of the given object in the array.
	 *
	 *  <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 *  @param array  the array to search through for the object, may be {@code null}
	 *  @param objectToFind  the object to find, may be {@code null}
	 *  @return the index of the object within the array,
	 *   {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(final Object[] array, final Object objectToFind) {
		return indexOf(array, objectToFind, 0);
	}

	/**
	 *  <p>Finds the index of the given object in the array starting at the given index.
	 *
	 *  <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 *  <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 *  length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 *  @param array  the array to search through for the object, may be {@code null}
	 *  @param objectToFind  the object to find, may be {@code null}
	 *  @param startIndex  the index to start searching at
	 *  @return the index of the object within the array starting at the index,
	 *   {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (objectToFind == null) {
			for (int i = startIndex; i < array.length; i++) {
				if (array[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = startIndex; i < array.length; i++) {
				if (objectToFind.equals(array[i])) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean isEmpty(boolean[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(char[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(byte[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(short[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(int[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(long[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(float[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(double[] a) {
		return a == null || a.length == 0;
	}

	public static Stream<Integer> stream(int[] array) {
		return StreamSupport.stream(Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
	}

	public static Stream<Long> stream(long[] array) {
		return StreamSupport.stream(Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
	}

	public static Stream<Double> stream(double[] array) {
		return StreamSupport.stream(Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
	}
}
