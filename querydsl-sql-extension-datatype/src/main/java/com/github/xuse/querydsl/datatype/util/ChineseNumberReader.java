package com.github.xuse.querydsl.datatype.util;

import com.github.xuse.querydsl.util.StringUtils;

/**
 * Convert a number to its Chinese reading. Supports these formats. You can
 * define the conversion strategy yourself through the constructor. For Example.
 * the character for '万' is capitalized as '万' by default. If you need to use
 * the character '萬', you can construct and pass in your own code table.
 * <ul>
 * <li>See {@linkplain #DEFAULT}</li>
 * <li>See {@linkplain #FINANCE}</li>
 * <li>See {@linkplain #CURRENCE}</li>
 * <li>See {@linkplain #MILITARY}</li>
 * </ul>
 * <p>
 * 可通过构造器自行定义转换策略。默认万字大写作'萬',如您需要使用'万'，字，自行构造码表传入即可。
 */
public class ChineseNumberReader {
	private static final int FLAG_REMOVE_ZERO_TAIL = 1;

	private static final char[] CHINESE_DIGITS1 = { '零', '一', '二', '三', '四', '五', '六', '七', '八', '九' };
	private static final char[] CHINESE_DIGITS2 = { '零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖' };
	private static final char[] CHINESE_DIGITS3 = { '洞', '幺', '两', '三', '四', '五', '六', '拐', '八', '勾' };

	private static final String[] CHINESE_UNITS1 = { "", "十", "百", "千", "万", "十万", "百万", "千万", "亿", "十亿", "百亿", "千亿",
			"万亿", "十万亿", "百万亿", "千万亿", "亿亿" };
	private static final String[] CHINESE_UNITS2 = { "", "拾", "佰", "仟", "萬", "拾萬", "佰萬", "仟萬", "亿", "拾亿", "佰亿", "仟亿",
			"萬亿", "拾萬亿", "佰萬亿", "仟萬亿", "亿亿" };

	private static final String[] NO_CURRENCY = { "点", "", "", "" };
	private static final String[] CHINESE_CURRENCY = { "元", "角", "分", "厘" };

	/**
	 * Conventional Chinese reading. For example, {@code 100} is read as "一百".
	 * <p>
	 * 常规中文读法。如{@code 100}读作"一百".
	 */
	public static final ChineseNumberReader DEFAULT = new ChineseNumberReader(CHINESE_DIGITS1, CHINESE_UNITS1,
			NO_CURRENCY, "", FLAG_REMOVE_ZERO_TAIL);

	/**
	 * Chinese characters in uppercase. For example, {@code 100} is read as "壹佰".
	 * <p>
	 * 汉字数字大写。如{@code 100}读作"壹佰".
	 */
	public static final ChineseNumberReader FINANCE = new ChineseNumberReader(CHINESE_DIGITS2, CHINESE_UNITS2,
			NO_CURRENCY, "", FLAG_REMOVE_ZERO_TAIL);

	/**
	 * Renminbi reading. For example, {@code 100.38} is read as "壹佰元叁角捌分".
	 * <p>
	 * 人民币读法。如{@code 100.38}读作"壹佰元叁角捌分".
	 */
	public static final ChineseNumberReader CURRENCE = new ChineseNumberReader(CHINESE_DIGITS2, CHINESE_UNITS2,
			CHINESE_CURRENCY, "整", FLAG_REMOVE_ZERO_TAIL);

	/**
	 * 军事领域数字读法，仅按数字顺序读出。旨在减少因方言差异和噪音干扰导致的通信错误。如{@code 0.007}读作"零点洞洞拐"
	 * <p>
	 * Military field numerical reading. It aims to reduce communication errors
	 * caused by dialect differences and noise interference. For example,
	 * {@code 0.007} is read as "零点洞洞拐".
	 */
	public static final ChineseNumberReader MILITARY = new ChineseNumberReader(CHINESE_DIGITS3, CHINESE_UNITS1,
			NO_CURRENCY, "", 0);

	/*
	 * Code tables. 
	 */
	private final char[] numberTable;
	private final String[] unitTable;
	private final String[] currencyTable;

	/*
	 * Whether remove the zero tail or not. 
	 */
	private final boolean removeDigitZeroTail;

	/*
	 * The Character '元整' in the end of the currency format.
	 */
	private final String rmbSuffix;

	private String minus = "负";
	
	/*
	 * Supports the convention that when '一十' is at the beginning, it will be abbreviated as '十'.
	 * <p>
	 * 支持中习惯，当"一十"在开头时，会简称"十"
	 */
	private final String specialOmit;


	public ChineseNumberReader(char[] chineseDigits, String[] chineseUnits, String[] currency, String suffix,
			int flags) {
		this.numberTable = chineseDigits;
		this.unitTable = chineseUnits;
		this.currencyTable = currency;
		this.rmbSuffix = StringUtils.isEmpty(suffix) ? "" : currencyTable[0] + suffix;
		this.removeDigitZeroTail = (flags & FLAG_REMOVE_ZERO_TAIL) > 0;
		specialOmit = chineseDigits[1] + chineseUnits[1];
	}

