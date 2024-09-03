package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.querydsl.sql.types.AbstractType;

public class PrimitiveBooleanType extends AbstractType<Boolean>{
	public PrimitiveBooleanType() {
		super(Types.BOOLEAN);
	}

	@Override
	public Boolean getValue(ResultSet rs, int startIndex) throws SQLException {
		return rs.getBoolean(startIndex);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Boolean value) throws SQLException {
		st.setBoolean(startIndex, value);
	}

	@Override
	public Class<Boolean> getReturnedClass() {
		return Boolean.TYPE;
	}
}
