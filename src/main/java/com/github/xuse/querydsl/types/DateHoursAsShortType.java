package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.querydsl.sql.types.AbstractType;

/**
 * 小时数映射为SMAILLINT(2bytes)数据库类型， Java中的为Date类型，数据库为SMALLINT类型(精度只能到2~4小时)
 * 
 * @author jiyi
 *
 */
public class DateHoursAsShortType extends AbstractType<Date> {
	/**
	 * 精度。如果需要5分钟秒精度的存储，传入TimeUnit.HOURS.toMillis(3)
	 */
	private long scale;

	private long offset;

	private static final long OFFSET = TimeUnit.DAYS.toMillis(22000);

	public DateHoursAsShortType(long scale) {
		super(Types.SMALLINT);
		this.scale = scale;
		this.offset = OFFSET / scale;
	}

	public DateHoursAsShortType() {
		this(TimeUnit.HOURS.toMillis(2));
	}

	@Override
	public Class<Date> getReturnedClass() {
		return Date.class;
	}

	@Override
	public Date getValue(ResultSet rs, int startIndex) throws SQLException {
		int n = rs.getInt(startIndex);
		return rs.wasNull() ? null : toDate(n);
	}

	/**
	 *
	 */
	@Override
	public void setValue(PreparedStatement st, int startIndex, Date value) throws SQLException {
		st.setInt(startIndex, toInt(value));
	}

	private int toInt(Date value) {
		int n = (int) ((value.getTime()) / scale - offset);
		return n;
	}

	private Date toDate(int n) {
		return new Date((n + offset) * scale);
	}
	
//	private int toInt(Date value) {
//		int n = (int) ((value.getTime()-OFFSET) / scale);
//		return n;
//	}
//
//	private Date toDate(int n) {
//		return new Date((n * scale) +OFFSET);
//	}

	public Date getMaxValue() {
		return toDate(Short.MAX_VALUE);
	}

	public Date getMinValue() {
		return toDate(Short.MIN_VALUE);
	}

	public static void main(String[] args) {
		
		System.out.println(new Date().getTime()/1000/3600/24);
		
		DateHoursAsShortType t = new DateHoursAsShortType();
		int n = t.toInt(new Date());
		System.out.println(n);
		System.out.println(t.toDate(n));

		System.out.println(t.getMaxValue());
		System.out.println(t.getMinValue());
	}

}
