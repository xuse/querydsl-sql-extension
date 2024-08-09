package com.github.xuse.querydsl.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import org.jetbrains.annotations.Nullable;


/**
 * Used to provide various thread-safe date and time formats.
 * <h2>Chinese:</h2> 用于提供各种线程安全的时间日期格式
 * <ul>
 * <li>G 年代标志符</li>
 * <li>y 年</li>
 * <li>M 月</li>
 * <li>d 日</li>
 * <li>h 时 在上午或下午 (1~12)</li>
 * <li>H 时 在一天中 (0~23)</li>
 * <li>m 分</li>
 * <li>s 秒</li>
 * <li>S 毫秒</li>
 * <li>E 星期</li>
 * <li>D 一年中的第几天</li>
 * <li>F 一月中第几个星期几</li>
 * <li>w 一年中第几个星期</li>
 * <li>W 一月中第几个星期</li>
 * <li>a 上午 / 下午 标记符</li>
 * <li>k 时 在一天中 (1~24)</li>
 * <li>K 时 在上午或下午 (0~11)</li>
 * <li>z 时区</li>
 * </ul>
 * 
 * @author Joey
 */
public abstract class DateFormats {

	/**
	 * 日期格式：美式日期 MM/DD/YYYY
	 */
	public static final TLDateFormat DATE_US = new TLDateFormat("MM/dd/yyyy");

	/**
	 * 日期格式：美式日期+时间 MM/DD/YYYY HH:MI:SS
	 */
	public static final TLDateFormat DATE_TIME_US = new TLDateFormat("MM/dd/yyyy HH:mm:ss");

	/**
	 * 日期格式：中式日期 YYYY-MM-DD
	 */
	public static final TLDateFormat DATE_CS = new TLDateFormat("yyyy-MM-dd");

	/**
	 * 日期格式：日期+时间 YYYY/MM/DD
	 */
	public static final TLDateFormat DATE_CS2 = new TLDateFormat("yyyy/MM/dd");

	/**
	 * 日期格式：日期+时间 YYYY-MM-DD HH:MI:SS
	 */
	public static final TLDateFormat DATE_TIME_CS = new TLDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final TLDateFormat DATE_TIME_CS_WITH_ZONE = new TLDateFormat("yyyy-MM-dd HH:mm:ss Z");

	/**
	 * 日期格式：日期+时间 YYYY/MM/DD HH:MI:SS
	 */
	public static final TLDateFormat DATE_TIME_CS2 = new TLDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	 * 日期格式：中式日期时间（到分） YYYY-MM-DD HH:MI
	 */
	public static final TLDateFormat DATE_TIME_ROUGH = new TLDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * 日期格式：中式日期+时间戳 YYYY-MM-DD HH:MI:SS.SSS
	 */
	public static final TLDateFormat TIME_STAMP_CS = new TLDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * 日期格式：仅时间 HH.MI.SS
	 */
	public static final TLDateFormat TIME_ONLY = new TLDateFormat("HH:mm:ss");

	/**
	 * 日期格式：日期紧凑 YYYYMMDD
	 */
	public static final TLDateFormat DATE_SHORT = new TLDateFormat("yyyyMMdd");

	/**
	 * 日期格式：日期时间紧凑 YYYYMMDDHHMISS
	 */
	public static final TLDateFormat DATE_TIME_SHORT_14 = new TLDateFormat("yyyyMMddHHmmss");

	/**
	 * 日期格式：日期时间紧凑 YYYYMMDDHHMI
	 */
	public static final TLDateFormat DATE_TIME_SHORT_12 = new TLDateFormat("yyyyMMddHHmm");

	/**
	 * 日期格式：yyyyMM
	 */
	public static final TLDateFormat YEAR_MONTH = new TLDateFormat("yyyyMM");

	// /////////////// 以下是别名，最常用的日期格式加上别名////////////////////
	/**
	 * 日期格式：中式日期 YYYY-MM-DD
	 */
	public static final TLDateFormat YYYY_MM_DD = DATE_CS;

	/**
	 * 日期格式：日期+时间 YYYY-MM-DD HH:MI:SS
	 */
	public static final TLDateFormat YYYY_MM_DD$HH_MI_SS = DATE_TIME_CS;

	/**
	 * 日期格式：日期紧凑 yyyyMMdd
	 */
	public static final TLDateFormat YYYYMMDD = DATE_SHORT;

	/**
	 * 日期格式：仅时间 HH.MI.SS
	 */
	public static final TLDateFormat HH_MI_SS = TIME_ONLY;

	/**
	 * 线程安全的日期格式转换。 注意本类比较重，建议设计为全局变量或静态变量，不要频繁的进行构造和回收。
	 * 
	 * 
	 */
	public static final class TLDateFormat extends java.lang.ThreadLocal<DateFormat> {
		private final String pattern;
		private final double hoursOffset;
		private final int millisOffset;

