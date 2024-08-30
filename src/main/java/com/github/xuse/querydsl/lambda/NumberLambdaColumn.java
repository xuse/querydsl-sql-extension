package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.NumberExpression;

public interface NumberLambdaColumn<B, T extends Number & Comparable<T>> extends LambdaColumn<B, T>,ExprNumber<T> {
	@SuppressWarnings("unchecked")
	default NumberExpression<T> mixinNumber() {
		return (NumberExpression<T>) PathCache.getPath(this);
	}
}
