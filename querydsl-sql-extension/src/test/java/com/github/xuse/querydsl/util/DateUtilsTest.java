package com.github.xuse.querydsl.util;

import static com.github.xuse.querydsl.util.DateUtils.today;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class DateUtilsTest {
	private final Date today = DateUtils.get(2024, 1, 1);
	private final Date afterDay = DateUtils.get(2024, 1, 3);
	private final long todayMillis=today.getTime();
	
	private final long millisTime=1075658522666L;
	
	@Test
	public void testDateUtils() {
		Date d=DateUtils.yesterday();
		d=DateUtils.now();
		d=DateUtils.sqlNow();
		d=today();
		
		
		long baseMillis=d.getTime();
		DateUtils.addDay(d, 1);
		assertTrue(TimeUnit.DAYS.toMillis(1)==(d.getTime()-baseMillis));
		d.setTime(baseMillis);
		
		DateUtils.addHour(d, 1);
		assertTrue(TimeUnit.HOURS.toMillis(1)==(d.getTime()-baseMillis));
		d.setTime(baseMillis);
		
		DateUtils.addMillis(d, 1);
		assertTrue(1==(d.getTime()-baseMillis));
		d.setTime(baseMillis);
		
		DateUtils.addMinute(d, 1);
		assertTrue(60_000==(d.getTime()-baseMillis));
		d.setTime(baseMillis);
		
		DateUtils.addSec(d, 1);
		assertEquals(1000L,d.getTime()-baseMillis);
		d.setTime(baseMillis);
		
		DateUtils.addMonth(this.today, 1);
		assertEquals("2024-02-01",DateFormats.DATE_CS.format(this.today));
		resetToday();
		
		DateUtils.addYear(this.today, 1);
		assertEquals("2025-01-01",DateFormats.DATE_CS.format(this.today));
		resetToday();
		
		Date ret = DateUtils.adjustDate(this.today, 1, 1, 1);
		assertEquals("2025-02-02",DateFormats.DATE_CS.format(ret));
		
		ret = DateUtils.futureDay(1);
		assertTrue(ret.getTime()-System.currentTimeMillis()>86399);
		
		ret=DateUtils.adjust(today, 1000);
		assertEquals(1000,ret.getTime()-today.getTime());
		
		ret=DateUtils.adjustTime(today, 1, 1, 1);
		assertEquals(3661000,ret.getTime()-today.getTime());
		
		List<Date> ds=new ArrayList<>();
		for(Date t:DateUtils.dayIterator(today, afterDay)) {
			ds.add(t);
		}
		assertEquals(3, ds.size());
		
		ds=new ArrayList<>();
		for(Date t:DateUtils.monthIterator(DateUtils.get(2023, 3, 1), today)) {
			ds.add(t);
		}
		assertEquals(11, ds.size());
		
		assertEquals(-2,DateUtils.daySubtract(today, afterDay));
		assertEquals(-2,DateUtils.daySubtract(today, afterDay,TimeZone.getDefault()));
		assertEquals(-172800,DateUtils.secondSubtract(today, afterDay));
		
		assertEquals("2024-01-01 00:00:00",DateUtils.format(today.getTime()));
		assertEquals("2024-01-01 00:00:00",DateUtils.formatDateTime(today).orElse(""));
		assertEquals("",DateUtils.formatDateTime(null).orElse(""));
		
		assertEquals("5天39分钟3秒",DateUtils.formatTimePeriod(434343));
		assertEquals("5 days 39 minutes 3 seconds",DateUtils.formatTimePeriod(434343,-1,Locale.US));
		assertEquals("120 hours 39 minutes 3 seconds",DateUtils.formatTimePeriod(434343,Calendar.HOUR,Locale.US));
		assertEquals("1 minute 3 seconds",DateUtils.formatTimePeriod(63,Calendar.YEAR,Locale.US));
		assertEquals("2年11月21天9小时59分钟3秒",DateUtils.formatTimePeriod(93434343,-1,Locale.CHINA));
		
		Date now=DateUtils.fromInstant(null);
		assertNull(now);
		now=DateUtils.fromSqlDate(null);
		assertNull(now);
		Time time=DateUtils.toSqlTime((LocalTime)null);
		assertNull(time);
		
		now=DateUtils.fromInstant(Instant.now());
		now=DateUtils.fromLocalDate(LocalDate.now());
		now=DateUtils.fromLocalDateTime(LocalDateTime.now());
		now=DateUtils.fromLocalTime(LocalTime.now());
		now=DateUtils.fromSqlDate(new java.sql.Date(baseMillis));
		assertEquals(baseMillis, now.getTime());
		
		assertEquals("2004/02/02 00:00:00", DateFormats.DATE_TIME_CS2.format(DateUtils.get(2004, 2, 2)));
		assertEquals("2004/02/02 02:02:02", DateFormats.DATE_TIME_CS2.format(DateUtils.get(2004, 2, 2, 2, 2, 2)));
		assertEquals("2004/02/02 00:00:00", DateFormats.DATE_TIME_CS2.format(DateUtils.getSqlDate(2004, 2, 2)));
		assertEquals("2004-02-02 10:02:02", DateFormats.DATE_TIME_CS.format(DateUtils.getUTC(2004, 2, 2, 2, 2, 2)));

		assertEquals("2004/02/02 02:02:02", DateFormats.DATE_TIME_CS2.format(DateUtils.getInstant(2004, 2, 2, 2, 2, 2)));
		assertEquals("2004/02/02 00:00:00", DateFormats.DATE_TIME_CS2.format(DateUtils.getInstant(2004, 2, 2)));
		
		ret = new Date(millisTime);
		assertEquals("2004-02-02 02:02:02.000",DateFormats.TIME_STAMP_CS.format(DateUtils.getTruncated(ret, Calendar.SECOND)));
		assertEquals("2004-02-02 02:02:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.getTruncated(ret, Calendar.MINUTE)));
		assertEquals("2004-02-02 02:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.getTruncated(ret, Calendar.HOUR)));
		assertEquals("2004-02-02 00:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.getTruncated(ret, Calendar.DATE)));
		assertEquals("2004-02-01 00:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.getTruncated(ret, Calendar.MONTH)));
		assertEquals("2004-01-01 00:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.getTruncated(ret, Calendar.YEAR)));
		
		assertEquals("2004-01-01 08:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.truncateToYear(ret, TimeZones.UTC)));
		assertEquals("2004-02-01 08:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.truncateToMonth(ret, TimeZones.UTC)));
		assertEquals("2004-02-01 08:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.truncateToDay(ret,TimeZones.UTC)));
		assertEquals("2004-02-02 02:00:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.truncateToHour(ret, TimeZones.UTC)));
		assertEquals("2004-02-02 02:02:00.000",DateFormats.TIME_STAMP_CS.format(DateUtils.truncateToMinute(ret)));
		assertEquals("2004-02-02 02:02:02.000",DateFormats.TIME_STAMP_CS.format(DateUtils.truncateToSecond(ret)));
		
		assertEquals(2004,DateUtils.getYear(ret));
		assertEquals(2004,DateUtils.getYear(ret,TimeZones.UTC_3));
		assertEquals(2,DateUtils.getMonth(ret));
		assertEquals(2,DateUtils.getMonth(ret,TimeZones.UTC_3));
		assertEquals(2,DateUtils.getDay(ret));
		assertEquals(1,DateUtils.getDay(ret,TimeZones.UTC_3));
		assertEquals(29,DateUtils.getDaysInMonth(ret));
		assertEquals(29,DateUtils.getDaysInMonth(ret,TimeZones.UTC_3));
		assertEquals(2,DateUtils.getHour(ret));
		assertEquals(21,DateUtils.getHour(ret,TimeZones.UTC_3));
		assertEquals(2,DateUtils.getMinute(ret));
		assertEquals(2,DateUtils.getMinute(ret,TimeZones.UTC_3));
		assertEquals(2,DateUtils.getSecond(ret));
		assertEquals(1,DateUtils.getWeekDay(ret));
		assertEquals(0,DateUtils.getWeekDay(ret,TimeZones.UTC_3));
		assertEquals(6,DateUtils.getWeekOfYear(ret));
		assertEquals(6,DateUtils.getWeekOfYear(ret,TimeZones.UTC_3));
		
		assertEquals("20040201180202.666",DateUtils.getFtpDate(millisTime));
		assertEquals(1075658522000L, DateUtils.parseFTPDate("20040201180202.666").getTime());
		assertEquals("2004-02-02T02:02:02",DateUtils.getISO8601Date(millisTime));
		assertEquals("Feb  2  2004",DateUtils.getUnixDate(millisTime));
		
		assertTrue(DateUtils.getUnixDate(DateUtils.yesterday().getTime()).length()>0);
		
		assertArrayEquals(new int[] { 2, 2, 2 }, DateUtils.getHMS(ret));
		assertArrayEquals(new int[] { 0, 0, 0 }, DateUtils.getHMS(null));
		assertArrayEquals(new int[] { 4, 2, 2 }, DateUtils.getHMS(ret, TimeZones.UTC_10));
		assertArrayEquals(new int[] { 2004, 2, 1 }, DateUtils.getYMD(ret,TimeZones.getByUTCOffset(5.5)));
		assertArrayEquals(new int[] { 23, 32, 2 }, DateUtils.getHMS(ret, TimeZones.getByUTCOffset(5.5)));
		assertArrayEquals(new int[] { 2004, 2, 2 }, DateUtils.getYMD(ret));
		assertArrayEquals(new int[] { 2004, 2, 1 }, DateUtils.getYMD(ret, TimeZones.UTC_1));
		assertArrayEquals(new int[] { 0, 0, 0 }, DateUtils.getYMD(null));

		ret=DateUtils.get(2004, 2, 1, 1, 0 ,0);
		assertFalse(DateUtils.isDayBegin(ret,TimeZone.getDefault()));
		assertTrue(DateUtils.isDayBegin(ret,TimeZones.UTC_7));
		
		assertFalse(DateUtils.isSameDay(ret,null,TimeZone.getDefault()));
		assertFalse(DateUtils.isSameDay(null,ret,TimeZone.getDefault()));
		assertTrue(DateUtils.isSameDay(null,null,TimeZone.getDefault()));
		assertFalse(DateUtils.isSameDay(ret, DateUtils.adjustTime(ret, -2,0,0), TimeZone.getDefault()));
		assertTrue(DateUtils.isSameDay(ret, DateUtils.adjustTime(ret, -2,0,0), TimeZones.UTC_6));
		assertFalse(DateUtils.isSameMonth(ret, DateUtils.adjustDate(ret, 0, -20, 0)));
		assertTrue(DateUtils.isSameMonth(ret, DateUtils.adjustTime(ret, 0, 2, 0)));
		assertFalse(DateUtils.isSameMonth(ret, DateUtils.adjustTime(ret, -2, 0, 0), TimeZone.getDefault()));
		assertTrue(DateUtils.isSameMonth(ret, DateUtils.adjustTime(ret, -2, 0, 0), TimeZones.UTC_6));
		
		
		assertEquals(1075050000000L, DateUtils.weekBegin(ret, TimeZone.getDefault()).getTime());
		assertEquals(1075136400000L, DateUtils.weekBegin(ret, TimeZones.UTC).getTime());
		assertEquals(1075568400000L, DateUtils.weekBeginUS(ret, TimeZone.getDefault()).getTime());
		assertEquals(1075050000000L, DateUtils.weekBeginUS(ret, TimeZones.UTC).getTime());
		assertEquals(1075568400000L, DateUtils.weekEnd(ret, TimeZone.getDefault()).getTime());
		assertEquals(1075654800000L, DateUtils.weekEnd(ret, TimeZones.UTC).getTime());
		assertEquals(1076086800000L, DateUtils.weekEndUS(ret, TimeZone.getDefault()).getTime());
		assertEquals(1075568400000L, DateUtils.weekEndUS(ret, TimeZones.UTC).getTime());
		
		assertEquals(YearMonth.of(2004,2),DateUtils.toYearMonth(ret));
		assertEquals(YearMonth.of(2004,2),DateUtils.toYearMonth(ret,TimeZones.Asia_Shanghai.toZoneId()));
		assertEquals(MonthDay.of(2,1),DateUtils.toMonthDay(ret));
		assertEquals(MonthDay.of(1,31),DateUtils.toMonthDay(ret,TimeZones.UTC.toZoneId()));
		
		assertEquals(DateUtils.toLocalDate(ret),DateUtils.toLocalDate(DateUtils.toSqlDate(ret)));
		assertEquals(DateUtils.toSqlDate(DateUtils.toLocalDate(ret)),DateUtils.toSqlDate(DateUtils.truncateToDay(ret)));
		assertEquals(DateUtils.toLocalDateTime(ret),DateUtils.toLocalDateTime(DateUtils.toSqlTimeStamp(ret)));
		assertEquals(DateUtils.toLocalTime(ret),DateUtils.toLocalTime(DateUtils.toSqlTime(ret)));
		assertEquals(DateUtils.toSqlTimeStamp(ret),DateUtils.toSqlTimeStamp(DateUtils.toLocalDateTime(ret)));
		assertEquals(DateUtils.toSqlTimeStamp(DateUtils.toInstant(ret)),DateUtils.toSqlTimeStamp(DateUtils.toLocalDateTime(ret)));
		assertEquals(DateUtils.toLocalTime(DateUtils.toSqlTimeStamp(ret)),DateUtils.toLocalTime(ret));
		
		assertEquals(DateUtils.toSqlTimeStamp(DateUtils.toLocalDate(ret),DateUtils.toLocalTime(ret)),DateUtils.toSqlTimeStamp(ret));
		assertArrayEquals(DateUtils.getHMS(DateUtils.toSqlTime(DateUtils.toLocalTime(ret))),DateUtils.getHMS(DateUtils.toSqlTime(ret)));
		{
			ret= null;
			assertEquals(DateUtils.toLocalDate(ret),DateUtils.toLocalDate(DateUtils.toSqlDate(ret)));
			assertEquals(DateUtils.toSqlDate(DateUtils.toLocalDate(ret)),DateUtils.toSqlDate(DateUtils.truncateToDay(ret)));
			assertEquals(DateUtils.toLocalDateTime(ret),DateUtils.toLocalDateTime(DateUtils.toSqlTimeStamp(ret)));
			assertEquals(DateUtils.toLocalTime(ret),DateUtils.toLocalTime(DateUtils.toSqlTime(ret)));
			assertEquals(DateUtils.toSqlTimeStamp(ret),DateUtils.toSqlTimeStamp(DateUtils.toLocalDateTime(ret)));
			assertEquals(DateUtils.toSqlTimeStamp(DateUtils.toInstant(ret)),DateUtils.toSqlTimeStamp(DateUtils.toLocalDateTime(ret)));
			assertEquals(DateUtils.toLocalTime(DateUtils.toSqlTimeStamp(ret)),DateUtils.toLocalTime(ret));
			assertEquals(DateUtils.toSqlTimeStamp(DateUtils.toLocalDate(ret),DateUtils.toLocalTime(ret)),DateUtils.toSqlTimeStamp(ret));
			
			
			assertEquals(0,DateUtils.getYear(ret));
			assertEquals(0,DateUtils.getYear(ret,TimeZones.UTC_3));
			assertEquals(0,DateUtils.getMonth(ret));
			assertEquals(0,DateUtils.getMonth(ret,TimeZones.UTC_3));
			assertEquals(0,DateUtils.getDay(ret));
			assertEquals(0,DateUtils.getDay(ret,TimeZones.UTC_3));
			assertEquals(-1,DateUtils.getHour(ret));
			assertEquals(-1,DateUtils.getHour(ret,TimeZones.UTC_3));
			assertEquals(-1,DateUtils.getMinute(ret));
			assertEquals(-1,DateUtils.getMinute(ret,TimeZones.UTC_3));
			assertEquals(-1,DateUtils.getSecond(ret));
			assertEquals(-1,DateUtils.getWeekDay(ret));
			assertEquals(-1,DateUtils.getWeekDay(ret,TimeZones.UTC_3));
			
			assertNull(DateUtils.truncateToYear(ret, TimeZones.UTC));
			assertNull(DateUtils.truncateToMonth(ret, TimeZones.UTC));
			assertNull(DateUtils.truncateToDay(ret,TimeZones.UTC));
			assertNull(DateUtils.truncateToHour(ret, TimeZones.UTC));
			assertNull(DateUtils.truncateToMinute(ret));
			assertNull(DateUtils.truncateToSecond(ret));
		}
		{
			ret = new Date(baseMillis);
			DateUtils.prevMillis(ret);
			assertEquals(baseMillis-1, ret.getTime());	
		}
	}

	private void resetToday() {
		today.setTime(todayMillis);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void exceptionCase() {
		DateFormats.TIME_STAMP_CS.format(DateUtils.getTruncated(today, Calendar.WEEK_OF_YEAR));
	}
	
	@Test
	public void testDataFormats() {
		Date d=new Date();
		assertNull(DateFormats.DATE_CS.format((Date)null));
		assertNull(DateFormats.DATE_CS.format((Instant)null));
	
		assertNull(DateFormats.DATE_CS.parse(""));
		assertEquals(d,DateFormats.DATE_CS.parse("",d));
		assertEquals(d,DateFormats.DATE_CS.parse("aaa",d));
		assertEquals(today,DateFormats.DATE_CS.parse("2024-01-01"));
		
		
		assertEquals("2024-01-01",DateFormats.DATE_CS.format(today,TimeZones.UTC_8));
		assertEquals("2023-12-31",DateFormats.DATE_CS.format(today,7.5));
		assertNull(DateFormats.DATE_CS.format((Date)null,TimeZones.UTC_8));
		assertNull(DateFormats.DATE_CS.format((Date)null,8d));
		
		
		assertEquals(today,DateFormats.DATE_CS.parse("2024-01-01"));
		assertEquals(today,DateFormats.DATE_CS.parse("2024-01-01",d));
		assertEquals(today,DateFormats.DATE_CS.parse("2024-01-01",8d));
		assertEquals(today,DateFormats.DATE_CS.parse("2024-01-01",TimeZones.UTC_8));
		assertEquals(null,DateFormats.DATE_CS.parse("",TimeZones.UTC_8));
		assertEquals(null,DateFormats.DATE_CS.parse("",8d));
		assertEquals(d,
		DateFormats.DATE_CS.parse("2024.01.01",d,TimeZones.UTC_8));
		assertEquals(d,
				DateFormats.DATE_CS.parse("",d,TimeZones.UTC_8));
		assertEquals(today,
				DateFormats.DATE_CS.parse("2024-01-01",d,TimeZones.UTC_8));
		assertEquals("2024-01-01",DateFormats.create("yyyy-MM-dd").format(today));
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDataParseException() {
		DateFormats.DATE_CS.parse("asadsada");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDataParseException2() {
		DateFormats.DATE_CS.parse("2024.01.01",TimeZones.UTC_8);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDataParseException3() {
		DateFormats.DATE_CS.parse("2024.01.01",8d);
	}
	
	
}
