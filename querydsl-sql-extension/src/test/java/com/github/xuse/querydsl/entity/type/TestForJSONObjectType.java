package com.github.xuse.querydsl.entity.type;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.alibaba.fastjson2.JSON;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：给予JSON序列化与反序列化进行数据存储与读取的类型映射
 * @author Administrator
 * @param <T> type of target
 */
public class TestForJSONObjectType<T> extends AbstractType<T> {

	/**
	 * 使用Class构造
	 * @param clz clz
	 */
	public TestForJSONObjectType(Class<T> clz) {
		super(Types.VARCHAR);
		this.clz = clz;
		this.type = clz;
	}

	/**
	 * 使用Type构造
	 * @param clz clz
	 */
	public TestForJSONObjectType(Type clz) {
		super(Types.VARCHAR);
		this.clz = null;
		this.type = clz;
	}

	private final Class<T> clz;

	private final Type type;

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
