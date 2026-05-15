package com.github.xuse.querydsl.lambda;

import java.util.function.Function;

import com.querydsl.core.types.dsl.DateTimeExpression;

@FunctionalInterface
public interface DateTimeLambdaColumn<B, T extends Comparable<?>> extends Function<B, T>, LambdaColumnBase<B, T>, ExprDateTime<T> {
	@SuppressWarnings({"unchecked", "rawtypes"})
	default DateTimeExpression<T> mixin() {
		return (DateTimeExpression) PathCache.getPath(this);
	}
}
