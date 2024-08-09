package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：Java类中String映射数据库BigInt
 * @author Administrator
 *
 */
public class StringAsBigIntType extends AbstractType<String>{

	public StringAsBigIntType() {
		  super(Types.BIGINT);
	}

	@Override
	public Class<String> getReturnedClass() {
		return String.class;
	}

	@Override
	public String getValue(ResultSet rs, int startIndex) throws SQLException {
		long code = rs.getLong(startIndex);
        return rs.wasNull() ? null : String.valueOf(code);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, String value) throws SQLException {
		try {
			if(StringUtils.isEmpty(value)) {
				st.setNull(startIndex, Types.BIGINT);
			}else {
				st.setLong(startIndex, Long.parseLong(value));
			}
		}catch(NumberFormatException e) {
			st.setInt(startIndex, 0);
		}
	}

}
