package com.github.xuse.querydsl.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.util.ArrayUtils;

public class SpecialMockPreparedStatement extends MockPreparedStatement {

	private Object[] params;

	private String key;

	private List<String> paramNames;

	private final ResultCallback callback;

	public SpecialMockPreparedStatement(String[] ss, ResultCallback callback) {
		super(null, 0);
		this.key = ss[0];
		this.callback = callback;
		paramNames = Arrays.asList(ArrayUtils.subArray(ss, 1, ss.length));
		params = new Object[paramNames.size()];
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		params[parameterIndex - 1] = Integer.valueOf(x);
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		params[parameterIndex - 1] = x;
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		params[parameterIndex - 1] = x;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < paramNames.size(); i++) {
			map.put(paramNames.get(i), params[i]);
		}
		return callback.get(key, map);
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		return getResultSet();
	}
}
