package com.github.xuse.querydsl.repository;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;

public class SelectBuilder<B> {
	private final List<Expression<?>> exprs = new ArrayList<>();
	
	public SelectStringExpr select(StringLambdaColumn<B> column) {
		return new SelectStringExpr(column);
	}
	
	public <C extends Number & Comparable<C>> SelectNumbergExpr<C> select(NumberLambdaColumn<B,C> column) {
		return new SelectNumbergExpr<>(column);
	}
	
	public <C extends Comparable<C>> SelectComparableExpr<C> select(LambdaColumn<B,C> column) {
		return new SelectComparableExpr<>(column);
	}
	
	private SelectBuilder<B> endExpr(Expression<?> expr) {
		this.exprs.add(expr);
		return SelectBuilder.this;
	}

	public CustomExpr select(SimpleExpression<?> expr) {
		return new CustomExpr(expr);
	}
	
	public <T> Expression<T> toBean(Class<T> clz) {
		return ProjectionsAlter.bean(clz, exprs.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
	}
	
	@SuppressWarnings("unchecked")
	public <T> Expression<T[]> toArray(Class<T> clz) {
		Class<T[]> clz2= (Class<T[]>) Array.newInstance(clz, 0).getClass();
		return Projections.array(clz2,(Expression<T>[])exprs.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
	}
	
	@SuppressWarnings("unchecked")
	public Expression<Object[]> toArray() {
		return Projections.array(Object[].class,exprs.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
	}
	
	public Expression<Tuple> toTuple() {
		return Projections.tuple(exprs);
	}
	
	public Expression<List<?>> toList(){
		return Projections.list(exprs);
	}
	
	public Expression<Map<Expression<?>,?>> toMap(){
		return Projections.map(exprs.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
	}
	
	public class CustomExpr{
		SimpleExpression<?> expr;
		public CustomExpr(SimpleExpression<?> apply) {
			this.expr=apply;
		}
		public SelectBuilder<B> as(String alias){
			if(StringUtils.isEmpty(alias)) {
				return endExpr(this.expr);
			}
			return endExpr(this.expr=expr.as(alias));
		}
	}
	
	public class SelectComparableExpr<C extends Comparable<C>>{
		protected ComparableExpression<C> expr;
		SelectComparableExpr(LambdaColumn<B,C> column){
			this.expr=PathCache.getPathAsExpr(column);
		}
		public CustomExpr to(Function<ComparableExpression<C>,SimpleExpression<?>> function) {
			return new CustomExpr(function.apply(expr));
		}
		public SelectBuilder<B> as(String alias){
			if(StringUtils.isEmpty(alias)) {
				return endExpr(this.expr);
			}
			return endExpr(this.expr=expr.as(alias));	
		}
	}
	public class SelectNumbergExpr<C extends Number & Comparable<C>> {
		protected NumberExpression<C> expr;
		
		@SuppressWarnings("unchecked")
		SelectNumbergExpr(NumberLambdaColumn<B,C> column){
			this.expr=(NumberExpression<C>)PathCache.getPath(column);
		}
		public CustomExpr to(Function<NumberExpression<C>,SimpleExpression<?>> function) {
			return new CustomExpr(function.apply(expr));
		}
		public SelectBuilder<B> as(String alias){
			if(StringUtils.isEmpty(alias)) {
				return endExpr(this.expr);
			}
			return endExpr(this.expr=expr.as(alias));
		}
	}
	public class SelectStringExpr {
		protected StringExpression expr;
		SelectStringExpr(StringLambdaColumn<B> column){
			this.expr=(StringExpression)PathCache.getPath(column);
		}
		public CustomExpr to(Function<StringExpression,SimpleExpression<?>> function) {
			return new CustomExpr(function.apply(expr));
		}
		public SelectBuilder<B> as(String alias){
			if(StringUtils.isEmpty(alias)) {
				return endExpr(this.expr);
			}
			return endExpr(this.expr=expr.as(alias));
		}
	}
	
}
