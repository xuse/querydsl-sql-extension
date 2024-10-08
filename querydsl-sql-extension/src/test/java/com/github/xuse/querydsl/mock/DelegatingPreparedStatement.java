package com.github.xuse.querydsl.mock;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Calendar;

public class DelegatingPreparedStatement extends DelegatingStatement implements PreparedStatement {
	public DelegatingPreparedStatement(PreparedStatement s) {
		super(s);
	}

	public ResultSet executeQuery() throws SQLException {
		return ((PreparedStatement) stmt).executeQuery();
	}

	public int executeUpdate() throws SQLException {
		return ((PreparedStatement) stmt).executeUpdate();
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		((PreparedStatement) stmt).setNull(parameterIndex, sqlType);
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		((PreparedStatement) stmt).setBoolean(parameterIndex, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		((PreparedStatement) stmt).setByte(parameterIndex, x);
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		((PreparedStatement) stmt).setShort(parameterIndex, x);
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		((PreparedStatement) stmt).setInt(parameterIndex, x);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		((PreparedStatement) stmt).setLong(parameterIndex, x);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		((PreparedStatement) stmt).setFloat(parameterIndex, x);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		((PreparedStatement) stmt).setDouble(parameterIndex, x);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		((PreparedStatement) stmt).setBigDecimal(parameterIndex, x);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		((PreparedStatement) stmt).setString(parameterIndex, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		((PreparedStatement) stmt).setBytes(parameterIndex, x);
	}

	public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
		((PreparedStatement) stmt).setDate(parameterIndex, x);
	}

	public void setTime(int parameterIndex, java.sql.Time x) throws SQLException {
		((PreparedStatement) stmt).setTime(parameterIndex, x);
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws SQLException {
		((PreparedStatement) stmt).setTimestamp(parameterIndex, x);
	}

	public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		((PreparedStatement) stmt).setAsciiStream(parameterIndex, x, length);
	}

	/** @deprecated */
	public void setUnicodeStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		((PreparedStatement) stmt).setUnicodeStream(parameterIndex, x, length);
	}

	public void setBinaryStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
		((PreparedStatement) stmt).setBinaryStream(parameterIndex, x, length);
	}

	public void clearParameters() throws SQLException {
		((PreparedStatement) stmt).clearParameters();
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
		((PreparedStatement) stmt).setObject(parameterIndex, x, targetSqlType, scale);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		((PreparedStatement) stmt).setObject(parameterIndex, x, targetSqlType);
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		((PreparedStatement) stmt).setObject(parameterIndex, x);
	}

	public boolean execute() throws SQLException {
		return ((PreparedStatement) stmt).execute();
	}

	public void addBatch() throws SQLException {
		((PreparedStatement) stmt).addBatch();
	}

	public void setCharacterStream(int parameterIndex, java.io.Reader reader, int length) throws SQLException {
		((PreparedStatement) stmt).setCharacterStream(parameterIndex, reader, length);
	}

	public void setRef(int i, Ref x) throws SQLException {
		((PreparedStatement) stmt).setRef(i, x);
	}

	public void setBlob(int i, Blob x) throws SQLException {
		((PreparedStatement) stmt).setBlob(i, x);
	}

	public void setClob(int i, Clob x) throws SQLException {
		((PreparedStatement) stmt).setClob(i, x);
	}

	public void setArray(int i, Array x) throws SQLException {
		((PreparedStatement) stmt).setArray(i, x);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return ((PreparedStatement) stmt).getMetaData();
	}

	public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
		((PreparedStatement) stmt).setDate(parameterIndex, x, cal);
	}

	public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws SQLException {
		((PreparedStatement) stmt).setTime(parameterIndex, x, cal);
	}

	public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws SQLException {
		((PreparedStatement) stmt).setTimestamp(parameterIndex, x, cal);
	}

	public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
		((PreparedStatement) stmt).setNull(paramIndex, sqlType, typeName);
	}

	public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
		((PreparedStatement) stmt).setURL(parameterIndex, x);
	}

	public java.sql.ParameterMetaData getParameterMetaData() throws SQLException {
		return ((PreparedStatement) stmt).getParameterMetaData();
	}

	/* JDBC_4_ANT_KEY_BEGIN */

	public void setRowId(int parameterIndex, RowId value) throws SQLException {
		((PreparedStatement) stmt).setRowId(parameterIndex, value);
	}

	public void setNString(int parameterIndex, String value) throws SQLException {
		((PreparedStatement) stmt).setNString(parameterIndex, value);
	}

	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		((PreparedStatement) stmt).setNCharacterStream(parameterIndex, value, length);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		((PreparedStatement) stmt).setNClob(parameterIndex, value);
	}

	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		((PreparedStatement) stmt).setClob(parameterIndex, reader, length);
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		((PreparedStatement) stmt).setBlob(parameterIndex, inputStream, length);
	}

	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		((PreparedStatement) stmt).setNClob(parameterIndex, reader, length);
	}

	public void setSQLXML(int parameterIndex, SQLXML value) throws SQLException {
		((PreparedStatement) stmt).setSQLXML(parameterIndex, value);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		((PreparedStatement) stmt).setAsciiStream(parameterIndex, inputStream, length);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		((PreparedStatement) stmt).setBinaryStream(parameterIndex, inputStream, length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		((PreparedStatement) stmt).setCharacterStream(parameterIndex, reader, length);
	}

	public void setAsciiStream(int parameterIndex, InputStream inputStream) throws SQLException {
		((PreparedStatement) stmt).setAsciiStream(parameterIndex, inputStream);
	}

	public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
		((PreparedStatement) stmt).setBinaryStream(parameterIndex, inputStream);
	}

	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) stmt).setCharacterStream(parameterIndex, reader);
	}

	public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) stmt).setNCharacterStream(parameterIndex, reader);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) stmt).setClob(parameterIndex, reader);
	}

	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		((PreparedStatement) stmt).setBlob(parameterIndex, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) stmt).setNClob(parameterIndex, reader);
	}
	/* JDBC_4_ANT_KEY_END */
}