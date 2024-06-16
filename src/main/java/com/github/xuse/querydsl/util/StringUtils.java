package com.github.xuse.querydsl.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.zip.CRC32;

public class StringUtils {
	private static final int INDEX_NOT_FOUND=-1;
	/**
     * A String for a space character.
     *
     * @since 3.2
     */
    public static final String SPACE = " ";
    
	private static final String invalidCharsInFilename = "\t\\/|\"*?:<>\t\n\r";// 文件名中禁用的字符
	
	private static final char NULL_CHAR=(char)0;
	
	/**
	 * @param s
	 * @param defaultValue
	 * @return 文本转换为boolean，如果不能转换则返回默认值
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
	 * @param o
	 * @param defaultValue
	 * @return 文本转换到小数float
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
	 * @param o
	 * @param defaultValue
	 * @return 文本转换到小数double
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

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static boolean isEmpty(String user) {
		return user == null || user.length() == 0;
	}

	public static boolean isNotEmpty(String user) {
		return user != null && user.length() > 0;
	}
	
	/**
	 * @param data 数据
	 * @param sep  分隔符
	 * @return 字符串的拼接 
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
	
	////////////

	/**
	 * This is a string replacement method.
	 * @param source
	 * @param oldStr
	 * @param newStr
	 * @return text replaced
	 * 
	 */
	public final static String replaceString(String source, String oldStr, String newStr) {
		StringBuilder sb = new StringBuilder(source.length());
		int sind = 0;
		int cind = 0;
		while ((cind = source.indexOf(oldStr, sind)) != INDEX_NOT_FOUND) {
			sb.append(source.substring(sind, cind));
			sb.append(newStr);
			sind = cind + oldStr.length();
		}
		sb.append(source.substring(sind));
		return sb.toString();
	}

