package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

public class FloatArrayAsVarcharType extends AbstractType<float[]>{
	public FloatArrayAsVarcharType() {
		super(Types.VARCHAR);
	}

	private char sepChar = ',';
	
	private boolean invalidStrAsZero = false;
	
	@Override
	public Class<float[]> getReturnedClass() {
		return float[].class;
	}

	@Override
	public float[] getValue(ResultSet rs, int startIndex) throws SQLException {
		String s = rs.getString(startIndex);
		if (s == null) {
			return null;
		}
		String[] ss = StringUtils.split(s, sepChar);
		int len = ss.length;
		float[] floats = new float[len];
		for (int i = 0; i < len; i++) {
			try {
				floats[i] = Float.parseFloat(ss[i]);
			} catch (NumberFormatException e) {
				if (invalidStrAsZero) {
					floats[i] = 0f;
				} else {
					throw e;
				}
			}
		}
		return floats;
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, float[] value) throws SQLException {
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

	public FloatArrayAsVarcharType setSepChar(char sepChar) {
		this.sepChar = sepChar;
		return this;
	}

	public boolean isInvalidStrAsZero() {
		return invalidStrAsZero;
	}

	public FloatArrayAsVarcharType invalidStrAsZero(boolean invalidStrAsZero) {
		this.invalidStrAsZero = invalidStrAsZero;
		return this;
	}
}
