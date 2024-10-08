package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.querydsl.sql.types.AbstractType;

public class PrimitiveShortType extends AbstractType<Short>{
	public PrimitiveShortType() {
		super(Types.SMALLINT);
	}

	@Override
	public Short getValue(ResultSet rs, int startIndex) throws SQLException {
		return rs.getShort(startIndex);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Short value) throws SQLException {
		st.setShort(startIndex, value);
	}

	@Override
	public Class<Short> getReturnedClass() {
		return Short.TYPE;
	}
}
