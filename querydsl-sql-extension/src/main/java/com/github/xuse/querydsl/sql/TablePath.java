package com.github.xuse.querydsl.sql;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public interface TablePath<T> extends RelationalPathEx<T> {
	
	StringPath get(StringLambdaColumn<T> column);
	
	<C extends Number & Comparable<C>> NumberPath<C> get(NumberLambdaColumn<T,C> column);
	
	<C extends Comparable<C>> ComparableExpression<C> get(LambdaColumn<T, C> column);
	
	default RelationalPathExImpl<T> forVariable(String variable){
    	@SuppressWarnings("unchecked")
		RelationalPathEx<T> result=(RelationalPathEx<T>) PathCache.get(getType(), variable);
    	if (result instanceof RelationalPathExImpl) {
			return (RelationalPathExImpl<T>) result;
		}
    	return ((RelationalPathBaseEx<T>)result).clone();
    }
}