	/**
	 * Convert the number to Chinese numeric reading.
	 * 
	 * @param num the number (integer or long).
	 * @return Chinese numeric reading.
	 */
	public String forNumber(long num) {
		return forNumber0(num).append(rmbSuffix).toString();
	}

	/**
	 * Convert the number to Chinese numeric reading.
	 * 
	 * @param num the number (float or double).
	 * @return Chinese numeric reading.
	 */
	public String forDouble(double num) {
		String v = String.valueOf(num);
		int index = v.lastIndexOf('.');
		if (v.endsWith(".0")) {
			// 是整数
			return forNumber0(Double.valueOf(num).longValue()).append(rmbSuffix).toString();
		}
		return forNumber0(Long.valueOf(v.substring(0, index))).append(forDigitsWithUnit(v.substring(index + 1)))
				.toString();
	}

	/**
	 * Convert the number to Chinese numeric reading.
	 * 
	 * @param num the number (float or double, string parameter to avoid loss of
	 *            precision).
	 * @return Chinese numeric reading.
	 */
	public String forDouble(String num) {
		boolean noDigits = true;
		int index = num.lastIndexOf('.');
		String afterDot = null;
		if (index > -1) {
			afterDot = num.substring(index + 1);
			num = num.substring(0, index);
			noDigits = afterDot.chars().filter(e -> e > 48).count() == 0;
		}
		if (noDigits) {
			return forNumber0(Double.valueOf(num).longValue()).append(rmbSuffix).toString();
		}
		if (removeDigitZeroTail) {
			afterDot = removeZeroTail(afterDot);
		}
		return forNumber0(Long.valueOf(num.substring(0, index))).append(forDigitsWithUnit(afterDot)).toString();
	}

	/**
	 * Convert the number to Chinese reading by each digits, and without unit.
	 * 
	 * @param num
	 * @return Chinese numeric reading.
	 */
	public String forDigits(String num) {
		if (num == null || num.length() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int offset = 0;
		if (num.charAt(0) == '-') {
			offset = 1;
			sb.append(minus);
		}
		for (int i = offset; i < num.length(); i++) {
			char c = num.charAt(i);
			switch (c) {
			case '.':
				sb.append(currencyTable[0]);
				break;
			default:
				int index = c - 48;
				if (index < 0 || index > 9) {
					throw new IllegalArgumentException("'" + num + "' is not a valid number.");
				}
				sb.append(numberTable[c - 48]);
			}
		}
		return sb.toString();
	}

	private String removeZeroTail(String afterDot) {
		int lastDigit = afterDot.length() - 1;
		for (; lastDigit > 0; lastDigit--) {
			if (afterDot.charAt(lastDigit) != '0') {
				break;
			}
		}
		lastDigit += 1;
		if (lastDigit < afterDot.length()) {
			return afterDot.substring(0, lastDigit);
		}
		return afterDot;
	}

	private StringBuilder forNumber0(long num) {
		char zero = numberTable[0];
		if (num == 0) {
			return new StringBuilder(3).append(zero);
		}
		if (num < 0) {
			return forNumber0(-num).insert(0, minus);
		}
		StringBuilder sb = new StringBuilder();
		int unitIndex = 0;
		boolean hasUnitSingel = false;
		while (num > 0) {
			int digit = (int) (num % 10);
			int level = unitIndex / 4;
			int newLevel = unitIndex % 4;
			if (newLevel == 0 && level < 3) {
				hasUnitSingel = false;
			}
			if (digit == 0) {
				if (sb.length() > 0 && sb.charAt(0) != zero) {
					sb.insert(0, zero);
				}
			} else {
				String unit = unitTable[unitIndex];
				if (unit.length() > 1 && hasUnitSingel) {
					if (unit.length() >= 3) {
						sb.insert(0, unit.subSequence(0, unit.length() - 1));
					} else {
						sb.insert(0, unit.charAt(0));
					}
				} else {
					sb.insert(0, unit);
				}
				sb.insert(0, numberTable[digit]);
				hasUnitSingel = true;
			}
			unitIndex++;
			num /= 10;
		}
		if (sb.length() > 1 && sb.toString().startsWith(specialOmit)) {
			sb.delete(0, 1);
		}
		return sb;
	}

	private String forDigitsWithUnit(String num) {
		if (num == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(currencyTable[0]);
		if (num.length() >= 1) {
			sb.append(numberTable[num.charAt(0) - 48]).append(currencyTable[1]);
		}
		if (num.length() >= 2) {
			sb.append(numberTable[num.charAt(1) - 48]).append(currencyTable[2]);
		}
		if (num.length() >= 3) {
			String li = currencyTable[3];
			int loopTo = li.length() == 0 ? num.length() : 3;
			for (int i = 2; i < loopTo; i++) {
				char c = num.charAt(i);
				sb.append(numberTable[c - 48]);
			}
			sb.append(li);
		}
		return sb.toString();
	}
}