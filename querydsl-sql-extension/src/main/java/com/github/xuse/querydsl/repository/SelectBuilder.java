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
import com.github.xuse.querydsl.sql.expression.AliasMapBeans;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.QAliasBeansContinuous;
import com.github.xuse.querydsl.sql.expression.QAliasBeansDiscontinuous;
import com.github.xuse.querydsl.sql.expression.QStringObjMap;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.QPair;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.TimeExpression;
import com.querydsl.sql.RelationalPath;


/**
 * 为了自定义查询语句返回值的形式提供的工具。包含两类方法——
 * <ol>
 * <li>指定查询列/表达式</li>
 * <li>指定返回类格式</li>
 * </ol>
 * <h3>指定查询列/表达式</h3>
 * <ul>
 * <li>{@link #column(LambdaColumn)} 选择一个列，可以使用它构建函数表达式</li>
 * <li>{@link #column(NumberLambdaColumn)}  选择一个数值列，可以使用它构建函数表达式</li>
 * <li>{@link #column(StringLambdaColumn)}  选择一个字符串列，可以使用它构建函数表达式</li>
 * <li>{@link #date(DateLambdaColumn)}  选择一个Date列，可以使用它构建函数表达式</li>
 * <li>{@link #datetime(DateTimeLambdaColumn)}  选择一个Datetime列，可以使用它构建函数表达式</li>
 * <li>{@link #time(TimeLambdaColumn)}  选择一个Time列，可以使用它构建函数表达式</li>
 * </ul>
 * 
 * <h4>指定返回格式</h4>
 * <ul>
 * <li>{@link #toArray()} 表的每行记录以Object[]数组返回</li>
 * <li>{@link #toArray(Class)} 表的每行记录以指定类型的T[]数组返回</li>
 * <li>{@link #toBean(Class)} 表的每行记录按字段名匹配到新的Class作为返回对象</li>
 * <li>{@link #toList()} 表的每行记录以List格式返回</li> 
 * <li>{@link #toMap()} 表的每行记录以Map&lt;String,?&gt;格式返回</li>
 * <li>{@link #toExprMap()} 表的每行记录以Map&lt;列表达式,?&gt;格式返回</li>
 * <li>{@link #toPair()} 当仅选取两个列时，每行记录以Pair&lt;?,?&gt;格式返回</li>
 * <li>{@link #toPair(Class, Class)} 当仅选取两个列时，每行记录以Pair&lt;K,V&gt;格式返回</li>
 * <li>{@link #toTuple()} 每行记录以Tuple格式返回，该格式和ExprMap()基本一样</li>
 * </ul>
 * @author Joey
 * @param <B> The bean type
 */
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
	
	public <C extends Number & Comparable<C>> SelectNumberExpr<C> column(NumberLambdaColumn<B,C> column) {
		return new SelectNumberExpr<>(column);
	}
	
	public <C extends Number & Comparable<C>> SelectNumberExpr<C> num(NumberLambdaColumn<B,C> column) {
		return new SelectNumberExpr<>(column);
	}
	
	public <C extends Comparable<C>> SelectDateExpr<C> date(DateLambdaColumn<B,C> column) {
		return new SelectDateExpr<>(column);
	}
	
	public <C extends Comparable<C>> SelectTimeExpr<C> time(TimeLambdaColumn<B,C> column) {
		return new SelectTimeExpr<>(column);
	}

	public <C extends Comparable<C>> SelectDateTimeExpr<C> datetime(DateTimeLambdaColumn<B,C> column) {
		return new SelectDateTimeExpr<>(column);
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
		return Projections.array(clz2, exprs.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
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
		return new QStringObjMap(exprs);
	}
	
	public FactoryExpression<AliasMapBeans> toBeans() {
		if(exprs.isEmpty()) {
			return new QAliasBeansContinuous();
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
			return new QAliasBeansContinuous(tables);
		}
		for(RelationalPath<?> table:tables) {
			for(Path<?> p:table.getColumns()) {
				List<Path<?>> list=paths.computeIfAbsent(table, t->new ArrayList<>());
				list.add(p);
			}
		}
		QAliasBeansDiscontinuous.Builder builder=QAliasBeansDiscontinuous.builder();
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
	public class SelectNumberExpr<C extends Number & Comparable<C>> {
		protected NumberExpression<C> expr;
		
		@SuppressWarnings("unchecked")
		SelectNumberExpr(NumberLambdaColumn<B,C> column){
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
