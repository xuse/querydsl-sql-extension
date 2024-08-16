package com.github.xuse.querydsl.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaColumnBase;
import com.querydsl.core.types.Expression;

public class ArrayBuilder<B,T> {
	protected List<Expression<T>> exprs = new ArrayList<>();
	
	
	public <C extends Comparable<C>> ArrayBuilder<B,T> item(LambdaColumn<B, C> c,Function<LambdaColumn<B, C>,Expression<T>> func){
		exprs.add(func.apply(c));
		return this;
	}
	
	public ArrayBuilder<B,T> item(LambdaColumnBase<B, T> c){
		exprs.add(c);
		return this;
	}
	

}
