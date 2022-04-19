package com.github.xuse.querydsl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.StringUtils;

public interface AssertEumSupport {
	String getArgName();
	
	/**
	 * 断言文字不为空
	 * @param text
	 */
	default void hasLength(String text) {
		if(text==null || text.length()==0) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must not be empty!");
		}
	}
	
	/**
	 * 断言文字长度达到size
	 * @param text
	 * @param size
	 */
	default void hasLength(String text, int size) {
		if(text==null || text.length()<0) {
			throw new IllegalArgumentException("Length of ["+ getArgName()+ "] must greater than "+size+"!");
		}
	}
	
	/**
	 * 断言文字不为空字符串（全是空格）
	 * @param text
	 */
	default void hasText(String text) {
		if (!StringUtils.hasText(text)) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must contain at least one non-space character.");
		}
	}
	
	/**
	 * 断言对象不为null
	 * @param obj
	 */
	default void notNull(Object obj) {
		if(obj==null) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must not be Null!");
		}
	}
	
	/**
	 * 断言对象为null
	 * @param obj
	 */
	default void isNull(Object obj) {
		if(obj==null) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must be Null.");
		}
	}
	
	/**
	 * 断言集合为空 
	 * @param obj
	 */
	default void isEmpty(Collection<?> obj) {
		if(obj!=null && !obj.isEmpty()) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must be empty.");
		}
	}
	
	/**
	 * 断言Map为空
	 * @param obj
	 */
	default void isEmpty(Map<?,?> obj) {
		if(obj!=null && !obj.isEmpty()) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must be empty.");
		}
	}
	
	/**
	 * 断言集合非空
	 * @param obj
	 */
	default void notEmpty(Collection<?> obj) {
		if(obj==null || obj.isEmpty()) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must contain at least on element.");
		}
	}
	
	/**
	 * 断言Map非空
	 * @param obj
	 */
	default void notEmpty(Map<?,?> obj) {
		if(obj==null || obj.isEmpty()) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must contain at least on entry.");
		}
	}
	
	/**
	 * Assert that an array has elements; that is, it must not be <code>null</code>
	 * and must have at least one element.
	 * 
	 * <pre class="code">
	 * Assert.notEmpty(array);
	 * </pre>
	 * 
	 * @param array the array to check
	 * @throws IllegalArgumentException if the object array is <code>null</code> or
	 *                                  has no elements
	 */
	default void notEmpty(Object[] obj) {
		if(obj==null || obj.length==0) {
			throw new IllegalArgumentException("["+ getArgName()+ "] must contain at least on element.");
		}
	}
	
	/**
	 * 断言数量必须不大于等于指定的上限。
	 * @param size
	 * @param max
	 * @param keyword
	 */
	default void lessThan(int size, int max) {
		if (size > max) {
			throw illegalArgument("{} too many, {} is exceed the maxium value {},", getArgName(), size, max);
		}
	}
	
	/**
	 * 断言成员个数等于size。
	 * @param collection
	 * @param size
	 */
	default void sizeIs(Collection<?> collection,int size) {
		int n = collection == null ? 0 : collection.size();
		if(n!=size) {
			throw illegalArgument("{}'s size must equal to {}, actually is {}.", getArgName(), size, n);   
		}
	}
	
	/**
	 * 断言成员个数等于size。
	 * @param map
	 * @param size
	 */
	default void sizeIs(Map<?,?> map, int size) {
		int n = map == null ? 0 : map.size();
		if(n!=size) {
			throw illegalArgument("{}'s size must equal to {}, actually is {}.", getArgName(), size, n);   
		}
	}
	
	/**
	 * 断言Map成员个数不少于size。
	 * @param map
	 * @param size
	 */
	default void sizeAtLeast(Map<?,?> map, int size) {
		int n = map == null ? 0 : map.size();
		if (n < size) {
			throw illegalArgument("{} must contain at least {} entries, actually is {}.", getArgName(), size, n);
		}
	}
	/**
	 * 断言集合成员个数不少于size。
	 * @param collection
	 * @param size
	 */
	default void sizeAtLeast(Collection<?> collection, int size) {
		int n = collection == null ? 0 : collection.size();
		if (n < size) {
			throw illegalArgument("{} must contain at least {} elements, actually is {}.", getArgName(), size, n);
		}
	}
	
	/**
	 *  断言数组成员个数不少于size。
	 * @param objs
	 * @param size
	 */
	default void sizeAtLeast(Object[] objs, int size) {
		int n = objs == null ? 0 : objs.length;
		if (n < size) {
			throw illegalArgument("{} must contain at least {} elements, actually is {}.", getArgName(), size, n);
		}
	}
	
	/**
	 * 断言数组成员个数不多于size。
	 * @param objs
	 * @param size
	 */
	default void sizeLessThan(Object[] objs, int size) {
		int n = objs == null ? 0 : objs.length;
		if (n > size) {
			throw illegalArgument("{} must contain at most {} elements, actually is {}.", getArgName(), size, n);
		}
	}
	
	/**
	 * 断言集合成员个数不多于size。
	 * @param collection
	 * @param size
	 */
	default void sizeLessThan(Collection<?> collection, int size) {
		int n = collection == null ? 0 : collection.size();
		if (n > size) {
			throw illegalArgument("{} must contain at most {} elements, actually is {}.", getArgName(), size, n);
		}
		
	}
	
	/**
	 * 断言Map成员个数不多于size。
	 * @param map
	 * @param size
	 */
	default void sizeLessThan(Map<?,?> map, int size) {
		int n = map == null ? 0 : map.size();
		if (n > size) {
			throw illegalArgument("{} must contain at most {} entries, actually is {}.", getArgName(), size, n);
		}
	}
	
	/**
	 * 断言数量必须大于指定的下限
	 * @param size
	 * @param min
	 * @param keyword
	 */
	default void greaterThan(int size, int min) {
		if (size <= min) {
			throw illegalArgument("{} too small, {} is exceed the minium value {},", getArgName(), size, min);
		}
	}

	/**
	 * 断言对象的class为c
	 * 
	 * @param obj
	 * @param c
	 */
	default void isType(Object obj, Class<?> c) {
		notNull(obj);
		if (!(c.isAssignableFrom(obj.getClass())))
			throw new ClassCastException("["+getArgName()+"] must be a subtype of class "+c.getName());
	}

	/**
	 * 断言两个对象相等
	 * @param obj1
	 * @param obj2
	 */
	default void equals(Object obj1, Object obj2) {
		if (!Objects.equals(obj1, obj2))
			throw new IllegalArgumentException("Expect " + obj1 + " as " + getArgName() + ", but get " + obj2);
	}


	/**
	 * 断言两个对象相等
	 * 
	 * @param obj1
	 * @param obj2
	 */
	default void notEquals(Object obj1, Object obj2) {
		if (Objects.equals(obj1, obj2)) {
			throw new IllegalArgumentException("Expect "+getArgName() +":" + obj1 + " not equals to " + obj2);
		}
	}
	

	/**
	 * 断言数值在指定的范围之内
	 * 
	 * @param length
	 * @param min
	 * @param max
	 * @param string
	 */
	default void between(int length, int min, int max, String string) {
		if (length < min || length > max) {
			throw illegalArgument("[{}] must between {} and {}, actually is {}.", getArgName(), min, max, length);
		}
	}

	/**
	 * 断言文字是指定枚举的一个枚举项
	 * 
	 * @param string
	 * @param name
	 * @param msg
	 */
	default <T extends Enum<T>> void isEnumOf(Object string, Class<T> name) {
		String s=String.valueOf(string);
		try {
			Enum.valueOf(name, s);
		} catch (IllegalArgumentException e) {
			throw illegalArgument("[{}] must be a enum text of class {}, actually is {}.", getArgName(), name.getName(), s);
		}
	}

	/**
	 * 断言文件(不是目录)存在
	 * 
	 * @param file
	 */
	default void fileExist(File file) {
		if (!file.exists()) {
			throw new RuntimeException(new FileNotFoundException(getArgName()+ " " + file.getAbsolutePath() + " is not exist."));
		} else if (file.isDirectory()) {
			throw new RuntimeException(new FileNotFoundException(getArgName()+ " "+ file.getAbsolutePath() + " is a directory."));
		}
	}

	/**
	 * 断言文件或目录存在
	 * 
	 * @param file
	 */
	default void exist(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException(
					new FileNotFoundException(getArgName()+ " " + file.getAbsolutePath() + " is not exist."));
		}
	}

	/**
	 * 断言目录存在
	 * 
	 * @param file
	 */
	default void folderExist(File file) {
		if (!file.exists()) {
			throw new RuntimeException(new FileNotFoundException(getArgName()+ " " + file.getAbsolutePath() + " is not exist."));
		} else if (file.isFile()) {
			throw new RuntimeException(new FileNotFoundException(getArgName()+ " " + file.getAbsolutePath() + " is not a directory."));
		}
	}

	/**
	 * Assert that an array has no null elements. Note: Does not complain if the
	 * array is empty! 检查数组，不允许数组中有null元素
	 * 
	 * <pre class="code">
	 * Assert.noNullElements(array, &quot;The array must have non-null elements&quot;);
	 * </pre>
	 * 
	 * @param array   the array to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object array contains a
	 *                                  <code>null</code> element
	 */
	default void noNullElements(Object[] array) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null) {
					throw illegalArgument("[{}] must not contain null element.",getArgName()) ;
				}
			}
		}
	}
	
	/**
	 * 断言输入参数为true
	 * @param b
	 */
	default void isTrue(boolean b) {
		if(!b) {
			throw new IllegalArgumentException(getArgName()+ " must be false.");
		}
	}
	
	/**
	 * 断言输入参数为false
	 * @param b
	 */
	default void isFalse(boolean b) {
		if(!b) {
			throw new IllegalArgumentException(getArgName()+" must be false.");
		}
	}
	
	/**
	 * 断言数字是奇数
	 * @param value
	 */
	default void isOdd(int value) {
		if(Primitives.isEven(value)) {
			throw new IllegalArgumentException(getArgName()+" must be a odd-number, actually is "+value+".");
		}
	}
	
	/**
	 * 断言数字是偶数
	 * @param value
	 */
	default void isEven(int value) {
		if(Primitives.isOdd(value)) {
			throw new IllegalArgumentException(getArgName()+" must be a even-number, actually is "+value+".");
		}
	}
	
	/**
	 * 使用slf4j的机制来生成异常信息
	 * 
	 * @param message
	 * @param objects
	 * @return
	 */
	static IllegalArgumentException illegalArgument(String message, Object... objects) {
		FormattingTuple f = MessageFormatter.arrayFormat(message, objects);
		return f.getThrowable() == null ? new IllegalArgumentException(f.getMessage())
				: new IllegalArgumentException(f.getMessage(), f.getThrowable());
	};
	
}
