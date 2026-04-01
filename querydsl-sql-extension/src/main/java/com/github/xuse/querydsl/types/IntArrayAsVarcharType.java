package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

public class IntArrayAsVarcharType extends AbstractType<int[]>{
	public IntArrayAsVarcharType() {
		super(Types.VARCHAR);
	}

	private char sepChar = ',';
	
	private boolean invalidStrAsZero = false;
	
	@Override
	public Class<int[]> getReturnedClass() {
		return int[].class;
	}

	@Override
	public int[] getValue(ResultSet rs, int startIndex) throws SQLException {
		String s = rs.getString(startIndex);
		if (s == null) {
			return null;
		}
		String[] ss = StringUtils.split(s, sepChar);
		int len = ss.length;
		int[] ints = new int[len];
		for (int i = 0; i < len; i++) {
			try {
				ints[i] = Integer.parseInt(ss[i]);
			} catch (NumberFormatException e) {
				if (invalidStrAsZero) {
					ints[i] = 0;
				} else {
					throw e;
				}
			}
		}
		return ints;
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, int[] value) throws SQLException {
		StringBuilder sb=new StringBuilder();
		for(double s:value) {
			sb.append(s).append(sepChar);
		}
		int len=sb.length();
		if(len>0) {
			sb.setLength(len - 1);
		}
		st.setString(startIndex, sb.toString());
	}

	public char getSepChar() {
		return sepChar;
	}

	public IntArrayAsVarcharType setSepChar(char sepChar) {
		this.sepChar = sepChar;
		return this;
	}

	public boolean isInvalidStrAsZero() {
		return invalidStrAsZero;
	}

	public IntArrayAsVarcharType invalidStrAsZero(boolean invalidStrAsZero) {
		this.invalidStrAsZero = invalidStrAsZero;
		return this;
	}
}
