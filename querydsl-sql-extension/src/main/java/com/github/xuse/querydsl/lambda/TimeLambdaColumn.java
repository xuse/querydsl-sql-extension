package com.github.xuse.querydsl.lambda;

import java.util.function.Function;

import com.querydsl.core.types.dsl.TimeExpression;

@FunctionalInterface
public interface TimeLambdaColumn<B, T extends Comparable<?>> extends Function<B, T>, LambdaColumnBase<B, T>, ExprTime<T> {
	@SuppressWarnings({"unchecked", "rawtypes"})
	default TimeExpression<T> mixin() {
		return (TimeExpression) PathCache.getPath(this);
	}
}
