package com.github.xuse.querydsl.util;

import java.util.HashMap;
import java.util.Map;

public final class Enums {
	private Enums(){}
	
	/**
	 * get the enumeration value. or return the defaultValue if absent.
	 * @param <T> the type of enum.
	 * @param clz class
	 * @param value value
	 * @param defaultValue defaultValue
	 * @return the enumeration value. or return the defaultValue if absent.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> clz, String value, T defaultValue) {
		try {
			return Enum.valueOf(clz, value);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	/**
	 * 根据序号获取枚举、
	 * 本方法主要是为了提供一个合适的异常信息。
	 * @param <T> type of enumeration
	 * @param clz enumeration class
	 * @param ordinal the ordinal
	 * @return the enumeration value.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> clz, int ordinal) {
		try {
			return clz.getEnumConstants()[ordinal];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw Exceptions.illegalArgument("The enum[{}] has no element of ordinal {}", clz.getSimpleName(), ordinal,
					e);
		}
	}
	
	/**
	 * 根据序号转换枚举
	 * @param <T> type of enumeration
	 * @param clz enumeration class
	 * @param ordinal ordinal
	 * @param defaultValue defaultValue
	 * @return the enumeration value.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> clz, Integer ordinal, T defaultValue) {
		try {
			return ordinal == null ? defaultValue : clz.getEnumConstants()[ordinal.intValue()];
		} catch (ArrayIndexOutOfBoundsException e) {
			return defaultValue;
		}
	}

	/**
	 * get the enumeration value. or throw exception if the name not exist.
	 * 
	 * @param <T> enumeration class
	 * @param clz enumeration class
	 * @param value value
	 * @param exceptionMessage 异常消息模板，用 {}来标记传入的value（类似slf4j日志格式）
	 * @param params exception message params.
	 * @return the enumeration value.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> clz, String value, String exceptionMessage, Object... params) {
		try {
			return Enum.valueOf(clz, value);
		} catch (IllegalArgumentException e) {
			throw Exceptions.illegalArgument(exceptionMessage, params);
		}
	}
	
	/**
	 * 将枚举常量转换为Map，便于快速检索
	 * @param <T> the enumeration type
	 * @param clz enumeration class
	 * @return the enumeration value.
	 */
	public static <T extends Enum<T>> Map<String,T> valuesMap(Class<T> clz){
		T[] values=clz.getEnumConstants();
		Map<String,T> result=new HashMap<String,T>(values.length);
		for(T t: values) {
			result.put(t.name(), t);
		}
		return result;
	}
}