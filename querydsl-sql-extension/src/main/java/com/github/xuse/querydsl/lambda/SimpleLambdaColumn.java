package com.github.xuse.querydsl.lambda;

import java.util.function.Function;

@FunctionalInterface
public interface SimpleLambdaColumn<B, T> extends Function<B, T>,LambdaColumnBase<B, T> {
//	@SuppressWarnings("unchecked")
//	default SimpleExpression<T> mixin() {
//		return (SimpleExpression<T>) PathCache.getPath(this);
//	}
}
