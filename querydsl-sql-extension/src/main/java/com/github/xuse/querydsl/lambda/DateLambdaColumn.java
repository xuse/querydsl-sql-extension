package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.DateExpression;

public interface DateLambdaColumn<B, T extends Comparable<T>> extends LambdaColumn<B, T>,ExprDate<T>{
	@SuppressWarnings("unchecked")
	default DateExpression<T> mixin() {
		return (DateExpression<T>) PathCache.getPath(this);
	}
}
