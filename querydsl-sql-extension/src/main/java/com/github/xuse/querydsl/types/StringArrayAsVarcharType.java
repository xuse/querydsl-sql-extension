package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

public class StringArrayAsVarcharType extends AbstractType<String[]>{
	public StringArrayAsVarcharType() {
		super(Types.VARCHAR);
	}

	private char sepChar = ',';
	
	@Override
	public Class<String[]> getReturnedClass() {
		return String[].class;
	}

	@Override
	public String[] getValue(ResultSet rs, int startIndex) throws SQLException {
		String s= rs.getString(startIndex);
		if(s==null) {
			return null;
		}
		return StringUtils.split(s,sepChar);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, String[] value) throws SQLException {
		StringBuilder sb=new StringBuilder();
		for(String s:value) {
			sb.append(s).append(sepChar);
		}
		int len=sb.length();
		if(len>0) {
			sb.setLength(len - 1);
		}
		st.setString(startIndex, sb.toString());
	}
}
