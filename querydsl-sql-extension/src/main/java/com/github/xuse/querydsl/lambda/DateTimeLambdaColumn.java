package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.DateTimeExpression;

@FunctionalInterface
public interface DateTimeLambdaColumn<B, T extends Comparable<?>> extends LambdaColumn<B, T>, ExprDateTime<T> {
	@SuppressWarnings({"unchecked", "rawtypes"})
	default DateTimeExpression<T> mixin() {
		return (DateTimeExpression) PathCache.getPath(this);
	}
}
