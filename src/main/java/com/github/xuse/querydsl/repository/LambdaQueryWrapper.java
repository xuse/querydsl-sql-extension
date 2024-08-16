package com.github.xuse.querydsl.repository;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
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
	
	public <C extends Comparable<C>> LambdaQuery<T,C> select(LambdaColumn<T, C> expr) {
		mixin.setProjection(expr);
		LambdaQuery<T,C> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public <C extends Comparable<C>,X> LambdaQuery<T,X> select(LambdaColumn<T, C> expr,Function<LambdaColumn<T, C>,Expression<X>> func) {
		mixin.setProjection(func.apply(expr));
		LambdaQuery<T,X> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	@SuppressWarnings("unchecked")
	public <C> LambdaQuery<T,C> selectBean(Class<C> clazz, LambdaColumn<T,?>...columns) {
		mixin.setProjection(ProjectionsAlter.bean(clazz, columns));
		LambdaQuery<T,C> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	@SuppressWarnings("unchecked")
	public <C extends Comparable<C>> LambdaQuery<T,C[]> selectArray(Class<C[]> type,LambdaColumn<T,C>...columns) {
		mixin.setProjection(Projections.array(type, columns));
		LambdaQuery<T,C[]> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	@SuppressWarnings("unchecked")
	public LambdaQuery<T,List<?>> selectList(LambdaColumn<T,?>...columns) {
		mixin.setProjection(Projections.list(columns));
		LambdaQuery<T,List<?>> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	@SuppressWarnings("unchecked")
	public LambdaQuery<T,Tuple> select(LambdaColumn<T,?>...columns) {
		mixin.setProjection(Projections.tuple(columns));
		LambdaQuery<T,Tuple> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}

//	public <X> LambdaQuery<T,X> select(Selects<T,X> selects) {
//		mixin.setProjection(selects.build());
//		LambdaQuery<T,X> ch=new LambdaQuery<>(table,mixin);
//		return ch;
//	}
}
