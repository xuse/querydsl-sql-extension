package com.github.xuse.querydsl.util.binary;

/**
 * 表达short与int的无符号数的工具类
 * @author Joey
 */
public class Unsigned {

	public static final int MAX_BYTE_VALUE = 0xFF;

	public static final int MAX_SHORT_VALUE = 0xFFFF;

	public static final long MAX_INT_VALUE = 0xFFFFFFFFL;

	public static int of(byte i) {
		return i < 0 ? i + 256 : i;
	}

	/**
	 * 还原一个 unsigned short
	 * @param i i
	 * @return as int
	 */
	public static int of(short i) {
		return i < 0 ? i + MAX_SHORT_VALUE + 1 : i;
	}

	/**
	 * 还原一个 unsigned int
	 * @param i i
	 * @return as long
	 */
	public static long of(int i) {
		return i < 0 ? MAX_INT_VALUE + i : i;
	}

	/**
	 * 构造一个unsigned short
	 * @param i i
	 * @return unsigned short
	 */
	public static short toShort(int i) {
		if (i > MAX_SHORT_VALUE) {
			throw new IllegalArgumentException("Unsigned short can not exceed: " + i);
		}
		return (short) i;
	}

	/**
	 * 构造一个unsigned int
	 * @param i i
	 * @return unsigned int
	 */
	public static int toInt(long i) {
		if (i >= MAX_INT_VALUE) {
			throw new IllegalArgumentException("Unsigned int can not exceed: " + i);
		}
		return (int) i;
	}

	public static byte toByte(int i) {
		if (i >= MAX_BYTE_VALUE) {
			throw new IllegalArgumentException("Unsigned byte can not exceed: " + i);
		}
		return (byte) i;
	}
}
