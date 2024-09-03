package com.github.xuse.querydsl.util;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录了所有个时区的常量，用于在编码时直接引用。
 */
public class TimeZones {
	/**
	 * 各地时区是个很复杂的问题。很多地区定义的当地时差甚至有半小时和45分等特殊时差。
	 * 我们这么来理解这件事情
	 * <p>
	 * 第一阶段，以格林尼治GMT为代表构建的全球24时区。
	 * 第二阶段，以UTC世界协调时替代GMT。
	 * 第三阶段，世界各国以本国治理和民意为目的，设置的本地时区。
	 * <p> 
	 */
	static final String[] TIME_ZONES = new String[] { "Etc/GMT+12", "Etc/GMT+11", "Etc/GMT+10", "Etc/GMT+9",
			"Etc/GMT+8", "Etc/GMT+7", "Etc/GMT+6", "Etc/GMT+5", "Etc/GMT+4", "Etc/GMT+3",
			"Etc/GMT+2", "Etc/GMT+1", "GMT", "Etc/GMT-1", "Etc/GMT-2", "Etc/GMT-3", "Etc/GMT-4",
			"Etc/GMT-5", "Etc/GMT-6", "Etc/GMT-7", "Etc/GMT-8", "Etc/GMT-9", "Etc/GMT-10", "Etc/GMT-11", "Etc/GMT-12",
			"Etc/GMT-13", "Etc/GMT-14" };

	public static final TimeZone UTC_M12 = TimeZone.getTimeZone(TIME_ZONES[0]);

	public static final TimeZone UTC_M11 = TimeZone.getTimeZone(TIME_ZONES[1]);

	public static final TimeZone UTC_M10 = TimeZone.getTimeZone(TIME_ZONES[2]);

	public static final TimeZone UTC_M9 = TimeZone.getTimeZone(TIME_ZONES[3]);

	public static final TimeZone UTC_M8 = TimeZone.getTimeZone(TIME_ZONES[4]);

	public static final TimeZone UTC_M7 = TimeZone.getTimeZone(TIME_ZONES[5]);

	public static final TimeZone UTC_M6 = TimeZone.getTimeZone(TIME_ZONES[6]);

	public static final TimeZone UTC_M5 = TimeZone.getTimeZone(TIME_ZONES[7]);

	public static final TimeZone UTC_M4 = TimeZone.getTimeZone(TIME_ZONES[8]);

	public static final TimeZone UTC_M3 = TimeZone.getTimeZone(TIME_ZONES[9]);

	public static final TimeZone UTC_M2 = TimeZone.getTimeZone(TIME_ZONES[10]);

	public static final TimeZone UTC_M1 = TimeZone.getTimeZone(TIME_ZONES[11]);

	public static final TimeZone UTC = TimeZone.getTimeZone(TIME_ZONES[12]);

	public static final TimeZone UTC_1 = TimeZone.getTimeZone(TIME_ZONES[13]);

	public static final TimeZone UTC_2 = TimeZone.getTimeZone(TIME_ZONES[14]);

	public static final TimeZone UTC_3 = TimeZone.getTimeZone(TIME_ZONES[15]);

	public static final TimeZone UTC_4 = TimeZone.getTimeZone(TIME_ZONES[16]);

	public static final TimeZone UTC_5 = TimeZone.getTimeZone(TIME_ZONES[17]);

	public static final TimeZone UTC_6 = TimeZone.getTimeZone(TIME_ZONES[18]);

	public static final TimeZone UTC_7 = TimeZone.getTimeZone(TIME_ZONES[19]);

	public static final TimeZone UTC_8 = TimeZone.getTimeZone(TIME_ZONES[20]);

	public static final TimeZone UTC_9 = TimeZone.getTimeZone(TIME_ZONES[21]);

	public static final TimeZone UTC_10 = TimeZone.getTimeZone(TIME_ZONES[22]);

	public static final TimeZone UTC_11 = TimeZone.getTimeZone(TIME_ZONES[23]);

	public static final TimeZone UTC_12 = TimeZone.getTimeZone(TIME_ZONES[24]);

	public static final TimeZone UTC_13 = TimeZone.getTimeZone(TIME_ZONES[25]);

	public static final TimeZone UTC_14 = TimeZone.getTimeZone(TIME_ZONES[26]);

	public static final TimeZone Asia_Shanghai = TimeZone.getTimeZone("Asia/Shanghai");
	
	private static final Map<Double, TimeZone> TimezoneCache=new ConcurrentHashMap<>();

	/**
	 * get the time zone object by a integer.
	 * 本方法返回的时区均不支持夏令时，以免因为夏令时因素造成不期望的调用结果。
	 * 
	 * @deprecated the param offset is in int. not compatible for India +5.5,PYT +8.5 and etc...
	 *  use {@link #getByUTCOffset(double)} please.
	 * @param utcOffset from -12 to 14.
	 * @return the time zone object.
	 */
	public static final TimeZone getByUTCOffset(int utcOffset) {
		if (utcOffset > 14 || utcOffset < -12) {
			throw new IllegalArgumentException("Invalid UTC time offset,must beetween -12 to +14.");
		}
		return TimeZone.getTimeZone(TIME_ZONES[utcOffset + 12]);
	}
	
	/**
	 * get the time zone object by a float/double.
	 * 例如：
	 * <ul>
	 * <li>新加坡时间：getByUTCOffset(8d);</li>
	 * <li>南极洲东部杜蒙特迪尔维尔考察站时间：getByUTCOffset(10d);</li>
	 * <li>印度标准时间：getByUTCOffset(5.5);</li>
	 * <li>查塔姆岛标准时间 ：getByUTCOffset(12.75);</li>
	 * <li>缅甸时间：getByUTCOffset(6.5);</li>
	 * <li>尼泊尔时间 ：getByUTCOffset(5.75);</li>
	 * <li>自定义与UTC时差为08:15的一个时区：getByUTCOffset(8.25);</li>
	 * </ul>
	 * <p>
	 * 本方法返回的时区均不支持夏令时，以免因为夏令时因素造成不期望的调用结果。
	 * 例如{@code getByUTCOffset(10d)}不会返回澳大利亚东区时区，因为澳大利亚东部时区存在夏令时。
	 * 
	 * @param utcOffset from -12 to 14.
	 * @return the time zone object.
	 */
	public static final TimeZone getByUTCOffset(double utcOffset) {
		if (utcOffset > 14 || utcOffset < -12) {
			throw new IllegalArgumentException("Invalid UTC time offset,must beetween -12 to +14.");
		}
		return TimezoneCache.computeIfAbsent(utcOffset,TimeZones::generate);
	}
	
	private static TimeZone generate(double offset) {
		int rawOffset = (int) (offset * 3600_000);
		String[] ss = TimeZone.getAvailableIDs(rawOffset);
		TimeZone tz = null;
		for (int i=ss.length-1;i>=0;i--) {
			String s=ss[i];
			tz = TimeZone.getTimeZone(s);
			if (!tz.useDaylightTime()) {
				break;
			}
		}
		// generate a time zone id using GMT+/-time format.
		if (tz == null) {
			int i = (int) offset;
			int m = (int) (Math.abs(offset - i) * 60);
			StringBuilder sb = new StringBuilder("GMT").append(i >= 0 ? '+' : '-');
			i = Math.abs(i);
			if (i < 10) {
				sb.append('0');
			}
			sb.append(i).append(':');
			if (m < 10) {
				sb.append('0');
			}
			sb.append(m);
			tz = TimeZone.getTimeZone(sb.toString());
		}
		return tz;
	}
}
