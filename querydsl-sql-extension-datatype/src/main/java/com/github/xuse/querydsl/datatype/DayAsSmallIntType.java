package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.TimeZone;

import com.querydsl.sql.types.AbstractType;

public class DayAsSmallIntType extends AbstractType<Date>{
	
	/**
	 * 时区
	 */
	private TimeZone tz=TimeZone.getDefault();

	public DayAsSmallIntType() {
		super(Types.SMALLINT);
	}

	@Override
	public Class<Date> getReturnedClass() {
		return Date.class;
	}

	@Override
	public Date getValue(ResultSet rs, int startIndex) throws SQLException {
		int i=rs.getInt(startIndex);
		return rs.wasNull()?null:toDate(i);
	}

	private Date toDate(int i) {
		return new Date(i*86400000L-tz.getRawOffset());
	}

	private int toInt(Date value) {
		return (int)((value.getTime()+tz.getRawOffset())/86400000L);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Date value) throws SQLException {
		st.setInt(startIndex, toInt(value));
	}
	
	public TimeZone getTimezone() {
		return tz;
	}

	/**
	 * 设置取日期的所在时区，使用本类型映射将日期存入数据库再取出，会截断到当天的开始时间，因此时区对这一时间有影响。
	 * 例如,传入北京时间2024-01-01 22:00:
	 * <ol>
	 * <li>如果本类时区也是北京时间，存入数据库再取出后，得到2024-01-01 00:00:00。</li>
	 * <li>如果本类时区是澳大利亚时区(UTC+10:00)，存入数据库后再取出，得到澳大利亚时间2024-01-02 00:00:00 
	 * 该时间等于北京时间2024-01-01 21:00:00.</li> 
	 * </ol>
	 * 因此，在多时区应用中，尤其需要注意不同时区的影响。
	 * @param tz Time zone.
	 */
	public void setTimezone(TimeZone tz) {
		this.tz = tz;
	}
}
