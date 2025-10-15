package com.github.xuse.querydsl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 断言工具类，用于检查
 */
public class Assert {

	/**
	 *  断言对象不为null
	 *  @param obj obj
	 *  @param template 错误信息模板
	 *  @param args 错误信息参数
	 *  @return t
	 *  @param <T> The type of target object.
	 */
	public static <T> T nonNull(T obj, String template, Object... args) {
		if (obj == null) {
			throw Exceptions.illegalArgument(template, args);
		}
		return obj;
	}

	/**
	 *  断言对象不为null
	 *  @param obj obj
	 */
	public static Object notNull(Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("The input parameter must not be null!");
		return obj;
	}

	/**
	 *   断言对象不为null
	 *  @param obj obj
	 *  @param msg msg
	 */
	public static void notNull(Object obj, String msg) {
		if (obj == null)
			throw new IllegalArgumentException(msg);
	}

	/**
	 *  断言对象为null
	 *  @param obj obj
	 *  @param msg msg
	 */
	public static void isNull(Object obj, String msg) {
		if (obj != null)
			throw new IllegalArgumentException(msg);
	}

	/**
	 *  断言对象为null
	 *  @param obj obj
	 */
	public static void isNull(Object obj) {
		if (obj != null)
			throw new IllegalArgumentException("The input param must be null.");
	}

	/**
	 *  断言对象的class为c
	 *  @param obj obj
	 *  @param c c
	 *  @param msg msg
	 */
	public static void isType(Object obj, Class<?> c, String msg) {
		if (!(c.isAssignableFrom(obj.getClass())))
			throw new ClassCastException(msg);
	}

	/**
	 *  断言对象的class为c
	 *  @param obj obj
	 *  @param c c
	 */
	public static void isType(Object obj, Class<?> c) {
		if (!(c.isAssignableFrom(obj.getClass())))
			throw new ClassCastException();
	}

	/**
	 *  断言为假
	 *  @param obj obj
	 */
	public static void isFalse(Boolean obj) {
		if (obj.equals(Boolean.TRUE))
			throw new IllegalArgumentException();
	}

	/**
	 *  断言为假
	 *  @param obj obj
	 *  @param string string
	 */
	public static void isFalse(Boolean obj, String string) {
		if (obj.equals(Boolean.TRUE))
			throw new IllegalArgumentException(string);
	}

	/**
	 *  断言为真
	 *  @param obj obj
	 */
	public static void isTrue(boolean obj) {
		if (!obj) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 *  断言为真
	 *  @param obj obj
	 */
	public static void isTrue(Boolean obj) {
		if (obj.equals(Boolean.FALSE))
			throw new IllegalArgumentException();
	}

	/**
	 *  断言为真
	 *  @param obj obj
	 *  @param string string
	 */
	public static void isTrue(Boolean obj, String string) {
		if (obj.equals(Boolean.FALSE))
			throw new IllegalArgumentException(string);
	}

	/**
	 *  断言两个对象相等
	 *  @param obj1 obj1
	 *  @param obj2 obj2
	 */
	public static void equals(Object obj1, Object obj2) {
		if (!Objects.equals(obj1, obj2))
			throw new IllegalArgumentException("Expect " + obj1 + " but get " + obj2);
	}

	/**
	 *  断言两个对象相等
	 *  @param obj1 obj1
	 *  @param obj2 obj2
	 *  @param string string
	 */
	public static void equals(Object obj1, Object obj2, String string) {
		if (!Objects.equals(obj1, obj2))
			throw new IllegalArgumentException(string);
	}

	/**
	 *  断言两个对象相等
	 *  @param obj1 obj1
	 *  @param obj2 obj2
	 */
	public static void notEquals(Object obj1, Object obj2) {
		if (Objects.equals(obj1, obj2))
			throw new IllegalArgumentException();
	}

	/**
	 *  断言两个对象相等
	 *  @param obj1 obj1
	 *  @param obj2 obj2
	 *  @param string string
	 */
	public static void notEquals(Object obj1, Object obj2, String string) {
		if (Objects.equals(obj1, obj2))
			throw new IllegalArgumentException(string);
	}

	/**
	 *  断言两个对象是同一个
	 *  @param o1 o1
	 *  @param o2 o2
	 *  @param string string
	 */
	public static void sameObject(Object o1, Object o2, String string) {
		if (o1 != o2)
			throw new IllegalArgumentException(string);
	}

	/**
	 *  断言数值在指定的范围之内
	 *  @param value length
	 *  @param i i
	 *  @param j j
	 *  @param string string
	 */
	public static void between(int value, int i, int j, String string) {
		if (value < i || value > j) {
			throw new IllegalArgumentException(string);
		}
	}

	/**
	 * 断言文字是指定枚举的一个枚举项
	 * @param string string
	 * @param name name
	 * @param msg msg
	 * @param <T> The type of target object.
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
	 * @param string string
	 * @param name name
	 * @param <T> The type of target object.
	 */
	public static <T extends Enum<T>> void isEnumOf(Object string, Class<T> name) {
		Enum.valueOf(name, string.toString());
	}

	/**
	 *  断言对象被数组所包含
	 *  @param value value
	 *  @param array array
	 *  @param msg msg
	 *  @param <T> The type of target object.
	 */
	public static <T> void isInArray(T value, T[] array, String msg) {
		if (!ArrayUtils.contains(array, value)) {
			throw new NoSuchElementException(msg);
		}
	}

	/**
	 *  断言对象被数组所包含
	 *  @param value value
	 *  @param array array
	 *  @param <T> The type of target object.
	 */
	public static <T> void isInArray(T value, T[] array) {
		if (!ArrayUtils.contains(array, value)) {
			throw new NoSuchElementException("Not found: " + value);
		}
	}

	/**
	 *  断言数值被数组所包含s
	 *  @param value value
	 *  @param array array
	 */
	public static void isInArray(int value, int[] array, String message) {
		if (!ArrayUtils.contains(array, value)) {
			throw new NoSuchElementException(message+" Not Found: " + value);
		}
	}

	/**
	 *  断言文件(不是目录)存在
	 *  @param file file
	 */
	public static void fileExist(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException(new FileNotFoundException("File " + file.getAbsolutePath() + " is not exist."));
		} else if (file.isDirectory()) {
			throw new IllegalArgumentException(new FileNotFoundException(file.getAbsolutePath() + " is a Directory."));
		}
	}

