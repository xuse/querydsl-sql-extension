package com.github.xuse.querydsl.sql.expression;

import java.util.function.Function;

import com.querydsl.core.types.FactoryExpression;

public class Streams {

	public static <T, K> StreamExpressionWrapper<T, K> map(FactoryExpression<T> fac, Class<K> clz,
			Function<T, K> function) {
		return new StreamExpressionWrapper<>(fac, function, clz);
	}
	
	
	

}
