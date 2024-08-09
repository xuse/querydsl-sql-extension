package com.github.xuse.querydsl.repository;

import java.util.List;

import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.util.Assert;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.sql.RelationalPath;

/**
 * 用于填入Where条件，并提供若干数据库操作动作。
 * QueryWrapper是不带数据库Session的查询操作对象，这个是带Session的操作对象。
 * @param <T> the entity type
 */
public class QueryExecutor<T> extends QueryWrapper<T, QueryExecutor<T>> {
	private final AbstractCrudRepository<T, ?> repository;

	QueryExecutor(QueryWrapper<T, ?> q, AbstractCrudRepository<T, ?> repository) {
		super(q.table, q.mixin);
		Assert.notNull(repository);
		if(q.table==null) {
			super.table = repository.getPath();
		}
		this.repository = repository;
	}

	QueryExecutor(RelationalPath<T> table, AbstractCrudRepository<T, ?> repository) {
		super(table, new DefaultQueryMetadata());
		this.repository = repository;
	}

	SQLQueryFactory getFactory() {
		return repository.getFactory();
	}

	private static final OrderSpecifier<?>[] EMPTY_OrderSpecifier = new OrderSpecifier[0];

	private SQLQueryAlter<T> createQuery(boolean all) {
		SQLQueryAlter<T> query = getFactory().selectFrom(table).where(mixin.getWhere());
		for (QueryFlag flag : mixin.getFlags()) {
			query.addFlag(flag);
		}
		query.groupBy(mixin.getGroupBy().toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
		query.having(mixin.getHaving());
		if (all) {
			query.orderBy(mixin.getOrderBy().toArray(EMPTY_OrderSpecifier));
			QueryModifiers modifiers = mixin.getModifiers();
			if (modifiers != null) {
				if (modifiers.getLimit() != null) {
					query.limit(modifiers.getLimit());
				}
				if (modifiers.getOffset() != null) {
					query.offset(modifiers.getOffset());
				}
			}
		}
		return query;
	}
	
	public Pair<Integer, List<T>> findAndCount(){
		return createQuery(true).fetchAndCount();
	}

	public List<T> fetch() {
		return createQuery(true).fetch();
	}

	public int count() {
		return (int) createQuery(false).fetchCount();
	}

	public T fetchFirst() {
		return createQuery(true).fetchFirst();
	}

	public int update(T t) {
		return (int) getFactory().update(table).populate(t).where(mixin.getWhere()).execute();
	}

	public T load() {
		return createQuery(true).fetchOne();
	}

	public int delete() {
		return (int) getFactory().delete(table).where(mixin.getWhere()).execute();
	}

	@Override
	protected QueryExecutor<T> subchain() {
		return new QueryExecutor<>(table, repository);
	}
}
