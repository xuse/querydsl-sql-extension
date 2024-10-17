package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import reactor.core.publisher.Flux;

public class JdbcClobImpl implements java.sql.Clob {
	private final io.r2dbc.spi.Clob clob;
	
	public JdbcClobImpl(io.r2dbc.spi.Clob clob) {
		this.clob=clob;
	}

	@Override
	public long length() throws SQLException {
		return Flux.from(clob.stream()).map(e -> (long) e.length()).reduce((a, b) -> a + b).block();
	}

	@Override
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		return new CharSequenceFluxReader(Flux.from(clob.stream()),pos,length);
	}
	@Override
	public InputStream getAsciiStream() throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Reader getCharacterStream() throws SQLException {
		return new CharSequenceFluxReader(Flux.from(clob.stream()), 0, Long.MAX_VALUE);
	}
	
	@Override
	public String getSubString(long pos, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long position(String searchstr, long start) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long position(Clob searchstr, long start) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int setString(long pos, String str) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int setString(long pos, String str, int offset, int len) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream setAsciiStream(long pos) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer setCharacterStream(long pos) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void truncate(long len) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void free() throws SQLException {
		throw new UnsupportedOperationException();
	}

}
