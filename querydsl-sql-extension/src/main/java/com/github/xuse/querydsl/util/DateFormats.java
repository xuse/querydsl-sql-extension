package com.github.xuse.querydsl.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/**
 * <h2>English:</h2>
 * Used to provide various thread-safe date and time formats.
 * <h2>Chinese:</h2> 用于提供各种线程安全的时间日期格式
 * <ul>
 * <li>G - Era designator</li>
 * <li>y - Year</li>
 * <li>M - Month</li>
 * <li>d - Day</li>
 * <li>h - Hour in AM/PM (1~12)</li>
 * <li>H - Hour in day (0~23)</li>
 * <li>m - Minute</li>
 * <li>s - Second</li>
 * <li>S - Millisecond</li>
 * <li>E - Day of the week</li>
 * <li>D - Day of the year</li>
 * <li>F - Day of the week in the month</li>
 * <li>w - Week of the year</li>
 * <li>W - Week of the month</li>
 * <li>a - AM/PM marker</li>
 * <li>k - Hour in day (1~24)</li>
 * <li>K - Hour in AM/PM (0~11)</li>
 * <li>z - Time zone</li>
 * </ul>
 * 
 * @author Joey
 */
public abstract class DateFormats {

	/**
	 * <h2>English:</h2> Date format: US date MM/DD/YYYY
	 * <h2>Chinese:</h2> 日期格式：美式日期 MM/DD/YYYY
	 */
	public static final TLDateFormat DATE_US = new TLDateFormat("MM/dd/yyyy");

	/**
	 * <h2>English:</h2> Date format: US date with time MM/DD/YYYY HH:MI:SS
	 * <h2>Chinese:</h2> 日期格式：美式日期+时间 MM/DD/YYYY HH:MI:SS
	 */
	public static final TLDateFormat DATE_TIME_US = new TLDateFormat("MM/dd/yyyy HH:mm:ss");

	/**
	 * <h2>English:</h2> Date format: Chinese date YYYY-MM-DD
	 * <h2>Chinese:</h2> 日期格式：中式日期 YYYY-MM-DD
	 */
	public static final TLDateFormat DATE_CS = new TLDateFormat("yyyy-MM-dd");

	/**
	 * <h2>English:</h2> Date format: date YYYY/MM/DD
	 * <h2>Chinese:</h2> 日期格式：日期+时间 YYYY/MM/DD
	 */
	public static final TLDateFormat DATE_CS2 = new TLDateFormat("yyyy/MM/dd");

	/**
	 * <h2>English:</h2> Date format: date with time YYYY-MM-DD HH:MI:SS
	 * <h2>Chinese:</h2> 日期格式：日期+时间 YYYY-MM-DD HH:MI:SS
	 */
	public static final TLDateFormat DATE_TIME_CS = new TLDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * <h2>English:</h2> Date format: date with time and timezone YYYY-MM-DD HH:MI:SS Z
	 * <h2>Chinese:</h2> 定义一个线程局部变量的日期格式常量，用于日期时间的格式化，包括时区信息
	 */
	public static final TLDateFormat DATE_TIME_CS_WITH_ZONE = new TLDateFormat("yyyy-MM-dd HH:mm:ss Z");

	/**
	 * <h2>English:</h2> Date format: date with time YYYY/MM/DD HH:MI:SS
	 * <h2>Chinese:</h2> 日期格式：日期+时间 YYYY/MM/DD HH:MI:SS
	 */
	public static final TLDateFormat DATE_TIME_CS2 = new TLDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	 * <h2>English:</h2> Date format: Chinese date with time (to minute) YYYY-MM-DD HH:MI
	 * <h2>Chinese:</h2> 日期格式：中式日期时间（到分） YYYY-MM-DD HH:MI
	 */
	public static final TLDateFormat DATE_TIME_ROUGH = new TLDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * <h2>English:</h2> Date format: Chinese date with timestamp YYYY-MM-DD HH:MI:SS.SSS
	 * <h2>Chinese:</h2> 日期格式：中式日期+时间戳 YYYY-MM-DD HH:MI:SS.SSS
	 */
	public static final TLDateFormat TIME_STAMP_CS = new TLDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * <h2>English:</h2> Date format: time only HH:MI:SS
	 * <h2>Chinese:</h2> 日期格式：仅时间 HH.MI.SS
	 */
	public static final TLDateFormat TIME_ONLY = new TLDateFormat("HH:mm:ss");

