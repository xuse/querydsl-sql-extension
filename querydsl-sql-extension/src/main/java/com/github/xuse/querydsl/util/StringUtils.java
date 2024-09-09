package com.github.xuse.querydsl.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.zip.CRC32;

import lombok.SneakyThrows;

public class StringUtils {
	private static final int INDEX_NOT_FOUND = -1;

	public static final String SPACE = " ";

	private static final String invalidCharsInFilename = "\t\\/|\"*?:<>\t\n\r";// 文件名中禁用的字符

	public static final char NULL_CHAR = (char) 0;

	/**
	 *
	 * @param s            string to convert
	 * @param defaultValue defaultValue
	 * @return 文本转换为boolean，如果不能转换则返回默认值
	 */
	public static boolean toBoolean(String s, Boolean defaultValue) {
		if ("true".equalsIgnoreCase(s) || "Y".equalsIgnoreCase(s) || "1".equals(s) || "ON".equalsIgnoreCase(s)
				|| "yes".equalsIgnoreCase(s) || "T".equalsIgnoreCase(s)) {
			return true;
		}
		if ("false".equalsIgnoreCase(s) || "N".equalsIgnoreCase(s) || "0".equals(s) || "OFF".equalsIgnoreCase(s)
				|| "no".equalsIgnoreCase(s) || "F".equalsIgnoreCase(s)) {
			return false;
		}
		if (defaultValue == null) {// 特别的用法，不希望有缺省值，如果字符串不能转换成布尔，则抛出异常。
			throw new IllegalArgumentException(s + "can't be cast to boolean.");
		}
		return defaultValue;
	}

