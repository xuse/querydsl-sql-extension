package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;

public interface ExprDate<T extends Comparable<T>> extends ExprTemporal<T>{
	
	DateExpression<T> mixin();
	
	
	  /**
     * Create a day of month expression (range 1-31)
     *
     * @return day of month
     */
    default NumberExpression<Integer> dayOfMonth() {
    	return mixin().dayOfMonth();
    }

    /**
     * Create a day of week expression (range 1-7 / SUN-SAT)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return day of week
     */
    default NumberExpression<Integer> dayOfWeek() {
    	return mixin().dayOfWeek();
    }

    /**
     * Create a day of year expression (range 1-356)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return day of year
     */
    default NumberExpression<Integer> dayOfYear() {
    	return mixin().dayOfYear();
    }

    /**
     * Get the maximum value of this expression (aggregation)
     *
     * @return max(this)
     */
    @Override
    default DateExpression<T> max() {
    	return mixin().max();
    }

    /**
     * Get the minimum value of this expression (aggregation)
     *
     * @return min(this)
     */
    @Override
    default DateExpression<T> min() {
    	return mixin().min();
    }

    /**
     * Create a month expression (range 1-12 / JAN-DEC)
     *
     * @return month
     */
    default NumberExpression<Integer> month() {
    	return mixin().month();
    }

    /**
     * Create a week expression
     *
     * @return week
     */
    default NumberExpression<Integer> week() {
    	return mixin().week();
    }

    /**
     * Create a year expression
     *
     * @return year
     */
    default NumberExpression<Integer> year() {
    	return mixin().year();
    }

    /**
     * Create a year / month expression
     *
     * @return year month
     */
    default NumberExpression<Integer> yearMonth() {
    	return mixin().yearMonth();
    }

    /**
     * Create a ISO yearweek expression
     *
     * @return year week
     */
    default NumberExpression<Integer> yearWeek() {
    	return mixin().yearWeek();
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
    @Override
    default DateExpression<T> nullif(Expression<T> other) {
    	return mixin().nullif(other);
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
    @Override
    default DateExpression<T> nullif(T other) {
        return mixin().nullif(ConstantImpl.create(other));
    }

    /**
     * Create a {@code coalesce(this, expr)} expression
     *
     * @param expr additional argument
     * @return coalesce
     */
    @Override
    default DateExpression<T> coalesce(Expression<T> expr) {
    	 return mixin().coalesce(expr);
    }

    /**
     * Create a {@code coalesce(this, exprs...)} expression
     *
     * @param exprs additional arguments
     * @return coalesce
     */
    @Override
    default DateExpression<T> coalesce(Expression<?>... exprs) {
    	return mixin().coalesce(exprs);
    }

    /**
     * Create a {@code coalesce(this, arg)} expression
     *
     * @param arg additional argument
     * @return coalesce
     */
    @Override
    default DateExpression<T> coalesce(T arg) {
    	return mixin().coalesce(arg);
    }

    /**
     * Create a {@code coalesce(this, args...)} expression
     *
     * @param args additional arguments
     * @return coalesce
     */
    @Override
    @SuppressWarnings("unchecked")
    default DateExpression<T> coalesce(T... args) {
    	return mixin().coalesce(args);
    }
}
