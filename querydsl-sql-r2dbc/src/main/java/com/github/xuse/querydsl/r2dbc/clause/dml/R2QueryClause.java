package com.github.xuse.querydsl.r2dbc.clause.dml;

import java.sql.Connection;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.AbstractR2QueryClause;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.support.QueryMixin;

import io.r2dbc.spi.ConnectionFactory;

public class R2QueryClause<T> extends AbstractR2QueryClause<T,R2QueryClause<T>>{

	private Connection conn;

	// /////////// 覆盖检查字段结束/////////////
	private boolean exceedSizeLog;

	private RoutingStrategy routing;
	
	public R2QueryClause(ConnectionFactory connection, QueryMixin<R2QueryClause<T>> queryMixin, ConfigurationEx configuration) {
		super(connection, queryMixin, configuration);
	}
	
	public R2QueryClause(ConnectionFactory connection, ConfigurationEx configuration) {
		this(connection, new DefaultQueryMetadata(), configuration);
	}

	protected R2QueryClause(ConnectionFactory connection, DefaultQueryMetadata metadata,
			ConfigurationEx configuration) {
		super(connection, new QueryMixin<R2QueryClause<T>>(metadata, false), configuration);
	}

}
