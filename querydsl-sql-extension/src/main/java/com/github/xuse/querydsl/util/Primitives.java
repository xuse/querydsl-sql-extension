package com.github.xuse.querydsl.util;

import java.util.Date;

/**
 * Utilities of operating Primitive types.
 * @author Joey
 */
public final class Primitives {
	private Primitives() {
	}

	/**
	 * Return the default values of the eight primitive types (as their boxed instance).
	 *
	 * @param javaClass javaClass
	 *            数据类型
	 * @return 返回该种技术类型的默认数值
	 * @throws IllegalArgumentException If encounter IllegalArgumentException
	 *             如果传入的javaClass不是八种基础类型之一抛出。
	 */
	public static Object defaultValueOfPrimitive(Class<?> javaClass) {
		if (javaClass.isPrimitive()) {
			String s=javaClass.getName();
			//采用s.length()+s.charAt(0) 可以得到8个基元类型的最小数值分布，以确保被编译为tableswitch。
			//{102=byte, 103=char, 105=boolean, 106=double, 107=float, 108=int, 112=long, 120=short}
			switch(s.length()+s.charAt(0)) {
				case 108://int
					return 0;
				case 112://long
					return 0L;
				case 106:
					return 0d;
				case 105:
					return Boolean.FALSE;
				case 107:
					return 0f;
				case 103:
					return (char) 0;
				case 102:
					return Byte.valueOf((byte) 0);
				default:
					return Short.valueOf((short) 0);
			}
		}
		throw new IllegalArgumentException(javaClass + " is not Primitive Type.");
	}

	/**
	 * Convert boxed class to primitive class.
	 *
	 * @param wrapperClass wrapperClass
	 * @return 如果不能转换，则返回输入的类
	 */
	public static Class<?> toPrimitiveClass(Class<?> wrapperClass) {
		switch(wrapperClass.getName()) {
			case "java.lang.Integer":
				return Integer.TYPE;
			case "java.lang.Byte":
				return Byte.TYPE;
			case "java.lang.Short":
				return Short.TYPE;
			case "java.lang.Long":
				return Long.TYPE;
			case "java.lang.Float":
				return Float.TYPE;
			case "java.lang.Double":
				return Double.TYPE;
			case "java.lang.Character":
				return Character.TYPE;
			case "java.lang.Boolean":
				return Boolean.TYPE;
			default:
				return wrapperClass;
		}
	}

	/**
	 * 将8原生类型的类转换为对应的包装的类型。
	 *
	 * @param primitiveClass primitiveClass
	 * @return 包装类型
	 */
	public static Class<?> toWrapperClass(Class<?> primitiveClass) {
		if (primitiveClass.isPrimitive()) {
			//采用s.length()+s.charAt(0) 可以得到8个基元类型的最小数值分布，以确保被编译为tableswitch。
			//{102=byte, 103=char, 105=boolean, 106=double, 107=float, 108=int, 112=long, 120=short}
			String s=primitiveClass.getName();
			switch(s.length()+s.charAt(0)) {
				case 108:
					return Integer.class;
				case 112:
					return Long.class;
				case 106:
					return Double.class;
				case 105:
					return Boolean.class;
				case 107:
					return Float.class;
				case 103:
					return Character.class;
				case 102:
					return Byte.class;
				default:
					return Short.class;
			}
		}
		return primitiveClass;
	}

	/**
	 * 得到原生对象和String的缺省值。
	 *
	 * @param cls cls
	 *            类型
	 *
	 * @return 指定类型数据的缺省值。如果传入类型是primitive和String之外的类型返回null。
	 */
	public static Object defaultValueForBasicType(Class<?> cls) {
		if (cls == String.class) {
			return "";
		} else if (cls.isPrimitive()) {
			return defaultValueOfPrimitive(cls);
		}
		return null;
	}

	/**
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return 安全拆箱
	 */
	public static int unbox(Integer value, int defaultValue) {
		return value == null ? defaultValue : value.intValue();
	}

	/**
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return 安全拆箱
	 */
	public static short unbox(Short value, short defaultValue) {
		return value == null ? defaultValue : value.shortValue();
	}

	/**
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return 安全拆箱
	 */
	public static long unbox(Long value, long defaultValue) {
		return value == null ? defaultValue : value.longValue();
	}

	/**
	 * 安全拆箱
	 *
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return double value
	 */
	public static double unbox(Double value, double defaultValue) {
		return value == null ? defaultValue : value.doubleValue();
	}

	/**
	 * 安全拆箱
	 *
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return value
	 */
	public static float unbox(Float value, float defaultValue) {
		return value == null ? defaultValue : value.floatValue();
	}

	/**
	 * 安全拆箱
	 *
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return value
	 */
	public static char unbox(Character value, char defaultValue) {
		return value == null ? defaultValue : value.charValue();
	}

	/**
	 * 安全拆箱
	 *
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return value
	 */
	public static byte unbox(Byte value, byte defaultValue) {
		return value == null ? defaultValue : value.byteValue();
	}

	/**
	 * 安全拆箱
	 *
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return value
	 */
	public static boolean unbox(Boolean value, boolean defaultValue) {
		return value == null ? defaultValue : value.booleanValue();
	}

	/**
	 * 安全拆箱（扩展）
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return value
	 */
	public static long unbox(Date value, long defaultValue) {
		return value == null ? defaultValue : value.getTime();
	}

	/**
	 *  将long安全的转换为int
	 *  @param num long value
	 *  @return int value
	 */
	public static int toIntSafely(long num) {
		if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
			throw Exceptions.illegalArgument("Unable to convert {} to a int value.", num);
		}
		return (int) num;
	}

	/**
	 *  将int安全的转换比为short
	 *  @param num int value
	 *  @return short value
	 */
	public static short toShortSafely(int num) {
		if (num > Short.MAX_VALUE || num < Short.MIN_VALUE) {
			throw Exceptions.illegalArgument("Unable to convert {} to a short value.", num);
		}
		return (short) num;
	}

	/**
	 * @param i i
	 * @return  判断是否为奇数
	 */
	public static boolean isOdd(int i) {
		return (i & 1) == 1;
	}

	/**
	 * @param i i
	 * @return 判断是否为偶数
	 */
	public static boolean isEven(int i) {
		return (i & 1) == 0;
	}

	/**
	 * @param i i
	 * @return 判断是否为奇数
	 */
	public static boolean isOdd(long i) {
		return (i & 1) == 1;
	}

	/**
	 * @param i i
	 * @return 判断是否为偶数
	 */
	public static boolean isEven(long i) {
		return (i & 1) == 0;
	}
}
