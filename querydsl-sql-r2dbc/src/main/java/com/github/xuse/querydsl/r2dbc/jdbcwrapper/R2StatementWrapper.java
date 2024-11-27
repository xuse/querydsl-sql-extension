package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import com.github.xuse.querydsl.util.DateUtils;
import io.r2dbc.spi.Statement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

/**
 * 使用这个适配器使得 r2dbc的Statement可以使用querydsl的 Type映射。 
 * @author jiyi
 *
 */
public class R2StatementWrapper extends UnsupportedJdbcStatement{
	private boolean closed;
	private int maxRows;
	private int fetchSize;
	private final Statement stmt;

	public R2StatementWrapper(Statement s) {
		this.stmt = s;
	}

	public void setNull(int parameterIndex, int sqlType) {
		stmt.bindNull(parameterIndex - 1, toClass(sqlType));
	}

	public void setBoolean(int parameterIndex, boolean x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setByte(int parameterIndex, byte x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setShort(int parameterIndex, short x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setInt(int parameterIndex, int x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setLong(int parameterIndex, long x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setFloat(int parameterIndex, float x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setDouble(int parameterIndex, double x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setString(int parameterIndex, String x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setBytes(int parameterIndex, byte[] x) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setDate(int parameterIndex, java.sql.Date x) {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDate(x));
	}

	public void setTime(int parameterIndex, java.sql.Time x) {
		stmt.bind(parameterIndex-1, DateUtils.toLocalTime(x));
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x) {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDateTime(x));
	}

	public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) {
		stmt.bind(parameterIndex-1, x);
	}

	public void setObject(int parameterIndex, Object x) {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setRef(int parameterIndex, Ref x) {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setBlob(int parameterIndex, Blob x) {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setClob(int parameterIndex, Clob x) {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setArray(int parameterIndex, Array x) {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDate(x));
	}

	public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) {
		stmt.bind(parameterIndex-1, DateUtils.toLocalTime(x));
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDateTime(x));
	}

	public void setNull(int paramIndex, int sqlType, String typeName) {
		stmt.bindNull(paramIndex, Object.class);
	}

	public void setURL(int parameterIndex, java.net.URL x) {
		stmt.bind(parameterIndex-1, x);
	}


	public void setNString(int parameterIndex, String value) {
		stmt.bind(parameterIndex-1, value);
	}

	public void setNCharacterStream(int parameterIndex, Reader value, long length) {
		stmt.bind(parameterIndex-1, value);
	}

	public void setNClob(int parameterIndex, NClob value) {
		stmt.bind(parameterIndex-1, value);
	}

	public void setSQLXML(int parameterIndex, SQLXML value) {
		stmt.bind(parameterIndex-1, value);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream, long length) {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, long length) {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream) {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream) {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setCharacterStream(int parameterIndex, Reader reader) {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setNCharacterStream(int parameterIndex, Reader reader) {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setClob(int parameterIndex, Reader reader) {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setBlob(int parameterIndex, InputStream inputStream) {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) {
		stmt.bind(parameterIndex-1, reader);
	}

	@Override
	public void close() {
		this.closed=true;
	}

	@Override
	public int getMaxRows() {
		return maxRows;
	}

	@Override
	public void setMaxRows(int max) {
		this.maxRows=max;
	}

	@Override
	public void setEscapeProcessing(boolean enable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getQueryTimeout() {
		return 0;
	}

	@Override
	public void setQueryTimeout(int seconds) {
	}

	@Override
	public void setFetchSize(int rows) {
		stmt.fetchSize(rows);
		this.fetchSize=rows;
	}

	@Override
	public int getFetchSize() {
		return fetchSize;
	}

	@Override
	public int getResultSetConcurrency() {
		return ResultSet.CONCUR_READ_ONLY ;
	}

	@Override
	public int getResultSetType() {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	@Override
	public int getResultSetHoldability() {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	public void setRowId(int parameterIndex, RowId value) {
		throw new UnsupportedOperationException();
	}
	
	/** @deprecated */
	public void setUnicodeStream(int parameterIndex, java.io.InputStream x, int length) {
		throw new UnsupportedOperationException();
	}

	public void setBinaryStream(int parameterIndex, java.io.InputStream x, int length) {
		throw new UnsupportedOperationException();
	}

	public void clearParameters() {
		throw new UnsupportedOperationException();
	}

	public boolean execute() {
		throw new UnsupportedOperationException();
	}

	public void addBatch() {
		throw new UnsupportedOperationException();
	}

	public void setCharacterStream(int parameterIndex, java.io.Reader reader, int length) {
		throw new UnsupportedOperationException();
	}


	
	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void setPoolable(boolean poolAble) {
	}

	@Override
	public boolean isPoolable() {
		return false;
	}

	@Override
	public void closeOnCompletion() {
	}

	@Override
	public boolean isCloseOnCompletion() {
		return false;
	}

	public void setClob(int parameterIndex, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length) {
		throw new UnsupportedOperationException();
	}

	public void setNClob(int parameterIndex, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) {
		if(iface == Statement.class) {
			return (T) stmt;
		}
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) {
		return iface == Statement.class;
	}
	
	private Class<?> toClass(int sqlType) {
		switch(sqlType) {
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BLOB:
			return byte[].class;
		case Types.CHAR:
		case Types.NCHAR:
		case Types.VARCHAR:
		case Types.NVARCHAR:
		case Types.CLOB:
		case Types.NCLOB:
		case Types.LONGVARCHAR:	
		case Types.LONGNVARCHAR:
		case Types.SQLXML:
		case Types.ROWID:
			return String.class;
		case Types.BIT:
		case Types.BOOLEAN:
			return Boolean.class;
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
			return Integer.class; 
		case Types.FLOAT:
			return Float.class;
		case Types.BIGINT:
			return Long.class;
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.REAL:
			return Double.class; 
		case Types.DATE:
			return java.sql.Date.class;
		case Types.TIME:
		case Types.TIME_WITH_TIMEZONE:
			return java.sql.Time.class;
		case Types.TIMESTAMP:
		case Types.TIMESTAMP_WITH_TIMEZONE:
			return java.sql.Timestamp.class; 
		
		case Types.DATALINK:
		case Types.REF:
		case Types.REF_CURSOR:
		case Types.JAVA_OBJECT:	
		case Types.DISTINCT:
		case Types.OTHER:
		case Types.ARRAY:
		case Types.STRUCT:
		case Types.NULL:
		}
		return Object.class;
	}
}