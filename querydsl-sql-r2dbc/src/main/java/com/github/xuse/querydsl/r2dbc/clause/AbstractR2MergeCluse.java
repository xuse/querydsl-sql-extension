package com.github.xuse.querydsl.r2dbc.clause;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.dml.InsertClause;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public abstract class AbstractR2MergeCluse <C extends AbstractR2MergeCluse<C>> extends R2ClauseBase<C> implements InsertClause<C>{

	public AbstractR2MergeCluse(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

}
