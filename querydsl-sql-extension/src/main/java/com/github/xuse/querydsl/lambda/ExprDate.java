package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;

public interface ExprDate<T extends Comparable<?>> extends ExprTemporal<T>{
	
	@SuppressWarnings("rawtypes")
	DateExpression mixin();

    /**
     * Create a day of month expression (range 1-31)
     *
     * @return day of month
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> dayOfMonth() {
    	return ((DateExpression) mixin()).dayOfMonth();
    }

    /**
     * Create a day of week expression (range 1-7 / SUN-SAT)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return day of week
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> dayOfWeek() {
    	return ((DateExpression) mixin()).dayOfWeek();
    }

    /**
     * Create a day of year expression (range 1-356)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return day of year
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> dayOfYear() {
    	return ((DateExpression) mixin()).dayOfYear();
    }

    /**
     * Get the maximum value of this expression (aggregation)
     *
     * @return max(this)
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> max() {
    	return ((DateExpression) mixin()).max();
    }

    /**
     * Get the minimum value of this expression (aggregation)
     *
     * @return min(this)
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> min() {
    	return ((DateExpression) mixin()).min();
    }

    /**
     * Create a month expression (range 1-12 / JAN-DEC)
     *
     * @return month
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> month() {
    	return ((DateExpression) mixin()).month();
    }

    /**
     * Create a week expression
     *
     * @return week
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> week() {
    	return ((DateExpression) mixin()).week();
    }

    /**
     * Create a year expression
     *
     * @return year
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> year() {
    	return ((DateExpression) mixin()).year();
    }

    /**
     * Create a year / month expression
     *
     * @return year month
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> yearMonth() {
    	return ((DateExpression) mixin()).yearMonth();
    }

    /**
     * Create a ISO yearweek expression
     *
     * @return year week
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> yearWeek() {
    	return ((DateExpression) mixin()).yearWeek();
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> nullif(Expression<T> other) {
    	return ((DateExpression) mixin()).nullif(other);
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
    default ComparableExpression<T> nullif(T other) {
        return nullif(ConstantImpl.create(other));
    }

    /**
     * Create a {@code coalesce(this, expr)} expression
     *
     * @param expr additional argument
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(Expression<T> expr) {
    	 return ((DateExpression) mixin()).coalesce(expr);
    }

    /**
     * Create a {@code coalesce(this, exprs...)} expression
     *
     * @param exprs additional arguments
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(Expression<?>... exprs) {
    	return ((DateExpression) mixin()).coalesce(exprs);
    }

    /**
     * Create a {@code coalesce(this, arg)} expression
     *
     * @param arg additional argument
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(T arg) {
    	return ((DateExpression) mixin()).coalesce(arg);
    }

    /**
     * Create a {@code coalesce(this, args...)} expression
     *
     * @param args additional arguments
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(T... args) {
    	return ((DateExpression) mixin()).coalesce(args);
    }
}
