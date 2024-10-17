package com.github.xuse.querydsl.r2dbc.listener;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContext;

public class R2ListenerContextImpl implements SQLListenerContext, R2ListenerContext {
	private final Map<String, Object> contextMap;

	private final QueryMetadata md;

	private final List<SQLBindings> sqlStatements;

	private RelationalPath<?> entity;

	private Exception exception;

	public R2ListenerContextImpl(QueryMetadata metadata, RelationalPath<?> entity) {
		this.contextMap = new HashMap<>();
		this.sqlStatements = new ArrayList<>();
		this.md = metadata;
		this.entity = entity;
	}

	@Override
	public Object getData(final String dataKey) {
		return contextMap.get(dataKey);
	}

	@Override
	public void setData(final String dataKey, final Object value) {
		contextMap.put(dataKey, value);
	}

	@Override
	public QueryMetadata getMetadata() {
		return md;
	}

	@Override
	public String getSQL() {
		return sqlStatements.isEmpty() ? null : sqlStatements.get(0).getSQL();
	}

	@Override
	public SQLBindings getSQLBindings() {
		  return sqlStatements.isEmpty() ? null : sqlStatements.get(0);
	}

	@Override
	public Collection<String> getSQLStatements() {
		 return sqlStatements.stream().map(SQLBindings::getSQL).collect(Collectors.toList());
	}

	@Override
	public Collection<SQLBindings> getAllSQLBindings() {
		 return sqlStatements;
	}

	@Override
	public RelationalPath<?> getEntity() {
		return entity;
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public Collection<PreparedStatement> getPreparedStatements() {
		return Collections.emptyList();
	}

	@Override
	public java.sql.Connection getConnection() {
		return null;
	}

	@Override
	public PreparedStatement getPreparedStatement() {
		return null;
	}
}
