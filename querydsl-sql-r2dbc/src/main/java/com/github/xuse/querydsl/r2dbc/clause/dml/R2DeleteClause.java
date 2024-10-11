package com.github.xuse.querydsl.r2dbc.clause.dml;


import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.R2ClauseBase;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public class R2DeleteClause extends R2ClauseBase<R2DeleteClause> {
	public R2DeleteClause(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

}
