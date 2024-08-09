package com.github.xuse.querydsl.types;

import java.sql.Types;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 低精度时间表示法。使用SMAILLINT(2bytes)数据存储。
 * <p>
 * 默认精度为4小时。可以表达UTC:2023-12-31 20:00:00 to UTC:2053-11-26 08:00:00的时间范围。
 * <p>
 * 如果收缩精度到3小时，可以表达2024-01-01 05:00:00 to 2046-06-06 02:00:00时间范围。
 * 一般来说精度不建议放到2小时以下。
 * 
 * 传入参数在按精度取整时遵循向下取整规则。比如2小时精度时，00:00,02:00是整数，01:00会被认为是00:00。
 */
public class DateHoursAsShortType extends AbstractLowPrecisionTime {
	/**
	 * 自定指定时间精度和基准位置的构造
	 * @param scale 时间精度
	 * @param offset 基准时间点
	 */
	public DateHoursAsShortType(long scale, long offset) {
		super(Types.SMALLINT, scale, offset);
	}
	
	public DateHoursAsShortType(long scale) {
		super(Types.SMALLINT, scale, 1704067200000L + (scale * Short.MAX_VALUE));
	}

	public DateHoursAsShortType() {
		this(TimeUnit.HOURS.toMillis(4));
	}

	public int getMaxNum() {
		return Short.MAX_VALUE;
	}

	public int getMinNum() {
		return Short.MIN_VALUE;
	}

	@Override
	public Date truncateAs(Date d) {
		int value=toInt(d);
		if(value>Short.MAX_VALUE || value<Short.MIN_VALUE) {
			throw new IndexOutOfBoundsException();
		}
		return toDate(value);
	}
	
	
}