	/**
	 * @param string       string to convert
	 * @param defaultValue default Value
	 * @return 文本转换到小数float
	 */
	public static float toFloat(String string, Float defaultValue) {
		if (isBlank(string))
			return defaultValue;
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	/**
	 * @param string       string to convert.
	 * @param defaultValue default Value
	 * @return 文本转换到小数double
	 */
	public static double toDouble(String string, Double defaultValue) {
		if (isBlank(string))
			return defaultValue;
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * @param data 数据
	 * @param sep  分隔符
	 * @return 字符串的拼接
	 */
	public static String join(List<String> data, char sep) {
		if (data == null || data.isEmpty())
			return EMPTY;
		StringBuilder sb = new StringBuilder(32);
		sb.append(data.get(0));
		int max = data.size();
		for (int i = 1; i < max; i++) {
			sb.append(sep).append(data.get(i));
		}
		return sb.toString();
	}

	////////////
	/**
	 * @param has   space character between bytes.
	 * @param bytes bytes
	 * @return Get hex string from byte array(lower cases).
	 */
	public static String toHexString(byte[] bytes, boolean hasSpace) {
		if (bytes == null || bytes.length == 0) {
			return EMPTY;
		}
		return hasSpace ? toHex1(bytes, 0, bytes.length, ' ', hexDigitsL) : toHex0(bytes, 0, bytes.length, hexDigitsL);
	}

	/**
	 * @param has   space character between bytes.
	 * @param bytes bytes
	 * @return Get hex string from byte array(upper cases).
	 */
	public final static String toHexStringUppercase(byte[] bytes, boolean hasSpace) {
		if (bytes == null || bytes.length == 0) {
			return EMPTY;
		}
		return hasSpace ? toHex1(bytes, 0, bytes.length, ' ', hexDigitsU) : toHex0(bytes, 0, bytes.length, hexDigitsU);
	}

	/**
	 * Get byte array from hex string
	 * 
	 * @param hexString hexString
	 * @return byte array.
	 */
	public final static byte[] toByteArray(String hexString) {
		int arrLength = hexString.length() >> 1;
		byte[] buff = new byte[arrLength];
		for (int i = 0; i < arrLength; i++) {
			int index = i << 1;
			String digit = hexString.substring(index, index + 2);
			buff[i] = (byte) Integer.parseInt(digit, 16);
		}
		return buff;
	}

	public static boolean isEmpty(CharSequence ext) {
		return ext == null || ext.length() == 0;
	}

	public static boolean isNotEmpty(CharSequence ext) {
		return !isEmpty(ext);
	}

	/**
	 * 给定若干字符，从后向前寻找，任意一个匹配的字符。
	 * 
	 * @param str         需要查找的字符串
	 * @param searchChars 需要查找的字符序列
	 * @param startPos    开始位置
	 * @return 位置
	 */
	public static int lastIndexOfAny(String str, char[] searchChars, int startPos) {
		if ((str == null) || (searchChars == null)) {
			return -1;
		}
		if (startPos < 0) {
			startPos = 0;
		}
		for (int i = str.length() - 1; i >= startPos; i--) {
			char c = str.charAt(i);
			for (int j = 0; j < searchChars.length; j++) {
				if (c == searchChars[j]) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static String replaceEach(final String text, final String[] searchList, final String[] replacementList) {
		return replaceEach(text, searchList, replacementList, false, 0);
	}

	private static String replaceEach(final String text, final String[] searchList, final String[] replacementList,
			final boolean repeat, final int timeToLive) {

		// mchyzer Performance note: This creates very few new objects (one major goal)
		// let me know if there are performance requests, we can create a harness to
		// measure

		if (text == null || text.isEmpty() || searchList == null || searchList.length == 0 || replacementList == null
				|| replacementList.length == 0) {
			return text;
		}

		// if recursing, this shouldn't be less than 0
		if (timeToLive < 0) {
			throw new IllegalStateException(
					"Aborting to protect against StackOverflowError - " + "output of one loop is the input of another");
		}

		final int searchLength = searchList.length;
		final int replacementLength = replacementList.length;

		// make sure lengths are ok, these need to be equal
		if (searchLength != replacementLength) {
			throw new IllegalArgumentException(
					"Search and Replace array lengths don't match: " + searchLength + " vs " + replacementLength);
		}

		// keep track of which still have matches
		final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

		// index on index that the match was found
		int textIndex = -1;
		int replaceIndex = -1;
		int tempIndex;

		// index of replace array that will replace the search string found
		// NOTE: logic duplicated below START
		for (int i = 0; i < searchLength; i++) {
			if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].isEmpty()
					|| replacementList[i] == null) {
				continue;
			}
			tempIndex = text.indexOf(searchList[i]);

			// see if we need to keep searching for this
			if (tempIndex == -1) {
				noMoreMatchesForReplIndex[i] = true;
			} else {
				if (textIndex == -1 || tempIndex < textIndex) {
					textIndex = tempIndex;
					replaceIndex = i;
				}
			}
		}
		// NOTE: logic mostly below END

		// no search strings found, we are done
		if (textIndex == -1) {
			return text;
		}

		int start = 0;

		// get a good guess on the size of the result buffer, so it doesn't have to
		// double if it goes over a bit
		int increase = 0;

		// count the replacement text elements that are larger than their corresponding
		// text being replaced
		for (int i = 0; i < searchList.length; i++) {
			if (searchList[i] == null || replacementList[i] == null) {
				continue;
			}
			final int greater = replacementList[i].length() - searchList[i].length();
			if (greater > 0) {
				increase += 3 * greater; // assume 3 matches
			}
		}
		// have upper-bound at 20% increase, then let Java take over
		increase = Math.min(increase, text.length() / 5);

		final StringBuilder buf = new StringBuilder(text.length() + increase);

		while (textIndex != -1) {

			for (int i = start; i < textIndex; i++) {
				buf.append(text.charAt(i));
			}
			buf.append(replacementList[replaceIndex]);

			start = textIndex + searchList[replaceIndex].length();

			textIndex = -1;
			replaceIndex = -1;
			// tempIndex = -1;
			// find the next earliest match
			// NOTE: logic mostly duplicated above START
			for (int i = 0; i < searchLength; i++) {
				if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].isEmpty()
						|| replacementList[i] == null) {
					continue;
				}
				tempIndex = text.indexOf(searchList[i], start);

				// see if we need to keep searching for this
				if (tempIndex == -1) {
					noMoreMatchesForReplIndex[i] = true;
				} else {
					if (textIndex == -1 || tempIndex < textIndex) {
						textIndex = tempIndex;
						replaceIndex = i;
					}
				}
			}
			// NOTE: logic duplicated above END

		}
		final int textLength = text.length();
		for (int i = start; i < textLength; i++) {
			buf.append(text.charAt(i));
		}
		final String result = buf.toString();
		if (!repeat) {
			return result;
		}

		return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
	}

	/**
	 * 文本转换到整数int
	 * 
	 * @param text         string to convert
	 * @param defaultValue defaultValue
	 * @return int value
	 */
	public static int toInt(String text, Integer defaultValue) {
		if (isBlank(text))
			return defaultValue;// 空白则返回默认值，即便默认值为null也返回null
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	public static boolean isNotBlank(final CharSequence cs) {
		return !isBlank(cs);
	}

	/**
	 * @param cs {@link CharSequence}
	 * @return true 字符串是为空白（含空格组成的字符串）
	 */
	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String toString(Object obj) {
		if (obj == null)
			return EMPTY;
		return obj.toString();
	}

	/**
	 * 返回子串，如果查找的字串不存在则返回全部 和substringAfter方法不同，substringAfter方法在查找不到时返回空串
	 * 
	 * @param source source
	 * @param rev    rev
	 * @return 子串
	 */
	public static String substringAfterIfExist(String source, String rev) {
		if (source == null)
			return source;
		int n = source.indexOf(rev);
		if (n == -1)
			return source;
		return source.substring(n + rev.length());
	}

	/**
	 * 返回字串，如果查找的字串不存在则返回全部<br>
	 * 和substringAfterLast方法不同，substringAfterLast方法在查找不到时返回空串
	 * 
	 * @param source  源字符串
	 * @param keyword 查找字
	 * @return 子串
	 */
	public static String substringAfterLastIfExist(String source, String keyword) {
		if (source == null)
			return source;
		int n = source.lastIndexOf(keyword);
		if (n == -1)
			return source;
		return source.substring(n + keyword.length());
	}

	/**
	 * 在StringBuilder或各种Appendable中重复添加某个字符串若干次
	 * 
	 * @param sb  源
	 * @param str 要重复添加的字符串
	 * @param n   重复次数
	 */
	@SneakyThrows
	public static void repeatTo(Appendable sb, CharSequence str, int n) {
		if (n <= 0)
			return;
		for (int i = 0; i < n; i++) {
			sb.append(str);
		}
	}

	private static final String EMPTY = "";

	/**
	 * 这个方法的返回约定与Apache commons-lang中的同名方法保持一致。
	 * 
	 * @param str
	 * @param separator 为空时返回空字符串
	 * @return substring after the separator.
	 */
	public static String substringAfterLast(final String str, final String separator) {
		if (isEmpty(str)) {
			return str;
		}
		if (isEmpty(separator)) {
			return EMPTY;
		}
		final int pos = str.lastIndexOf(separator);
		if (pos == -1 || pos == str.length() - separator.length()) {
			return EMPTY;
		}
		return str.substring(pos + separator.length());
	}

	/**
	 * 文本转换到整数Long
	 * 
	 * @param text         text
	 * @param defaultValue defaultValue
	 * @return long value
	 */
	public static long toLong(String text, Long defaultValue) {
		if (isBlank(text))
			return defaultValue;// 空白则返回默认值，即便默认值为null也返回null
		try {
			return Long.parseLong(text);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}

	/**
	 * <p>
	 * Gets the substring before the first occurrence of a separator. The separator
	 * is not returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} string input will return {@code null}. An empty ("") string
	 * input will return the empty string. A {@code null} separator will return the
	 * input string.
	 * </p>
	 *
	 * <p>
	 * If nothing is found, the string input is returned.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringBefore(null, *)      = null
	 * StringUtils.substringBefore("", *)        = ""
	 * StringUtils.substringBefore("abc", "a")   = ""
	 * StringUtils.substringBefore("abcba", "b") = "a"
	 * StringUtils.substringBefore("abc", "c")   = "ab"
	 * StringUtils.substringBefore("abc", "d")   = "abc"
	 * StringUtils.substringBefore("abc", "")    = ""
	 * StringUtils.substringBefore("abc", null)  = "abc"
	 * </pre>
	 *
	 * @param str       the String to get a substring from, may be null
	 * @param separator the String to search for, may be null
	 * @return the substring before the first occurrence of the separator,
	 *         {@code null} if null String input
	 * @since 2.0
	 */
	public static String substringBefore(final String str, final String separator) {
		if (isEmpty(str) || separator == null) {
			return str;
		}
		if (separator.isEmpty()) {
			return EMPTY;
		}
		final int pos = str.indexOf(separator);
		if (pos == INDEX_NOT_FOUND) {
			return str;
		}
		return str.substring(0, pos);
	}

	public static String substringAfter(final String str, final String separator) {
		if (isEmpty(str) || StringUtils.isEmpty(separator)) {
			return str;
		}
		final int pos = str.indexOf(separator);
		if (pos == -1) {
			return EMPTY;
		}
		return str.substring(pos + separator.length());
	}

	/**
	 * <p>
	 * Removes control characters (char &lt;= 32) from both ends of this String
	 * returning {@code null} if the String is empty ("") after the trim or if it is
	 * {@code null}.
	 *
	 * <pre>
	 * StringUtils.trimToNull(null)          = null
	 * StringUtils.trimToNull("")            = null
	 * StringUtils.trimToNull("     ")       = null
	 * StringUtils.trimToNull("abc")         = "abc"
	 * StringUtils.trimToNull("    abc    ") = "abc"
	 * </pre>
	 *
	 * @param str the String to be trimmed, may be null
	 * @return the trimmed String, {@code null} if only chars &lt;= 32, empty or
	 *         null String input
	 * @since 2.0
	 */
	public static String trimToNull(final String str) {
		final String ts = trim(str);
		return isEmpty(ts) ? null : ts;
	}

	// Trim
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Removes control characters (char &lt;= 32) from both ends of this String,
	 * handling {@code null} by returning {@code null}.
	 * </p>
	 *
	 * <p>
	 * The String is trimmed using {@link String#trim()}. Trim removes start and end
	 * characters &lt;= 32.
	 * 
	 * <pre>
	 * StringUtils.trim(null)          = null
	 * StringUtils.trim("")            = ""
	 * StringUtils.trim("     ")       = ""
	 * StringUtils.trim("abc")         = "abc"
	 * StringUtils.trim("    abc    ") = "abc"
	 * </pre>
	 *
	 * @param str the String to be trimmed, may be null
	 * @return the trimmed string, {@code null} if null String input
	 */
	public static String trim(final String str) {
		return str == null ? null : str.trim();
	}

	/**
	 * <p>
	 * Gets the substring before the last occurrence of a separator. The separator
	 * is not returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} string input will return {@code null}. An empty ("") string
	 * input will return the empty string. An empty or {@code null} separator will
	 * return the input string.
	 * </p>
	 *
	 * <p>
	 * If nothing is found, the string input is returned.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringBeforeLast(null, *)      = null
	 * StringUtils.substringBeforeLast("", *)        = ""
	 * StringUtils.substringBeforeLast("abcba", "b") = "abc"
	 * StringUtils.substringBeforeLast("abc", "c")   = "ab"
	 * StringUtils.substringBeforeLast("a", "a")     = ""
	 * StringUtils.substringBeforeLast("a", "z")     = "a"
	 * StringUtils.substringBeforeLast("a", null)    = "a"
	 * StringUtils.substringBeforeLast("a", "")      = "a"
	 * </pre>
	 *
	 * @param str       the String to get a substring from, may be null
	 * @param separator the String to search for, may be null
	 * @return the substring before the last occurrence of the separator,
	 *         {@code null} if null String input
	 * @since 2.0
	 */
	public static String substringBeforeLast(final String str, final String separator) {
		if (isEmpty(str) || isEmpty(separator)) {
			return str;
		}
		final int pos = str.lastIndexOf(separator);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	/**
	 * <p>
	 * Right pad a String with spaces (' ').
	 * </p>
	 *
	 * <p>
	 * The String is padded to the size of {@code size}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.rightPad(null, *)   = null
	 * StringUtils.rightPad("", 3)     = "   "
	 * StringUtils.rightPad("bat", 3)  = "bat"
	 * StringUtils.rightPad("bat", 5)  = "bat  "
	 * StringUtils.rightPad("bat", 1)  = "bat"
	 * StringUtils.rightPad("bat", -1) = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size the size to pad to
	 * @return right padded String or original String if no padding is necessary,
	 *         {@code null} if null String input
	 */
	public static String rightPad(final String str, final int size) {
		return rightPad(str, size, ' ');
	}

	/**
	 * <p>
	 * Right pad a String with a specified character.
	 * </p>
	 *
	 * <p>
	 * The String is padded to the size of {@code size}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.rightPad(null, *, *)     = null
	 * StringUtils.rightPad("", 3, 'z')     = "zzz"
	 * StringUtils.rightPad("bat", 3, 'z')  = "bat"
	 * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
	 * StringUtils.rightPad("bat", 1, 'z')  = "bat"
	 * StringUtils.rightPad("bat", -1, 'z') = "bat"
	 * </pre>
	 *
	 * @param str     the String to pad out, may be null
	 * @param size    the size to pad to
	 * @param padChar the character to pad with
	 * @return right padded String or original String if no padding is necessary,
	 *         {@code null} if null String input
	 * @since 2.0
	 */
	public static String rightPad(final String str, final int size, final char padChar) {
		if (str == null) {
			return null;
		}
		final int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		return str.concat(repeat(padChar, pads));
	}

	/**
	 * <p>
	 * Right pad a String with a specified String.
	 * </p>
	 *
	 * <p>
	 * The String is padded to the size of {@code size}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.rightPad(null, *, *)      = null
	 * StringUtils.rightPad("", 3, "z")      = "zzz"
	 * StringUtils.rightPad("bat", 3, "yz")  = "bat"
	 * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
	 * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
	 * StringUtils.rightPad("bat", 1, "yz")  = "bat"
	 * StringUtils.rightPad("bat", -1, "yz") = "bat"
	 * StringUtils.rightPad("bat", 5, null)  = "bat  "
	 * StringUtils.rightPad("bat", 5, "")    = "bat  "
	 * </pre>
	 *
	 * @param str    the String to pad out, may be null
	 * @param size   the size to pad to
	 * @param padStr the String to pad with, null or empty treated as single space
	 * @return right padded String or original String if no padding is necessary,
	 *         {@code null} if null String input
	 */
	public static String rightPad(final String str, final int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = SPACE;
		}
		final int padLen = padStr.length();
		final int strLen = str.length();
		final int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1) {
			return rightPad(str, size, padStr.charAt(0));
		}
		if (pads == padLen) {
			return str.concat(padStr);
		} else if (pads < padLen) {
			return str.concat(padStr.substring(0, pads));
		} else {
			final char[] padding = new char[pads];
			final char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return str.concat(new String(padding));
		}
	}

	/**
	 * <p>
	 * Left pad a String with spaces (' ').
	 * </p>
	 *
	 * <p>
	 * The String is padded to the size of {@code size}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *)   = null
	 * StringUtils.leftPad("", 3)     = "   "
	 * StringUtils.leftPad("bat", 3)  = "bat"
	 * StringUtils.leftPad("bat", 5)  = "  bat"
	 * StringUtils.leftPad("bat", 1)  = "bat"
	 * StringUtils.leftPad("bat", -1) = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size the size to pad to
	 * @return left padded String or original String if no padding is necessary,
	 *         {@code null} if null String input
	 */
	public static String leftPad(final String str, final int size) {
		return leftPad(str, size, ' ');
	}

	/**
	 * <p>
	 * Left pad a String with a specified character.
	 * </p>
	 *
	 * <p>
	 * Pad to a size of {@code size}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *, *)     = null
	 * StringUtils.leftPad("", 3, 'z')     = "zzz"
	 * StringUtils.leftPad("bat", 3, 'z')  = "bat"
	 * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
	 * StringUtils.leftPad("bat", 1, 'z')  = "bat"
	 * StringUtils.leftPad("bat", -1, 'z') = "bat"
	 * </pre>
	 *
	 * @param str     the String to pad out, may be null
	 * @param size    the size to pad to
	 * @param padChar the character to pad with
	 * @return left padded String or original String if no padding is necessary,
	 *         {@code null} if null String input
	 * @since 2.0
	 */
	public static String leftPad(final String str, final int size, final char padChar) {
		if (str == null) {
			return null;
		}
		final int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		return repeat(padChar, pads).concat(str);
	}

	/**
	 * <p>
	 * Left pad a String with a specified String.
	 * </p>
	 *
	 * <p>
	 * Pad to a size of {@code size}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *, *)      = null
	 * StringUtils.leftPad("", 3, "z")      = "zzz"
	 * StringUtils.leftPad("bat", 3, "yz")  = "bat"
	 * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
	 * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
	 * StringUtils.leftPad("bat", 1, "yz")  = "bat"
	 * StringUtils.leftPad("bat", -1, "yz") = "bat"
	 * StringUtils.leftPad("bat", 5, null)  = "  bat"
	 * StringUtils.leftPad("bat", 5, "")    = "  bat"
	 * </pre>
	 *
	 * @param str    the String to pad out, may be null
	 * @param size   the size to pad to
	 * @param padStr the String to pad with, null or empty treated as single space
	 * @return left padded String or original String if no padding is necessary,
	 *         {@code null} if null String input
	 */
	public static String leftPad(final String str, final int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = SPACE;
		}
		final int padLen = padStr.length();
		final int strLen = str.length();
		final int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1) {
			return leftPad(str, size, padStr.charAt(0));
		}

		if (pads == padLen) {
			return padStr.concat(str);
		} else if (pads < padLen) {
			return padStr.substring(0, pads).concat(str);
		} else {
			final char[] padding = new char[pads];
			final char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return new String(padding).concat(str);
		}
	}

	/**
	 * <p>
	 * Repeat a String {@code repeat} times to form a new String.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.repeat(null, 2) = null
	 * StringUtils.repeat("", 0)   = ""
	 * StringUtils.repeat("", 2)   = ""
	 * StringUtils.repeat("a", 3)  = "aaa"
	 * StringUtils.repeat("ab", 2) = "abab"
	 * StringUtils.repeat("a", -2) = ""
	 * </pre>
	 *
	 * @param str    the String to repeat, may be null
	 * @param repeat number of times to repeat str, negative treated as zero
	 * @return a new String consisting of the original String repeated, {@code null}
	 *         if null String input
	 */
	public static String repeat(final String str, final int repeat) {
		if (str == null) {
			return null;
		}
		if (repeat <= 0) {
			return EMPTY;
		}
		final int inputLength = str.length();
		if (repeat == 1 || inputLength == 0) {
			return str;
		}
		final int outputLength = inputLength * repeat;
		switch (inputLength) {
		case 1:
			return repeat(str.charAt(0), repeat);
		case 2:
			final char ch0 = str.charAt(0);
			final char ch1 = str.charAt(1);
			final char[] output2 = new char[outputLength];
			for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
				output2[i] = ch0;
				output2[i + 1] = ch1;
			}
			return new String(output2);
		default:
			final StringBuilder buf = new StringBuilder(outputLength);
			for (int i = 0; i < repeat; i++) {
				buf.append(str);
			}
			return buf.toString();
		}
	}

