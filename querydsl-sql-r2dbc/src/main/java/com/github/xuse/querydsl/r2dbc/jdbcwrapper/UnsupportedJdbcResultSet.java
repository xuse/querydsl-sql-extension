package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

public abstract class UnsupportedJdbcResultSet implements ResultSet {
	public void insertRow() {
		// 不支持结果集插入数据
		throw new UnsupportedOperationException();
	}

	public void moveToInsertRow() {
		// 不支持结果集插入数据
		throw new UnsupportedOperationException();
	}

	public void deleteRow() {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateRow() {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateNull(String columnName) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateObject(String columnName, Object value) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateClob(int columnIndex, Reader reader) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateBlob(int columnIndex, InputStream inputStream) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateNull(int columnIndex) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateClob(String columnLabel, Reader reader, long length) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateClob(int columnIndex, Reader reader, long length) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateBlob(String columnLabel, InputStream inputStream, long length) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	public void updateObject(int columnIndex, Object x) {
		// 不支持结果集写数据
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateByte(int columnIndex, byte x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateShort(int columnIndex, short x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateInt(int columnIndex, int x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateLong(int columnIndex, long x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateFloat(int columnIndex, float x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDouble(int columnIndex, double x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateString(int columnIndex, String x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDate(int columnIndex, Date x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTime(int columnIndex, Time x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateByte(String columnLabel, byte x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateShort(String columnLabel, short x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateInt(String columnLabel, int x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateLong(String columnLabel, long x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateFloat(String columnLabel, float x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateString(String columnLabel, String x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDate(String columnLabel, Date x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTime(String columnLabel, Time x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRef(int columnIndex, Ref x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRef(String columnLabel, Ref x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(int columnIndex, Clob x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(String columnLabel, Clob x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateArray(int columnIndex, Array x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateArray(String columnLabel, Array x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void updateRowId(String columnLabel, RowId x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNString(int columnIndex, String nString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNString(String columnLabel, String nString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean rowUpdated() {
		throw new UnsupportedOperationException("rowUpdated");
	}

	@Override
	public boolean rowInserted() {
		throw new UnsupportedOperationException("rowInserted");
	}

	@Override
	public boolean rowDeleted() {
		throw new UnsupportedOperationException("rowDeleted");
	}

	@Override
	public void updateDouble(String columnLabel, double x) {
		throw new UnsupportedOperationException("updateDouble");
	}

	@Override
	public void refreshRow() {
		throw new UnsupportedOperationException("refreshRow");
	}

	@Override
	public void cancelRowUpdates() {
		throw new UnsupportedOperationException("cancelRowUpdates");
	}

}
