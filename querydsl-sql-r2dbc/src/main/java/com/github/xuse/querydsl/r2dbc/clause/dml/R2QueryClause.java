package com.github.xuse.querydsl.r2dbc.clause.dml;

import java.sql.Connection;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.AbstractR2QueryClause;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.querydsl.core.support.QueryMixin;

public class R2QueryClause<T> extends AbstractR2QueryClause<T>{

	private Connection conn;

	// /////////// 覆盖检查字段结束/////////////
	private boolean exceedSizeLog;

	private RoutingStrategy routing;
	
	public R2QueryClause(QueryMixin<AbstractR2QueryClause<T>> queryMixin, ConfigurationEx configuration) {
		super(queryMixin, configuration);
	}

}
