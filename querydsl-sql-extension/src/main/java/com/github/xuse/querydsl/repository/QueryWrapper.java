package com.github.xuse.querydsl.repository;


import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.sql.column.UnsavedValuePredicateFactory;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.QueryMixin.Role;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.FactoryExpressionUtils;
import com.querydsl.core.types.FactoryExpressionUtils.FactoryExpressionAdapter;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.ProjectionRole;
import com.querydsl.sql.RelationalPath;

/**
 * 查询操作封装
 * @author Joey
 *
 * @param <T> Entity
 * @param <R> the result type for select
 * @param <Chain> the query object to support a chained operation.
 */
public abstract class QueryWrapper<T,R,Chain extends QueryWrapper<T,R,Chain>> {
	RelationalPath<T> table;

	final QueryMetadata mixin;
	
	private transient final Chain typedThis;

	@SuppressWarnings("unchecked")
	protected QueryWrapper(RelationalPath<T> table, QueryMetadata mixin) {
		this.table = table;
		this.mixin = mixin;
		typedThis = (Chain) this;
	}

	public Chain where(Predicate... predicates) {
		for (Predicate p : predicates) {
			if (p != null) {
				mixin.addWhere(p);
			}
		}
		return typedThis;
	}

	public <C extends Comparable<C>> Chain eq(LambdaColumn<T, C> column, C value) {
		mixin.addWhere(column.eq(value));
		return typedThis;
	}

	public <C extends Comparable<C>> Chain ne(LambdaColumn<T, C> column, C value) {
		mixin.addWhere(column.ne(value));
		return typedThis;
	}

	public <C extends Comparable<C>> Chain gt(LambdaColumn<T, C> column, C value) {
		mixin.addWhere(column.gt(value));
		return typedThis;
	}

	public <C extends Comparable<C>> Chain ge(LambdaColumn<T, C> column, C value) {
		mixin.addWhere(column.goe(value));
		return typedThis;
	}

	public <C extends Comparable<C>> Chain allEq(boolean condition, Map<LambdaColumn<T, C>, C> params, boolean null2IsNull) {
		java.util.function.Predicate<Object> p = UnsavedValuePredicateFactory.NullOrEmpty;
		if (condition && CollectionUtils.isNotEmpty(params)) {
			params.forEach((k, v) -> {
				if (p.test(v)) {
					eq(k, v);
				} else {
					if (null2IsNull) {
						isNull(k);
					}
				}
			});
		}
		return typedThis;
	}

	public <C extends Comparable<C>> Chain allEq(boolean condition, BiPredicate<LambdaColumn<T, C>, C> filter,
			Map<LambdaColumn<T, C>, C> params, boolean null2IsNull) {
		java.util.function.Predicate<Object> p = UnsavedValuePredicateFactory.NullOrEmpty;
		if (condition && CollectionUtils.isNotEmpty(params)) {
			params.forEach((k, v) -> {
				if (filter.test(k, v)) {
					if (p.test(v)) {
						eq(k, v);
					} else {
						if (null2IsNull) {
							isNull(k);
						}
					}
				}
			});
		}
		return typedThis;
	}

	public <C extends Comparable<C>> Chain isNull(LambdaColumn<T, C> column) {
		mixin.addWhere(column.isNull());
		return typedThis;
	}
	
	public <C extends Comparable<C>> Chain isNotNull(LambdaColumn<T, C> column) {
		mixin.addWhere(column.isNotNull());
		return typedThis;
	}
	
	public <C extends Comparable<C>> Chain lt(LambdaColumn<T, C> column, C value) {
		mixin.addWhere(column.lt(value));
		return typedThis;
	}

	public <C extends Comparable<C>> Chain le(LambdaColumn<T, C> column, C value) {
		mixin.addWhere(column.loe(value));
		return typedThis;
	}

