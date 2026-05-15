package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.TimeExpression;

public interface ExprTime<T extends Comparable<?>> extends ExprTemporal<T> {

	@SuppressWarnings("rawtypes")
	TimeExpression mixin();

	/**
     * Create a hours expression (range 0-23)
     *
     * @return hour
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> hour() {
        return ((TimeExpression) mixin()).hour();
    }

    /**
     * Create a minutes expression (range 0-59)
     *
     * @return minute
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> minute() {
    	 return ((TimeExpression) mixin()).minute();
    }

    /**
     * Create a seconds expression (range 0-59)
     *
     * @return second
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> second() {
    	return ((TimeExpression) mixin()).second();
    }

    /**
     * Create a milliseconds expression (range 0-999)
     * <p>Is always 0 in JPA and JDO modules</p>
     *
     * @return milli second
     */
	@SuppressWarnings("rawtypes")
    default NumberExpression<Integer> milliSecond() {
    	return ((TimeExpression) mixin()).milliSecond();
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> nullif(Expression<T> other) {
    	return ((TimeExpression) mixin()).nullif(other);
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
    	return ((TimeExpression) mixin()).coalesce(expr);
    }

    /**
     * Create a {@code coalesce(this, exprs...)} expression
     *
     * @param exprs additional arguments
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(Expression<?>... exprs) {
    	return ((TimeExpression) mixin()).coalesce(exprs);
    }

    /**
     * Create a {@code coalesce(this, arg)} expression
     *
     * @param arg additional argument
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(T arg) {
    	return ((TimeExpression) mixin()).coalesce(arg);
    }

    /**
     * Create a {@code coalesce(this, args...)} expression
     *
     * @param args additional arguments
     * @return coalesce
     */
	@SuppressWarnings({"unchecked", "rawtypes"})
    default ComparableExpression<T> coalesce(T... args) {
    	return ((TimeExpression) mixin()).coalesce(args);
    }
}
