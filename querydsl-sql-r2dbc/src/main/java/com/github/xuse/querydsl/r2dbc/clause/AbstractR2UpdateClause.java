package com.github.xuse.querydsl.r2dbc.clause;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.dml.UpdateClause;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public abstract class AbstractR2UpdateClause<C extends AbstractR2UpdateClause<C>> extends R2ClauseBase<C> implements UpdateClause<C>{

	public AbstractR2UpdateClause(ConnectionFactory connection, ConfigurationEx configuration,
			RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

}
