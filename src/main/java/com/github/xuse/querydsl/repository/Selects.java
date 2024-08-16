//package com.github.xuse.querydsl.repository;
//
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//
//import com.github.xuse.querydsl.lambda.LambdaColumn;
//import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
//import com.github.xuse.querydsl.lambda.StringLambdaColumn;
//import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
//import com.querydsl.core.Tuple;
//import com.querydsl.core.types.ArrayConstructorExpression;
//import com.querydsl.core.types.Expression;
//import com.querydsl.core.types.Projections;
//
//public class Selects<B,T>{
//	private final Function<Expression<?>[],Expression<T>> creator;
//	protected List<Expression<?>> exprs = new ArrayList<>();
//	
//	protected Selects(Function<Expression<?>[],Expression<T>> creator) {
//		this.creator=creator;
//	}
//	
//	public static <B,T> Selects<B,T> bean(Class<B> table,Class<T> target){
//		return new Selects<B,T>(e->ProjectionsAlter.bean(target, e)); 
//	}
//	
//	public static <B> Selects<B,Tuple> tuple(){
//		return new Selects<B,Tuple>(e->Projections.tuple(e));
//	}
//	
//	
//	public static <B> Selects<B,Object[]> array(){
//		return new Selects<B,Object[]>(e-> new ArrayConstructorExpression<Object>((Expression[])e));
//	}
//
//	@SuppressWarnings("unchecked")
//	public static <B,E extends Comparable<E>> Selects<B,E[]> array(Class<E> type){
//		Class<E[]> clz=(Class<E[]>) Array.newInstance(type, 0).getClass();
//		return new Selects<>(e-> new ArrayConstructorExpression<E>(clz, (Expression[])e));
//	}
//	
//	public <C extends Comparable<C>> Selects<B,T> item(LambdaColumn<B,C> column){
//		exprs.add(column);
//		return this;
//	}
//	
//	public <C extends Comparable<C>> Selects<B,T> item(LambdaColumn<B,C> column,Function<LambdaColumn<B,C>,Expression<?>> func){
//		exprs.add(func.apply(column));
//		return this;
//	}
//	
//	public <C extends Number & Comparable<C>> Selects<B,T> numItem(NumberLambdaColumn<B,C> column,Function<NumberLambdaColumn<B,C>,Expression<?>> func){
//		exprs.add(func.apply(column));
//		return this;
//	}
//
//	public Selects<B,T> stringItem(StringLambdaColumn<B> column,Function<StringLambdaColumn<B>,Expression<?>> func){
//		exprs.add(func.apply(column));
//		return this;
//	}
//
//	
//	public Expression<T> build() {
//		return creator.apply(exprs.toArray(EMPTY));
//	}
//	
//	private static final Expression<?>[] EMPTY=new Expression<?>[0]; 
//	
//
//}
