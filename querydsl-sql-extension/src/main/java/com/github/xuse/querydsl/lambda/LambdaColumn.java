package com.github.xuse.querydsl.lambda;

import java.util.function.Function;

import com.querydsl.core.types.dsl.ComparableExpression;

/**
 * @author Joey
 *
 * @param <B> the type of the bean, where this column come from.
 * @param <T> the type of the column
 */
@FunctionalInterface
public interface LambdaColumn<B,T extends Comparable<?>> extends Function<B, T>, LambdaColumnBase<B, T>,ExprComparable<T> {
	@SuppressWarnings({"unchecked", "rawtypes"})
	default ComparableExpression<T> mixin() {
		return (ComparableExpression) PathCache.getPathAsExpr(this);
	}
}
