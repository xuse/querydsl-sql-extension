package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.Calendar;

import com.github.xuse.querydsl.util.DateUtils;

import io.r2dbc.spi.Statement;

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

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		stmt.bindNull(parameterIndex - 1, toClass(sqlType));
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDate(x));
	}

	public void setTime(int parameterIndex, java.sql.Time x) throws SQLException {
		stmt.bind(parameterIndex-1, DateUtils.toLocalTime(x));
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws SQLException {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDateTime(x));
	}

	public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		stmt.bind(parameterIndex - 1, x);
	}

	public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDate(x));
	}

	public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws SQLException {
		stmt.bind(parameterIndex-1, DateUtils.toLocalTime(x));
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws SQLException {
		stmt.bind(parameterIndex-1, DateUtils.toLocalDateTime(x));
	}

	public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
		stmt.bindNull(paramIndex, Object.class);
	}

	public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
		stmt.bind(parameterIndex-1, x);
	}


	public void setNString(int parameterIndex, String value) throws SQLException {
		stmt.bind(parameterIndex-1, value);
	}

	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		stmt.bind(parameterIndex-1, value);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		stmt.bind(parameterIndex-1, value);
	}

	public void setSQLXML(int parameterIndex, SQLXML value) throws SQLException {
		stmt.bind(parameterIndex-1, value);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream) throws SQLException {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex-1, reader);
	}

	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		stmt.bind(parameterIndex-1, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex-1, reader);
	}

	@Override
	public void close() throws SQLException {
		this.closed=true;
	}

	@Override
	public int getMaxRows() throws SQLException {
		return maxRows;
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		this.maxRows=max;
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return 0;
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		stmt.fetchSize(rows);
		this.fetchSize=rows;
	}

	@Override
	public int getFetchSize() throws SQLException {
		return fetchSize;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return ResultSet.CONCUR_READ_ONLY ;
	}

	@Override
	public int getResultSetType() throws SQLException {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	public void setRowId(int parameterIndex, RowId value) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	/** @deprecated */
	public void setUnicodeStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void setBinaryStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void clearParameters() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public boolean execute() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void addBatch() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void setCharacterStream(int parameterIndex, java.io.Reader reader, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}


	
	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
	}

	@Override
	public boolean isPoolable() throws SQLException {
		return false;
	}

	@Override
	public void closeOnCompletion() throws SQLException {
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return false;
	}

	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if(iface == Statement.class) {
			return (T) stmt;
		}
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
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
			return String.class;
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