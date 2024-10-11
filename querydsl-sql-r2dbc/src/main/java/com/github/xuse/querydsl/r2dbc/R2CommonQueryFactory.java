package com.github.xuse.querydsl.r2dbc;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLCommonQuery;

public interface R2CommonQueryFactory<Q extends SQLCommonQuery<?>, D, U, I, M> extends QueryFactory<Q> {

	/**
	 * Create a new DELETE clause
	 *
	 * @param path table to delete from
	 * @return delete clause
	 */
	D delete(RelationalPath<?> path);

	/**
	 * Create a new SELECT query
	 *
	 * @param from query source
	 * @return query
	 */
	Q from(Expression<?> from);

	/**
	 * Create a new SELECT query
	 *
	 * @param from query sources
	 * @return query
	 */
	Q from(Expression<?>... from);

	/**
	 * Create a new SELECT query
	 *
	 * @param subQuery query source
	 * @param alias    alias
	 * @return query
	 */
	Q from(SubQueryExpression<?> subQuery, Path<?> alias);

	/**
	 * Create a new INSERT INTO clause
	 *
	 * @param path table to insert to
	 * @return insert clause
	 */
	I insert(RelationalPath<?> path);

	/**
	 * Create a new MERGE clause
	 *
	 * @param path table to merge into
	 * @return merge clause
	 */
	M merge(RelationalPath<?> path);

	/**
	 * Create a new UPDATE clause
	 *
	 * @param path table to update
	 * @return update clause
	 */
	U update(RelationalPath<?> path);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.querydsl.core.QueryFactory#query()
	 */
	@Override
	Q query();

}