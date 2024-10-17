package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.Calendar;

import io.r2dbc.spi.Statement;

/**
 * 使用这个适配器使得 r2dbc的Statement可以使用querydsl的 Type映射。 
 * @author jiyi
 *
 */
public class PreparedStatementWrapper implements PreparedStatement {

	private boolean closed;
	private int maxRows;
	private int fetchSize;
	private final Statement stmt;

	public PreparedStatementWrapper(Statement s) {
		this.stmt = s;
	}

	public ResultSet executeQuery() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public int executeUpdate() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		stmt.bindNull(parameterIndex, toClass(sqlType));
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setTime(int parameterIndex, java.sql.Time x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		stmt.bind(parameterIndex, x);
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

	public void setRef(int i, Ref x) throws SQLException {
		stmt.bind(i, x);
	}

	public void setBlob(int i, Blob x) throws SQLException {
		stmt.bind(i, x);
	}

	public void setClob(int i, Clob x) throws SQLException {
		stmt.bind(i, x);
	}

	public void setArray(int i, Array x) throws SQLException {
		stmt.bind(i, x);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		throw  new UnsupportedOperationException();
	}

	public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
		stmt.bindNull(paramIndex, Object.class);
	}

	public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
		stmt.bind(parameterIndex, x);
	}

	public java.sql.ParameterMetaData getParameterMetaData() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/* JDBC_4_ANT_KEY_BEGIN */
	public void setRowId(int parameterIndex, RowId value) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void setNString(int parameterIndex, String value) throws SQLException {
		stmt.bind(parameterIndex, value);
	}

	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		stmt.bind(parameterIndex, value);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		stmt.bind(parameterIndex, value);
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

	public void setSQLXML(int parameterIndex, SQLXML value) throws SQLException {
		stmt.bind(parameterIndex, value);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		stmt.bind(parameterIndex, inputStream);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		stmt.bind(parameterIndex, inputStream);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		stmt.bind(parameterIndex, reader);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream) throws SQLException {
		stmt.bind(parameterIndex, inputStream);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
		stmt.bind(parameterIndex, inputStream);
	}

	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex, reader);
	}

	public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex, reader);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex, reader);
	}

	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		stmt.bind(parameterIndex, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		stmt.bind(parameterIndex, reader);
	}
	/* JDBC_4_ANT_KEY_END */
	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws SQLException {
		this.closed=true;
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		throw new UnsupportedOperationException();
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
	public void cancel() throws SQLException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getUpdateCount() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException();
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
	public void addBatch(String sql) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearBatch() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int[] executeBatch() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Connection getConnection() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
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