	/**
	 * Replace string
	 * @param source
	 * @param args
	 * @return replaced string. 
	 * 
	 */
	public final static String replaceString(String source, Object[] args) {
		int startIndex = 0;
		int openIndex = source.indexOf('{', startIndex);
		if (openIndex == INDEX_NOT_FOUND) {
			return source;
		}

		int closeIndex = source.indexOf('}', startIndex);
		if ((closeIndex == INDEX_NOT_FOUND) || (openIndex > closeIndex)) {
			return source;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(source.substring(startIndex, openIndex));
		while (true) {
			String intStr = source.substring(openIndex + 1, closeIndex);
			int index = Integer.parseInt(intStr);
			sb.append(args[index]);

			startIndex = closeIndex + 1;
			openIndex = source.indexOf('{', startIndex);
			if (openIndex == -1) {
				sb.append(source.substring(startIndex));
				break;
			}

			closeIndex = source.indexOf('}', startIndex);
			if ((closeIndex == -1) || (openIndex > closeIndex)) {
				sb.append(source.substring(startIndex));
				break;
			}
			sb.append(source.substring(startIndex, openIndex));
		}
		return sb.toString();
	}

	public final static String replaceString(String source, Map<String, Object> args) {
		int startIndex = 0;
		int openIndex = source.indexOf('{', startIndex);
		if (openIndex == -1) {
			return source;
		}

		int closeIndex = source.indexOf('}', startIndex);
		if ((closeIndex == -1) || (openIndex > closeIndex)) {
			return source;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(source.substring(startIndex, openIndex));
		while (true) {
			String key = source.substring(openIndex + 1, closeIndex);
			Object val = args.get(key);
			if (val != null) {
				sb.append(val);
			}

			startIndex = closeIndex + 1;
			openIndex = source.indexOf('{', startIndex);
			if (openIndex == -1) {
				sb.append(source.substring(startIndex));
				break;
			}

			closeIndex = source.indexOf('}', startIndex);
			if ((closeIndex == -1) || (openIndex > closeIndex)) {
				sb.append(source.substring(startIndex));
				break;
			}
			sb.append(source.substring(startIndex, openIndex));
		}
		return sb.toString();
	}

	/**
	 * This method is used to insert HTML block dynamically
	 *
	 * @param source        the HTML code to be processes
	 * @param bReplaceNl    if true '\n' will be replaced by <br>
	 * @param bReplaceTag   if true '<' will be replaced by &lt; and '>' will be
	 *                      replaced by &gt;
	 * @param bReplaceQuote if true '\"' will be replaced by &quot;
	 * @return html formatted.
	 */
	public final static String formatHtml(String source, boolean bReplaceNl, boolean bReplaceTag,
			boolean bReplaceQuote) {

		StringBuilder sb = new StringBuilder();
		int len = source.length();
		for (int i = 0; i < len; i++) {
			char c = source.charAt(i);
			switch (c) {
			case '\"':
				if (bReplaceQuote)
					sb.append("&quot;");
				else
					sb.append(c);
				break;

			case '<':
				if (bReplaceTag)
					sb.append("&lt;");
				else
					sb.append(c);
				break;

			case '>':
				if (bReplaceTag)
					sb.append("&gt;");
				else
					sb.append(c);
				break;

			case '\n':
				if (bReplaceNl) {
					if (bReplaceTag)
						sb.append("&lt;br&gt;");
					else
						sb.append("<br>");
				} else {
					sb.append(c);
				}
				break;

			case '\r':
				break;

			case '&':
				sb.append("&amp;");
				break;

			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public final static String pad(String src, char padChar, boolean rightPad, int totalLength) {

		int srcLength = src.length();
		if (srcLength >= totalLength) {
			return src;
		}

		int padLength = totalLength - srcLength;
		StringBuilder sb = new StringBuilder(padLength);
		for (int i = 0; i < padLength; ++i) {
			sb.append(padChar);
		}

		if (rightPad) {
			return src + sb.toString();
		} else {
			return sb.toString() + src;
		}
	}

	/**
	 * @param bytes
	 * @return Get hex string from byte array(lower cases).
	 */
	public final static String toHexString(byte[] bytes) {
		if(bytes==null || bytes.length==0) {
			return EMPTY;
		}
		return toHex0(bytes, 0, bytes.length, hexDigitsL);
	}
	
	/**
	 * @param bytes
	 * @return Get hex string from byte array(upper cases).
	 */
	public final static String toHexStringUppercase(byte[] bytes) {
		if(bytes==null || bytes.length==0) {
			return EMPTY;
		}
		return toHex0(bytes, 0, bytes.length, hexDigitsU);
	}

	/**
	 * Get byte array from hex string
	 * @param hexString
	 * @return byte array. 
	 */
	public final static byte[] toByteArray(String hexString) {
		int arrLength = hexString.length() >> 1;
		byte buff[] = new byte[arrLength];
		for (int i = 0; i < arrLength; i++) {
			int index = i << 1;
			String digit = hexString.substring(index, index + 2);
			buff[i] = (byte) Integer.parseInt(digit, 16);
		}
		return buff;
	}

	public static boolean isEmpty(CharSequence ext) {
		return ext == null || ext.length()==0;
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
		int tempIndex = -1;

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

		// get a good guess on the size of the result buffer so it doesn't have to
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
			tempIndex = -1;
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
	 * @param o
	 * @param defaultValue
	 * @return int value
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
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 将两个数值的比值作为百分比显示
	 * 
	 * @param a
	 * @param b
	 * @return 百分比显示
	 */ 
	public static String toPercent(long a, long b) {
		return String.valueOf(10000 * a / b / 100f).concat("%");
	}

	public static String toString(Object obj) {
		if (obj == null)
			return "";
		return obj.toString();
	}
	

	/**
	 * 返回子串，如果查找的字串不存在则返回全部 和substringAfter方法不同，substringAfter方法在查找不到时返回空串
	 * 
	 * @param source
	 * @param rev
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
	public static void repeat(Appendable sb, CharSequence str, int n) {
		if (n <= 0)
			return;
		try {
			for (int i = 0; i < n; i++) {
				sb.append(str);
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private static final String EMPTY="";

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
	 * @param o
	 * @param defaultValue
	 * @return long value
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
     * <p>Gets the substring before the first occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A {@code null} string input will return {@code null}.
     * An empty ("") string input will return the empty string.
     * A {@code null} separator will return the input string.</p>
     *
     * <p>If nothing is found, the string input is returned.</p>
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
     * @param str  the String to get a substring from, may be null
     * @param separator  the String to search for, may be null
     * @return the substring before the first occurrence of the separator,
     *  {@code null} if null String input
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
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == -1) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }
    

    /**
     * <p>Removes control characters (char &lt;= 32) from both
     * ends of this String returning {@code null} if the String is
     * empty ("") after the trim or if it is {@code null}.
     *
     * <pre>
     * StringUtils.trimToNull(null)          = null
     * StringUtils.trimToNull("")            = null
     * StringUtils.trimToNull("     ")       = null
     * StringUtils.trimToNull("abc")         = "abc"
     * StringUtils.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str  the String to be trimmed, may be null
     * @return the trimmed String,
     *  {@code null} if only chars &lt;= 32, empty or null String input
     * @since 2.0
     */
    public static String trimToNull(final String str) {
        final String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }
    
    // Trim
    //-----------------------------------------------------------------------
    /**
     * <p>Removes control characters (char &lt;= 32) from both
     * ends of this String, handling {@code null} by returning
     * {@code null}.</p>
     *
     * <p>The String is trimmed using {@link String#trim()}.
     * Trim removes start and end characters &lt;= 32.
     * <pre>
     * StringUtils.trim(null)          = null
     * StringUtils.trim("")            = ""
     * StringUtils.trim("     ")       = ""
     * StringUtils.trim("abc")         = "abc"
     * StringUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str  the String to be trimmed, may be null
     * @return the trimmed string, {@code null} if null String input
     */
    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }
    
    /**
     * <p>Gets the substring before the last occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A {@code null} string input will return {@code null}.
     * An empty ("") string input will return the empty string.
     * An empty or {@code null} separator will return the input string.</p>
     *
     * <p>If nothing is found, the string input is returned.</p>
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
     * @param str  the String to get a substring from, may be null
     * @param separator  the String to search for, may be null
     * @return the substring before the last occurrence of the separator,
     *  {@code null} if null String input
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
     * <p>Right pad a String with spaces (' ').</p>
     *
     * <p>The String is padded to the size of {@code size}.</p>
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
     * @param size  the size to pad to
     * @return right padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     */
    public static String rightPad(final String str, final int size) {
        return rightPad(str, size, ' ');
    }
    
    /**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;

    /**
     * <p>Right pad a String with a specified character.</p>
     *
     * <p>The String is padded to the size of {@code size}.</p>
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
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padChar  the character to pad with
     * @return right padded String or original String if no padding is necessary,
     *  {@code null} if null String input
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
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(repeat(padChar, pads));
    }

    /**
     * <p>Right pad a String with a specified String.</p>
     *
     * <p>The String is padded to the size of {@code size}.</p>
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
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padStr  the String to pad with, null or empty treated as single space
     * @return right padded String or original String if no padding is necessary,
     *  {@code null} if null String input
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
        if (padLen == 1 && pads <= PAD_LIMIT) {
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
     * <p>Left pad a String with spaces (' ').</p>
     *
     * <p>The String is padded to the size of {@code size}.</p>
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
     * @param size  the size to pad to
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     */
    public static String leftPad(final String str, final int size) {
        return leftPad(str, size, ' ');
    }

    /**
     * <p>Left pad a String with a specified character.</p>
     *
     * <p>Pad to a size of {@code size}.</p>
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
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padChar  the character to pad with
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
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
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    /**
     * <p>Left pad a String with a specified String.</p>
     *
     * <p>Pad to a size of {@code size}.</p>
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
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padStr  the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
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
        if (padLen == 1 && pads <= PAD_LIMIT) {
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
     * <p>Repeat a String {@code repeat} times to form a
     * new String.</p>
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
     * @param str  the String to repeat, may be null
     * @param repeat  number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     *  {@code null} if null String input
     */
    public static String repeat(final String str, final int repeat) {
        // Performance tuned for 2.0 (JDK1.4)

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
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return repeat(str.charAt(0), repeat);
        }

        final int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1 :
                return repeat(str.charAt(0), repeat);
            case 2 :
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default :
                final StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }

    /**
     * <p>Repeat a String {@code repeat} times to form a
     * new String, with a String separator injected each time. </p>
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
     * @param str        the String to repeat, may be null
     * @param separator  the String to inject, may be null
     * @param repeat     number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     *  {@code null} if null String input
     * @since 2.5
     */
    public static String repeat(final String str, final String separator, final int repeat) {
        if(str == null || separator == null) {
            return repeat(str, repeat);
        }
        // given that repeat(String, int) is quite optimized, better to rely on it than try and splice this into it
        final String result = repeat(str + separator, repeat);
        return removeEnd(result, separator);
    }

    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     *
     * <p>Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
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
     * <p>Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.</p>
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
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
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  {@code null} if null String input
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
     * <p>Splits the provided text into an array, using whitespace as the
     * separator.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.</p>
     *
     * <pre>
     * StringUtils.split(null)       = null
     * StringUtils.split("")         = []
     * StringUtils.split("abc def")  = ["abc", "def"]
     * StringUtils.split("abc  def") = ["abc", "def"]
     * StringUtils.split(" abc ")    = ["abc"]
     * </pre>
     *
     * @param str  the String to parse, may be null
     * @return an array of parsed Strings, {@code null} if null String input
     */
    public static String[] split(final String str) {
        return split(str, null, -1);
    }

    /**
     * <p>Splits the provided text into an array, separator specified.
     * This is an alternative to using StringTokenizer.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.</p>
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
     * @param str  the String to parse, may be null
     * @param separatorChar  the character used as the delimiter
     * @return an array of parsed Strings, {@code null} if null String input
     * @since 2.0
     */
    public static String[] split(final String str, final char separatorChar) {
        return splitWorker(str, separatorChar, false);
    }

    /**
     * <p>Splits the provided text into an array, separators specified.
     * This is an alternative to using StringTokenizer.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} separatorChars splits on whitespace.</p>
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
     * @param str  the String to parse, may be null
     * @param separatorChars  the characters used as the delimiters,
     *  {@code null} splits on whitespace
     * @return an array of parsed Strings, {@code null} if null String input
     */
    public static String[] split(final String str, final String separatorChars) {
        return splitWorker(str, separatorChars, -1, false);
    }

    /**
     * <p>Splits the provided text into an array with a maximum length,
     * separators specified.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} separatorChars splits on whitespace.</p>
     *
     * <p>If more than {@code max} delimited substrings are found, the last
     * returned string includes all characters after the first {@code max - 1}
     * returned strings (including separator characters).</p>
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
     * @param str  the String to parse, may be null
     * @param separatorChars  the characters used as the delimiters,
     *  {@code null} splits on whitespace
     * @param max  the maximum number of elements to include in the
     *  array. A zero or negative value implies no limit
     * @return an array of parsed Strings, {@code null} if null String input
     */
    public static String[] split(final String str, final String separatorChars, final int max) {
        return splitWorker(str, separatorChars, max, false);
    }

    /**
     * <p>Splits the provided text into an array, separator string specified.</p>
     *
     * <p>The separator(s) will not be included in the returned String array.
     * Adjacent separators are treated as one separator.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} separator splits on whitespace.</p>
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
     * @param str  the String to parse, may be null
     * @param separator  String containing the String to be used as a delimiter,
     *  {@code null} splits on whitespace
     * @return an array of parsed Strings, {@code null} if null String was input
     */
    public static String[] splitByWholeSeparator(final String str, final String separator) {
        return splitByWholeSeparatorWorker( str, separator, -1, false ) ;
    }

    /**
     * <p>Splits the provided text into an array, separator string specified.
     * Returns a maximum of {@code max} substrings.</p>
     *
     * <p>The separator(s) will not be included in the returned String array.
     * Adjacent separators are treated as one separator.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} separator splits on whitespace.</p>
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
     * @param str  the String to parse, may be null
     * @param separator  String containing the String to be used as a delimiter,
     *  {@code null} splits on whitespace
     * @param max  the maximum number of elements to include in the returned
     *  array. A zero or negative value implies no limit.
     * @return an array of parsed Strings, {@code null} if null String was input
     */
    public static String[] splitByWholeSeparator( final String str, final String separator, final int max) {
        return splitByWholeSeparatorWorker(str, separator, max, false);
    }

    /**
     * <p>Splits the provided text into an array, separator string specified. </p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as separators for empty tokens.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} separator splits on whitespace.</p>
     *
     * <pre>
     * StringUtils.splitByWholeSeparatorPreserveAllTokens(null, *)               = null
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("", *)                 = []
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null)      = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null)    = ["ab", "", "", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
     * </pre>
     *
     * @param str  the String to parse, may be null
     * @param separator  String containing the String to be used as a delimiter,
     *  {@code null} splits on whitespace
     * @return an array of parsed Strings, {@code null} if null String was input
     * @since 2.4
     */
    public static String[] splitByWholeSeparatorPreserveAllTokens(final String str, final String separator) {
        return splitByWholeSeparatorWorker(str, separator, -1, true);
    }

    /**
     * <p>Splits the provided text into an array, separator string specified.
     * Returns a maximum of {@code max} substrings.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as separators for empty tokens.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} separator splits on whitespace.</p>
     *
     * <pre>
     * StringUtils.splitByWholeSeparatorPreserveAllTokens(null, *, *)               = null
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("", *, *)                 = []
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null, 0)      = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null, 0)    = ["ab", "", "", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":", 2)       = ["ab", "cd:ef"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 5) = ["ab", "cd", "ef"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 2) = ["ab", "cd-!-ef"]
     * </pre>
     *
     * @param str  the String to parse, may be null
     * @param separator  String containing the String to be used as a delimiter,
     *  {@code null} splits on whitespace
     * @param max  the maximum number of elements to include in the returned
     *  array. A zero or negative value implies no limit.
     * @return an array of parsed Strings, {@code null} if null String was input
     * @since 2.4
     */
    public static String[] splitByWholeSeparatorPreserveAllTokens(final String str, final String separator, final int max) {
        return splitByWholeSeparatorWorker(str, separator, max, true);
    }

    /**
     * Performs the logic for the {@code splitByWholeSeparatorPreserveAllTokens} methods.
     *
     * @param str  the String to parse, may be {@code null}
     * @param separator  String containing the String to be used as a delimiter,
     *  {@code null} splits on whitespace
     * @param max  the maximum number of elements to include in the returned
     *  array. A zero or negative value implies no limit.
     * @param preserveAllTokens if {@code true}, adjacent separators are
     * treated as empty token separators; if {@code false}, adjacent
     * separators are treated as one separator.
     * @return an array of parsed Strings, {@code null} if null String input
     * @since 2.4
     */
    private static String[] splitByWholeSeparatorWorker(
            final String str, final String separator, final int max, final boolean preserveAllTokens) {
        if (str == null) {
            return null;
        }

        final int len = str.length();

        if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        if (separator == null || EMPTY.equals(separator)) {
            // Split on whitespace.
            return splitWorker(str, null, max, preserveAllTokens);
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
                    // We found a consecutive occurrence of the separator, so skip it.
                    if (preserveAllTokens) {
                        numberOfSubstrings += 1;
                        if (numberOfSubstrings == max) {
                            end = len;
                            substrings.add(str.substring(beg));
                        } else {
                            substrings.add(EMPTY);
                        }
                    }
                    beg = end + separatorLength;
                }
            } else {
                // String.substring( beg ) goes from 'beg' to the end of the String.
                substrings.add(str.substring(beg));
                end = len;
            }
        }

        return substrings.toArray(new String[substrings.size()]);
    }

    // -----------------------------------------------------------------------
    /**
     * <p>Splits the provided text into an array, using whitespace as the
     * separator, preserving all tokens, including empty tokens created by
     * adjacent separators. This is an alternative to using StringTokenizer.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as separators for empty tokens.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.</p>
     *
     * <pre>
     * StringUtils.splitPreserveAllTokens(null)       = null
     * StringUtils.splitPreserveAllTokens("")         = []
     * StringUtils.splitPreserveAllTokens("abc def")  = ["abc", "def"]
     * StringUtils.splitPreserveAllTokens("abc  def") = ["abc", "", "def"]
     * StringUtils.splitPreserveAllTokens(" abc ")    = ["", "abc", ""]
     * </pre>
     *
     * @param str  the String to parse, may be {@code null}
     * @return an array of parsed Strings, {@code null} if null String input
     * @since 2.1
     */
    public static String[] splitPreserveAllTokens(final String str) {
        return splitWorker(str, null, -1, true);
    }

    /**
     * <p>Splits the provided text into an array, separator specified,
     * preserving all tokens, including empty tokens created by adjacent
     * separators. This is an alternative to using StringTokenizer.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as separators for empty tokens.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.</p>
     *
     * <pre>
     * StringUtils.splitPreserveAllTokens(null, *)         = null
     * StringUtils.splitPreserveAllTokens("", *)           = []
     * StringUtils.splitPreserveAllTokens("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a..b.c", '.')   = ["a", "", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.splitPreserveAllTokens("a\tb\nc", null) = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a b c", ' ')    = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a b c ", ' ')   = ["a", "b", "c", ""]
     * StringUtils.splitPreserveAllTokens("a b c  ", ' ')   = ["a", "b", "c", "", ""]
     * StringUtils.splitPreserveAllTokens(" a b c", ' ')   = ["", a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("  a b c", ' ')  = ["", "", a", "b", "c"]
     * StringUtils.splitPreserveAllTokens(" a b c ", ' ')  = ["", a", "b", "c", ""]
     * </pre>
     *
     * @param str  the String to parse, may be {@code null}
     * @param separatorChar  the character used as the delimiter,
     *  {@code null} splits on whitespace
     * @return an array of parsed Strings, {@code null} if null String input
     * @since 2.1
     */
    public static String[] splitPreserveAllTokens(final String str, final char separatorChar) {
        return splitWorker(str, separatorChar, true);
    }

    /**
     * Performs the logic for the {@code split} and
     * {@code splitPreserveAllTokens} methods that do not return a
     * maximum array length.
     *
     * @param str  the String to parse, may be {@code null}
     * @param separatorChar the separate character
     * @param preserveAllTokens if {@code true}, adjacent separators are
     * treated as empty token separators; if {@code false}, adjacent
     * separators are treated as one separator.
     * @return an array of parsed Strings, {@code null} if null String input
     */
    private static String[] splitWorker(final String str, final char separatorChar, final boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final List<String> list = new ArrayList<>();
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

    private static String[] splitWorker(final String str, final String separatorChars, final int max, final boolean preserveAllTokens) {
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
                    if (match || preserveAllTokens) {
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
                    if (match || preserveAllTokens) {
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
                    if (match || preserveAllTokens) {
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
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }
    
	private static final double SIZE_1K = 1024;
	private static final double SIZE_1M = 1048576;
	private static final double SIZE_1G = 1073741824;
	
	/**
	 * 将文件大小格式化成xxG xxM等格式
	 * 
	 * @param size
	 * @return text
	 */
	public static String formatSize(long size) {
		DecimalFormat df = new DecimalFormat("#.##");
		if (size < SIZE_1K) {
			return String.valueOf(size);
		} else if (size < SIZE_1M) {
			return df.format(size / SIZE_1K).concat("K");
		} else if (size < SIZE_1G) {
			return df.format(size / SIZE_1M).concat("M");
		} else {
			return df.format(size / SIZE_1G).concat("G");
		}
	}
	 /**
     * <p>Search a CharSequence to find the first index of any
     * character in the given set of characters.</p>
     *
     * <p>A {@code null} String will return {@code -1}.
     * A {@code null} search string will return {@code -1}.</p>
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
     * @param cs  the CharSequence to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     * @since 3.0 Changed signature from indexOfAny(String, String) to indexOfAny(CharSequence, String)
     */
    public static int indexOfAny(final CharSequence cs, final String searchChars) {
        if (isEmpty(cs) || isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        return indexOfAny(cs, searchChars.toCharArray());
    }
    
    /**
     * <p>Search a CharSequence to find the first index of any
     * character in the given set of characters.</p>
     *
     * <p>A {@code null} String will return {@code -1}.
     * A {@code null} or zero length search array will return {@code -1}.</p>
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
     * @param cs  the CharSequence to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     * @since 3.0 Changed signature from indexOfAny(String, char[]) to indexOfAny(CharSequence, char...)
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
	 * @param str
	 * @param searchChars
	 * @param startPos
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
	 * @param fname
	 * @param to
	 * @return 将文件名中的非法字符替换成合法字符 
	 */
	public static String toFilename(String fname, String to) {
		StringBuilder sb = new StringBuilder();
		for (char c : fname.toCharArray()) {
			if (invalidCharsInFilename.indexOf(c) > -1) {
				sb.append(to);
			} else {
				sb.append(c);
			}
		}
		fname = sb.toString();
		if (fname.endsWith(".")) {
			fname = StringUtils.substringBeforeLast(fname, ".");
		}
		return fname;
	}
	
	/**
	 * @param source
	 * @param charset
	 * @return URL解码 
	 */
	public static String urlDecode(String source, String charset) {
		try {
			return URLDecoder.decode(source, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * @param source
	 * @return URL解码 
	 */
	public static String urlDecode(String source) {
		return urlDecode(source, "UTF-8");
	}

	/**
	 * @param source
	 * @return URL编码 
	 */
	public static String urlEncode(String source) {
		try {
			return URLEncoder.encode(source, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * @param source
	 * @param charset
	 * @return URL编码 
	 */
	public static String urlEncode(String source, String charset) {
		try {
			return URLEncoder.encode(source, charset).replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	  /**
     * <p>Removes a substring only if it is at the beginning of a source string,
     * otherwise returns the source string.</p>
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
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
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  {@code null} if null String input
     * @since 2.1
     */
    public static String removeStart(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)){
            return str.substring(remove.length());
        }
        return str;
    }
    
    
    private static Random random=new Random();

	/**
	 * @return 产生8位的随机数字 
	 */
	public static final String randomString() {
		int i=random.nextInt();
		return String.valueOf(Math.abs(i));
	}
	
	
	/**
	 * @param s
	 * @return 把字符串左边的空格给去掉 
	 */
	public static final String ltrim(String s) {
		if (s == null) {
			return s;
		}
		int len = s.length();
		int st = 0;
		int off = 0; /* avoid getfield opcode */
		char[] val = s.toCharArray(); /* avoid getfield opcode */
		while ((st < len) && (val[off + st] <= ' ')) {
			st++;
		}
		return (st > 0) ? s.substring(st, len) : s;
	}

	/**
	 * @param s
	 * @param trimChars
	 * @return 从左侧删除指定的字符
	 */
	public static final String ltrim(String s, char... trimChars) {
		if (s == null) {
			return s;
		}
		int len = s.length();
		int st = 0;
		int off = 0;
		while ((st < len) && (ArrayUtils.contains(trimChars, s.charAt(off + st)))) {
			st++;
		}
		return (st > 0) ? s.substring(st, len) : s;
	}

	/**
	 * @param s
	 * @param trimChars
	 * @return 从右侧删除指定的字符
	 */
	public static final String rtrim(String s, char... trimChars) {
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
	 * @param s
	 * @return  把字符串右边的空格给去掉
	 */
	public static final String rtrim(String s) {
		if (s == null) {
			return s;
		}
		int len = s.length();
		int st = 0;
		int off = 0; /* avoid getfield opcode */
		char[] val = s.toCharArray(); /* avoid getfield opcode */
		while ((st < len) && (val[off + len - 1] <= ' ')) {
			len--;
		}
		return (len < s.length()) ? s.substring(st, len) : s;
	}

	/**
	 * @param s
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
	 * @param in
	 * @return CRC
	 */
	public static String getCRC(InputStream in) {
		CRC32 crc32 = new CRC32();
		byte[] b = new byte[65536];
		int len = 0;
		try {
			while ((len = in.read(b)) != -1) {
				crc32.update(b, 0, len);
			}
			return Long.toHexString(crc32.getValue());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 计算CRC摘要,8位十六进制数
	 * @param s
	 * @return CRC
	 */
	public static String getCRC(String s) {
		try(ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes())){
			return getCRC(in);
		} catch (IOException e) {
			throw Exceptions.illegalState(e);
		}
	}

	/**
	 * 计算MD5摘要,
	 * 
	 * @param s 输入
	 * @return 32位十六进制数的MD5值
	 */
	public final static String getMD5(String s) {
		try(ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes())){
			byte[] md = hash(in, "MD5");
			return join(md, (char) 0, 0, md.length);
		} catch (IOException e) {
			throw Exceptions.illegalState(e);
		}
	}
	
	/**
	 * 计算MD5摘要
	 * @param s
	 * @return MD5
	 */
	public final static String getMD5InBase64(String s) {
		try(ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes())){
			byte[] md = hash(in, "MD5");
			return JefBase64.encode(md);
		} catch (IOException e) {
			throw Exceptions.illegalState(e);
		}
	}
	

	/**
	 * 计算MD5摘要
	 * @param file
	 * @return 32位十六进制数的MD5值
	 */
	public final static String getMD5(File file) {
		try(InputStream in=new FileInputStream(file)){
			byte[] md = hash(in, "MD5");
			return join(md, (char) 0, 0, md.length);	
		} catch (IOException e) {
			throw Exceptions.illegalState(e);
		}
	}

	/**
	 * 计算SHA-1
	 * @param s
	 * @return SHA-1
	 */
	public final static String getSHA1(String s) {
		ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
		byte[] md = hash(in, "SHA-1");
		return join(md, (char) 0, 0, md.length);
	}

	/**
	 * 计算SHA256
	 * 
	 * @param s
	 * @return SHA256
	 */
	public final static String getSHA256(String s) {
		ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
		byte[] md = hash(in, "SHA-256");
		return join(md, (char) 0, 0, md.length);
	}
	
	/*
	 * 计算消息摘要
	 */
	public final static byte[] hash(InputStream in, String algorithm) {
		try {
			MessageDigest mdTemp = MessageDigest.getInstance(algorithm);
			byte[] b = new byte[4096];
			int len = 0;
			while ((len = in.read(b)) != -1) {
				mdTemp.update(b, 0, len);
			}
			return mdTemp.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final char hexDigitsU[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
	'F' };
	public static final char hexDigitsL[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
	'e', 'f' };

	
	/**
	 * 将数组或列表拼成文本
	 * 
	 * @param b
	 * @param c
	 * @return 拼接文本
	 */
	public static String join(byte[] b, char c) {
		if (b == null)
			return "";
		return join(b, c, 0, b.length);
	}

	/**
	 * 将数组或列表拼成文本
	 * 
	 * @param b
	 * @param dchar
	 * @param offset
	 * @param len
	 * @return 拼接文本
	 */
	public static String join(byte[] b, char dchar, int offset, int len) {
		if (b == null || b.length == 0)
			return "";
		return dchar == NULL_CHAR ? toHex0(b, offset, len, hexDigitsL) : toHex0(b, offset, len, dchar, hexDigitsL);
	}
	
	/**
	 * 将数组或列表拼成文本
	 * 
	 * @param os
	 * @param separator
	 * @return 拼接文本
	 */
	public static String join(Object[] os, String separator) {
		if (os == null || os.length == 0)
			return EMPTY;
		String[] ss = new String[os.length];
		int len = 0;
		int sepLen = separator.length();
		for (int i = 0; i < os.length; i++) {
			Object o = os[i];
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
     * <p>Joins the elements of the provided {@code Iterable} into
     * a single String containing the provided elements.</p>
     *
     * <p>No delimiter is added before or after the list.
     * A {@code null} separator is the same as an empty String ("").</p>
     *
     * <p>See the examples here: {@link #join(Object[],String)}. </p>
     *
     * @param iterable  the {@code Iterable} providing the values to join together, may be null
     * @param separator  the separator character to use, null treated as ""
     * @return the joined String, {@code null} if null iterator input
     * @since 2.3
     */
    public static String join(final Iterable<?> iterable, final String separator) {
        if (iterable == null) {
            return null;
        }
        StringBuilder sb=new StringBuilder();
        joinTo(iterable,separator,sb);
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
		sb.append(String.valueOf(iterator.next()));
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			sb.append(sep).append(String.valueOf(obj));
		}
	}
	
	/*
	 * 有分隔符时的编码处理
	 * 
	 * @return
	 */
	private static String toHex0(byte[] b, int offset, int len, char dchar, char[] hexDigits) {
		int j = offset + len;
		if (j > b.length)
			j = b.length; // 上限
		char str[] = new char[j * (3)];
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
		char str[] = new char[j * 2];
		int k = 0;
		for (int i = offset; i < j; i++) {
			byte byte0 = b[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // >>是带符号移位， >>>是无符号移位
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}
	
	/**
	 * 合并多个String,在参数为3个和以内时请直接使用String.concat。
	 * 5个和超过5个String相加后，concat方法性能急剧下降，此时此方法最快
	 * 
	 * @param args
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
	 * 
	 * @param s
	 * @return true if 存在东亚字符 
	 */
	public static boolean hasAsian(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 255 && c != 65279) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return 覆盖apache-lang3中父类的方法，从两端取,Apache commons默认是从前取的 
	 */
	public static String substringBetween(String arg1, String arg2, String arg3) {
		int a = arg1.indexOf(arg2);
		int b = arg1.lastIndexOf(arg3);
		if (a == -1 || b == -1)
			return "";
		if (a == b)
			return "";
		if (a > b)
			return "";
		return arg1.substring(a + arg2.length(), b);
	}
	   /**
     * <p>Converts a String to lower case as per {@link String#toLowerCase()}.</p>
     *
     * <p>A {@code null} input String returns {@code null}.</p>
     *
     * <pre>
     * StringUtils.lowerCase(null)  = null
     * StringUtils.lowerCase("")    = ""
     * StringUtils.lowerCase("aBc") = "abc"
     * </pre>
     *
     * <p><strong>Note:</strong> As described in the documentation for {@link String#toLowerCase()},
     * the result of this method is affected by the current locale.
     * should be used with a specific locale (e.g. {@link Locale#ENGLISH}).</p>
     *
     * @param str  the String to lower case, may be null
     * @return the lower cased String, {@code null} if null String input
     */
    public static String lowerCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase();
    }
    
    //-----------------------------------------------------------------------
    /**
     * <p>Converts a String to upper case as per {@link String#toUpperCase()}.</p>
     *
     * <p>A {@code null} input String returns {@code null}.</p>
     *
     * <pre>
     * StringUtils.upperCase(null)  = null
     * StringUtils.upperCase("")    = ""
     * StringUtils.upperCase("aBc") = "ABC"
     * </pre>
     *
     * <p><strong>Note:</strong> As described in the documentation for {@link String#toUpperCase()},
     * the result of this method is affected by the current locale.
     * should be used with a specific locale (e.g. {@link Locale#ENGLISH}).</p>
     *
     * @param str  the String to upper case, may be null
     * @return the upper cased String, {@code null} if null String input
     */
    public static String upperCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
    }
    
	/**
	 * 获得32位的Hex uuid
	 * @return String of UUID(size=36)
	 */
	public static final String generateGuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public static String trimToEmpty(String value) {
		return value==null? "": value.trim();
	}
}