	/**
	 * <p>
	 * Repeat a String {@code repeat} times to form a new String, with a String
	 * separator injected each time.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.repeat(null, null, 2) = null
	 * StringUtils.repeat(null, "x", 2)  = null
	 * StringUtils.repeat("", null, 0)   = ""
	 * StringUtils.repeat("", "", 2)     = ""
	 * StringUtils.repeat("", "x", 3)    = "xxx"
	 * StringUtils.repeat("?", ", ", 3)  = "?, ?, ?"
	 * </pre>
	 *
	 * @param str       the String to repeat, may be null
	 * @param separator the String to inject, may be null
	 * @param repeat    number of times to repeat str, negative treated as zero
	 * @return a new String consisting of the original String repeated, {@code null}
	 *         if null String input
	 * @since 2.5
	 */
	public static String repeat(final String str, final String separator, final int repeat) {
		if (str == null || separator == null) {
			return repeat(str, repeat);
		}
		// given that repeat(String, int) is quite optimized, better to rely on it than
		// try and splice this into it
		final String result = repeat(str + separator, repeat);
		return removeEnd(result, separator);
	}

	/**
	 * <p>
	 * Returns padding using the specified delimiter repeated to a given length.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.repeat('e', 0)  = ""
	 * StringUtils.repeat('e', 3)  = "eee"
	 * StringUtils.repeat('e', -2) = ""
	 * </pre>
	 *
	 * <p>
	 * Note: this method doesn't not support padding with
	 * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode
	 * Supplementary Characters</a> as they require a pair of {@code char}s to be
	 * represented. If you are needing to support full I18N of your applications
	 * consider using {@link #repeat(String, int)} instead.
	 * </p>
	 *
	 * @param ch     character to repeat
	 * @param repeat number of times to repeat char, negative treated as zero
	 * @return String with repeated character
	 * @see #repeat(String, int)
	 */
	public static String repeat(final char ch, final int repeat) {
		if (repeat <= 0) {
			return EMPTY;
		}
		final char[] buf = new char[repeat];
		for (int i = repeat - 1; i >= 0; i--) {
			buf[i] = ch;
		}
		return new String(buf);
	}

