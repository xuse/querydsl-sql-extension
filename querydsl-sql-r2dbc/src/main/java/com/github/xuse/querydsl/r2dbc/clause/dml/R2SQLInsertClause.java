package com.github.xuse.querydsl.r2dbc.clause.dml;

import static com.github.xuse.querydsl.sql.expression.AbstractMapperSupport.SCENARIO_INSERT;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.AbstractR2InsertClause;
import com.github.xuse.querydsl.sql.Mappers;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public class R2SQLInsertClause extends AbstractR2InsertClause<R2SQLInsertClause>{



	private Boolean writeNulls;
	
	public R2SQLInsertClause(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public R2SQLInsertClause populate(Object bean) {
		int type = Mappers.TYPE_BEAN;
		if (writeNulls!=null && writeNulls) {
			type = type | Mappers.NULLS_BIND;
		}
		return populate(bean, Mappers.get(SCENARIO_INSERT, type));
	}
}
