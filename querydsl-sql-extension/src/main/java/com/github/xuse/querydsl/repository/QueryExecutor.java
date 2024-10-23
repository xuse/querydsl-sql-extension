package com.github.xuse.querydsl.repository;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.expression.AliasMapBeans;
import com.github.xuse.querydsl.sql.expression.QAliasBeansDiscontinuous;
import com.github.xuse.querydsl.util.Assert;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.group.QPair;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.sql.RelationalPath;

/**
 * 用于填入Where条件，并提供若干数据库操作动作。
 * QueryWrapper是不带数据库Session的查询操作对象，这个是带Session的操作对象。
 * @param <T> the entity type.
 * @param <R> the result type.
 */
public class QueryExecutor<T,R> extends QueryWrapper<T,R, QueryExecutor<T,R>> {
	private final AbstractCrudRepository<T, ?> repository;

	QueryExecutor(QueryWrapper<T,R,?> q, AbstractCrudRepository<T, ?> repository) {
		super(q.table, q.mixin);
		Assert.notNull(repository);
		if(q.table==null) {
			super.table = repository.getPath();
		}
		this.repository = repository;
	}

	QueryExecutor(RelationalPath<T> table, QueryMetadata mixin, AbstractCrudRepository<T, ?> repository) {
		super(table, mixin);
		this.repository = repository;
	}

	SQLQueryFactory getFactory() {
		return repository.getFactory();
	}

	private static final OrderSpecifier<?>[] EMPTY_OrderSpecifier = new OrderSpecifier[0];

	@SuppressWarnings("unchecked")
	private SQLQueryAlter<R> createQuery(boolean all) {
		SQLQueryAlter<R> query;
		if(mixin.getProjection()!=null) {
			 query = (SQLQueryAlter<R>) getFactory().select(mixin.getProjection()).from(table);	
		}else {
			 query = (SQLQueryAlter<R>) getFactory().selectFrom(table);	
		}
		query.where(mixin.getWhere());
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
	
	public Pair<Integer, List<R>> fetchAndCount(){
		return createQuery(true).fetchAndCount();
	}
	
	public Pair<Integer, List<R>> findAndCount(){
		return createQuery(true).fetchAndCount();
	}

	public List<R> fetch() {
		return createQuery(true).fetch();
	}

	public int count() {
		return (int) createQuery(false).fetchCount();
	}

	public R fetchFirst() {
		return createQuery(true).fetchFirst();
	}

	public int update(T t) {
		return (int) getFactory().update(table).populate(t).where(mixin.getWhere()).execute();
	}

	public UpdateHandler<T> update() {
		return new UpdateHandler<>(getFactory().update(table).where(mixin.getWhere()));
	}

	public R load() {
		return createQuery(true).fetchOne();
	}

	public int delete() {
		return (int) getFactory().delete(table).where(mixin.getWhere()).execute();
	}

	@Override
	protected QueryExecutor<T, R> subchain() {
		return new QueryExecutor<>(table, new DefaultQueryMetadata(),repository);
	}
	
	
	public <C extends Comparable<C>> QueryExecutor<T,C> selectSingleColumn(LambdaColumn<T, C> expr) {
		mixin.setProjection(expr);
		QueryExecutor<T,C> ch=new QueryExecutor<>(this.table,mixin,this.repository);
		return ch;
	}
	
	public <C extends Comparable<C>,X> QueryExecutor<T,X> selectSingleColumn(LambdaColumn<T, C> expr,Function<LambdaColumn<T, C>,Expression<X>> func) {
		mixin.setProjection(func.apply(expr));
		QueryExecutor<T,X> ch=new QueryExecutor<>(table,mixin,repository);
		return ch;
	}
	
	public <K extends Comparable<K>,V extends Comparable<V>> QueryExecutor<T,Pair<K,V>> selectPair(LambdaColumn<T, K> expr1,LambdaColumn<T, V> expr2) {
		mixin.setProjection(new QPair<K,V>(expr1,expr2));
		QueryExecutor<T,Pair<K,V>> ch=new QueryExecutor<>(table,mixin,repository);
		return ch;
	}
	
	public <K extends Comparable<K>,V extends Comparable<V>> QueryExecutor<T,Pair<K,V>> selectPair(Expression<K> expr1,Expression<V> expr2) {
		mixin.setProjection(new QPair<K,V>(expr1,expr2));
		QueryExecutor<T,Pair<K,V>> ch=new QueryExecutor<>(table,mixin,repository);
		return ch;
	}
	
	public <X> QueryExecutor<T,X> select(Function<SelectBuilder<T>,Expression<X>> factoryExpression) {
		mixin.setProjection(factoryExpression.apply(new SelectBuilder<T>()));
		QueryExecutor<T,X> ch=new QueryExecutor<>(table,mixin,repository);
		return ch;
	}
	
	public QueryExecutor<T,AliasMapBeans> selectBeans(Consumer<QAliasBeansDiscontinuous.Builder> consumer) {
		QAliasBeansDiscontinuous.Builder builder = QAliasBeansDiscontinuous.builder();
		consumer.accept(builder);
		mixin.setProjection(builder.build());
		QueryExecutor<T, AliasMapBeans> ch = new QueryExecutor<>(table, mixin,repository);
		return ch;
	}
}
