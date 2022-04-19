package com.github.xuse.querydsl.util;

import java.util.ArrayList;
import java.util.List;

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
		if ("true".equalsIgnoreCase(s) || "Y".equalsIgnoreCase(s) || "1".equals(s) || "ON".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)
				|| "T".equalsIgnoreCase(s)) {
			return true;
		}
		if ("false".equalsIgnoreCase(s) || "N".equalsIgnoreCase(s) || "0".equals(s) || "OFF".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)
				|| "F".equalsIgnoreCase(s)) {
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

	/**
	 * <p>
	 * Splits the provided text into an array, separator specified. This is an
	 * alternative to using StringTokenizer.
	 * </p>
	 *
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator. For more control over the split use
	 * the StrTokenizer class.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.split(null, *)         = null
	 * StringUtils.split("", *)           = []
	 * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
	 * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
	 * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
	 * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
	 * </pre>
	 *
	 * @param str           the String to parse, may be null
	 * @param separatorChar the character used as the delimiter
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 * @since 2.0
	 */
	public static String[] split(String str, char separatorChar) {
		return splitWorker(str, separatorChar, false);
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * Performs the logic for the <code>split</code> and
	 * <code>splitPreserveAllTokens</code> methods that do not return a maximum
	 * array length.
	 *
	 * @param str               the String to parse, may be <code>null</code>
	 * @param separatorChar     the separate character
	 * @param preserveAllTokens if <code>true</code>, adjacent separators are
	 *                          treated as empty token separators; if
	 *                          <code>false</code>, adjacent separators are treated
	 *                          as one separator.
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 */
	private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)

		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return EMPTY_STRING_ARRAY;
		}
		List<String> list = new ArrayList<>();
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match || preserveAllTokens) {
					list.add(str.substring(start, i));
					match = false;
					lastMatch = true;
				}
				start = ++i;
				continue;
			}
			lastMatch = false;
			match = true;
			i++;
		}
		if (match || (preserveAllTokens && lastMatch)) {
			list.add(str.substring(start, i));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	public static boolean isEmpty(String user) {
		return user == null || user.length() == 0;
	}

	public static boolean isNotEmpty(String user) {
		return user != null && user.length() > 0;
	}
	
	/**
	 * 用于字符串的拼接
	 * 
	 * @param data 数据
	 * @param sep  分隔符
	 */
	public static String join(List<String> data, char sep) {
		if (data == null || data.isEmpty())
			return "";
		StringBuilder sb=new StringBuilder(32);
		sb.append(data.get(0));
		int max=data.size();
		for (int i = 1; i < max; i++) {
			sb.append(sep).append(data.get(i));
		}
		return sb.toString();
	}

}
