package com.github.xuse.querydsl.annotation.partition;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

final class DateFormats {
	
	static final DateTimeFormatter P_YEAR_MONTH = DateTimeFormatter.ofPattern("yyyyMM");
	static final DateTimeFormatter P_DATE_SHORT = DateTimeFormatter.ofPattern("yyyyMMdd");
	static final DateTimeFormatter P_DATE_CS = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	static Function<Date, String> YEAR_MONTH = (d) -> format(P_YEAR_MONTH, d);
	static Function<Date, String> DATE_SHORT = (d) -> format(P_DATE_SHORT, d);
	static Function<Date, String> DATE_CS = (d) -> format(P_DATE_CS, d);
	
	
	static String format(DateTimeFormatter f, Date d) {
		return d == null ? "" : f.format(LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()));
	}
}
