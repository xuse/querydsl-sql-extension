package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：Java类中String映射数据库Int
 * @author Administrator
 *
 */
public class StringAsIntegerType extends AbstractType<String>{

	public StringAsIntegerType() {
		  super(Types.INTEGER);
	}

	@Override
	public Class<String> getReturnedClass() {
		return String.class;
	}

	@Override
	public String getValue(ResultSet rs, int startIndex) throws SQLException {
		int code = rs.getInt(startIndex);
        return rs.wasNull() ? null : String.valueOf(code);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, String value) throws SQLException {
		try {
			if(StringUtils.isEmpty(value)) {
				st.setNull(startIndex, Types.INTEGER);
			}else {
				st.setInt(startIndex, Integer.parseInt(value));
			}
		}catch(NumberFormatException e) {
			st.setInt(startIndex, 0);
		}
	}

}
