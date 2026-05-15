package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;

public interface ExprDateTime<T extends Comparable<?>> extends ExprTemporal<T> {

	@SuppressWarnings("rawtypes")
	DateTimeExpression mixin();

    /**
     * Create a day of month expression (range 1-31)
     *
     * @return day of month
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> dayOfMonth() {
        return ((DateTimeExpression) mixin()).dayOfMonth();
    }

    /**
     * Create a day of week expression (range 1-7 / SUN-SAT)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return day of week
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> dayOfWeek() {
    	  return ((DateTimeExpression) mixin()).dayOfWeek();
    }

    /**
     * Create a day of year expression (range 1-356)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return day of year
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> dayOfYear() {
    	return ((DateTimeExpression) mixin()).dayOfYear();
    }

    /**
     * Create a hours expression (range 0-23)
     *
     * @return hour
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> hour() {
    	return ((DateTimeExpression) mixin()).hour();
    }

    /**
     * Get the maximum value of this expression (aggregation)
     *
     * @return max(this)
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> max() {
    	return ((DateTimeExpression) mixin()).max();
    }

    /**
     * Create a milliseconds expression (range 0-999)
     * <p>Is always 0 in HQL and JDOQL modules</p>
     *
     * @return milli seconds
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> milliSecond() {
    	return ((DateTimeExpression) mixin()).milliSecond();
    }

    /**
     * Get the minimum value of this expression (aggregation)
     *
     * @return min(this)
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> min() {
    	return ((DateTimeExpression) mixin()).min();
    }

    /**
     * Create a minutes expression (range 0-59)
     *
     * @return minute
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> minute() {
    	return ((DateTimeExpression) mixin()).minute();
    }

    /**
     * Create a month expression (range 1-12 / JAN-DEC)
     *
     * @return month
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> month() {
    	return ((DateTimeExpression) mixin()).month();
    }

    /**
     * Create a seconds expression (range 0-59)
     *
     * @return second
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> second() {
    	return ((DateTimeExpression) mixin()).second();
    }

    /**
     * Create a week expression
     *
     * @return week
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> week() {
    	return ((DateTimeExpression) mixin()).week();
    }

    /**
     * Create a year expression
     *
     * @return year
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> year() {
    	return ((DateTimeExpression) mixin()).year();
    }

    /**
     * Create a year / month expression
     *
     * @return year month
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> yearMonth() {
    	return ((DateTimeExpression) mixin()).yearMonth();
    }

    /**
     * Create a ISO yearweek expression
     *
     * @return year week
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> yearWeek() {
    	return ((DateTimeExpression) mixin()).yearWeek();
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> nullif(Expression<T> other) {
    	return ((DateTimeExpression) mixin()).nullif(other);
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
    	return ((DateTimeExpression) mixin()).coalesce(expr);
    }

    /**
     * Create a {@code coalesce(this, exprs...)} expression
     *
     * @param exprs additional arguments
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(Expression<?>... exprs) {
    	return ((DateTimeExpression) mixin()).coalesce(exprs);
    }

    /**
     * Create a {@code coalesce(this, arg)} expression
     *
     * @param arg additional argument
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(T arg) {
    	return ((DateTimeExpression) mixin()).coalesce(arg);
    }

    /**
     * Create a {@code coalesce(this, args...)} expression
     *
     * @param args additional arguments
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(T... args) {
    	return ((DateTimeExpression) mixin()).coalesce(args);
    }
}
