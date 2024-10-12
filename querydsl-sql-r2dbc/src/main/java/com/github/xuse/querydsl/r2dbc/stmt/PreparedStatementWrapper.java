package com.github.xuse.querydsl.r2dbc.stmt;

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
import java.util.Calendar;

import io.r2dbc.spi.Statement;

public class PreparedStatementWrapper implements PreparedStatement {
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
		//stmt.bindNull(parameterIndex, sqlType);
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		//stmt.setBoolean(parameterIndex, x);
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

	/** @deprecated */
	public void setUnicodeStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		
	}

	public void setBinaryStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {

	}

	public void clearParameters() throws SQLException {
		throw new UnsupportedOperationException();
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
		return null;
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		return 0;
	}

	@Override
	public void close() throws SQLException {
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return 0;
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		

	}

	@Override
	public int getMaxRows() throws SQLException {
		
		return 0;
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		

	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		

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
		

	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		

	}

	@Override
	public void setCursorName(String name) throws SQLException {
		

	}

	@Override
	public boolean execute(String sql) throws SQLException {
		
		return false;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		
		return null;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		
		return false;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		

	}

	@Override
	public int getFetchDirection() throws SQLException {
		
		return 0;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		

	}

	@Override
	public int getFetchSize() throws SQLException {
		
		return 0;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		
		return 0;
	}

	@Override
	public int getResultSetType() throws SQLException {
		
		return 0;
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		

	}

	@Override
	public void clearBatch() throws SQLException {
		

	}

	@Override
	public int[] executeBatch() throws SQLException {
		
		return null;
	}

	@Override
	public Connection getConnection() throws SQLException {
		
		return null;
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		
		return false;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		
		return null;
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		
		return 0;
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		
		return 0;
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		
		return 0;
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		
		return false;
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		
		return false;
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		
		return false;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		
		return 0;
	}

	@Override
	public boolean isClosed() throws SQLException {
		
		return false;
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
		
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		
		return false;
	}
}