	/**
	 * <h2>English:</h2> Date format: compact date YYYYMMDD
	 * <h2>Chinese:</h2> 日期格式：日期紧凑 YYYYMMDD
	 */
	public static final TLDateFormat DATE_SHORT = new TLDateFormat("yyyyMMdd");

	/**
	 * <h2>English:</h2> Date format: compact date with time YYYYMMDDHHMISS
	 * <h2>Chinese:</h2> 日期格式：日期时间紧凑 YYYYMMDDHHMISS
	 */
	public static final TLDateFormat DATE_TIME_SHORT_14 = new TLDateFormat("yyyyMMddHHmmss");

	/**
	 * <h2>English:</h2> Date format: compact date with time YYYYMMDDHHMI
	 * <h2>Chinese:</h2> 日期格式：日期时间紧凑 YYYYMMDDHHMI
	 */
	public static final TLDateFormat DATE_TIME_SHORT_12 = new TLDateFormat("yyyyMMddHHmm");

	/**
	 * <h2>English:</h2> Date format: yyyyMM
	 * <h2>Chinese:</h2> 日期格式：yyyyMM
	 */
	public static final TLDateFormat YEAR_MONTH = new TLDateFormat("yyyyMM");

	// /////////////// 以下是别名，最常用的日期格式加上别名////////////////////
	/**
	 * <h2>English:</h2> Date format: Chinese date YYYY-MM-DD (alias of DATE_CS)
	 * <h2>Chinese:</h2> 日期格式：中式日期 YYYY-MM-DD
	 */
	public static final TLDateFormat YYYY_MM_DD = DATE_CS;

	/**
	 * <h2>English:</h2> Date format: date with time YYYY-MM-DD HH:MI:SS (alias of DATE_TIME_CS)
	 * <h2>Chinese:</h2> 日期格式：日期+时间 YYYY-MM-DD HH:MI:SS
	 */
	public static final TLDateFormat YYYY_MM_DD$HH_MI_SS = DATE_TIME_CS;

	/**
	 * <h2>English:</h2> Date format: compact date yyyyMMdd (alias of DATE_SHORT)
	 * <h2>Chinese:</h2> 日期格式：日期紧凑 yyyyMMdd
	 */
	public static final TLDateFormat YYYYMMDD = DATE_SHORT;

	/**
	 * <h2>English:</h2> Date format: time only HH:MI:SS (alias of TIME_ONLY)
	 * <h2>Chinese:</h2> 日期格式：仅时间 HH.MI.SS
	 */
	public static final TLDateFormat HH_MI_SS = TIME_ONLY;

	/**
	 * <h2>English:</h2>
	 * Thread-safe date format. This class is relatively heavy; it is recommended
	 * to use it as a global or static variable rather than constructing and
	 * recycling it frequently.
	 * <h2>Chinese:</h2>
	 * 线程安全的日期格式转换。 注意本类比较重，建议设计为全局变量或静态变量，不要频繁的进行构造和回收。
	 */
	public static final class TLDateFormat extends java.lang.ThreadLocal<DateFormat> {
		private final String pattern;
		private final double hoursOffset;
		private final int millisOffset;
		/**
		 * <h2>English:</h2>
		 * Expose a read-only DateTimeFormatter object for use with
		 * {@link LocalTime#parse(CharSequence, DateTimeFormatter)},
		 * {@link LocalDateTime#parse(CharSequence, DateTimeFormatter)}, etc.
		 * <h2>Chinese:</h2>
		 * 暴露出只读的DateTimeFormatter对象，供{@link LocalTime#parse(CharSequence, DateTimeFormatter)}
		 * {@link LocalDateTime#parse(CharSequence, DateTimeFormatter)}等使用
		 */
		public final DateTimeFormatter df;

		public TLDateFormat(String p) {
			this.pattern = p;
			this.df = DateTimeFormatter.ofPattern(p);
			TimeZone defaultTz = TimeZone.getDefault();
			millisOffset = defaultTz.getRawOffset();
			hoursOffset = defaultTz.getRawOffset() / 3600_000d;
		}

		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(pattern);
		}

