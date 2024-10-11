package com.github.xuse.querydsl.r2dbc;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2DeleteClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2InsertClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2MergeClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2QueryClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2UpdateClause;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public class R2dbcQueryFactory extends AbstractR2CommonQueryFactory<R2QueryClause<?>>{
	protected R2dbcQueryFactory(ConfigurationEx config, ConnectionFactory connection) {
		super(config, connection);
	}

	@Override
	public R2DeleteClause delete(RelationalPath<?> path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R2QueryClause<?> from(Expression<?> from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R2QueryClause<?> from(Expression<?>... from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R2QueryClause<?> from(SubQueryExpression<?> subQuery, Path<?> alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R2InsertClause insert(RelationalPath<?> path) {
		return new R2InsertClause(connection,configEx,path);
	}

	@Override
	public R2MergeClause merge(RelationalPath<?> path) {
		return new R2MergeClause(connection,configEx,path);
	}

	@Override
	public R2UpdateClause update(RelationalPath<?> path) {
		return new R2UpdateClause(connection,configEx,path);
	}

	@Override
	public R2QueryClause<?> query() {
		// TODO Auto-generated method stub
		return null;
	}

}
