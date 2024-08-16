package com.github.xuse.querydsl.repository;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaColumnBase;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;

public class UpdateHandler<B> {
	
	final SQLUpdateClauseAlter update;

	public UpdateHandler(SQLUpdateClauseAlter update) {
		this.update=update;
	}
	
	public <C extends Comparable<C>> UpdateHandler<B> set(LambdaColumn<B, C> path, C value) {
		update.set(path, value);
		return this;
	}
	
	protected <C extends Comparable<C>> UpdateHandler<B> set0(LambdaColumnBase<B, C> path, C value) {
		update.set(path, value);
		return this;
	}
	
	public <C extends Comparable<C>> UpdateHandler<B> set(LambdaColumn<B, C> path, Expression<C> value) {
		update.set(path, value);
		return this;
	}
	
	/**
	 * 针对Number类型的字段，提供更丰富的算术运算API
	 * @param <C>
	 * @param path
	 * @return UpdateSetNumber
	 */
	public <C extends Number & Comparable<C>> UpdateSetNumber<B,C> set(NumberLambdaColumn<B, C> path) {
		return new UpdateSetNumber<B,C>(path,this);
	}

	/**
	 * 针对String类型的字段，提供更丰富的算术运算API
	 * @param path 字段
	 * @return UpdateSetString
	 */
	public UpdateSetString<B> set(StringLambdaColumn<B> path){
		return new UpdateSetString<B>(path,this);
	}
	
	
	/**
	 * 将字段值更新为原值进行字符串运算后的数值
	 * <p>Example:
	 * {@code .setMathExpr(Foo::getCode,code-> code.concat("Suffix"))}
	 * <p>is equal to SQL
	 * <p>{@code SET code = code + 'Suffix'}     ({@code SET code = code || 'Suffix'} as in Oracle)
	 * 
	 * @param path the column
	 * @param expr 表达式Lambda
	 * @return this
	 */
	public UpdateHandler<B> setStringExpr(StringLambdaColumn<B> path,Function<StringLambdaColumn<B>,Expression<String>> expr) {
		update.set(path, expr.apply(path));
		return this;
	}
	
	public <C extends Comparable<C>> UpdateHandler<B> setIf(boolean mayDo,LambdaColumn<B, C> path, C value) {
		if(mayDo) {
			update.set(path, value);
		}
		return this;
	}
	
	public <C extends Comparable<C>> UpdateHandler<B> setIf(boolean mayDo,LambdaColumn<B, C> path, Expression<C> value) {
		if(mayDo) {
			update.set(path, value);
		}
		return this;
	}
	
	public int execute() {
		return (int)update.execute();
	}
	
	public UpdateHandler<B> setQueryTimeout(int queryTimeout) {
		if(queryTimeout>0) {
			update.setQueryTimeout(queryTimeout);
		}
		return this;
	}
	
	
	/**
	 * 提供针对String类字段的一些函数运算
	 * @param <B> bean type.
	 */
	public static class UpdateSetString<B>{
		private final StringLambdaColumn<B> path;
		private final UpdateHandler<B> update;
		UpdateSetString(StringLambdaColumn<B> path, UpdateHandler<B> update){
			this.path = path;
			this.update = update;
		}
		
		public UpdateHandler<B> to(Function<StringLambdaColumn<B>,Expression<String>> expr){
			return update.set(path, expr.apply(path));
		}
		
		public UpdateHandler<B> to(StringLambdaColumn<B> another, Function<StringLambdaColumn<B>,Expression<String>> expr){
			return update.set(path, expr.apply(another));
		}
		
		public UpdateHandler<B> to(StringLambdaColumn<B> another, BiFunction<StringLambdaColumn<B>,StringLambdaColumn<B>,Expression<String>> expr){
			return update.set(path, expr.apply(path,another));
		}
		
		public UpdateHandler<B> concat(String s){
			return update.set(path, path.concat(s));
		}
		
		public UpdateHandler<B> concat(Expression<String> expr){
			return update.set(path, path.concat(expr));
		}
	}
	
	/**
	 * 提供针对Number类字段的一些函数运算
	 * @param <B> bean type.
	 * @param <C> type of number.
	 */
	public static class UpdateSetNumber<B,C extends Number & Comparable<C>>{
		private final NumberLambdaColumn<B, C> path;
		private final UpdateHandler<B> update;
		
		UpdateSetNumber(NumberLambdaColumn<B, C> path, UpdateHandler<B> update){
			this.path = path;
			this.update = update;
		}
		
		public UpdateHandler<B> to(Function<NumberLambdaColumn<B, C>,Expression<C>> expr){
			return update.set(path, expr.apply(path));
		}
		
		public <D extends Number & Comparable<D>> UpdateHandler<B> to(NumberLambdaColumn<B, D> another,Function<NumberLambdaColumn<B, D>,Expression<C>> expr){
			return update.set(path, expr.apply(another));
		}
		
		public <D extends Number & Comparable<D>> UpdateHandler<B> to(NumberLambdaColumn<B, D> another,BiFunction<NumberLambdaColumn<B,C>,NumberLambdaColumn<B, D>,Expression<C>> expr){
			return update.set(path, expr.apply(path,another));
		}
		
		public UpdateHandler<B> add(int number){
			return update.set(path, path.add(number));
		}
		
		public <N extends Number & Comparable<?>> UpdateHandler<B> increment(){
			return update.set(path, path.add(Expressions.ONE));
		}

		public <N extends Number & Comparable<?>> UpdateHandler<B> add(Expression<N> number){
			return update.set(path, path.add(number));
		}
		
		public UpdateHandler<B> subtract(int number){
			return update.set(path, path.subtract(number));
		}
		
		public <N extends Number & Comparable<?>> UpdateHandler<B> subtract(Expression<N> number){
			return update.set(path, path.subtract(number));
		}
	}
	
}
