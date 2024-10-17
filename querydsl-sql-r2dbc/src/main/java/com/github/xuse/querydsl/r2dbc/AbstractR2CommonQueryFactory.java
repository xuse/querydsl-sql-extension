package com.github.xuse.querydsl.r2dbc;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2DeleteClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2MergeClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2QueryClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2SQLInsertClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2UpdateClause;

import io.r2dbc.spi.ConnectionFactory;

public abstract class AbstractR2CommonQueryFactory<Q extends R2QueryClause<?>>
		implements R2CommonQueryFactory<Q, R2DeleteClause, R2UpdateClause, R2SQLInsertClause, R2MergeClause> {
	protected final ConfigurationEx configEx;

	protected final ConnectionFactory connection;

	protected AbstractR2CommonQueryFactory(ConfigurationEx config, ConnectionFactory connection) {
		this.configEx = config;
		this.connection = connection;
	}
}
