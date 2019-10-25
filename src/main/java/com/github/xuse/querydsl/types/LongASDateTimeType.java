package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：Java类中Long(毫秒时间戳)映射数据库DateTime
 * 
 * @author Administrator
 *
 */
public class LongASDateTimeType extends AbstractType<Long> {
	private final boolean primtive;

	public LongASDateTimeType() {
		this(false);
	}
	
	public LongASDateTimeType(boolean isPrimtive) {
		super(Types.TIMESTAMP);
		this.primtive=isPrimtive;
	}

	@Override
	public Class<Long> getReturnedClass() {
		return Long.class;
	}

	@Override
	public Long getValue(ResultSet rs, int startIndex) throws SQLException {
		Timestamp ts = rs.getTimestamp(startIndex);
		return ts == null ? primtive ? 0L : null : ts.getTime();
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Long value) throws SQLException {
		if (value == null || (primtive && value==0L)) {
			st.setNull(startIndex, Types.TIMESTAMP);
		} else {
			st.setTimestamp(startIndex, new java.sql.Timestamp(value.longValue()));
		}

	}

}
