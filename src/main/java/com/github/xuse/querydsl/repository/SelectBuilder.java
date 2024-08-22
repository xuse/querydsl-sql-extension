package com.github.xuse.querydsl.repository;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.lambda.DateLambdaColumn;
import com.github.xuse.querydsl.lambda.DateTimeLambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.lambda.TimeLambdaColumn;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.QBeans1;
import com.github.xuse.querydsl.sql.expression.QBeans2;
import com.github.xuse.querydsl.sql.expression.QStringMap;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.QPair;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.TimeExpression;
import com.querydsl.sql.Beans;
import com.querydsl.sql.RelationalPath;

public class SelectBuilder<B> {
	private final List<Expression<?>> exprs = new ArrayList<>();

	
	public SelectBuilder<B> all(LambdaTable<B> table) {
		exprs.add(table);
		return this;
	}
	
	public SelectBuilder<B> columns(Expression<?>... paths) {
		for(Expression<?> expr:paths) {
			if(expr!=null) {
				exprs.add(expr);
			}
		}
		return this;
	}
	
	public <C extends Comparable<C>> SelectComparableExpr<C> column(LambdaColumn<B,C> column) {
		return new SelectComparableExpr<>(column);
	}
	
	public SelectStringExpr column(StringLambdaColumn<B> column) {
		return new SelectStringExpr(column);
	}
	
	public SelectStringExpr string(StringLambdaColumn<B> column) {
		return new SelectStringExpr(column);
	}
	
	public <C extends Number & Comparable<C>> SelectNumbergExpr<C> column(NumberLambdaColumn<B,C> column) {
		return new SelectNumbergExpr<>(column);
	}
	
	public <C extends Number & Comparable<C>> SelectNumbergExpr<C> num(NumberLambdaColumn<B,C> column) {
		return new SelectNumbergExpr<>(column);
	}
	
	public <C extends Comparable<C>> SelectComparableExpr<C> date(DateLambdaColumn<B,C> column) {
		return new SelectComparableExpr<>(column);
	}
	
	public <C extends Comparable<C>> SelectComparableExpr<C> time(TimeLambdaColumn<B,C> column) {
		return new SelectComparableExpr<>(column);
	}

	public <C extends Comparable<C>> SelectComparableExpr<C> datetime(DateTimeLambdaColumn<B,C> column) {
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
	
	public Expression<Map<String,?>> toMap(){
		return new QStringMap(exprs);
	}
	
	public Expression<Beans> toBeans() {
		if(exprs.isEmpty()) {
			return new QBeans1();
		}
		List<RelationalPath<?>> tables = new ArrayList<>();
		Map<RelationalPath<?>,List<Path<?>>> paths=new HashMap<>();
		for(Expression<?> expr:exprs) {
			if(expr instanceof RelationalPath<?>) {
				tables.add((RelationalPath<?>)expr);
			}else if(expr instanceof Path<?>) {
				Path<?> p=(Path<?>)expr;
				RelationalPath<?> table=(RelationalPath<?>) p.getMetadata().getParent();
				List<Path<?>> list=paths.computeIfAbsent(table, t->new ArrayList<>());
				list.add(p);
			}else {
				throw Exceptions.illegalArgument("The value of expression {} belongs to which bean? Please use Qbean2.Builder to create the strategy.", expr);
			}
		}
		if(paths.isEmpty()) {
			return new QBeans1(tables);
		}
		for(RelationalPath<?> table:tables) {
			for(Path<?> p:table.getColumns()) {
				List<Path<?>> list=paths.computeIfAbsent(table, t->new ArrayList<>());
				list.add(p);
			}
		}
		QBeans2.Builder builder=QBeans2.builder();
		for(Map.Entry<RelationalPath<?>,List<Path<?>>> entry:paths.entrySet()) {
			builder.addBean(entry.getKey(), entry.getValue());	
		}
		return builder.build();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Expression<Pair<?,?>> toPair(){
		if(exprs.size()!=2) {
			throw Exceptions.illegalArgument("To return a Pair<A,B> result, you must assign 2 selected columns. current is {}", exprs.size());
		}
    	return new QPair(exprs.get(0),exprs.get(1));
	}
	
	@SuppressWarnings({ "unchecked"})
	public <K,V> Expression<Pair<K,V>> toPair(Class<K> clazz1,Class<V> clazz2){
		if(exprs.size()!=2) {
			throw Exceptions.illegalArgument("To return a Pair<A,B> result, you must assign 2 selected columns. current is {}", exprs.size());
		}
    	return new QPair<K,V>((Expression<K>)exprs.get(0),(Expression<V>)exprs.get(1));
	}
	
	public Expression<Map<Expression<?>,?>> toExprMap(){
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

		public SelectBuilder<B> and(){
			return endExpr(this.expr);
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

		public SelectBuilder<B> and(){
			return endExpr(this.expr);
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
		
		public SelectBuilder<B> and(){
			return endExpr(this.expr);
		}
	}
	
	public class SelectDateExpr<C extends Comparable<C>> {
		protected DateExpression<C> expr;
		
		@SuppressWarnings("unchecked")
		SelectDateExpr(DateLambdaColumn<B,C> column){
			this.expr=(DateExpression<C>)PathCache.getPath(column);
		}
		public CustomExpr to(Function<DateExpression<C>,SimpleExpression<?>> function) {
			return new CustomExpr(function.apply(expr));
		}
		public SelectBuilder<B> as(String alias){
			if(StringUtils.isEmpty(alias)) {
				return endExpr(this.expr);
			}
			return endExpr(this.expr=expr.as(alias));
		}

		public SelectBuilder<B> and(){
			return endExpr(this.expr);
		}
	}
	public class SelectTimeExpr<C extends Comparable<C>> {
		protected TimeExpression<C> expr;
		
		@SuppressWarnings("unchecked")
		SelectTimeExpr(TimeLambdaColumn<B,C> column){
			this.expr=(TimeExpression<C>)PathCache.getPath(column);
		}
		public CustomExpr to(Function<TimeExpression<C>,SimpleExpression<?>> function) {
			return new CustomExpr(function.apply(expr));
		}
		public SelectBuilder<B> as(String alias){
			if(StringUtils.isEmpty(alias)) {
				return endExpr(this.expr);
			}
			return endExpr(this.expr=expr.as(alias));
		}

		public SelectBuilder<B> and(){
			return endExpr(this.expr);
		}
	}
	public class SelectDateTimeExpr<C extends Comparable<C>> {
		protected DateTimeExpression<C> expr;
		
		@SuppressWarnings("unchecked")
		SelectDateTimeExpr(DateTimeLambdaColumn<B,C> column){
			this.expr=(DateTimeExpression<C>)PathCache.getPath(column);
		}
		public CustomExpr to(Function<DateTimeExpression<C>,SimpleExpression<?>> function) {
			return new CustomExpr(function.apply(expr));
		}
		public SelectBuilder<B> as(String alias){
			if(StringUtils.isEmpty(alias)) {
				return endExpr(this.expr);
			}
			return endExpr(this.expr=expr.as(alias));
		}

		public SelectBuilder<B> and(){
			return endExpr(this.expr);
		}
	}
}
