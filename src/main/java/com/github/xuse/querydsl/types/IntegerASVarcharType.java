package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：Java类中Integer映射数据库Varchar
 * @author Administrator
 *
 */
public class IntegerASVarcharType extends AbstractType<Integer> {

	public IntegerASVarcharType() {
		super(Types.VARCHAR);
	}

	
	@Override
	public Class<Integer> getReturnedClass() {
		 return Integer.class;
	}

	@Override
	public Integer getValue(ResultSet rs, int startIndex) throws SQLException {
		String s=rs.getString(startIndex);
		if(StringUtils.isEmpty(s)) {
			return null;
		}else {
			return Integer.valueOf(s);
		}
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Integer value) throws SQLException {
		if(value==null) {
			st.setNull(startIndex, Types.VARCHAR);
		}else {
			st.setString(startIndex, value.toString());
		}
	}

}
