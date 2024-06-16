package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.querydsl.sql.types.AbstractType;

/**
 * 分钟数映射为Integer(4bytes)数据库类型，
 * Java中的为Date类型，数据库为Integer类型
 * @author jiyi
 *
 */
public class DateSecondAsIntegerType extends AbstractType<Date>{
	/**
	 * 精度。如果需要5秒精度的存储，传入TimeUnit.SECONDS.toMillis(5)
	 */
	private long scale;
	
	private static final long OFFSET=TimeUnit.DAYS.toMillis(19578);
	
	
	public DateSecondAsIntegerType(long scale) {
		super(Types.INTEGER);
		this.scale=scale;
	}
	public DateSecondAsIntegerType() {
		this(TimeUnit.SECONDS.toMillis(1));
	}

	@Override
	public Class<Date> getReturnedClass() {
		return Date.class;
	}

	@Override
	public Date getValue(ResultSet rs, int startIndex) throws SQLException {
		int n=rs.getInt(startIndex);
		return rs.wasNull()?null:toDate(n);
	}

	/**
	 *
	 */
	@Override
	public void setValue(PreparedStatement st, int startIndex, Date value) throws SQLException {
		st.setInt(startIndex, toInt(value));
	}

	private int toInt(Date value) {
		int min=(int)((value.getTime()-OFFSET)/scale);
		return min;
	}

	private Date toDate(int n) {
		return new Date((n*scale)+OFFSET);
	}
	
	
	public static void main(String[] args) {
		DateSecondAsIntegerType t=new DateSecondAsIntegerType();
		
		
		System.out.println(new Date().getTime()/1000/3600/24);
		
		int n=t.toInt(new Date());
		System.out.println(n);
		System.out.println(t.toDate(n));
		
		System.out.println(t.toDate(Integer.MAX_VALUE));
		System.out.println(t.toDate(Integer.MIN_VALUE));
		
		
		
	}
	
}
