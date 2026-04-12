package com.github.xuse.querydsl.sql.expression;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;

public class SQLFunctions {
	
	public static final <T extends Comparable<T>> ComparableExpression<T> ifnull(ComparableExpression<T> mixin, T other){
		return ifnull(mixin,ConstantImpl.create(other));
	}
	
	public static final <T extends Comparable<T>> ComparableExpression<T> ifnull(ComparableExpression<T> mixin, Expression<T> other){
		return Expressions.comparableOperation(mixin.getType(),FunctionOps.IF_NULL, mixin, other);
	}
}