		/**
		 * <h2>English:</h2> Format time.
		 * <h2>Chinese:</h2> 格式化时间
		 * 
		 * @param time time
		 * @return String
		 */
		public String format(long time) {
			return get().format(time);
		}

		/**
		 * <h2>English:</h2> Format date. Returns null if input is null (Null-safety).
		 * <h2>Chinese:</h2> 格式化日期，如果传入null返回null(Null-safety.)
		 * 
		 * @param date date
		 * @return text
		 */
		public String format(Date date) {
			return date == null ? null : get().format(date);
		}

		public String format(Instant date) {
			return date == null ? null : get().format(Date.from(date));
		}

		/**
		 * <h2>English:</h2> Format date/time from the java.time framework.
		 * <h2>Chinese:</h2> 格式化java time框架下的日期时间。
		 * 
		 * @param date data
		 * @return text
		 */
		public String format(Temporal date) {
			return date == null ? null : df.format(date);
		}

		/**
		 * <h2>English:</h2> Format date and return an Optional object.
		 * <h2>Chinese:</h2> 格式化日期，返回Optional对象
		 * 
		 * @param date 可以为null
		 * @return Optional String result
		 */
		public Optional<String> format2(Date date) {
			return date == null ? Optional.empty() : Optional.of(get().format(date));
		}

		/**
		 * <h2>English:</h2> Parse time. Returns null if input is empty.
		 * <h2>Chinese:</h2> 解析时间，如果为空返回null
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
		
		public LocalDate parseLocalDate(CharSequence s) {
			return LocalDate.parse(s, df);
		}
		
		public LocalDateTime parseLocalDateTime(CharSequence s) {
			return LocalDateTime.parse(s,df);
		}
		
		public java.sql.Date parseSqlDate(String s)throws IllegalArgumentException {
			java.util.Date d=parse(s);
			return d == null ? null : new java.sql.Date(d.getTime());
		}

		/**
		 * <h2>English:</h2> Parse time. Returns default value if input is empty or parsing fails.
		 * <h2>Chinese:</h2> 解析时间，如果为空或者转换出错，都返回默认值
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
		 * <h2>English:</h2> Format date in the specified timezone. Returns null if input is null (Null-safety).
		 * <h2>Chinese:</h2> 格式化日期，按指定的时区进行输出
		 * 
		 * @param date 如果传入null返回null(Null-safety.)
		 * @param zone zone 时区（如果该时区有夏令时会被忽略）
		 * @return text
		 */
		public String format(Date date, TimeZone zone) {
			return format0(date, zone.getRawOffset() - millisOffset);
		}

		/**
		 * <h2>English:</h2> Format date in the specified UTC offset. Returns null if input is null (Null-safety).
		 * <h2>Chinese:</h2> 格式化日期，按指定的时区进行输出
		 *
		 * @param date           如果传入null返回null(Null-safety.)
		 * @param utcHoursOffset 相对国际原子时的时差，从-12到+14(中国为8)，可以传小数
		 * @return 指定时区内的时间
		 */
		public String format(Date date, double utcHoursOffset) {
			return format0(date, (int) ((utcHoursOffset - hoursOffset) * 3600_000));
		}

		/**
		 * <h2>English:</h2> Parse date in the specified timezone.
		 * <h2>Chinese:</h2> 解析日期
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
		 * <h2>English:</h2> Parse date in the specified UTC offset.
		 * <h2>Chinese:</h2> 解析日期
		 * 
		 * @param text           text
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
		 * <h2>English:</h2> Parse time. Returns default value if input is empty or parsing fails.
		 * <h2>Chinese:</h2> 解析时间，如果为空或者转换出错，都返回默认值
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
	 * <h2>English:</h2> Create a thread-safe DateFormat.
	 * <h2>Chinese:</h2> 得到Thread-safe的DateFormat
	 * 
	 * @param pattern pattern
	 * @return TLDateFormat
	 */
	public static final TLDateFormat create(String pattern) {
		return new TLDateFormat(pattern);
	}
}
