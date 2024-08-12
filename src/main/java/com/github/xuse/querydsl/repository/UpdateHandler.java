package com.github.xuse.querydsl.repository;

import java.util.function.Function;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.querydsl.core.types.Expression;

public class UpdateHandler<B> {
	
	final SQLUpdateClauseAlter update;

	public UpdateHandler(SQLUpdateClauseAlter update) {
		this.update=update;
	}
	
	public <C extends Comparable<C>> UpdateHandler<B> set(LambdaColumn<B, C> path, C value) {
		update.set(path, value);
		return this;
	}
	
	public <C extends Comparable<C>> UpdateHandler<B> set(LambdaColumn<B, C> path, Expression<C> value) {
		update.set(path, value);
		return this;
	}
	
	/**
	 * 将字段值更新为原值进行算术运算后的数值
	 * <p>Example:
	 * {@code .setMathExpr(Foo::getVolumn,volumn-> volumn.add(100))}
	 * <p>is equal to SQL
	 * {@code SET volumn = volumn + 100}
	 * 
	 * @param <C> Type of the number
	 * @param path the column
	 * @param expr 表达式Lambda
	 * @return this
	 */
	public <C extends Number & Comparable<C>> UpdateHandler<B> setMathExpr(NumberLambdaColumn<B, C> path,Function<NumberLambdaColumn<B, C>,Expression<C>> expr) {
		update.set(path, expr.apply(path));
		return this;
	}
	
	/**
	 * 将字段值更新为原值进行字符串运算后的数值
	 * <p>Example:
	 * {@code .setMathExpr(Foo::getCode,code-> code.concat("Suffix"))}
	 * <p>is equal to SQL
	 * <p>{@code SET code = code + 'Suffix'}     ({@code SET code = code || 'Suffix'} as in Oracle)
	 * 
	 * @param <C> Type of the number
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
	

}
