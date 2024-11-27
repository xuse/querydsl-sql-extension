package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.sql.ResultSetMetaData;

public class R2ResultWrapper extends AbstractR2ResultSet {
	@Override
	public boolean next() {
		throw new UnsupportedOperationException();
	}
	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ResultSetMetaData getMetaData() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isBeforeFirst() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isAfterLast() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isFirst() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isLast() {
		throw new UnsupportedOperationException();
	}
	@Override
	public void beforeFirst() {
		throw new UnsupportedOperationException();
	}
	@Override
	public void afterLast() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean first() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean last() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean previous() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isClosed() {
		return false;
	}
}
