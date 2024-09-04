package com.github.xuse.querydsl.util;

import static com.github.xuse.querydsl.util.DateUtils.today;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class DateUtilsTest {
	private final Date today = DateUtils.get(2024, 1, 1);
	private final Date tomorrow = DateUtils.get(2024, 1, 2);
	
	@Test
	public void testDateUtils() {
		Date d=today();
		long baseMillis=d.getTime();
		DateUtils.addDay(d, 1);
		assertTrue(TimeUnit.DAYS.toMillis(1)==(d.getTime()-baseMillis));
		d.setTime(baseMillis);
		
		DateUtils.addHour(d, 1);
		assertTrue(TimeUnit.HOURS.toMillis(1)==(d.getTime()-baseMillis));
		d.setTime(baseMillis);
		
	}

	
}
