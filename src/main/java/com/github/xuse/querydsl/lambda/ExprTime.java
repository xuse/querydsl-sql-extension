package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.TimeExpression;

public interface ExprTime<T extends Comparable<T>> extends ExprTemporal<T> {
	TimeExpression<T> mixin();

	/**
     * Create a hours expression (range 0-23)
     *
     * @return hour
     */
    default NumberExpression<Integer> hour() {
        return mixin().hour();
    }

    /**
     * Create a minutes expression (range 0-59)
     *
     * @return minute
     */
    default NumberExpression<Integer> minute() {
    	 return mixin().minute();
    }

    /**
     * Create a seconds expression (range 0-59)
     *
     * @return second
     */
    default NumberExpression<Integer> second() {
    	return mixin().second();
    }

    /**
     * Create a milliseconds expression (range 0-999)
     * <p>Is always 0 in JPA and JDO modules</p>
     *
     * @return milli second
     */
    default NumberExpression<Integer> milliSecond() {
    	return mixin().milliSecond();
    }
    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
    @Override
    default TimeExpression<T> nullif(Expression<T> other) {
    	return mixin().nullif(other);
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other
     * @return nullif(this, other)
     */
    @Override
    default TimeExpression<T> nullif(T other) {
        return nullif(ConstantImpl.create(other));
    }

    /**
     * Create a {@code coalesce(this, expr)} expression
     *
     * @param expr additional argument
     * @return coalesce
     */
    @Override
    default TimeExpression<T> coalesce(Expression<T> expr) {
    	return mixin().coalesce(expr);
    }

    /**
     * Create a {@code coalesce(this, exprs...)} expression
     *
     * @param exprs additional arguments
     * @return coalesce
     */
    @Override
    default TimeExpression<T> coalesce(Expression<?>... exprs) {
    	return mixin().coalesce(exprs);
    }

    /**
     * Create a {@code coalesce(this, arg)} expression
     *
     * @param arg additional argument
     * @return coalesce
     */
    @Override
    default TimeExpression<T> coalesce(T arg) {
    	return mixin().coalesce(arg);    }

    /**
     * Create a {@code coalesce(this, args...)} expression
     *
     * @param args additional arguments
     * @return coalesce
     */
    @Override
    @SuppressWarnings({"unchecked"})
    default TimeExpression<T> coalesce(T... args) {
    	return mixin().coalesce(args);
    }
}
