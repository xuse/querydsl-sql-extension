/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 断言工具类，用于检查
 * @author Administrator
 *
 */
public class Assert {
	/**
	 * 断言对象不为null
	 * @param obj
	 * @param template 错误信息模板
	 * @param args 错误信息参数
	 * @return t
	 */
	public static <T> T nonNull(T obj, String template, Object... args) {
		if(obj==null) {
			throw Exceptions.illegalArgument(template, args);
		}
		return obj;
	}
	
	/**
	 * 断言对象不为null
	 * @param obj
	 */
	public static void notNull(Object obj) {
		if (obj == null)
			throw new NullPointerException("The input parameter must not be null!");
	}
	/**
	 *  断言对象不为null
	 * @param obj
	 * @param msg
	 */
	public static void notNull(Object obj, String msg) {
		if (obj == null)
			throw new NullPointerException(msg);
	}
	
	/**
	 * 仅为向下兼容保留
	 * @param obj
	 * @param msg
	 */
	@Deprecated
	public static void notNull(Object obj, String... msg) {
		if(msg.length==0)notNull(obj);
		notNull(obj,msg[0]);
	}

	/**
	 * 断言对象为null
	 * @param obj
	 */
	public static void isNull(Object obj, String msg) {
		if (obj != null)
			throw new NullPointerException(msg);
	}
	
	/**
	 * 断言对象为null
	 * @param obj
	 */
	public static void isNull(Object obj) {
		if (obj != null)
			throw new IllegalArgumentException("The input param must be null.");
	}

	/**
	 * 断言对象的class为c
	 * @param obj
	 * @param c
	 * @param msg
	 */
	public static void isType(Object obj, Class<?> c, String msg) {
		if (!(c.isAssignableFrom(obj.getClass())))
			throw new ClassCastException(msg);
	}

	/**
	 * 断言对象的class为c
	 * @param obj
	 * @param c
	 */
	public static void isType(Object obj, Class<?> c) {
		if (!(c.isAssignableFrom(obj.getClass())))
			throw new ClassCastException();
	}

	/**
	 * 断言为假
	 * @param obj
	 */
	public static void isFalse(Boolean obj) {
		if (obj.equals(Boolean.TRUE))
			throw new IllegalArgumentException();
	}

	/**
	 * 断言为假
	 * @param obj
	 * @param string
	 */
	public static void isFalse(Boolean obj, String string) {
		if (obj.equals(Boolean.TRUE))
			throw new IllegalArgumentException(string);
	}

	/**
	 * 断言为真
	 * @param obj
	 */
	public static void isTrue(boolean obj) {
		if(!obj){
			throw new IllegalArgumentException();
		}
	}
	
	
	/**
	 * 断言为真
	 * @param obj
	 */
	public static void isTrue(Boolean obj) {
		if (obj.equals(Boolean.FALSE))
			throw new IllegalArgumentException();
	}

	/**
	 * 断言为真
	 * @param obj
	 * @param string
	 */
	public static void isTrue(Boolean obj, String string) {
		if (obj.equals(Boolean.FALSE))
			throw new IllegalArgumentException(string);
	}

	/**
	 * 断言两个对象相等
	 * @param obj1
	 * @param obj2
	 */
	public static void equals(Object obj1, Object obj2) {
		if (!Objects.equals(obj1, obj2))
			throw new RuntimeException("Expect "+obj1+" but get "+ obj2);
	}

	/**
	 * 断言两个对象相等
	 * @param obj1
	 * @param obj2
	 * @param string
	 */
	public static void equals(Object obj1, Object obj2, String string) {
		if (!Objects.equals(obj1, obj2))
			throw new RuntimeException(string);
	}
	
	/**
	 * 断言两个对象相等
	 * @param obj1
	 * @param obj2
	 */
	public static void notEquals(Object obj1, Object obj2) {
		if (Objects.equals(obj1, obj2))
			throw new IllegalArgumentException();
	}

	/**
	 * 断言两个对象相等
	 * @param obj1
	 * @param obj2
	 * @param string
	 */
	public static void notEquals(Object obj1, Object obj2, String string) {
		if (Objects.equals(obj1, obj2))
			throw new IllegalArgumentException(string);
	}

	/**
	 * 断言两个对象是同一个
	 * @param o1
	 * @param o2
	 * @param string
	 */
	public static void sameObject(Object o1, Object o2, String string) {
		if (o1 != o2)
			throw new IllegalArgumentException(string);
	}

	/**
	 * 断言数值在指定的范围之内
	 * @param length
	 * @param i
	 * @param j
	 * @param string
	 */
	public static void between(int length, int i, int j, String string) {
		if (length < i || length > j) {
			throw new RuntimeException(string);
		}
	}

	/**
	 * 断言文字是指定枚举的一个枚举项
	 * @param string
	 * @param name
	 * @param msg
	 */
	public static <T extends Enum<T>> void isEnumOf(Object string, Class<T> name, String msg) {
		try {
			Enum.valueOf(name, string.toString());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(msg);
		}
	}
	/**
	 * 断言文字是指定枚举的一个枚举项
	 * @param string
	 * @param name
	 */
	public static <T extends Enum<T>> void isEnumOf(Object string, Class<T> name) {
		Enum.valueOf(name, string.toString());
	}