	/**
	 *  断言文件或目录存在
	 *  @param file file
	 */
	public static void exist(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException(new FileNotFoundException("File " + file.getAbsolutePath() + " is not exist."));
		}
	}

	/**
	 *  断言目录存在
	 *  @param file file
	 */
	public static void folderExist(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException(new FileNotFoundException("File " + file.getAbsolutePath() + " is not exist."));
		} else if (file.isFile()) {
			throw new IllegalArgumentException(new FileNotFoundException(file.getAbsolutePath() + " is a File."));
		}
	}

	/**
	 *  断言文本不为空
	 *  @param text text
	 */
	public static void isNotEmpty(String text) {
		if (text == null || text.length() == 0) {
			throw new IllegalArgumentException("String must not empty!");
		}
	}

	/**
	 *  断言文本不为空
	 *  @param text text
	 *  @param msg msg
	 */
	public static void isNotEmpty(String text, String msg) {
		if (text == null || text.length() == 0) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 *  断言文本为空
	 *  @param text text
	 */
	public static String hasLength(String text) {
		if (text == null || text.length() == 0)
			throw new IllegalArgumentException();
		return text;
	}

	/**
	 *  断言文本为空
	 *  @param text text
	 *  @param msg msg
	 */
	public static void hasLength(String text, String msg) {
		if (text == null || text.length() == 0)
			throw new IllegalArgumentException(msg);
	}

	public static<T> Collection<T> hasElements(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return collection;
	}

	public static <K,V> Map<K,V> hasElements(Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return map;
	}

	public static void hasElements(Collection<?> collection, String msg) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void hasElements(Map<?, ?> collection, String msg) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 *  Assert that an array has elements; that is, it must not be
	 *  <code>null</code> and must have at least one element.
	 *  <pre class="code">
	 *  Assert.notEmpty(array, &quot;The array must have elements&quot;);
	 *  </pre>
	 *  @param array array
	 *             the array to check
	 *  @param message message
	 *             the exception message to use if the assertion fails
	 *  @throws IllegalArgumentException If encounter IllegalArgumentException
	 *              if the object array is <code>null</code> or has no elements
	 */
	public static void notEmpty(Object[] array, String message) {
		if (ArrayUtils.isEmpty(array)) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 *  Assert that an array has elements; that is, it must not be
	 *  <code>null</code> and must have at least one element.
	 *  <pre class="code">
	 *  Assert.notEmpty(array);
	 *  </pre>
	 *  @param array array
	 *             the array to check
	 *  @throws IllegalArgumentException If encounter IllegalArgumentException
	 *              if the object array is <code>null</code> or has no elements
	 */
	public static void notEmpty(Object[] array) {
		notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
	}

	/**
	 *  Assert that an array has no null elements. Note: Does not complain if the
	 *  array is empty!
	 *  检查数组，不允许数组中有null元素
	 *  <pre class="code">
	 *  Assert.noNullElements(array, &quot;The array must have non-null elements&quot;);
	 *  </pre>
	 *  @param array array
	 *             the array to check
	 *  @param message message
	 *             the exception message to use if the assertion fails
	 *  @throws IllegalArgumentException If encounter IllegalArgumentException
	 *              if the object array contains a <code>null</code> element
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
	 *  Assert that an array has no null elements. Note: Does not complain if the
	 *  array is empty!
	 *  检查数组，不允许数组中有null元素
	 *  <pre class="code">
	 *  Assert.noNullElements(array);
	 *  </pre>
	 *  @param array array
	 *             the array to check
	 *  @throws IllegalArgumentException If encounter IllegalArgumentException
	 *              if the object array contains a <code>null</code> element
	 */
	public static void noNullElements(Object[] array) {
		noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
	}
	
	public static int notNegative(int value) {
	    if(value>=0) {
	        return value;
	    }
	    throw new IllegalArgumentException(value+" is a negative number.");
	}
	
	public static int isPositive(int value) {
	    if(value>0) {
	        return value;
	    }
	    throw new IllegalArgumentException(value+" is not a positive number.");
	}
	
    public static Object nonNull(Object value) {
        if (value != null) {
            return value;
        }
        throw new IllegalArgumentException(value + " is null.");
    }
}
