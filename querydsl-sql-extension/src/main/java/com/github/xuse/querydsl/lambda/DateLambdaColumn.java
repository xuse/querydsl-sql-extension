package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.DateExpression;

@FunctionalInterface
public interface DateLambdaColumn<B, T extends Comparable<?>> extends LambdaColumn<B, T>, ExprDate<T> {
	@SuppressWarnings({"unchecked", "rawtypes"})
	default DateExpression<T> mixin() {
		return (DateExpression) PathCache.getPath(this);
	}
}
