package com.github.xuse.querydsl.lambda;

import java.util.function.Function;

import com.querydsl.core.types.dsl.DateExpression;

@FunctionalInterface
public interface DateLambdaColumn<B, T extends Comparable<?>> extends Function<B, T>, LambdaColumnBase<B, T>, ExprDate<T> {
	@SuppressWarnings({"unchecked", "rawtypes"})
	default DateExpression<T> mixin() {
		return (DateExpression) PathCache.getPath(this);
	}
}
