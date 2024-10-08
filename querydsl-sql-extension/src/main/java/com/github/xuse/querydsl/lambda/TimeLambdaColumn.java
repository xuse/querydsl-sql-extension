package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.TimeExpression;

@FunctionalInterface
public interface TimeLambdaColumn<B, T extends Comparable<T>> extends LambdaColumn<B, T>, ExprTime<T> {

	@SuppressWarnings("unchecked")
	default TimeExpression<T> mixin() {
		return (TimeExpression<T>) PathCache.getPath(this);
	}
}
