package com.github.xuse.querydsl.r2dbc.clause;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.FetchableQuery;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.ProjectableSQLQuery;
import com.querydsl.sql.SQLSerializer;

import io.r2dbc.spi.ConnectionFactory;

public abstract class AbstractR2QueryClause<T,Q extends AbstractR2QueryClause<T, Q>> extends ProjectableSQLQuery<T,Q>{

	protected final ConfigurationEx configuration;
	
	protected final ConnectionFactory connection;
	
	public AbstractR2QueryClause(ConnectionFactory connection, QueryMixin<Q> queryMixin, ConfigurationEx configuration) {
		super(queryMixin, configuration.get());
		this.connection=connection;
		this.configuration=configuration;
	}

	@Override
	public <U> FetchableQuery<U, ?> select(Expression<U> expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FetchableQuery<Tuple, ?> select(Expression<?>... exprs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CloseableIterator<T> iterate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResults<T> fetchResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Q clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SQLSerializer createSerializer() {
		// TODO Auto-generated method stub
		return null;
	}

}
