package com.github.xuse.querydsl.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.github.xuse.querydsl.sql.ConnectionAdapter;
import com.github.xuse.querydsl.util.StringUtils;

public class MockedConnection extends ConnectionAdapter {
	

	private final MockMySQLDriver parent;

	public MockedConnection(Connection connection, MockMySQLDriver parent) {
		super(connection);
		this.parent = parent;
	}

	@Override
	public void close() throws SQLException {
		conn.close();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return super.prepareStatement(process(sql));
	}

	private String process(String sql) {
		if(parent.sqlRewriter!=null) {
			sql= parent.sqlRewriter.apply(sql);
			if(StringUtils.isEmpty(sql)) {
				//execute a dummy sql.
				sql="delete from querydsl_auto_init_data_log where false";
			}
		}
		return sql;
	}
	
	private PreparedStatement specialStatement(String keyword) {
		String[] ss = StringUtils.split(keyword.substring(1), "#");
		return new SpecialMockPreparedStatement(ss,parent);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		sql=process(sql);
		return super.prepareStatement(sql, columnNames);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		sql=process(sql);
		if(sql.startsWith("#")) {
			return specialStatement(sql);
		}
		return super.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int i) throws SQLException {
		sql=process(sql);
		if(sql.startsWith("#")) {
			return specialStatement(sql);
		}
		return super.prepareStatement(sql, i);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		sql=process(sql);
		if(sql.startsWith("#")) {
			return specialStatement(sql);
		}
		return super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		sql=process(sql);
		if(sql.startsWith("#")) {
			return specialStatement(sql);
		}
		return super.prepareStatement(sql, columnIndexes);
	}

}
