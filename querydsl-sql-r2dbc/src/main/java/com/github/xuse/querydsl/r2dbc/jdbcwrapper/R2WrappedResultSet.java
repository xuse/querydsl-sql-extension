package com.github.xuse.querydsl.r2dbc.jdbcwrapper;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class R2WrappedResultSet extends AbstractResultSet {
	@Override
	public boolean next() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public void close() throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isBeforeFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isAfterLast() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isLast() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public void beforeFirst() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public void afterLast() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean first() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean last() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean previous() throws SQLException {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}
}
