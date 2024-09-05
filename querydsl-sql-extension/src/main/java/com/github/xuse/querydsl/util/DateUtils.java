/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.xuse.querydsl.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;

public class DateUtils {

	private static final int MILLISECONDS_IN_DAY = 86400000;

	private static final int MILLISECONDS_IN_HOUR = 3600000;

	private static final int MILLISECONDS_IN_MINUTE = 60000;

	private static final int MILLISECONDS_IN_SECOND = 1000;

	public static final int SECONDS_IN_DAY = 86400;

	public static final int SECONDS_IN_HOUR = 3600;

	public static final int SECONDS_IN_MINITE = 60;

	private static final TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

	private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
			"Nov", "Dec" };

	/*
	 * Creates the DateFormat object used to parse/format dates in FTP format.
	 */
	private static final ThreadLocal<DateFormat> FTP_DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			df.setLenient(false);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			return df;
		}
	};

	/**
	 * Get unix style date string.
	 * 
	 * @param millis millis
	 * @return String in unix format.
	 */
	public static final String getUnixDate(long millis) {
		if (millis < 0) {
			return "------------";
		}
		StringBuilder sb = new StringBuilder(16);
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(millis);
		// month
		sb.append(MONTHS[cal.get(Calendar.MONTH)]);
		sb.append(' ');
		// day
		int day = cal.get(Calendar.DATE);
		if (day < 10) {
			sb.append(' ');
		}
		sb.append(day);
		sb.append(' ');
		// 183L * 24L * 60L * 60L * 1000L;
		long sixMonth = 15811200000L;
		long nowTime = System.currentTimeMillis();
		if (Math.abs(nowTime - millis) > sixMonth) {
			// year
			int year = cal.get(Calendar.YEAR);
			sb.append(' ');
			sb.append(year);
		} else {
			// hour
			int hh = cal.get(Calendar.HOUR_OF_DAY);
			if (hh < 10) {
				sb.append('0');
			}
			sb.append(hh);
			sb.append(':');
			// minute
			int mm = cal.get(Calendar.MINUTE);
			if (mm < 10) {
				sb.append('0');
			}
			sb.append(mm);
		}
		return sb.toString();
	}

	/**
	 * @param millis millis
	 * @return ISO 8601 timestamp.
	 */
	public static final String getISO8601Date(long millis) {
		StringBuilder sb = new StringBuilder(19);
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(millis);
		// year
		sb.append(cal.get(Calendar.YEAR));
		// month
		sb.append('-');
		int month = cal.get(Calendar.MONTH) + 1;
		if (month < 10) {
			sb.append('0');
		}
		sb.append(month);
		// date
		sb.append('-');
		int date = cal.get(Calendar.DATE);
		if (date < 10) {
			sb.append('0');
		}
		sb.append(date);
		// hour
		sb.append('T');
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) {
			sb.append('0');
		}
		sb.append(hour);
		// minute
		sb.append(':');
		int min = cal.get(Calendar.MINUTE);
		if (min < 10) {
			sb.append('0');
		}
		sb.append(min);
		// second
		sb.append(':');
		int sec = cal.get(Calendar.SECOND);
		if (sec < 10) {
			sb.append('0');
		}
		sb.append(sec);
		return sb.toString();
	}

	/**
	 * Get FTP date.
	 * 
	 * @param millis millis
	 * @return formatted string in UTC
	 */
	public static final String getFtpDate(long millis) {
		StringBuilder sb = new StringBuilder(20);
		// MLST should use UTC
		Calendar cal = new GregorianCalendar(TIME_ZONE_UTC);
		cal.setTimeInMillis(millis);
		// year
		sb.append(cal.get(Calendar.YEAR));
		// month
		int month = cal.get(Calendar.MONTH) + 1;
		if (month < 10) {
			sb.append('0');
		}
		sb.append(month);
		// date
		int date = cal.get(Calendar.DATE);
		if (date < 10) {
			sb.append('0');
		}
		sb.append(date);
		// hour
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) {
			sb.append('0');
		}
		sb.append(hour);
		// minute
		int min = cal.get(Calendar.MINUTE);
		if (min < 10) {
			sb.append('0');
		}
		sb.append(min);
		// second
		int sec = cal.get(Calendar.SECOND);
		if (sec < 10) {
			sb.append('0');
		}
		sb.append(sec);
		// millisecond
		sb.append('.');
		int milli = cal.get(Calendar.MILLISECOND);
		if (milli < 100) {
			sb.append('0');
		}
		if (milli < 10) {
			sb.append('0');
		}
		sb.append(milli);
		return sb.toString();
	}

	/*
	 * Parses a date in the format used by the FTP commands involving dates(MFMT,
	 * MDTM)
	 */
	@SneakyThrows
	public static final Date parseFTPDate(String dateStr) {
		return FTP_DATE_FORMAT.get().parse(dateStr);
	}

	/**
	 * 当两个时间属于同一天时，返回true.
	 * 在特定时区，计算时间是否在同一天是非常容易的。但在不同时区，日期分隔线的不同会造成不同的结果。
	 * 此方法支持传入时区进行计算。
	 * <p>
	 * 请注意：当传入两个日期均为null时，返回true。
	 * <p>
	 * return true if the date1 and date2 is on the same day. Specifically, return
	 * true when both input dates are null.
	 * 
	 * @param d1   d1
	 * @param d2   d2
	 * @param zone zone 时区，不同地区对“当天”的范围是不一样的
	 * @return true if is save date in the time zone.
	 */
	public static boolean isSameDay(Date d1, Date d2, TimeZone zone) {
		if (d1 == null && d2 == null)
			return true;
		if (d1 == null || d2 == null)
			return false;
		return truncateToDay(d1, zone).getTime() == truncateToDay(d2, zone).getTime();
	}

	/**
	 * 是否同一个月内
	 * 
	 * @param d1 d1 日期1
	 * @param d2 d2 日期2
	 * @return true if is same month in current time zone.
	 */
	public static boolean isSameMonth(Date d1, Date d2) {
		return truncateToMonth(d1).getTime() == truncateToMonth(d2).getTime();
	}

	/**
	 * 是否同一个月内
	 * 
	 * @param d1   d1
	 * @param d2   d2
	 * @param zone zone 时区，不同地区对“当天”的范围是不一样的
	 * @return true if is same month in the time zone.
	 */
	public static boolean isSameMonth(Date d1, Date d2, TimeZone zone) {
		return truncateToMonth(d1, zone).getTime() == truncateToMonth(d2, zone).getTime();
	}

	/**
	 * 得到年份
	 * 
	 * @param d 时间
	 * @return 年份
	 */
	public static int getYear(Date d) {
		return getYear(d, TimeZone.getDefault());
	}

	/**
	 * 得到年份
	 *
	 * @param d    时间
	 * @param zone 时区
	 * @return year
	 */
	public static int getYear(Date d, TimeZone zone) {
		if (d == null)
			return 0;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		return c.get(Calendar.YEAR);
	}

	public static int getWeekOfYear(Date date) {
		return getWeekOfYear(date, TimeZone.getDefault());
	}

	public static int getWeekOfYear(Date date, TimeZone zone) {
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(date);
		return c.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 得到月份 (1~12)
	 * 
	 * @param d d
	 * @return 月份，范围 [1,12]。
	 */
	public static int getMonth(Date d) {
		return getMonth(d, TimeZone.getDefault());
	}

	/**
	 * 得到月份 (1~12)
	 * 
	 * @param d    d
	 * @param zone zone
	 * @return 月份，范围 [1,12]。
	 */
	public static int getMonth(Date d, TimeZone zone) {
		if (d == null)
			return 0;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		return c.get(Calendar.MONTH) + 1;
	}

	/**
	 * 得到当月时的天数
	 * 
	 * @param d d
	 * @return 当月内的日期：天
	 */
	public static int getDay(Date d) {
		return getDay(d, TimeZone.getDefault());
	}

	/**
	 * 得到当月时的天数
	 * 
	 * @param d    d
	 * @param zone zone
	 * @return 日期
	 */
	public static int getDay(Date d, TimeZone zone) {
		if (d == null)
			return 0;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		return c.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 得到该时间是星期几
	 * 
	 * @param d 日期
	 * @return 0=周日,1~6=周一到周六<br>
	 *         注意返回值和Calendar定义的sunday等常量不同，而是星期一返回数字1，这更符合国人的习惯。
	 *         如果传入null，那么返回-1表示无效。
	 */
	public static int getWeekDay(Date d) {
		return getWeekDay(d, TimeZone.getDefault());
	}

	/**
	 * 得到该时间是星期几
	 *
	 * @param d    日期
	 * @param zone 时区
	 * @return 0: 周日,1~6 周一到周六<br>
	 *         注意返回值和Calendar定义的Sunday等常量不同，星期一返回数字1，更符合中国人的习惯。
	 *         如果传入null，那么返回-1表示无效。
	 */
	public static int getWeekDay(Date d, TimeZone zone) {
		if (d == null)
			return -1;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		return c.get(Calendar.DAY_OF_WEEK) - 1;
	}

	/**
	 * 返回传入日期所在周的第一天。<br>
	 * 按中国和部分欧洲习惯，<strong>星期一 作为每周的第一天</strong>
	 * 返回新的日期对象，输入的时分秒将被保留。
	 * <p>
	 * <strong>A Week is Monday to Sunday</strong>
	 * @param date date
	 * @param zone zone
	 * @return The first day of the week. Note: only the date was adjusted. time is
	 *         kept as original.
	 */
	public static Date weekBegin(Date date, TimeZone zone) {
		return toWeekDayCS(date, 1, zone);
	}

	/**
	 * 返回传入日期所在周的最后一天。<br>
	 * 按中国和部分欧洲习惯，<strong>星期天 作为每周的最后一天</strong>
	 * 返回新的日期对象，输入的时分秒将被保留。
	 * <p>
	 * <strong>A Week is Monday to Sunday</strong>
	 * @param date date
	 * @param zone zone
	 * @return The last day of the week. Note: only the date was adjusted. time is
	 *         kept as original.
	 */
	public static Date weekEnd(Date date, TimeZone zone) {
		return toWeekDayCS(date, 7, zone);
	}

	/**
	 * 返回传入日期所在周的第一天。<br>
	 * 按美国人/天主教习惯，<strong>星期天 作为每周的第一天</strong>
	 * 返回新的日期对象，输入的时分秒将被保留。
	 * <p>
	 * <strong>A Week is Sunday to Saturday</strong>
	 * @param date date
	 * @param zone zone
	 * @return The first day of the week. Note: only the date was adjusted. time is
	 *         kept as original.
	 */
	public static Date weekBeginUS(Date date, TimeZone zone) {
		return toWeekDayUS(date, 0, zone);
	}

	/**
	 * 返回传入日期所在周的最后一天。 按美国人/天主教习惯，<strong>星期六 作为每周的最后一天</strong>
	 * 返回新的日期对象，输入的时分秒将被保留。
	 * <p>
	 * <strong>A Week is Sunday to Saturday</strong>
	 * @param date date
	 * @param zone zone
	 * @return The last day of the week. Note: only the date was adjusted. time is
	 *         kept as original.
	 */
	public static Date weekEndUS(Date date, TimeZone zone) {
		return toWeekDayUS(date, 6, zone);
	}

	private static Date toWeekDayCS(Date date, int expect, TimeZone zone) {
		int day = getWeekDay(date, zone);
		if (day == 0)
			day = 7;
		return adjustDate(date, 0, 0, expect - day);
	}

	private static Date toWeekDayUS(Date date, int expect, TimeZone zone) {
		int day = getWeekDay(date, zone);
		return adjustDate(date, 0, 0, expect - day);
	}

	/**
	 * 得到小时数：24小时制
	 * 
	 * @param d d
	 * @return 24小时制的小时数,return -1 if date is null.
	 */
	public static int getHour(Date d) {
		return getHour(d, TimeZone.getDefault());
	}

	/**
	 * 得到小时数：24小时制
	 * 
	 * @param d    date
	 * @param zone 时区
	 * @return 24小时制的小时数。 return -1 if date is null.
	 */
	public static int getHour(Date d, TimeZone zone) {
		if (d == null)
			return -1;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		return c.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * @param d date
	 * @return 获得该时间的分钟数,return -1 if date is null.
	 */
	public static int getMinute(Date d) {
		return getMinute(d,TimeZone.getDefault());
	}
	
	/**
	 * @param d date
	 * @param zone time zone.
	 * @return 获得该时间的分钟数,return -1 if date is null.
	 */
	public static int getMinute(Date d, TimeZone zone) {
		if (d == null)
			return -1;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		return c.get(Calendar.MINUTE);
	}

	/**
	 * 获得该时间的秒数,如果传入日期为null，返回-1
	 * 
	 * @param d d
	 * @return 秒,return -1 if date is null.
	 */
	public static int getSecond(Date d) {
		if (d == null)
			return -1;
		final Calendar c = new GregorianCalendar();
		c.setTime(d);
		return c.get(Calendar.SECOND);
	}

	/**
	 * 以数组的形式，返回年、月、日三个值
	 * 
	 * @param d d
	 * @return int[]{year, month, day}，其中month的范围是1~12。
	 */
	public static int[] getYMD(Date d) {
		return getYMD(d, TimeZone.getDefault());
	}

	/**
	 * 以数组的形式，返回年、月、日三个值
	 * 
	 * @param d    d
	 * @param zone 时区
	 * @return int[]{year, month, day}，其中month的范围是1~12。
	 */
	public static int[] getYMD(Date d, TimeZone zone) {
		int[] ymd = new int[3];
		if (d == null)
			return ymd;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		ymd[0] = c.get(Calendar.YEAR);
		ymd[1] = c.get(Calendar.MONTH) + 1;
		ymd[2] = c.get(Calendar.DAY_OF_MONTH);
		return ymd;
	}

	/**
	 * 以数组的形式，返回时、分、秒 三个值
	 * 
	 * @param d d
	 * @return 时、分、秒
	 */
	public static int[] getHMS(Date d) {
		return getHMS(d, TimeZone.getDefault());
	}

	/**
	 * 以数组的形式，返回时、分、秒 三个值
	 * 
	 * @param d    d
	 * @param zone 时区
	 * @return 时、分、秒
	 */
	public static int[] getHMS(Date d, TimeZone zone) {
		int[] hms = new int[3];
		if (d == null)
			return hms;
		final Calendar c = new GregorianCalendar(zone);
		c.setTime(d);
		hms[0] = c.get(Calendar.HOUR_OF_DAY);
		hms[1] = c.get(Calendar.MINUTE);
		hms[2] = c.get(Calendar.SECOND);
		return hms;
	}

	/**
	 * 在指定日期上减去1毫秒
	 * 
	 * @param d date
	 */
	public static void prevMillis(Date d) {
		d.setTime(d.getTime() - 1);
	}

	/**
	 * @param d     日期
	 * @param value 加指定毫秒
	 */
	public static void addMillis(Date d, long value) {
		d.setTime(d.getTime() + value);
	}

	/**
	 * @param d     日期
	 * @param value 加指定秒
	 */
	public static void addSec(Date d, long value) {
		d.setTime(d.getTime() + TimeUnit.SECONDS.toMillis(value));
	}

	/**
	 * @param d     时间
	 * @param value 加指定分
	 */
	public static void addMinute(Date d, int value) {
		d.setTime(d.getTime() + TimeUnit.MINUTES.toMillis(value));
	}

	/**
	 * @param d     时间
	 * @param value 加指定小时
	 */
	public static void addHour(Date d, int value) {
		d.setTime(d.getTime() + TimeUnit.HOURS.toMillis(value));
	}

	/**
	 * @param d     时间
	 * @param value 加指定天
	 */
	public static void addDay(Date d, int value) {
		d.setTime(d.getTime() + TimeUnit.DAYS.toMillis(value));
	}

	/**
	 * 加指定月
	 * 
	 * @param d     时间
	 * @param value 加指定月
	 */
	public static void addMonth(Date d, int value) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, value);
		d.setTime(c.getTime().getTime());
	}

	/**
	 * 加指定年
	 * 
	 * @param d     d
	 * @param value value
	 */
	public static void addYear(Date d, int value) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.YEAR, value);
		d.setTime(c.getTime().getTime());
	}

	/**
	 * 在原日期上增加指定的 年、月、日数 。这个方法不会修改传入的Date对象，而是一个新的Date对象
	 * 
	 * @param date  date 原日期时间
	 * @param year  year 增加的年（可为负数）
	 * @param month month 增加的月（可为负数）
	 * @param day   day 增加的日（可为负数）
	 * @return 调整后的日期（新的日期对象）
	 */
	public static Date adjustDate(Date date, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, year);
		c.add(Calendar.MONTH, month);
		c.add(Calendar.DAY_OF_YEAR, day);
		return c.getTime();
	}

	/**
	 * 在原日期上增加指定的 时、分、秒数 。这个方法不会修改传入的Date对象，而是一个新的Date对象
	 * 
	 * @param date   date 原日期时间
	 * @param hour   hour 增加的时（可为负数）
	 * @param minute minute 增加的分（可为负数）
	 * @param second second 增加的秒（可为负数）
	 * @return 调整后的日期时间（新的日期对象）
	 */
	public static Date adjustTime(Date date, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR, hour);
		c.add(Calendar.MINUTE, minute);
		c.add(Calendar.SECOND, second);
		return c.getTime();
	}

	/**
	 * 在原日期上调整指定的毫秒并返回新对象。这个方法不会修改传入的Date对象，而是一个新的Date对象。
	 * 
	 * @param date  date 原日期时间
	 * @param mills mills 毫秒数（可为负数）
	 * @return 调整后的日期时间（新的日期对象）
	 */
	public static Date adjust(Date date, long mills) {
		return new Date(date.getTime() + mills);
	}

	/**
	 * 获取一个日期对象(java.util.Date)
	 * 
	 * @param year  year 格式为：2004
	 * @param month month 从1开始
	 * @param date  date 从1开始
	 * @return 要求的日期
	 */
	public static final Instant getInstant(int year, int month, int date) {
		return get(year, month, date).toInstant();
	}

	/**
	 * 获取一个日期对象(java.util.Date)
	 * 
	 * @param year  year 格式为：2004
	 * @param month month 从1开始
	 * @param date  date 从1开始
	 * @return 要求的日期
	 */
	public static final Date get(int year, int month, int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, date, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取一个日期对象(java.sql.Date)
	 * 
	 * @param year  year
	 * @param month month
	 * @param date  date
	 * @return Date
	 */
	public static final java.sql.Date getSqlDate(int year, int month, int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, date, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(calendar.getTime().getTime());
	}

	/**
	 * 获得一个UTC时间
	 * 
	 * @param year   year
	 * @param month  month
	 * @param date   date
	 * @param hour   hour
	 * @param minute minute
	 * @param second second
	 * @return Date
	 */
	public static final Date getUTC(int year, int month, int date, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance(TimeZones.UTC);
		calendar.set(year, month - 1, date, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取一个时间对象，时间对象以当前默认时区为准。
	 * 
	 * @param year   year 格式为：2004
	 * @param month  month 从1开始
	 * @param date   date 从1开始
	 * @param hour   hour 小时(0-24)
	 * @param minute minute 分(0-59)
	 * @param second second 秒(0-59)
	 * @return Date
	 */
	public static final Date get(int year, int month, int date, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, date, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取一个时间对象
	 * 
	 * @param year   year 格式为：2004
	 * @param month  month 从1开始
	 * @param date   date 从1开始
	 * @param hour   hour 小时(0-24)
	 * @param minute minute 分(0-59)
	 * @param second second 秒(0-59)
	 * @return Instant
	 */
	public static final Instant getInstant(int year, int month, int date, int hour, int minute, int second) {
		return get(year, month, date, hour, minute, second).toInstant();
	}

	/**
	 * 返回两个时间相差的天数
	 * 
	 * @param a a
	 * @param b b
	 * @return TimeZone
	 */
	public static final int daySubtract(Date a, Date b) {
		return daySubtract(a, b, TimeZone.getDefault());
	}

	/**
	 * 返回在指定时区下，两个时间之间相差的天数。
	 * 
	 * @param a    a
	 * @param b    b
	 * @param zone zone，(备注:当前算法不考虑该时区的夏令时。
	 * @return 相差的天数
	 */
	public static final int daySubtract(Date a, Date b, TimeZone zone) {
		int offset = zone.getRawOffset();
		int date = (int) (((a.getTime() + offset) / MILLISECONDS_IN_DAY
				- (b.getTime() + offset) / MILLISECONDS_IN_DAY));
		return date;
	}

	/**
	 * 返回两个时间相差多少秒
	 * 
	 * @param a a
	 * @param b b
	 * @return 相差的秒数
	 */
	public static final long secondSubtract(Date a, Date b) {
		return ((a.getTime() - b.getTime()) / 1000);
	}

	/**
	 * 得到该日期所在月包含的天数
	 * 
	 * @param date date
	 * @return 2月返回28或29，1月返回31
	 */
	public static final int getDaysInMonth(Date date) {
		return getDaysInMonth(date, TimeZone.getDefault());
	}

	/**
	 * 得到该日期所在月包含的天数
	 * 
	 * @param date date
	 * @param zone zone
	 * @return 2月返回28或29，1月返回31
	 */
	public static final int getDaysInMonth(Date date, TimeZone zone) {
		Assert.notNull(date);
		Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(date);
		int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		return day;
	}

	/**
	 * 格式化时间段
	 * 
	 * @param second second
	 * @return String
	 */
	public static String formatTimePeriod(long second) {
		return formatTimePeriod(second, Calendar.DATE, Locale.getDefault());
	}
	
	
	private static final long SECONDS_IN_365_DAY = 86400 * 365;
	private static final long SECONDS_IN_30_DAY = 86400 * 30;
	
	/**
	 * 将秒数转换为时长描述
	 * @param second second
	 * @param maxUnit maxUnit
	 *            :最大单位,Calendar.DAY
	 *
	 * @param  locale 区域，根据区域返回语言
	 * @return 时长市场信息
	 */
	public static String formatTimePeriod(long second, int maxUnit, Locale locale) {
		if (locale == null)
			locale = Locale.getDefault();
		LanguageResoruce lang=TIME_LANGUAGES.get(locale);
		if(lang==null) {
			lang=TIME_LANGUAGES.get(Locale.US);
		}
		StringBuilder sb = new StringBuilder();
		if (maxUnit <= Calendar.YEAR && second > SECONDS_IN_365_DAY) {
			int yy = (int) (second / SECONDS_IN_365_DAY);
			if(yy>0) {
				sb.append(lang.get(Calendar.YEAR, yy));
				second = second - SECONDS_IN_365_DAY * yy;
			}
		}
		if (maxUnit <= Calendar.MONTH) {
			int mm = (int) (second / SECONDS_IN_30_DAY);
			if (mm > 0) {
				sb.append(lang.get(Calendar.MONTH,mm));
				second = second - SECONDS_IN_30_DAY * mm;
			}
		}
		if (maxUnit <= Calendar.DAY_OF_MONTH) {
			int days = (int) (second / SECONDS_IN_DAY);
			if (days > 0) {
				sb.append(lang.get(Calendar.DAY_OF_MONTH,days));
				second = second - SECONDS_IN_DAY * days;
			}
		}
		if (maxUnit<= Calendar.HOUR) {
			int hours = (int) (second / SECONDS_IN_HOUR);
			if (hours > 0) {
				sb.append(lang.get(Calendar.HOUR,hours));
				second = second - SECONDS_IN_HOUR * hours;
			}
		}
		if (maxUnit <= Calendar.MINUTE) {
			int min = (int) (second / SECONDS_IN_MINITE);
			if (min > 0) {
				sb.append(lang.get(Calendar.MINUTE,min));
				second = second - SECONDS_IN_MINITE * min;
			}
		}
		if (second > 0) {
			sb.append(lang.get(Calendar.SECOND,(int)second));
		}
		return sb.toString();
	}

	public static final Map<Locale, LanguageResoruce> TIME_LANGUAGES = new HashMap<>();
	static {
		LanguageResoruce CN = new LanguageResoruce(
				new String[] { "公元", "年", "月", "周", "周", "天", "天", "天", "天", "上下午", "小时", "小时", "分钟", "秒" }, null);
		LanguageResoruce TW = new LanguageResoruce(
				new String[] { "公元", "年", "月", "周", "周", "天", "天", "天", "天", "上下午", "小時", "小時", "分鐘", "秒" }, null);
		LanguageResoruce EN = new LanguageResoruce(
				new String[] { "AD/BC", " years ", " months ", " weeks", " weeks", " days ", " days ", " days ",
						" days ", " AM/PM ", " hours ", " hours ", " minutes ", " seconds" },
				new String[] { "AD/BC", " year ", " month ", " week", " week", " day ", " day ", " day ", " day ",
						" AM/PM ", " hour ", " hour ", " minute ", " second" });
		TIME_LANGUAGES.put(Locale.CHINA, CN);
		TIME_LANGUAGES.put(Locale.CHINESE, CN);
		TIME_LANGUAGES.put(Locale.TRADITIONAL_CHINESE, TW);
		TIME_LANGUAGES.put(Locale.TAIWAN, TW);
		TIME_LANGUAGES.put(Locale.US, EN);
	}

	static class LanguageResoruce {
		private final String[] resource;
		private final String[] singular;

		LanguageResoruce(String[] a, String[] b) {
			this.resource = a;
			this.singular = b == null ? a : b;
		}

		public String getSingular(int code) {
			return singular[code];
		};

		public CharSequence get(int code, int value) {
			if (value == 0) {
				return "";
			} else if (value == 1) {
				return value + singular[code];
			} else {
				return value + resource[code];
			}
		};
	}

	/**
	 * 返回“昨天”的同一时间
	 *
	 * @return 昨天
	 */
	public static Date yesterday() {
		return futureDay(-1);
	}

	/**
	 * @param i 天数，可以传入负数，比如-1表示昨天，-2表示前天
	 * @return 未来多少天的同一时间
	 */
	public static Date futureDay(int i) {
		return new Date(System.currentTimeMillis() + (long) MILLISECONDS_IN_DAY * i);
	}

	/**
	 * 将系统格式时间(毫秒)转换为文本格式
	 * 
	 * @param millseconds millseconds
	 * @return 日期格式
	 */
	public static String format(long millseconds) {
		return DateFormats.DATE_TIME_CS.format(millseconds);
	}

	/**
	 * 格式化为日期+时间（中式）
	 * 
	 * @param d d
	 * @return format后的字符
	 */
	public static Optional<String> formatDateTime(Date d) {
		return DateFormats.DATE_TIME_CS.format2(d);
	}

	/**
	 * 月份遍历器 指定两个日期，遍历两个日期间的所有月份。（包含开始时间和结束时间所在的月份）
	 * 
	 * @param includeStart includeStart
	 * @param includeEnd   includeEnd
	 * @return 月份遍历
	 */
	public static Iterable<Date> monthIterator(Date includeStart, Date includeEnd) {
		return new TimeIterable(includeStart, includeEnd, Calendar.MONTH).setIncludeEndDate(true);
	}

	/**
	 * 日遍历器 指定两个时间，遍历两个日期间的所有天。（包含开始时间和结束时间所在的天）
	 * 
	 * @param includeBegin includeStart the begin date.(include)
	 * @param includeEnd   includeEnd the end date(include)
	 * @return A iterable object that can iterate the date.
	 */
	public static Iterable<Date> dayIterator(final Date includeBegin, final Date includeEnd) {
		return new TimeIterable(includeBegin, includeEnd, Calendar.DATE).setIncludeEndDate(true);
	}

	/**
	 * 返回今天
	 *
	 * @return the begin of today.
	 */
	public static Date today() {
		return truncateToDay(new Date());
	}

	/**
	 * @return 返回现在
	 */
	public static java.sql.Timestamp sqlNow() {
		return new java.sql.Timestamp(System.currentTimeMillis());
	}

	/**
	 * 返回现在时间
	 *
	 * @return the current date time.
	 */
	public static Date now() {
		return new Date();
	}

	/**
	 * 指定时间是否为一天的开始
	 * 
	 * @param date date
	 * @param zone zone
	 * @return true if the date is the begin of day.
	 */
	public static boolean isDayBegin(Date date, TimeZone zone) {
		Date d1 = truncateToDay(date, zone);
		return d1.getTime() == date.getTime();
	}

	/**
	 * Convert to Instance
	 * 
	 * @see Instant
	 * @param date date java.util.Date
	 * @return instant
	 */
	public static Instant toInstant(Date date) {
		return date == null ? null : date.toInstant();
	}

	/**
	 * Convert LocalDate to jud
	 * 
	 * @param date date LocalDate
	 * @return java.util.Date
	 */
	public static Date fromLocalDate(LocalDate date) {
		return date == null ? null : Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Convert LocalTime to jud.
	 * 
	 * @param time time LocalTime
	 * @return java.util.Date
	 */
	public static Date fromLocalTime(LocalTime time) {
		return time == null ? null
				: Date.from(LocalDateTime.of(LocalDate.now(), time).atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts LocalDateTime to java.util.Date (null safety)
	 *
	 * @param value LocalDateTime
	 * @return java.util.Date
	 */
	public static Date fromLocalDateTime(LocalDateTime value) {
		return value == null ? null : Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts java.sql.Date to LocalDate (null safety)
	 *
	 * @param date java.sql.Date
	 * @return LocalDate
	 */
	public static LocalDate toLocalDate(java.sql.Date date) {
		return date == null ? null : date.toLocalDate();
	}

	/**
	 * Converts java.util.Date to LocalDate (null safety)
	 * 
	 * @param date date java.util.Date
	 * @return LocalDate
	 */
	public static LocalDate toLocalDate(java.util.Date date) {
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * Converts Time to LocalTime (null safety)
	 * 
	 * @param time time
	 * @return LocalTime
	 */
	public static LocalTime toLocalTime(java.sql.Time time) {
		return time == null ? null : time.toLocalTime();
	}

	/**
	 * Converts Timestamp to LocalTime (null safety)
	 * 
	 * @param ts ts Timestamp
	 * @return LocalTime
	 */
	public static LocalTime toLocalTime(java.sql.Timestamp ts) {
		return ts == null ? null : ts.toLocalDateTime().toLocalTime();
	}

	/**
	 * Converts java.util.Date to LocalTime (null safety)
	 * 
	 * @param date date
	 * @return {@link LocalTime}
	 */
	public static LocalTime toLocalTime(java.util.Date date) {
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
	}

	/**
	 * Convert Timestamp to LocalDateTime (null safety)
	 * 
	 * @param ts ts Timestamp
	 * @return LocalDateTime
	 */
	public static LocalDateTime toLocalDateTime(java.sql.Timestamp ts) {
		return ts == null ? null : ts.toLocalDateTime();
	}

	/**
	 * Convert java.util.Date to LocalDateTime (null safety)
	 * 
	 * @param date date date
	 * @return LocalDateTime
	 */
	public static LocalDateTime toLocalDateTime(java.util.Date date) {
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	/**
	 * Convert java.util.Date to YearMonth (null safety)
	 * 
	 * @param date date
	 * @return YearMonth
	 */
	public static YearMonth toYearMonth(java.util.Date date) {
		return toYearMonth(date, ZoneId.systemDefault());
	}
	
	public static YearMonth toYearMonth(java.util.Date date,ZoneId zone) {
		return date == null ? null : YearMonth.from(LocalDateTime.ofInstant(date.toInstant(), zone).toLocalDate());
	}
	
	public static MonthDay toMonthDay(java.util.Date date) {
		return toMonthDay(date, ZoneId.systemDefault());
	}
	
	/**
	 * Convert java.util.Date to MonthDay (null safety)
	 * 
	 * @param date date
	 * @return MonthDay
	 */
	public static MonthDay toMonthDay(java.util.Date date,ZoneId zone) {
		return date == null ? null : MonthDay.from(LocalDateTime.ofInstant(date.toInstant(), zone));
	}

	/**
	 * Converts LocalDate to Time (null safety)
	 * 
	 * @param date date LocalDate
	 * @return java.sql.Date
	 */
	public static java.sql.Date toSqlDate(LocalDate date) {
		return date == null ? null : java.sql.Date.valueOf(date);
	}

	/**
	 * Converts LocalTime to Time (null safety)
	 * 
	 * @param time time LocalTime
	 * @return java.sql.Time
	 */
	public static java.sql.Time toSqlTime(LocalTime time) {
		return time == null ? null : java.sql.Time.valueOf(time);
	}

	/**
	 * 转换为java.sql.timestamp
	 * 
	 * @param d d
	 * @return Timestamp
	 */
	public static Timestamp toSqlTimeStamp(Date d) {
		if (d == null)
			return null;
		return new java.sql.Timestamp(d.getTime());
	}

	/**
	 * Converts LocalDateTime to Timestamp (null safety)
	 * 
	 * @param time time LocalDateTime
	 * @return Timestamp
	 */
	public static java.sql.Timestamp toSqlTimeStamp(LocalDateTime time) {
		return time == null ? null : java.sql.Timestamp.valueOf(time);
	}

	/**
	 * Converts instant to Timestamp (null safety)
	 * 
	 * @param instant instant Instant
	 * @return java.sql.Timestamp
	 */
	public static java.sql.Timestamp toSqlTimeStamp(Instant instant) {
		return instant == null ? null : java.sql.Timestamp.from(instant);
	}

	/**
	 * Converts instant to JUD (null safety)
	 * 
	 * @param instant instant
	 * @return java.util.Date
	 */
	public static Date fromInstant(Instant instant) {
		return instant == null ? null : Date.from(instant);
	}

	/**
	 * Converts LocalTime to Timestamp (null safety)
	 * 
	 * @param localTime localTime LocalTime
	 * @return Timestamp
	 */
	public static Timestamp toSqlTimeStamp(LocalDate localDate,LocalTime localTime) {
		if(localDate==null) {
			localDate=LocalDate.now();
		}
		return localTime == null ? null : java.sql.Timestamp.valueOf(LocalDateTime.of(localDate, localTime));
	}

	/**
	 * 转换为java.sql.Date
	 * 
	 * @param d d
	 * @return Date
	 */
	public static java.sql.Date toSqlDate(Date d) {
		if (d == null)
			return null;
		return new java.sql.Date(d.getTime());
	}

	/**
	 * 转换为Sql的Time对象（不含日期）
	 * 
	 * @param date date
	 * @return Time
	 */
	public static java.sql.Time toSqlTime(Date date) {
		if (date == null)
			return null;
		return new java.sql.Time(date.getTime());
	}

	/**
	 * 从java.sql.Date转换到java.util.Date
	 * 
	 * @param d d
	 * @return Date
	 */
	public static Date fromSqlDate(java.sql.Date d) {
		if (d == null)
			return null;
		return new Date(d.getTime());
	}

	/**
	 * 取得截断后的日期/时间。注意这个方法不会修改传入的日期时间值，而是创建一个新的对象并返回。
	 * 
	 * @param start start
	 * @param unit  unit 时间单位，使用Calendar中的Field常量。
	 * @return 截断后的日期/时间
	 * @see Calendar
	 */
	public static Date getTruncated(Date start, int unit) {
		switch (unit) {
		case Calendar.SECOND:
			return truncateToSecond(start);
		case Calendar.MINUTE:
			return truncateToMinute(start);
		case Calendar.HOUR:
			return truncateToHour(start);
		case Calendar.DATE:
		case Calendar.DAY_OF_YEAR:
			return truncateToDay(start);
		case Calendar.MONTH:
			return truncateToMonth(start);
		case Calendar.YEAR:
			return truncateToYear(start);
		default:
			throw new UnsupportedOperationException("Unsupported unit:" + unit);
		}
	}

	/**
	 * 取得截去年以下单位的时间。注意这个方法不会修改传入的日期时间值，而是创建一个新的对象并返回。
	 * 
	 * @param d d 时间，如果传入null本方法将返回null.
	 * @return 截断后的时间
	 */
	public static Date truncateToYear(Date d) {
		return truncateToYear(d, TimeZone.getDefault());
	}

	/**
	 * 取得截去年以下单位的时间。注意这个方法不会修改传入的日期时间值，而是创建一个新的对象并返回。
	 * 
	 * @param d    d 时间，如果传入null本方法将返回null.
	 * @param zone zone 时区
	 * @return 截断后的时间
	 */
	public static Date truncateToYear(Date d, TimeZone zone) {
		if (d == null) {
			return null;
		}
		long l = d.getTime();
		long left = (l + zone.getRawOffset()) % MILLISECONDS_IN_DAY;
		l = l - left;
		Calendar c = Calendar.getInstance(zone);
		c.setTimeInMillis(l);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, 0);
		return c.getTime();
	}

	/**
	 * 取得截去月以下单位的时间。注意这个方法不会修改传入的日期时间值，而是创建一个新的对象并返回。
	 * 
	 * @param d d 时间，如果传入null本方法将返回null.
	 * @return 截断后的时间
	 */
	public static Date truncateToMonth(Date d) {
		return truncateToMonth(d, TimeZone.getDefault());
	}

	/**
	 * 取得截去月以下单位的时间。注意这个方法不会修改传入的日期时间值，而是创建一个新的对象并返回。
	 * 
	 * @param d    d 时间，如果传入null本方法将返回null.
	 * @param zone zone 时区
	 * @return 截断后的时间
	 */
	public static Date truncateToMonth(Date d, TimeZone zone) {
		if (d == null) {
			return null;
		}
		long l = d.getTime();
		long left = (l + zone.getRawOffset()) % MILLISECONDS_IN_DAY;
		l = l - left;
		Calendar c = Calendar.getInstance(zone);
		c.setTimeInMillis(l);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTime();
	}

	/**
	 * 取得截断后日期。注意这个方法不会修改传入的日期时间值，而是创建一个新的对象并返回。
	 * 
	 * @param d d 时间，如果传入null本方法将返回null.
	 * @return 截断后的时间
	 */
	public static Date truncateToDay(Date d) {
		return truncateToDay(d, TimeZone.getDefault());
	}

	/**
	 * 取得截断后日期。注意这个方法不会修改传入的日期时间值，而是创建一个新的对象并返回。
	 * 
	 * @param d    d 时间，如果传入null本方法将返回null.
	 * @param zone zone 时区
	 * @return 截断后的时间
	 */
	public static Date truncateToDay(Date d, TimeZone zone) {
		if (d == null) {
			return null;
		}
		long l = d.getTime();
		long left = (l + zone.getRawOffset()) % MILLISECONDS_IN_DAY;
		return new Date(l - left);
	}

	/**
	 * 取得截去分钟以下单位的时间。得到整点
	 * 
	 * @param d 时间，，如果传入null本方法将返回null.
	 * @return 截去分钟以下单位的时间。
	 */
	public static Date truncateToHour(Date d) {
		return truncateToHour(d, TimeZone.getDefault());
	}
	
	/**
	 * 取得截去分钟以下单位的时间。得到整点
	 * 
	 * @param d 时间，，如果传入null本方法将返回null.
	 * @param zone Time zone.
	 * @return 截去分钟以下单位的时间。
	 */
	public static Date truncateToHour(Date d, TimeZone zone) {
		if (d == null) {
			return null;
		}
		long l = d.getTime();
		long left = (l + zone.getRawOffset()) % MILLISECONDS_IN_HOUR;
		return new Date(l - left);
	}

	/**
	 * 取得截去秒以下单位的时间。得到整分钟时间点
	 * 
	 * @param d d 时间，，如果传入null本方法将返回null.
	 * @return 截去秒以下单位的时间。
	 */
	public static Date truncateToMinute(Date d) {
		if (d == null) {
			return null;
		}
		long l = d.getTime();
		long left = l % MILLISECONDS_IN_MINUTE;
		return new Date(l - left);
	}

	/**
	 * 取得截去毫秒以下单位的时间。得到整秒时间点
	 * 
	 * @param d d 时间，，如果传入null本方法将返回null.
	 * @return 截去毫秒以下单位的时间。
	 */
	public static Date truncateToSecond(Date d) {
		if (d == null) {
			return null;
		}
		long l = d.getTime();
		long left = l % MILLISECONDS_IN_SECOND;
		return new Date(l - left);
	}
}
