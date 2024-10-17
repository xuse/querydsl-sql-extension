package com.github.xuse.querydsl.r2dbc;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2DeleteClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2MergeClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2QueryClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2SQLInsertClause;
import com.github.xuse.querydsl.r2dbc.clause.dml.R2UpdateClause;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public class R2dbcQueryFactory extends AbstractR2CommonQueryFactory<R2QueryClause<?>>{
	protected R2dbcQueryFactory(ConnectionFactory connection,ConfigurationEx config) {
		super(config, connection);
	}

	@Override
	public R2DeleteClause delete(RelationalPath<?> path) {
		return new R2DeleteClause(connection, configEx, path);
	}

	@Override
	public R2QueryClause<?> from(Expression<?> from) {
		return query().from(from);
	}

	@Override
	public R2QueryClause<?> from(Expression<?>... from) {
		return query().from(from);
	}

	@SuppressWarnings("unchecked")
	@Override
	public R2QueryClause<?> from(SubQueryExpression<?> subQuery, Path<?> alias) {
		 return query().from((Expression) ExpressionUtils.as((Expression) subQuery, alias));
	}

	@Override
	public R2SQLInsertClause insert(RelationalPath<?> path) {
		return new R2SQLInsertClause(connection,configEx,path);
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
		return new R2QueryClause<Void>(connection, configEx);
	}
}
