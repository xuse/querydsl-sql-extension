package com.github.xuse.querydsl.entity.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.DateFormats.TLDateFormat;
import com.querydsl.sql.types.AbstractType;

public class TestForStringAsDateTimeType extends AbstractType<String> {

	private final TLDateFormat df;
	
	public TestForStringAsDateTimeType(String format) {
		super(Types.TIMESTAMP);
		df=DateFormats.create(format);
	}
	
	public TestForStringAsDateTimeType() {
		super(Types.DATE);
		df=DateFormats.DATE_TIME_CS;
	}

	@Override
	public Class<String> getReturnedClass() {
		return String.class;
	}

	@Override
	public String getValue(ResultSet rs, int startIndex) throws SQLException {
		java.sql.Timestamp date=rs.getTimestamp(startIndex);
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
			st.setNull(startIndex, Types.TIMESTAMP);
		}else {
			st.setTimestamp(startIndex, new java.sql.Timestamp(d.getTime()))	;
		}

	}

}
