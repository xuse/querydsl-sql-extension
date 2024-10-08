package com.github.xuse.querydsl.datatype;

import com.querydsl.sql.types.AbstractType;

import java.sql.*;

/**
 * 扩展类型：Java类中Long(毫秒时间戳)映射数据库DateTime
 * 
 * @author Administrator
 *
 */
public class LongASDateTimeType extends AbstractType<Long> {
	private final boolean primitive;

	public LongASDateTimeType() {
		this(false);
	}
	
	public LongASDateTimeType(boolean isPrimitive) {
		super(Types.TIMESTAMP);
		this.primitive =isPrimitive;
	}

	@Override
	public Class<Long> getReturnedClass() {
		return Long.class;
	}

	@Override
	public Long getValue(ResultSet rs, int startIndex) throws SQLException {
		Timestamp ts = rs.getTimestamp(startIndex);
		return ts == null ? (primitive ? 0L : null): ts.getTime();
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Long value) throws SQLException {
		if (value == null || (primitive && value==0L)) {
			st.setNull(startIndex, Types.TIMESTAMP);
		} else {
			st.setTimestamp(startIndex, new java.sql.Timestamp(value.longValue()));
		}

	}

}
