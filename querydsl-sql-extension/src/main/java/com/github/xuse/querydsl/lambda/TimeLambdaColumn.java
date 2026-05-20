package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.TimeExpression;

@FunctionalInterface
public interface TimeLambdaColumn<B, T extends Comparable<?>> extends LambdaColumn<B, T>, ExprTime<T> {
	@SuppressWarnings({"unchecked", "rawtypes"})
	default TimeExpression<T> mixin() {
		return (TimeExpression) PathCache.getPath(this);
	}
}
