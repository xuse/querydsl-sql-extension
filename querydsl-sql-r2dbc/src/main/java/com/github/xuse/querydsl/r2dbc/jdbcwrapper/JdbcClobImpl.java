package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;

import org.jetbrains.annotations.NotNull;

import reactor.core.publisher.Flux;

public class JdbcClobImpl implements java.sql.Clob {
	private final io.r2dbc.spi.Clob clob;
	
	public JdbcClobImpl(@NotNull io.r2dbc.spi.Clob clob) {
		this.clob=clob;
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	public long length() {
		return Flux.from(clob.stream()).map(CharSequence::length).reduce(Integer::sum).block();
	}

	@Override
	public Reader getCharacterStream(long pos, long length) {
		return new CharSequenceFluxReader(Flux.from(clob.stream()),pos,length);
	}
	@Override
	public InputStream getAsciiStream() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Reader getCharacterStream() {
		return new CharSequenceFluxReader(Flux.from(clob.stream()), 0, Long.MAX_VALUE);
	}
	
	@Override
	public String getSubString(long pos, int length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long position(String searchStr, long start) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long position(Clob searchStr, long start) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int setString(long pos, String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int setString(long pos, String str, int offset, int len) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream setAsciiStream(long pos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer setCharacterStream(long pos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void truncate(long len) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void free() {
		throw new UnsupportedOperationException();
	}

}