	public Chain like(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.like(value, '/'));
		return typedThis;
	}

	public Chain notlike(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.notLike(value, '/'));
		return typedThis;
	}

	public Chain startsWith(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.startsWith(value));
		return typedThis;
	}

	public Chain notStartsWith(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.startsWith(value).not());
		return typedThis;
	}

	public Chain endsWith(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.endsWith(value));
		return typedThis;
	}

	public Chain notEndsWith(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.endsWith(value).not());
		return typedThis;
	}

	public Chain contains(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.contains(value));
		return typedThis;
	}

	public Chain notContains(StringLambdaColumn<T> column, String value) {
		mixin.addWhere(column.contains(value).not());
		return typedThis;
	}

	public <C extends Comparable<C>> Chain between(LambdaColumn<T, C> column, C value, C value2) {
		mixin.addWhere(column.between(value, value2));
		return typedThis;
	}
	
	public Chain orderBy(OrderSpecifier<?>... orders) {
		for(OrderSpecifier<?> order:orders) {
			if(order!=null) {
				mixin.addOrderBy(order);	
			}
		}
		return typedThis; 
	}
	
	public <C extends Comparable<C>> Chain groupBy(LambdaColumn<T, C> column) {
		mixin.addGroupBy(column);
		return typedThis; 
	}
	
	public <C extends Comparable<C>,D extends Comparable<D>> Chain groupBy(LambdaColumn<T, C> c1,LambdaColumn<T, D> c2) {
		mixin.addGroupBy(c1);
		mixin.addGroupBy(c2);
		return typedThis; 
	}
	
	public Chain groupBy(Expression<?>... o) {
		for (Expression<?> e : o) {
			groupBy(e);
		}
		return typedThis; 
	}
	
    public final Chain groupBy(Expression<?> e) {
        e = convert(e, Role.GROUP_BY);
        mixin.addGroupBy(e);
        return typedThis; 
    }
    
    public Chain having(Predicate e) {
    	mixin.addHaving(e);
    	return typedThis;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <RT> Expression<RT> convert(Expression<RT> expr, Role role) {
        if (expr instanceof ProjectionRole<?>) {
            return convert(((ProjectionRole) expr).getProjection(), role);
        } else if (expr instanceof FactoryExpression<?> && !(expr instanceof FactoryExpressionAdapter<?>)) {
            return FactoryExpressionUtils.wrap((FactoryExpression<RT>) expr);
        } else {
            return expr;
        }
    }
	
	public <C extends Comparable<C>> Chain orderByDesc(LambdaColumn<T, C> column) {
		mixin.addOrderBy(new OrderSpecifier<>(Order.DESC, column));
		return typedThis; 
	}
	
	public <C extends Comparable<C>> Chain orderByAsc(LambdaColumn<T, C> column) {
		mixin.addOrderBy(new OrderSpecifier<>(Order.ASC, column));
		return typedThis; 
	}
	
	public Chain limit(int size) {
		mixin.setLimit((long)size);
		return typedThis; 
	}
	
	public Chain offset(int size) {
		mixin.setOffset((long)size);
		return typedThis; 
	}
	

	/////////////////////////////////////////////
	private static Predicate or(Predicate lhs, Predicate rhs) {
		if (lhs == null) {
			return rhs;
		} else {
			return ExpressionUtils.or(lhs, rhs);
		}
	}
	
	private static Predicate and(Predicate lhs, Predicate rhs) {
		if (lhs == null) {
			return rhs;
		} else {
			return ExpressionUtils.and(lhs, rhs);
		}
	}
	 
    /**
     * 子类返回一个自己的新对象
     */
    protected abstract Chain subchain();
    
	
	public Chain and(Consumer<Chain> consumer) {
		Chain subChain = subchain();
		consumer.accept(subChain);
		Predicate where = mixin.getWhere();
		mixin.clearWhere();
		mixin.addWhere(and(where, subChain.mixin.getWhere()));
		return typedThis;
	}

	public Chain or(Consumer<Chain> consumer) {
		Chain subChain = subchain();
		consumer.accept(subChain);
		Predicate where = mixin.getWhere();
		mixin.clearWhere();
		mixin.addWhere(or(where, subChain.mixin.getWhere()));
		return typedThis;
	}

	public Chain not(Consumer<Chain> consumer) {
		Chain subChain = subchain();
		consumer.accept(subChain);
		Predicate where = mixin.getWhere();
		Predicate newWhere=subChain.mixin.getWhere();
		if(newWhere==null){
			return typedThis;
		}
		if(where==null){
			mixin.addWhere(newWhere.not());
		}else{
			mixin.clearWhere();
			mixin.addWhere(and(where, newWhere.not()));
		}
		return typedThis;
	}

	public Chain not() {
		Predicate where = mixin.getWhere();
		if(where!=null){
			mixin.clearWhere();
			mixin.addWhere(where.not());
		}
		return typedThis;
	}
}
