package com.github.xuse.querydsl.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberUtils {

	private static final double SIZE_1K = 1024;
	private static final double SIZE_1M = 1048576;
	private static final double SIZE_1G = 1_073_741_824;
	private static final long SIZE_1T = 1099511627776L;

	/**
	 * 常用格式缓存
	 */
	private static final DecimalFormat[] formats = new DecimalFormat[] { new DecimalFormat("#"),
			new DecimalFormat("#.#"), new DecimalFormat("#.##"), new DecimalFormat("#.###"),
			new DecimalFormat("#.####"), new DecimalFormat("#.#####") };

	private static String FORMAT_TEMPLATE = "#.#######################";

	/**
	 * 将文件大小格式化成xxG xxM等格式
	 * 
	 * @param size size
	 * @return text  格式化后的文件大小
	 */
	public static String formatSize(long size) {
		return formatSize(size, 2);
	}

	/**
	 * @param size 数据大小
	 * @param digits 保留小数点后位数
	 * @return 格式化后的文件大小
	 */
	public static String formatSize(long size, int digits) {
		DecimalFormat df;
		if (digits >= 0 && digits <= 5) {
			df = formats[digits];
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
		} else {
			BigDecimal b = new BigDecimal(size);
			b = b.divide(new BigDecimal(SIZE_1T), digits, RoundingMode.HALF_UP);
			return b.toString().concat("T");
		}
	}

	/**
	 * 将两个数值的比值作为百分比显示
	 * 
	 * @param a the numerator.
	 * @param b the denominator.
	 * @return 百分比显示
	 */
	public static String toPercent(long a, long b) {
		return String.valueOf(10000 * a / b / 100f).concat("%");
	}
}