		public TLDateFormat(String p) {
			this.pattern = p;
			TimeZone defaultTz = TimeZone.getDefault();
			millisOffset = defaultTz.getRawOffset();
			hoursOffset = defaultTz.getRawOffset() / 3600_000d;
		}

		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(pattern);
		}

		/**
		 * 格式化时间
		 * @param time time
		 * @return String
		 */
		public String format(long time) {
			return get().format(time);
		}

		/**
		 * 格式化日期，如果传入null返回null(Null-safety.)
		 * 
		 * @param date date
		 * @return text
		 */
		@Nullable
		public String format(Date date) {
			return date == null ? null : get().format(date);
		}

		/**
		 * 格式化日期
		 * 
		 * @param date 可以为null
		 * @return Optional String result
		 */
		public Optional<String> format2(Date date) {
			return date == null ? Optional.empty() : Optional.of(get().format(date));
		}

		/**
		 * 解析时间，如果为空返回null
		 * 
		 * @param text text
		 * @return Date parsed.
		 * @throws IllegalArgumentException 格式不对抛出异常
		 */
		public Date parse(String text) throws IllegalArgumentException {
			if (StringUtils.isEmpty(text)) {
				return null;
			}
			try {
				return get().parse(text);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid date:" + text, e);
			}
		}

		/**
		 * 解析时间，如果为空或者转换出错，都返回默认值
		 * 
		 * @param text         text
		 * @param defaultValue defaultValue
		 * @return Date parsed.
		 */
		public Date parse(String text, Date defaultValue) {
			if (StringUtils.isEmpty(text)) {
				return defaultValue;
			}
			try {
				return get().parse(text);
			} catch (ParseException e) {
				return defaultValue;
			}
		}

		/**
		 * 格式化日期，按指定的时区进行输出
		 * 
		 * @param date 如果传入null返回null(Null-safety.)
		 * @param zone zone 时区（如果该时区有夏令时会被忽略）
		 * @return text
		 */
		public String format(Date date, TimeZone zone) {
			return format0(date, zone.getRawOffset() - millisOffset);
		}

		/**
		 * 格式化日期，按指定的时区进行输出
		 *
		 * @param date      如果传入null返回null(Null-safety.)
		 * @param utcHoursOffset 相对国际原子时的时差，从-12到+14(中国为8)，可以传小数
		 * @return 指定时区内的时间
		 */
		public String format(Date date, double utcHoursOffset) {
			return format0(date, (int) ((utcHoursOffset - hoursOffset) * 3600_000));
		}

		/**
		 * 解析日期
		 * 
		 * @param text 时间文字
		 * @param zone 时区（如果该时区有夏令时会被忽略）
		 * @return 解析日期
		 * @throws IllegalArgumentException If encounter IllegalArgumentException
		 */
		public Date parse(String text, TimeZone zone) throws IllegalArgumentException {
			if (StringUtils.isEmpty(text)) {
				return null;
			}
			try {
				return parse0(text, zone.getRawOffset() - millisOffset);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid date:" + text, e);
			}
		}

		/**
		 * 解析日期
		 * 
		 * @param text      text
		 * @param utcHoursOffset 相对国际原子时的时差，单位小时，从-12到+14(中国为8)，涉及半时区可以传入0.5/0.75等
		 * @throws IllegalArgumentException If encounter IllegalArgumentException
		 * @return Date
		 */
		public Date parse(String text, double utcHoursOffset) throws IllegalArgumentException {
			if (StringUtils.isEmpty(text)) {
				return null;
			}
			try {
				return parse0(text, (int) ((utcHoursOffset - hoursOffset) * 3600_000));
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid date:" + text, e);
			}
		}

		/**
		 * 解析时间，如果为空或者转换出错，都返回默认值
		 * 
		 * @param text         text
		 * @param defaultValue defaultValue
		 * @return 解析日期
		 * @param timeZone TimeZone时区（如果该时区有夏令时会被忽略）
		 */
		public Date parse(String text, Date defaultValue, TimeZone timeZone) {
			if (StringUtils.isEmpty(text)) {
				return defaultValue;
			}
			try {
				return parse0(text, timeZone.getRawOffset() - millisOffset);
			} catch (ParseException e) {
				return defaultValue;
			}
		}

		private String format0(Date date, long offset) {
			if (date == null) {
				return null;
			}
			return get().format(new Date(date.getTime() + offset));
		}

		private Date parse0(String text, int zoneOffset) throws ParseException {
			return new Date(get().parse(text).getTime() - zoneOffset);
		}
	}

	/**
	 * 得到Thread-safe的DateFormat
	 * 
	 * @param pattern pattern
	 * @return TLDateFormat
	 */
	public static final TLDateFormat create(String pattern) {
		return new TLDateFormat(pattern);
	}
}
