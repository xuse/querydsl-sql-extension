package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：给予JSON序列化与反序列化进行数据存储与读取的类型映射
 * @author Administrator
 *
 * @param <T>
 */
public class JSONObjectType<T> extends AbstractType<T>{
	public JSONObjectType(Class<T> clz) {
		super(Types.VARCHAR);
		this.clz=clz;
	}

	private final Class<T> clz;
	
	@Override
	public Class<T> getReturnedClass() {
		return clz;
	}

	@Override
	public T getValue(ResultSet rs, int startIndex) throws SQLException {
		String s=rs.getString(startIndex);
		if(StringUtils.isEmpty(s)) {
			return null;
		}else {
			return JSON.parseObject(s,clz);
		}
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, T value) throws SQLException {
		if(value==null) {
			st.setNull(startIndex,Types.VARCHAR);
		}else {
			st.setString(startIndex, JSON.toJSONString(value));
		}
	}
}
