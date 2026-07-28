package com.github.xuse.querydsl.datatype;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.datatype.json.JSON;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

public class JacksonJsonType<T> extends AbstractType<T> {
	private final Class<T> clz;

	private final Type type;
	
	/**
	 * 使用Class构造
	 * @param clz clz
	 */
	public JacksonJsonType(Class<T> clz) {
		super(Types.VARCHAR);
		this.clz = clz;
		this.type = clz;
	}

	/**
	 * 使用Type构造
	 * @param clz clz
	 */
	public JacksonJsonType(Type clz) {
		super(Types.VARCHAR);
		this.clz = null;
		this.type = clz;
	}


	@Override
	public Class<T> getReturnedClass() {
		return clz;
	}

	@Override
	public T getValue(ResultSet rs, int startIndex) throws SQLException {
		String s = rs.getString(startIndex);
		if (StringUtils.isEmpty(s)) {
			return null;
		} else {
			return parse(s);
		}
	}

	private T parse(String s) {
		return clz == null ? JSON.parseObject(s, type) : JSON.parseObject(s, clz);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, T value) throws SQLException {
		if (value == null) {
			st.setNull(startIndex, Types.VARCHAR);
		} else {
			st.setString(startIndex, JSON.toJSONString(value));
		}
	}
}