	/**
	 * 断言对象被数组所包含
	 * @param value
	 * @param array
	 * @param msg
	 */
	public static <T> void isInArray(T value, T[] array, String msg) {
		if (!ArrayUtils.contains(array, value)) {
			throw new NoSuchElementException(msg);
		}
	}

	/**
	 * 断言对象被数组所包含
	 * @param value
	 * @param array
	 */
	public static <T> void isInArray(T value, T[] array) {
		if (!ArrayUtils.contains(array, value)) {
			throw new NoSuchElementException("Not found: " + value);
		}
	}

	/**
	 * 断言数值被数组所包含s
	 * @param value
	 * @param array
	 */
	public static void isInArray(int value, int[] array) {
		if (!ArrayUtils.contains(array, value)) {
			throw new NoSuchElementException("Not Found: " + value);
		}
	}

	/**
	 * 断言文件(不是目录)存在
	 * @param file
	 */
	public static void fileExist(File file) {
		if (!file.exists()) {
			throw new RuntimeException(new FileNotFoundException("File " + file.getAbsolutePath() + " is not exist."));
		} else if (file.isDirectory()) {
			throw new RuntimeException(new FileNotFoundException(file.getAbsolutePath() + " is a Directory."));
		}
	}
	
	/**
	 * 断言文件或目录存在
	 * @param file
	 */
	public static void exist(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException(new FileNotFoundException("File " + file.getAbsolutePath() + " is not exist."));
		}
	}

	/**
	 * 断言目录存在
	 * @param file
	 */
	public static void folderExist(File file) {
		if (!file.exists()) {
			throw new RuntimeException(new FileNotFoundException("File " + file.getAbsolutePath() + " is not exist."));
		} else if (file.isFile()) {
			throw new RuntimeException(new FileNotFoundException(file.getAbsolutePath() + " is a File."));
		}
	}

	/**
	 * 断言文本不为空
	 * @param text
	 */
	public static void isNotEmpty(String text) {
		if (text == null || text.length() == 0) {
			throw new IllegalArgumentException("String must not empty!");
		}
	}

	/**
	 * 断言文本不为空
	 * @param text
	 * @param msg
	 */
	public static void isNotEmpty(String text, String msg) {
		if (text == null || text.length() == 0) {
			throw new RuntimeException(msg);
		}
	}

	/**
	 * 断言文本为空
	 * @param text
	 */
	public static void hasLength(String text) {
		if (text==null || text.length()==0)
			throw new IllegalArgumentException();
	}

	/**
	 * 断言文本为空
	 * @param text
	 * @param msg
	 */
	public static void hasLength(String text, String msg) {
		if (text==null || text.length()==0)
			throw new IllegalArgumentException(msg);
	}
	
	public static void hasElements(Collection<?> collection) {
		if(collection==null || collection.isEmpty()) {
			throw new IllegalArgumentException();
		}
	}
	
	public static void hasElements(Map<?,?> collection) {
		if(collection==null || collection.isEmpty()) {
			throw new IllegalArgumentException();
		}
	}
	
	public static void hasElements(Collection<?> collection, String msg) {
		if(collection==null || collection.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void hasElements(Map<?,?> collection, String msg) {
		if(collection==null || collection.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}
	/**
	 * Assert that an array has elements; that is, it must not be
	 * <code>null</code> and must have at least one element.
	 * 
	 * <pre class="code">
	 * Assert.notEmpty(array, &quot;The array must have elements&quot;);
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object array is <code>null</code> or has no elements
	 */
	public static void notEmpty(Object[] array, String message) {
		if (ArrayUtils.isEmpty(array)) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that an array has elements; that is, it must not be
	 * <code>null</code> and must have at least one element.
	 * 
	 * <pre class="code">
	 * Assert.notEmpty(array);
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @throws IllegalArgumentException
	 *             if the object array is <code>null</code> or has no elements
	 */
	public static void notEmpty(Object[] array) {
		notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
	}

	/**
	 * Assert that an array has no null elements. Note: Does not complain if the
	 * array is empty!
	 * 检查数组，不允许数组中有null元素
	 * <pre class="code">
	 * Assert.noNullElements(array, &quot;The array must have non-null elements&quot;);
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object array contains a <code>null</code> element
	 */
	public static void noNullElements(Object[] array, String message) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null) {
					throw new IllegalArgumentException(message);
				}
			}
		}
	}

	/**
	 * Assert that an array has no null elements. Note: Does not complain if the
	 * array is empty!
	 * 检查数组，不允许数组中有null元素
	 * 
	 * <pre class="code">
	 * Assert.noNullElements(array);
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @throws IllegalArgumentException
	 *             if the object array contains a <code>null</code> element
	 */
	public static void noNullElements(Object[] array) {
		noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
	}
}
