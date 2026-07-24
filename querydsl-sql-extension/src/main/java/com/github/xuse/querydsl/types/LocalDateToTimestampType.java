package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;

import org.jetbrains.annotations.Nullable;

import com.querydsl.sql.types.AbstractType;

/**
 * Maps Java {@link LocalDate} to database TIMESTAMP/DATETIME column.
 * <p>
 * Use this type when the database column is DATETIME/TIMESTAMP but business
 * only cares about the date portion. Reading truncates time to date;
 * writing sets time to 00:00:00 ensuring date-level equality comparison works.
 * <p>
 * 将Java的LocalDate映射到数据库的TIMESTAMP/DATETIME列。
 * 适用于数据库列为高精度时间类型，但业务上只关心日期的场景。
 * 读取时截断时分秒，写入时以Timestamp(00:00:00)传参，
 * 确保参数类型与列类型一致，避免隐式类型转换导致比较不正确。
 */
public class LocalDateToTimestampType extends AbstractType<LocalDate> {

	public static final LocalDateToTimestampType INSTANCE = new LocalDateToTimestampType();

	public LocalDateToTimestampType() {
		super(Types.TIMESTAMP);
	}

	@Override
	public Class<LocalDate> getReturnedClass() {
		return LocalDate.class;
	}

	@Override
	public @Nullable LocalDate getValue(ResultSet rs, int startIndex) throws SQLException {
		Timestamp ts = rs.getTimestamp(startIndex);
		return ts != null ? ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, LocalDate value) throws SQLException {
		if (value == null) {
			st.setNull(startIndex, Types.TIMESTAMP);
		} else {
			st.setTimestamp(startIndex, Timestamp.valueOf(value.atStartOfDay()));
		}
	}
}
