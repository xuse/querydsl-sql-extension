package com.github.xuse.querydsl.sql.spring;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.sql.ConnectionAdapter;

public class UnmanagedConnection extends ConnectionAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(UnmanagedConnection.class);

	protected UnmanagedConnection(Connection conn) {
		super(conn);
		LOG.debug("UnmanagedConnection created, {}", conn);
	}

	@Override
	public void close() throws SQLException {
		LOG.debug("UnmanagedConnection closing, {}", conn);
		conn.close();
	}
}
