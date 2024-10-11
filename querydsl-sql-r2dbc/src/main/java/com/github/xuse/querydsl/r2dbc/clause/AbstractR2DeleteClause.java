package com.github.xuse.querydsl.r2dbc.clause;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public abstract class AbstractR2DeleteClause<C extends AbstractR2DeleteClause<C>> extends R2ClauseBase<C> implements DeleteClause<C>{
	public AbstractR2DeleteClause(ConnectionFactory connection, ConfigurationEx configuration,
			RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}
}