	/**
	 * <p>
	 * Removes a substring only if it is at the end of a source string, otherwise
	 * returns the source string.
	 * </p>
	 *
	 * <p>
	 * A {@code null} source string will return {@code null}. An empty ("") source
	 * string will return the empty string. A {@code null} search string will return
	 * the source string.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.removeEnd(null, *)      = null
	 * StringUtils.removeEnd("", *)        = ""
	 * StringUtils.removeEnd(*, null)      = *
	 * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
	 * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
	 * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeEnd("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str    the source String to search, may be null
	 * @param remove the String to search for and remove, may be null
	 * @return the substring with the string removed if found, {@code null} if null
	 *         String input
	 * @since 2.1
	 */
	public static String removeEnd(final String str, final String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.endsWith(remove)) {
			return str.substring(0, str.length() - remove.length());
		}
		return str;
	}

	/**
	 * <p>
	 * Splits the provided text into an array, using whitespace as the separator.
	 * Whitespace is defined by {@link Character#isWhitespace(char)}.
	 * </p>
	 *
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator. For more control over the split use
	 * the StrTokenizer class.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.split(null)       = null
	 * StringUtils.split("")         = []
	 * StringUtils.split("abc def")  = ["abc", "def"]
	 * StringUtils.split("abc  def") = ["abc", "def"]
	 * StringUtils.split(" abc ")    = ["abc"]
	 * </pre>
	 *
	 * @param str the String to parse, may be null
	 * @return an array of parsed Strings, {@code null} if null String input
	 */
	public static String[] split(final String str) {
		return split(str, null, -1);
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
	 * A {@code null} input String returns {@code null}.
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
	 * @return an array of parsed Strings, {@code null} if null String input
	 * @since 2.0
	 */
	public static String[] split(final String str, final char separatorChar) {
		return splitWorker(str, separatorChar, false);
	}

	/**
	 * <p>
	 * Splits the provided text into an array, separators specified. This is an
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
	 * A {@code null} input String returns {@code null}. A {@code null}
	 * separatorChars splits on whitespace.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.split(null, *)         = null
	 * StringUtils.split("", *)           = []
	 * StringUtils.split("abc def", null) = ["abc", "def"]
	 * StringUtils.split("abc def", " ")  = ["abc", "def"]
	 * StringUtils.split("abc  def", " ") = ["abc", "def"]
	 * StringUtils.split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
	 * </pre>
	 *
	 * @param str            the String to parse, may be null
	 * @param separatorChars the characters used as the delimiters, {@code null}
	 *                       splits on whitespace
	 * @return an array of parsed Strings, {@code null} if null String input
	 */
	public static String[] split(final String str, final String separatorChars) {
		return splitWorker(str, separatorChars, -1);
	}

	/**
	 * <p>
	 * Splits the provided text into an array with a maximum length, separators
	 * specified.
	 * </p>
	 *
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}. A {@code null}
	 * separatorChars splits on whitespace.
	 * </p>
	 *
	 * <p>
	 * If more than {@code max} delimited substrings are found, the last returned
	 * string includes all characters after the first {@code max - 1} returned
	 * strings (including separator characters).
	 * </p>
	 *
	 * <pre>
	 * StringUtils.split(null, *, *)            = null
	 * StringUtils.split("", *, *)              = []
	 * StringUtils.split("ab cd ef", null, 0)   = ["ab", "cd", "ef"]
	 * StringUtils.split("ab   cd ef", null, 0) = ["ab", "cd", "ef"]
	 * StringUtils.split("ab:cd:ef", ":", 0)    = ["ab", "cd", "ef"]
	 * StringUtils.split("ab:cd:ef", ":", 2)    = ["ab", "cd:ef"]
	 * </pre>
	 *
	 * @param str            the String to parse, may be null
	 * @param separatorChars the characters used as the delimiters, {@code null}
	 *                       splits on whitespace
	 * @param max            the maximum number of elements to include in the array.
	 *                       A zero or negative value implies no limit
	 * @return an array of parsed Strings, {@code null} if null String input
	 */
	public static String[] split(final String str, final String separatorChars, final int max) {
		return splitWorker(str, separatorChars, max);
	}

	/**
	 * <p>
	 * Splits the provided text into an array, separator string specified.
	 * </p>
	 *
	 * <p>
	 * The separator(s) will not be included in the returned String array. Adjacent
	 * separators are treated as one separator.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}. A {@code null} separator
	 * splits on whitespace.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.splitByWholeSeparator(null, *)               = null
	 * StringUtils.splitByWholeSeparator("", *)                 = []
	 * StringUtils.splitByWholeSeparator("ab de fg", null)      = ["ab", "de", "fg"]
	 * StringUtils.splitByWholeSeparator("ab   de fg", null)    = ["ab", "de", "fg"]
	 * StringUtils.splitByWholeSeparator("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
	 * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
	 * </pre>
	 *
	 * @param str       the String to parse, may be null
	 * @param separator String containing the String to be used as a delimiter,
	 *                  {@code null} splits on whitespace
	 * @return an array of parsed Strings, {@code null} if null String was input
	 */
	public static String[] splitByWholeSeparator(final String str, final String separator) {
		return splitByWholeSeparatorWorker(str, separator, -1);
	}

	/**
	 * <p>
	 * Splits the provided text into an array, separator string specified. Returns a
	 * maximum of {@code max} substrings.
	 * </p>
	 *
	 * <p>
	 * The separator(s) will not be included in the returned String array. Adjacent
	 * separators are treated as one separator.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}. A {@code null} separator
	 * splits on whitespace.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.splitByWholeSeparator(null, *, *)               = null
	 * StringUtils.splitByWholeSeparator("", *, *)                 = []
	 * StringUtils.splitByWholeSeparator("ab de fg", null, 0)      = ["ab", "de", "fg"]
	 * StringUtils.splitByWholeSeparator("ab   de fg", null, 0)    = ["ab", "de", "fg"]
	 * StringUtils.splitByWholeSeparator("ab:cd:ef", ":", 2)       = ["ab", "cd:ef"]
	 * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 5) = ["ab", "cd", "ef"]
	 * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2) = ["ab", "cd-!-ef"]
	 * </pre>
	 *
	 * @param str       the String to parse, may be null
	 * @param separator String containing the String to be used as a delimiter,
	 *                  {@code null} splits on whitespace
	 * @param max       the maximum number of elements to include in the returned
	 *                  array. A zero or negative value implies no limit.
	 * @return an array of parsed Strings, {@code null} if null String was input
	 */
	public static String[] splitByWholeSeparator(final String str, final String separator, final int max) {
		return splitByWholeSeparatorWorker(str, separator, max);
	}

	/**
	 * Performs the logic for the {@code splitByWholeSeparatorPreserveAllTokens}
	 * methods.
	 *
	 * @param str               the String to parse, may be {@code null}
	 * @param separator         String containing the String to be used as a
	 *                          delimiter, {@code null} splits on whitespace
	 * @param max               the maximum number of elements to include in the
	 *                          returned array. A zero or negative value implies no
	 *                          limit.
	 * @param preserveAllTokens if {@code true}, adjacent separators are treated as
	 *                          empty token separators; if {@code false}, adjacent
	 *                          separators are treated as one separator.
	 * @return an array of parsed Strings, {@code null} if null String input
	 * @since 2.4
	 */
	private static String[] splitByWholeSeparatorWorker(final String str, final String separator, final int max) {
		if (str == null) {
			return null;
		}

		final int len = str.length();

		if (len == 0) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}

		if (separator == null || EMPTY.equals(separator)) {
			// Split on whitespace.
			return splitWorker(str, null, max);
		}

		final int separatorLength = separator.length();

		final ArrayList<String> substrings = new ArrayList<>();
		int numberOfSubstrings = 0;
		int beg = 0;
		int end = 0;
		while (end < len) {
			end = str.indexOf(separator, beg);

			if (end > -1) {
				if (end > beg) {
					numberOfSubstrings += 1;

					if (numberOfSubstrings == max) {
						end = len;
						substrings.add(str.substring(beg));
					} else {
						// The following is OK, because String.substring( beg, end ) excludes
						// the character at the position 'end'.
						substrings.add(str.substring(beg, end));

						// Set the starting point for the next search.
						// The following is equivalent to beg = end + (separatorLength - 1) + 1,
						// which is the right calculation:
						beg = end + separatorLength;
					}
				} else {
					beg = end + separatorLength;
				}
			} else {
				// String.substring( beg ) goes from 'beg' to the end of the String.
				substrings.add(str.substring(beg));
				end = len;
			}
		}

		return substrings.toArray(EMPTY_STRING_ARRAY);
	}

	/**
	 * Performs the logic for the {@code split} and {@code splitPreserveAllTokens}
	 * methods that do not return a maximum array length.
	 *
	 * @param str               the String to parse, may be {@code null}
	 * @param separatorChar     the separate character
	 * @param preserveAllTokens if {@code true}, adjacent separators are treated as
	 *                          empty token separators; if {@code false}, adjacent
	 *                          separators are treated as one separator.
	 * @return an array of parsed Strings, {@code null} if null String input
	 */
	private static String[] splitWorker(final String str, final char separatorChar, final boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)

		if (str == null) {
			return null;
		}
		final int len = str.length();
		if (len == 0) {
			return EMPTY_STRING_ARRAY;
		}
		final List<String> list = new ArrayList<String>();
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
		if (match || preserveAllTokens && lastMatch) {
			list.add(str.substring(start, i));
		}
		return list.toArray(new String[list.size()]);
	}

	private static String[] splitWorker(final String str, final String separatorChars, final int max) {
		// Performance tuned for 2.0 (JDK1.4)
		// Direct code is quicker than StringTokenizer.
		// Also, StringTokenizer uses isSpace() not isWhitespace()

		if (str == null) {
			return null;
		}
		final int len = str.length();
		if (len == 0) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		final List<String> list = new ArrayList<>();
		int sizePlus1 = 1;
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		if (separatorChars == null) {
			// Null separator means use whitespace
			while (i < len) {
				if (Character.isWhitespace(str.charAt(i))) {
					if (match) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else if (separatorChars.length() == 1) {
			// Optimise 1 character case
			final char sep = separatorChars.charAt(0);
			while (i < len) {
				if (str.charAt(i) == sep) {
					if (match) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else {
			// standard case
			while (i < len) {
				if (separatorChars.indexOf(str.charAt(i)) >= 0) {
					if (match) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		}
		if (match || lastMatch) {
			list.add(str.substring(start, i));
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * <p>
	 * Search a CharSequence to find the first index of any character in the given
	 * set of characters.
	 * </p>
	 *
	 * <p>
	 * A {@code null} String will return {@code -1}. A {@code null} search string
	 * will return {@code -1}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.indexOfAny(null, *)            = -1
	 * StringUtils.indexOfAny("", *)              = -1
	 * StringUtils.indexOfAny(*, null)            = -1
	 * StringUtils.indexOfAny(*, "")              = -1
	 * StringUtils.indexOfAny("zzabyycdxx", "za") = 0
	 * StringUtils.indexOfAny("zzabyycdxx", "by") = 3
	 * StringUtils.indexOfAny("aba","z")          = -1
	 * </pre>
	 *
	 * @param cs          the CharSequence to check, may be null
	 * @param searchChars the chars to search for, may be null
	 * @return the index of the chars, -1 if no match or null input
	 * @since 2.0
	 * @since 3.0 Changed signature from indexOfAny(String, String) to
	 *        indexOfAny(CharSequence, String)
	 */
	public static int indexOfAny(final CharSequence cs, final String searchChars) {
		if (isEmpty(cs) || isEmpty(searchChars)) {
			return INDEX_NOT_FOUND;
		}
		return indexOfAny(cs, searchChars.toCharArray());
	}

	/**
	 * <p>
	 * Search a CharSequence to find the first index of any character in the given
	 * set of characters.
	 * </p>
	 *
	 * <p>
	 * A {@code null} String will return {@code -1}. A {@code null} or zero length
	 * search array will return {@code -1}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.indexOfAny(null, *)                = -1
	 * StringUtils.indexOfAny("", *)                  = -1
	 * StringUtils.indexOfAny(*, null)                = -1
	 * StringUtils.indexOfAny(*, [])                  = -1
	 * StringUtils.indexOfAny("zzabyycdxx",['z','a']) = 0
	 * StringUtils.indexOfAny("zzabyycdxx",['b','y']) = 3
	 * StringUtils.indexOfAny("aba", ['z'])           = -1
	 * </pre>
	 *
	 * @param cs          the CharSequence to check, may be null
	 * @param searchChars the chars to search for, may be null
	 * @return the index of the chars, -1 if no match or null input
	 * @since 2.0
	 * @since 3.0 Changed signature from indexOfAny(String, char[]) to
	 *        indexOfAny(CharSequence, char...)
	 */
	public static int indexOfAny(final CharSequence cs, final char... searchChars) {
		if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
			return INDEX_NOT_FOUND;
		}
		final int csLen = cs.length();
		final int csLast = csLen - 1;
		final int searchLen = searchChars.length;
		final int searchLast = searchLen - 1;
		for (int i = 0; i < csLen; i++) {
			final char ch = cs.charAt(i);
			for (int j = 0; j < searchLen; j++) {
				if (searchChars[j] == ch) {
					if (i < csLast && j < searchLast && Character.isHighSurrogate(ch)) {
						// ch is a supplementary character
						if (searchChars[j + 1] == cs.charAt(i + 1)) {
							return i;
						}
					} else {
						return i;
					}
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	/**
	 * 
	 * 查找字符串的方法
	 * 
	 * @param str         string
	 * @param searchChars search chars
	 * @param startPos    start position
	 * @return 出现在第几个字符
	 */
	public static int indexOfAny(String str, char[] searchChars, int startPos) {
		if (isEmpty(str) || ArrayUtils.isEmpty(searchChars)) {
			return -1;
		}
		for (int i = startPos; i < str.length(); i++) {
			char ch = str.charAt(i);
			for (int j = 0; j < searchChars.length; j++) {
				if (searchChars[j] == ch) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * @param fname file name.
	 * @param to    escape string.
	 * @return 将文件名中的非法字符替换成合法字符
	 */
	public static String toFilename(String fname, String to) {
		StringBuilder sb = new StringBuilder();
		int len = fname.length();
		if (fname.charAt(len - 1) == '.') {
			len--;
		}
		for (int index = 0; index < len; index++) {
			char c = fname.charAt(index);
			if (invalidCharsInFilename.indexOf(c) > -1) {
				sb.append(to);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * @param source  source
	 * @param charset charset
	 * @return URL解码
	 */
	@SneakyThrows
	public static String urlDecode(String source, Charset charset) {
		return URLDecoder.decode(source, charset.name());
	}

	/**
	 * @param source source
	 * @return URL解码
	 */
	@SneakyThrows
	public static String urlDecode(String source) {
		return URLDecoder.decode(source, "UTF-8");
	}

	/**
	 * @param source source
	 * @return URL编码
	 */
	@SneakyThrows
	public static String urlEncode(String source) {
		return URLEncoder.encode(source, "UTF-8").replace("+", "%20");
	}

	/**
	 * @param source  source
	 * @param charset charset
	 * @return URL编码
	 */
	@SneakyThrows
	public static String urlEncode(String source, Charset charset) {
		return URLEncoder.encode(source, charset.name()).replace("+", "%20");
	}

	/**
	 * <p>
	 * Removes a substring only if it is at the beginning of a source string,
	 * otherwise returns the source string.
	 * </p>
	 *
	 * <p>
	 * A {@code null} source string will return {@code null}. An empty ("") source
	 * string will return the empty string. A {@code null} search string will return
	 * the source string.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.removeStart(null, *)      = null
	 * StringUtils.removeStart("", *)        = ""
	 * StringUtils.removeStart(*, null)      = *
	 * StringUtils.removeStart("www.domain.com", "www.")   = "domain.com"
	 * StringUtils.removeStart("domain.com", "www.")       = "domain.com"
	 * StringUtils.removeStart("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeStart("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str    the source String to search, may be null
	 * @param remove the String to search for and remove, may be null
	 * @return the substring with the string removed if found, {@code null} if null
	 *         String input
	 * @since 2.1
	 */
	public static String removeStart(final String str, final String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.startsWith(remove)) {
			return str.substring(remove.length());
		}
		return str;
	}

	private static final Random random = new Random();

	/**
	 * @return 产生随机数字
	 */
	public static String randomString() {
		int i = random.nextInt();
		return String.valueOf(Math.abs(i));
	}

	/**
	 * @param string text
	 * @return 把字符串左边的空格给去掉
	 */
	public static final String ltrim(String string) {
		if (string == null) {
			return string;
		}
		int len = string.length();
		int st = 0;
		int off = 0; /* avoid getfield opcode */
		char[] val = string.toCharArray(); /* avoid getfield opcode */
		while ((st < len) && (val[off + st] <= ' ')) {
			st++;
		}
		return (st > 0) ? string.substring(st, len) : string;
	}

	/**
	 * @param text      text
	 * @param trimChars characters to remove.
	 * @return 从左侧删除指定的字符
	 */
	public static final String ltrim(String text, char... trimChars) {
		if (text == null) {
			return text;
		}
		int len = text.length();
		int st = 0;
		int off = 0;
		while ((st < len) && (ArrayUtils.contains(trimChars, text.charAt(off + st)))) {
			st++;
		}
		return (st > 0) ? text.substring(st, len) : text;
	}

	/**
	 * @param s         string
	 * @param trimChars characters to remove.
	 * @return 从右侧删除指定的字符
	 */
	public static String rtrim(String s, char... trimChars) {
		if (s == null) {
			return s;
		}
		int len = s.length();
		int st = 0;
		int off = 0; /* avoid getfield opcode */
		while ((st < len) && ArrayUtils.contains(trimChars, s.charAt(off + len - 1))) {
			len--;
		}
		return (len < s.length()) ? s.substring(st, len) : s;
	}

	/**
	 * @param text text
	 * @return 把字符串右边的空格给去掉
	 */
	public static final String rtrim(String text) {
		if (text == null) {
			return text;
		}
		int len = text.length();
		int st = 0;
		int off = 0; /* avoid get field opcode */
		char[] val = text.toCharArray(); /* avoid getfield opcode */
		while ((st < len) && (val[off + len - 1] <= ' ')) {
			len--;
		}
		return (len < text.length()) ? text.substring(st, len) : text;
	}

	/**
	 * @param s          string
	 * @param lTrimChars 左侧要trim的字符
	 * @param rTrimChars 右侧要trim的字符
	 * @return 左右两边做不同的trim
	 */
	public static final String lrtrim(String s, char[] lTrimChars, char[] rTrimChars) {
		if (s == null) {
			return s;
		}
		int len = s.length();
		int st = 0;
		int off = 0;
		while ((st < len) && (ArrayUtils.contains(lTrimChars, s.charAt(off + st)))) {
			st++;
		}
		while ((st < len) && ArrayUtils.contains(rTrimChars, s.charAt(off + len - 1))) {
			len--;
		}
		return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
	}

	/**
	 * 计算CRC摘要,8位十六进制数
	 * 
	 * @param in input stream.
	 * @return CRC
	 */
	@SneakyThrows
	public static String getCRC(InputStream in) {
		CRC32 crc32 = new CRC32();
		byte[] buffer = new byte[65536];
		int len;
		while ((len = in.read(buffer)) != -1) {
			crc32.update(buffer, 0, len);
		}
		return Long.toHexString(crc32.getValue());
	}

	/**
	 * 计算CRC摘要,8位十六进制数
	 * 
	 * @param s string
	 * @return CRC
	 */
	public static String getCRC(String s) {
		try (ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes())) {
			return getCRC(in);
		}
	}

	/**
	 * 计算MD5摘要,
	 * 
	 * @param s 输入
	 * @return 32位十六进制数的MD5值
	 */
	public final static String getMD5(String s) {
		try (ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes())) {
			byte[] md = hash(in, "MD5");
			return join(md, (char) 0, 0, md.length);
		}
	}

	/**
	 * 计算MD5摘要
	 * 
	 * @param string source text.
	 * @return MD5 md5 value in base64.
	 */
	public final static String getMD5InBase64(String string) {
		try (ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes())) {
			byte[] md = hash(in, "MD5");
			return JefBase64.encode(md);
		}
	}

	public static class ByteArrayInputStream extends InputStream{
		protected byte buf[];
		protected int pos;
		protected int count;
		public ByteArrayInputStream(byte buf[]) {
			this.buf = buf;
			this.pos = 0;
			this.count = buf.length;
		}
		public int read() {
			return (pos < count) ? (buf[pos++] & 0xff) : -1;
		}
		public int read(byte b[], int off, int len) {
			Objects.checkFromIndexSize(off, len, b.length);
			if (pos >= count) {
				return -1;
			}
			int avail = count - pos;
			if (len > avail) {
				len = avail;
			}
			if (len <= 0) {
				return 0;
			}
			System.arraycopy(buf, pos, b, off, len);
			pos += len;
			return len;
		}
		public void close() {
		}
	}

	/**
	 * 计算MD5摘要
	 * 
	 * @param file file
	 * @return 32位十六进制数的MD5值
	 */
	public final static String getMD5(InputStream in) {
		byte[] md = hash(in, "MD5");
		return join(md, (char) 0, 0, md.length);
	}

	/**
	 * 计算SHA-1
	 * 
	 * @param text text
	 * @return SHA-1 value in binary string.
	 */
	public final static String getSHA1(String text) {
		ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes());
		byte[] md = hash(in, "SHA-1");
		return join(md, (char) 0, 0, md.length);
	}

	public final static String getSHA1(InputStream in) {
		byte[] md = hash(in, "SHA-1");
		return join(md, (char) 0, 0, md.length);
	}

	/**
	 * 计算SHA256
	 * 
	 * @param string string
	 * @return SHA256
	 */
	public final static String getSHA256(String string) {
		ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes());
		byte[] md = hash(in, "SHA-256");
		return join(md, (char) 0, 0, md.length);
	}

	public final static String getSHA256(InputStream in) {
		byte[] md = hash(in, "SHA-256");
		return join(md, (char) 0, 0, md.length);
	}

	/*
	 * 计算消息摘要
	 */
	@SneakyThrows
	public final static byte[] hash(InputStream in, String algorithm) {
		MessageDigest mdTemp = MessageDigest.getInstance(algorithm);
		byte[] b = new byte[4096];
		int len;
		while ((len = in.read(b)) != -1) {
			mdTemp.update(b, 0, len);
		}
		return mdTemp.digest();
	}

	public static final char[] hexDigitsU = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };
	public static final char[] hexDigitsL = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	/**
	 * 将数组或列表拼成文本
	 * 
	 * @param bytes bytes
	 * @param c     character
	 * @return 拼接文本
	 */
	public static String join(byte[] bytes, char c) {
		if (bytes == null)
			return EMPTY;
		return join(bytes, c, 0, bytes.length);
	}

	/**
	 * 将数组或列表拼成文本
	 * 
	 * @param bytes  bytes
	 * @param dchar  dchar
	 * @param offset offset
	 * @param len    len
	 * @return 拼接文本
	 */
	public static String join(byte[] bytes, char dchar, int offset, int len) {
		if (bytes == null || bytes.length == 0 || len == 0)
			return EMPTY;
		return dchar == NULL_CHAR ? toHex0(bytes, offset, len, hexDigitsL)
				: toHex1(bytes, offset, len, dchar, hexDigitsL);
	}

	public static String joinUpper(byte[] bytes, char dchar, int offset, int len) {
		if (bytes == null || bytes.length == 0 || len == 0)
			return EMPTY;
		return dchar == NULL_CHAR ? toHex0(bytes, offset, len, hexDigitsU)
				: toHex1(bytes, offset, len, dchar, hexDigitsU);
	}

	/**
	 * 将数组或列表拼成文本
	 * 
	 * @param objects   objects
	 * @param separator separator
	 * @return 拼接文本
	 */
	public static String join(Object[] objects, String separator) {
		if (objects == null || objects.length == 0)
			return EMPTY;
		String[] ss = new String[objects.length];
		int len = 0;
		int sepLen = separator.length();
		for (int i = 0; i < objects.length; i++) {
			Object o = objects[i];
			ss[i] = o == null ? "" : o.toString();
			len += ss[i].length();
			len += sepLen;
		}
		StringBuilder sb = new StringBuilder(len - sepLen);
		int n = 0;
		sb.append(ss[n++]);
		while (n < ss.length) {
			sb.append(separator);
			sb.append(ss[n++]);
		}
		return sb.toString();
	}

	/**
	 * <p>
	 * Joins the elements of the provided {@code Iterable} into a single String
	 * containing the provided elements.
	 * </p>
	 *
	 * <p>
	 * No delimiter is added before or after the list. A {@code null} separator is
	 * the same as an empty String ("").
	 * </p>
	 *
	 * <p>
	 * See the examples here: {@link #join(Object[],String)}.
	 * </p>
	 *
	 * @param iterable  the {@code Iterable} providing the values to join together,
	 *                  may be null
	 * @param separator the separator character to use, null treated as ""
	 * @return the joined String, {@code null} if null iterator input
	 * @since 2.3
	 */
	public static String join(final Iterable<?> iterable, final String separator) {
		if (iterable == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		joinTo(iterable, separator, sb);
		return sb.toString();
	}

	/**
	 * 用于字符串的拼接
	 * 
	 * @param data 数据
	 * @param sep  分隔符
	 * @param sb   拼接目标
	 */
	public static void joinTo(Iterable<?> data, String sep, StringBuilder sb) {
		if (data == null)
			return;
		Iterator<?> iterator = data.iterator();
		if (iterator.hasNext()) {
			sb.append(String.valueOf(iterator.next()));
			while (iterator.hasNext()) {
				Object obj = iterator.next();
				sb.append(sep).append(toString(obj));
			}
		}
	}

	/*
	 * 有分隔符时的编码处理
	 * 
	 * @return
	 */
	private static String toHex1(byte[] b, int offset, int len, char dchar, char[] hexDigits) {
		int j = offset + len;
		if (j > b.length)
			j = b.length; // 上限
		char[] str = new char[j * (3)];
		int k = 0;
		for (int i = offset; i < j; i++) {
			byte byte0 = b[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // >>是带符号移位， >>>是无符号移位
			str[k++] = hexDigits[byte0 & 0xf];
			str[k++] = dchar;
		}
		return new String(str, 0, k - 1);
	}

	/*
	 * 无分隔符时的编码处理
	 */
	private static String toHex0(byte[] b, int offset, int len, char[] hexDigits) {
		int j = offset + len;
		if (j > b.length)
			j = b.length; // 上限
		char[] str = new char[j * 2];
		int k = 0;
		for (int i = offset; i < j; i++) {
			byte byte0 = b[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // >>是带符号移位， >>>是无符号移位
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}

	/**
	 * 将二进制文本列表转换为字节数组
	 * 
	 * @param hexString hexString
	 * @param hasSpace  hasSpace
	 * @throws IOException If encounter IOException
	 * @return byte[] value
	 */
	public static byte[] fromHex(char[] hexString, boolean hasSpace){
		int len = hexString.length;
		byte[] result = new byte[hasSpace ? (len + 1) / 3 : len / 2];
		int count = 0;
		for (int i = 0; i < len; i++) {
			char c1 = hexString[i];
			char c2 = hexString[++i];
			int i1 = hexChar2Dec(c1);
			int i2 = hexChar2Dec(c2);
			result[count++] = (byte) ((i1 << 4) + i2);
			if (hasSpace)
				++i;
		}
		return result;
	}

	/**
	 * byte2hex的逆运算（有实际用处吗？） 实际使用可以用Byte Byte.parseByte("dd", 16);
	 * 
	 * @param hexString hexString
	 * @param hasSpace  hasSpace
	 * @return 二进制数据
	 */
	public static byte[] fromHex(CharSequence hexString, boolean hasSpace) {
		int len = hexString.length();
		byte[] result = new byte[hasSpace ? (len + 1) / 3 : len / 2];
		int count = 0;
		for (int i = 0; i < len; i++) {
			char c1 = hexString.charAt(i);
			char c2 = hexString.charAt(++i);
			int i1 = hexChar2Dec(c1);
			int i2 = hexChar2Dec(c2);
			result[count++] = (byte) ((i1 << 4) + i2);
			if (hasSpace)
				++i;
		}
		return result;
	}

	private static int hexChar2Dec(char hex) {
		if (hex > 47 && hex < 58) {
			hex -= 48;
		} else if (hex > 64 && hex < 71) {
			hex -= 55;
		} else if (hex > 96 && hex < 103) {
			hex -= 87;
		} else {
			throw new IllegalArgumentException(hex + "is not a valid hex char.");
		}
		return hex;
	}

	/**
	 * 合并多个String,在参数为3个和以内时请直接使用String.concat。
	 * 5个和超过5个String相加后，concat方法性能急剧下降，此时此方法最快
	 * 
	 * @param args args
	 * @return 拼接文本
	 */
	public final static String concat(String... args) {
		if (args.length == 1)
			return args[0];
		int n = 0;
		for (String s : args) {
			if (s == null)
				continue;
			n += s.length();
		}
		StringBuilder sb = new StringBuilder(n);
		for (String s : args) {
			if (s == null)
				continue;
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * @param string string
	 * @return true if 存在东亚字符
	 */
	public static boolean hasAsian(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c > 255 && c != 65279) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * @param arg3 arg3
	 * @return 覆盖apache-lang3中父类的方法，从两端取,Apache commons默认是从前取的
	 */
	public static String substringBetween(String arg1, String arg2, String arg3) {
		int a = arg1.indexOf(arg2);
		int b = arg1.lastIndexOf(arg3);
		if (a == -1 || b == -1)
			return EMPTY;
		if (a == b)
			return EMPTY;
		if (a > b)
			return EMPTY;
		return arg1.substring(a + arg2.length(), b);
	}

	/**
	 * <p>
	 * Converts a String to lower case as per {@link String#toLowerCase()}.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.lowerCase(null)  = null
	 * StringUtils.lowerCase("")    = ""
	 * StringUtils.lowerCase("aBc") = "abc"
	 * </pre>
	 *
	 * <p>
	 * <strong>Note:</strong> As described in the documentation for
	 * {@link String#toLowerCase()}, the result of this method is affected by the
	 * current locale. should be used with a specific locale (e.g.
	 * {@link Locale#ENGLISH}).
	 * </p>
	 *
	 * @param str the String to lower case, may be null
	 * @return the lower cased String, {@code null} if null String input
	 */
	public static String lowerCase(final String str) {
		if (str == null) {
			return null;
		}
		return str.toLowerCase();
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Converts a String to upper case as per {@link String#toUpperCase()}.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.upperCase(null)  = null
	 * StringUtils.upperCase("")    = ""
	 * StringUtils.upperCase("aBc") = "ABC"
	 * </pre>
	 *
	 * <p>
	 * <strong>Note:</strong> As described in the documentation for
	 * {@link String#toUpperCase()}, the result of this method is affected by the
	 * current locale. should be used with a specific locale (e.g.
	 * {@link Locale#ENGLISH}).
	 * </p>
	 *
	 * @param str the String to upper case, may be null
	 * @return the upper-cased String, {@code null} if null String input
	 */
	public static String upperCase(final String str) {
		if (str == null) {
			return null;
		}
		return str.toUpperCase();
	}

	/**
	 * 获得32位的Hex uuid
	 * 
	 * @return String of UUID(size=36)
	 */
	public static final String generateGuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public static String trimToEmpty(String value) {
		return value == null ? "" : value.trim();
	}

	/**
	 * 文本截断
	 * 
	 * @param string    string
	 * @param maxLength maxLength
	 * @param append    阶段后要添加的内容
	 * @return String truncated.
	 */
	public static String truncate(String string, int maxLength, String... append) {
		if (string.length() <= maxLength)
			return string;
		string = string.substring(0, maxLength);
		if (append.length > 0) {
			return string.concat(append[0]);
		} else {
			return string;
		}
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer. Trims
	 * tokens and omits empty tokens.
	 * <p>
	 * The given delimiters string is supposed to consist of any number of delimiter
	 * characters. Each of those characters can be used to separate tokens. A
	 * delimiter is always a single character; for multi-character delimiters,
	 * consider using {@code delimitedListToStringArray}
	 * 
	 * @param str        the String to tokenize
	 * @param delimiters the delimiter characters, assembled as String (each of
	 *                   those characters is individually considered as delimiter).
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * <p>
	 * The given delimiters string is supposed to consist of any number of delimiter
	 * characters. Each of those characters can be used to separate tokens. A
	 * delimiter is always a single character; for multi-character delimiters,
	 * consider using {@code delimitedListToStringArray}
	 * 
	 * @param str               the String to tokenize
	 * @param delimiters        the delimiter characters, assembled as String (each
	 *                          of those characters is individually considered as
	 *                          delimiter)
	 * @param trimTokens        trim the tokens via String's {@code trim}
	 * @param ignoreEmptyTokens omit empty tokens from the result array (only
	 *                          applies to tokens that are empty after trimming;
	 *                          StringTokenizer will not consider subsequent
	 *                          delimiters as token in the first place).
	 * @return an array of the tokens ({@code null} if the input String was
	 *         {@code null})
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
			boolean ignoreEmptyTokens) {

		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return tokens.toArray(EMPTY_STRING_ARRAY);
	}

	/**
	 * Determines whether the sting 'searchIn' contains the string 'searchFor',
	 * disregarding case and leading whitespace
	 * 
	 * @param searchIn  the string to search in
	 * @param searchFor the string to search for
	 * 
	 * @return true if the string starts with 'searchFor' ignoring whitespace
	 */
	public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
		return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
	}

	/**
	 * Determines whether the sting 'searchIn' contains the string 'searchFor',
	 * disregarding case and leading whitespace
	 * 
	 * @param searchIn  the string to search in
	 * @param searchFor the string to search for
	 * @param beginPos  where to start searching
	 * 
	 * @return true if the string starts with 'searchFor' ignoring whitespace
	 */
	public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
		if (searchIn == null) {
			return searchFor == null;
		}

		int inLength = searchIn.length();

		for (; beginPos < inLength; beginPos++) {
			if (!Character.isWhitespace(searchIn.charAt(beginPos))) {
				break;
			}
		}

		return startsWithIgnoreCase(searchIn, beginPos, searchFor);
	}

	/**
	 * Determines whether the string 'searchIn' contains the string 'searchFor',
	 * dis-regarding case starting at 'startAt' Shorthand for a
	 * String.regionMatch(...)
	 * 
	 * @param searchIn  the string to search in
	 * @param startAt   the position to start at
	 * @param searchFor the string to search for
	 * 
	 * @return whether searchIn starts with searchFor, ignoring case
	 */
	public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
		return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
	}

	public static String removeChars(String str, char... remove) {
		if (isEmpty(str))
			return str;
		char chars[] = str.toCharArray();
		int pos = 0;
		for (int i = 0; i < chars.length; i++)
			if (!ArrayUtils.contains(remove, chars[i]))
				chars[pos++] = chars[i];
		return new String(chars, 0, pos);
	}

	public static boolean equalsIgnoreCase(final CharSequence str1, final CharSequence str2) {
		if (str1 == null || str2 == null) {
			return str1 == str2;
		} else if (str1 == str2) {
			return true;
		} else if (str1.length() != str2.length()) {
			return false;
		} else {
			int len = str1.length();
			for (int i = 0; i < len; i++) {
				char c1 = str1.charAt(i);
				char c2 = str2.charAt(i);
				if (c1 == c2) {
					continue;
				}
				if (Character.toUpperCase(c1) != Character.toUpperCase(c2)) {
					return false;
				}
			}
			return true;
		}
	}

	public static String removeBucket(String check) {
		if (isEmpty(check)) {
			return check;
		}
		char f = check.charAt(0);
		char l = check.charAt(check.length() - 1);
		if (f == '(' && l == ')') {
			return check.substring(1, check.length() - 1);
		}
		return check;
	}
}
