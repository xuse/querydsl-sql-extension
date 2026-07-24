package com.github.xuse.querydsl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;

import org.jetbrains.annotations.Nullable;

import com.querydsl.sql.types.AbstractType;

/**
 * Maps Java {@link java.sql.Date} to database TIMESTAMP/DATETIME column.
 * <p>
 * Use this type when the database column is DATETIME/TIMESTAMP but business
 * only cares about the date portion. Writing always truncates to 00:00:00.000,
 * ensuring the parameter type matches the column type and date-level equality
 * comparison works correctly (avoids MySQL implicit type promotion between DATE and DATETIME).
 * <p>
 * Reading behavior is controlled by {@code truncateOnGet}:
 * <ul>
 *   <li>{@code false} (default) — faithfully returns the raw value from database,
 *       suitable when data is always written through this Type (already 00:00:00).</li>
 *   <li>{@code true} — truncates time component on read, useful when the column
 *       may contain non-zero time written by other systems.</li>
 * </ul>
 * <p>
 * 将Java的java.sql.Date映射到数据库的TIMESTAMP/DATETIME列。
 * 适用于数据库列为高精度时间类型，但业务上只关心日期的场景。
 * 写入时始终截断为00:00:00.000，避免java.sql.Date内部残留的时分秒导致eq比较失败。
 * 读取时根据{@code truncateOnGet}参数决定是否截断：默认不截断（false），
 * 如需防御外部系统写入的非零时间数据可传入true。
 */
public class SqlDateToTimestampType extends AbstractType<java.sql.Date> {

	public static final SqlDateToTimestampType INSTANCE = new SqlDateToTimestampType(false);

	private final boolean truncateOnGet;

	/**
	 * @param truncateOnGet if true, truncate time component when reading from database;
	 *                      if false, return the raw value faithfully.
	 */
	public SqlDateToTimestampType(boolean truncateOnGet) {
		super(Types.TIMESTAMP);
		this.truncateOnGet = truncateOnGet;
	}

	@Override
	public Class<java.sql.Date> getReturnedClass() {
		return java.sql.Date.class;
	}

	@Override
	public @Nullable java.sql.Date getValue(ResultSet rs, int startIndex) throws SQLException {
		Timestamp ts = rs.getTimestamp(startIndex);
		if (ts == null) {
			return null;
		}
		if (truncateOnGet) {
			return java.sql.Date.valueOf(ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
		return new java.sql.Date(ts.getTime());
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, java.sql.Date value) throws SQLException {
		if (value == null) {
			st.setNull(startIndex, Types.TIMESTAMP);
		} else {
			// Truncate to day boundary to ensure no residual time component
			st.setTimestamp(startIndex, Timestamp.valueOf(value.toLocalDate().atStartOfDay()));
		}
	}
}
