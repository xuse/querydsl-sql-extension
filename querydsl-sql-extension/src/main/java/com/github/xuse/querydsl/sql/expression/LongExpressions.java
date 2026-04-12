package com.github.xuse.querydsl.sql.expression;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;

import com.querydsl.core.types.dsl.NumberExpression;

public class LongExpressions {
	public static final NumberExpression<Long> ONE = numberTemplate(Long.class, "1");

    public static final NumberExpression<Long> TWO = numberTemplate(Long.class, "2");
	
    public static final NumberExpression<Long> ZERO = numberTemplate(Long.class, "0");
}
