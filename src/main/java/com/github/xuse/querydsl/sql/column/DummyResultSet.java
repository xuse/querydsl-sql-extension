package com.github.xuse.querydsl.sql.column;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.querydsl.core.util.ResultSetAdapter;

final class DummyResultSet extends ResultSetAdapter {
	private final Object source;

	public DummyResultSet(Object source) {
		super(null);
		this.source = source;
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		if (source == null) {
			return null;
		}
		if (source instanceof BigDecimal) {
			return (BigDecimal) source;
		}
		if (source instanceof Number) {
			return new BigDecimal(source.toString());
		}
		// 此处无法强转，使其报错，让用户注意该问题。
		return (BigDecimal) source;
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return getBigDecimal(1);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return getBigDecimal(1);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		if (source instanceof Boolean) {
			return (Boolean) source;
		}
		return "1".equals(String.valueOf(source));
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return getBoolean(1);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		if (source == null) {
			return 0;
		}
		// 如果无效使其报错。
		return (Byte) source;
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return getByte(1);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return (byte[]) source;
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return getBytes(1);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return getDate(1);
	}

	@Override
	public java.sql.Date getDate(int columnIndex) throws SQLException {
		if (source instanceof java.sql.Date) {
			return (java.sql.Date) source;
		}
		return new java.sql.Date(((Date) source).getTime());

	}

	@Override
	public java.sql.Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return getDate(1);
	}

	@Override
	public java.sql.Date getDate(String columnLabel) throws SQLException {
		return getDate(1);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		if (source instanceof Number) {
			return ((Number) source).doubleValue();
		}
		return 0D;
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return getDouble(1);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		if (source instanceof Number) {
			return ((Number) source).floatValue();
		}
		return 0F;
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return getFloat(1);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		if (source instanceof Number) {
			return ((Number) source).intValue();
		}
		return 0;
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return getInt(1);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		if (source instanceof Number) {
			return ((Number) source).longValue();
		}
		return 0L;
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return getLong(1);
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		return source;
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		return source;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return (T) source;
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getObject(1);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return getObject(1, type);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		if (source instanceof Number) {
			return ((Number) source).shortValue();
		}
		return 0;
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return getShort(1);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		if (source == null) {
			return null;
		}
		return String.valueOf(source);
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return getString(1);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return (Time) source;
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return (Time) source;
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return getTime(1);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return getTime(1);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return getTimestamp(1);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		if (source instanceof Timestamp) {
			return (Timestamp) source;
		}
		// 如果不是Date的子类，会报错。预期如此。
		return new java.sql.Timestamp(((Date) source).getTime());
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return getTimestamp(1);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(1);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}
}
