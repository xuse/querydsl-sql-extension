package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

public class DoubleArrayAsVarcharType extends AbstractType<double[]>{
	public DoubleArrayAsVarcharType() {
		super(Types.VARCHAR);
	}

	private char sepChar = ',';
	
	private boolean invalidStrAsZero = false;
	
	@Override
	public Class<double[]> getReturnedClass() {
		return double[].class;
	}

	@Override
	public double[] getValue(ResultSet rs, int startIndex) throws SQLException {
		String s = rs.getString(startIndex);
		if (s == null) {
			return null;
		}
		String[] ss = StringUtils.split(s, sepChar);
		int len = ss.length;
		double[] doubles = new double[len];
		for (int i = 0; i < len; i++) {
			try {
				doubles[i] = Double.parseDouble(ss[i]);
			} catch (NumberFormatException e) {
				if (invalidStrAsZero) {
					doubles[i] = 0d;
				} else {
					throw e;
				}
			}
		}
		return doubles;
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, double[] value) throws SQLException {
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

	public DoubleArrayAsVarcharType setSepChar(char sepChar) {
		this.sepChar = sepChar;
		return this;
	}

	public boolean isInvalidStrAsZero() {
		return invalidStrAsZero;
	}

	public DoubleArrayAsVarcharType invalidStrAsZero(boolean invalidStrAsZero) {
		this.invalidStrAsZero = invalidStrAsZero;
		return this;
	}
}
