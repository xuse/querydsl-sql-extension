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

public abstract class AbstractR2QueryClause<T> extends ProjectableSQLQuery<T,AbstractR2QueryClause<T>>{

	protected final ConfigurationEx configuration;
	
	public AbstractR2QueryClause(QueryMixin<AbstractR2QueryClause<T>> queryMixin, ConfigurationEx configuration) {
		super(queryMixin, configuration.get());
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
	public AbstractR2QueryClause<T> clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SQLSerializer createSerializer() {
		// TODO Auto-generated method stub
		return null;
	}

}
