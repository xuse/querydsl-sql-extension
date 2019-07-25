package com.github.xuse.querydsl.util;

public class StringUtils {

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
    
	/**
	 * 文本转换为boolean，如果不能转换则返回默认值
	 * 
	 * @param s
	 * @param defaultValue
	 * @return
	 */
	public static final boolean toBoolean(String s, Boolean defaultValue) {
		if ("true".equalsIgnoreCase(s) || "Y".equalsIgnoreCase(s) || "1".equals(s) || "ON".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "T".equalsIgnoreCase(s)) {
			return true;
		}
		if ("false".equalsIgnoreCase(s) || "N".equalsIgnoreCase(s) || "0".equals(s) || "OFF".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "F".equalsIgnoreCase(s)) {
			return false;
		}
		if (defaultValue == null) {// 特别的用法，不希望有缺省值，如果字符串不能转换成布尔，则抛出异常。
			throw new IllegalArgumentException(s + "can't be cast to boolean.");
		}
		return defaultValue;
	}
    
	/**
	 * 文本转换到整数Long
	 * 
	 * @param o
	 * @param defaultValue
	 * @return
	 */
	public static long toLong(String o, Long defaultValue) {
		if (isBlank(o))
			return defaultValue;// 空白则返回默认值，即便默认值为null也返回null
		try {
			return Long.parseLong(o);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	/**
	 * 文本转换到整数int
	 * 
	 * @param o
	 * @param defaultValue
	 * @return
	 */
	public static int toInt(String o, Integer defaultValue) {
		if (isBlank(o))
			return defaultValue;// 空白则返回默认值，即便默认值为null也返回null
		try {
			return Integer.valueOf(o);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	/**
	 * 文本转换到小数float
	 * 
	 * @param o
	 * @param defaultValue
	 * @return
	 */
	public static float toFloat(String o, Float defaultValue) {
		if (isBlank(o))
			return defaultValue;
		try {
			return Float.parseFloat(o);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	/**
	 * 文本转换到小数double
	 * 
	 * @param o
	 * @param defaultValue
	 * @return
	 */
	public static double toDouble(String o, Double defaultValue) {
		if (isBlank(o))
			return defaultValue;
		try {
			return Double.parseDouble(o);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}
}
