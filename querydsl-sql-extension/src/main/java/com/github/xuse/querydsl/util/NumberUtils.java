package com.github.xuse.querydsl.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberUtils {

	private static final double SIZE_1K = 1024;
	private static final double SIZE_1M = 1048576;
	private static final double SIZE_1G = 1_073_741_824;
	private static final long SIZE_1T = 1_099_511_627_776L;
	private static final long SIZE_1P = 1_125_899_906_842_624L;
	

	private static final DecimalFormat[] COMMON_FORMATS = new DecimalFormat[] { new DecimalFormat("#"),
			new DecimalFormat("#.#"), new DecimalFormat("#.##"), new DecimalFormat("#.###"),
			new DecimalFormat("#.####"), new DecimalFormat("#.#####") };

	private static String FORMAT_TEMPLATE = "#.#######################";

	/**
	 * Format the file size into formats like xxG xxM, etc. 
	 * 
	 * @param size Size to format.
	 * @return text  The formatted file size.
	 */
	public static String formatSize(long size) {
		return formatSize(size, 2);
	}

	/**
	 * @param size Size to format.
	 * @param digits Digits preserved decimal places
	 * @return  The formatted file size.
	 */
	public static String formatSize(long size, int digits) {
		DecimalFormat df;
		if (digits >= 0 && digits <= 5) {
			df = COMMON_FORMATS[digits];
		} else {
			df = new DecimalFormat(FORMAT_TEMPLATE.substring(0, digits + 2));
		}
		if (size < SIZE_1K) {
			return String.valueOf(size);
		} else if (size < SIZE_1M) {
			return df.format(size / SIZE_1K).concat("K");
		} else if (size < SIZE_1G) {
			return df.format(size / SIZE_1M).concat("M");
		} else if (size < SIZE_1T) {
			return df.format(size / SIZE_1G).concat("G");
		} else if (size < SIZE_1P) {
			BigDecimal b = new BigDecimal(size);
			b = b.divide(new BigDecimal(SIZE_1T), digits, RoundingMode.HALF_UP);
			return b.toString().concat("T");
		} else {
			BigDecimal b = new BigDecimal(size);
			b = b.divide(new BigDecimal(SIZE_1P), digits, RoundingMode.HALF_UP);
			return b.toString().concat("P");
		}
	}

	/**
	 * Calculate the ratio of two numbers as a percentage
	 * 
	 * @param a the numerator.
	 * @param b the denominator.
	 * @return percentage string.
	 */
	public static String toPercent(long a, long b) {
		return String.valueOf(10000 * a / b / 100f).concat("%");
	}
}
