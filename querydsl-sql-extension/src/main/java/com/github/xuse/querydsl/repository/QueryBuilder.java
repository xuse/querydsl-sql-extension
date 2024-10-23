package com.github.xuse.querydsl.repository;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.RelationalPath;

/**
 * @param <T> the type of entity/
 * @param <R> the type of result(Select )
 * @param <C> the type of Parent context bean (typed root).
 */
public class QueryBuilder<T,R,C> extends QueryWrapper<T, R, QueryBuilder<T, R, C>>{
	private C typedRoot;
	
	
	public C build() {
		return typedRoot;
	}
	
	public QueryBuilder(RelationalPath<T> table, QueryMetadata mixin, C root) {
		super(table, mixin);
		this.typedRoot=root;
	}

	@Override
	protected QueryBuilder<T, R, C> subchain() {
		return new QueryBuilder<>(table,new DefaultQueryMetadata(),typedRoot);
	}
}
