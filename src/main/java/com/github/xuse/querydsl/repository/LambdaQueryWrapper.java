package com.github.xuse.querydsl.repository;

import java.util.function.Function;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.RelationalPath;

public class LambdaQueryWrapper<T> extends LambdaQuery<T, T> {

	public LambdaQueryWrapper() {
	}
	
	public LambdaQueryWrapper(Class<T> clz) {
		super(clz);
	}
	
	protected LambdaQueryWrapper(RelationalPath<T> table, DefaultQueryMetadata mixin) {
		super(table,mixin);
	}

	@Override
	protected LambdaQueryWrapper<T> subchain() {
		return new LambdaQueryWrapper<>(table,new DefaultQueryMetadata());
	}
	
	public <C extends Comparable<C>> LambdaQuery<T,C> selectOne(LambdaColumn<T, C> expr) {
		mixin.setProjection(expr);
		LambdaQuery<T,C> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public <C extends Comparable<C>,X> LambdaQuery<T,X> selectOne(LambdaColumn<T, C> expr,Function<LambdaColumn<T, C>,Expression<X>> func) {
		mixin.setProjection(func.apply(expr));
		LambdaQuery<T,X> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public <R> LambdaQuery<T,R> select(Function<SelectBuilder<T>,Expression<R>> factoryExpression) {
		mixin.setProjection(factoryExpression.apply(new SelectBuilder<>()));
		LambdaQuery<T,R> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
}
