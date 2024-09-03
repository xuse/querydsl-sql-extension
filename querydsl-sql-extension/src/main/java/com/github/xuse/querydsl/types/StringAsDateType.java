package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.DateFormats.TLDateFormat;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：Java类中String映射数据库Date
 * @author Administrator
 *
 */
public class StringAsDateType extends AbstractType<String> {

	private final TLDateFormat df;
	
	public StringAsDateType(String format) {
		super(Types.DATE);
		df=DateFormats.create(format);
	}
	
	public StringAsDateType() {
		super(Types.DATE);
		df=DateFormats.DATE_CS;
	}

	@Override
	public Class<String> getReturnedClass() {
		return String.class;
	}

	@Override
	public String getValue(ResultSet rs, int startIndex) throws SQLException {
		java.sql.Date date=rs.getDate(startIndex);
		if(date==null) {
			return null;
		}else {
			return df.format(date);
		}
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, String value) throws SQLException {
		Date d=df.parse(value);
		if(d==null) {
			st.setNull(startIndex, Types.DATE);
		}else {
			st.setDate(startIndex, new java.sql.Date(d.getTime()))	;
		}

	}

}
