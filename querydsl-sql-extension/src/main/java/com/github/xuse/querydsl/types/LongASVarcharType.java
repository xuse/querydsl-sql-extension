package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：Java类中Long映射数据库Varchar
 * @author Administrator
 *
 */
public class LongASVarcharType extends AbstractType<Long> {

	public LongASVarcharType() {
		super(Types.VARCHAR);
	}

	
	@Override
	public Class<Long> getReturnedClass() {
		 return Long.class;
	}

	@Override
	public Long getValue(ResultSet rs, int startIndex) throws SQLException {
		String s=rs.getString(startIndex);
		if(StringUtils.isEmpty(s)) {
			return null;
		}else {
			return Long.valueOf(s);
		}
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Long value) throws SQLException {
		if(value==null) {
			st.setNull(startIndex, Types.VARCHAR);
		}else {
			st.setString(startIndex, value.toString());
		}
	}

}
