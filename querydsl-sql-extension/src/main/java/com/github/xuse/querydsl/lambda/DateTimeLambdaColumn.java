package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.DateTimeExpression;

public interface DateTimeLambdaColumn<B, T extends Comparable<T>> extends LambdaColumn<B, T>,ExprDateTime<T> {
	@SuppressWarnings("unchecked")
	default DateTimeExpression<T> mixin() {
		return (DateTimeExpression<T>) PathCache.getPath(this);
	}
}
