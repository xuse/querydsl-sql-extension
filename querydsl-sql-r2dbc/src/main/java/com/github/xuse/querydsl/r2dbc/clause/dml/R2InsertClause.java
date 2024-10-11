package com.github.xuse.querydsl.r2dbc.clause.dml;

import static com.github.xuse.querydsl.sql.expression.AbstractMapperSupport.SCENARIO_INSERT;

import java.util.Map;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.AbstractR2InsertClause;
import com.github.xuse.querydsl.sql.Mappers;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.Mapper;

import io.r2dbc.spi.ConnectionFactory;

public class R2InsertClause extends AbstractR2InsertClause<R2InsertClause>{

	private RoutingStrategy routing;

	private Boolean writeNulls;
	
	public R2InsertClause(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public R2InsertClause populate(Object bean) {
		int type = Mappers.TYPE_BEAN;
		if (writeNulls!=null && writeNulls) {
			type = type | Mappers.NULLS_BIND;
		}
		return populate(bean, Mappers.get(SCENARIO_INSERT, type));
	}
}
