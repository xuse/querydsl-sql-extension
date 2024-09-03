package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface ExprTemporal<T extends Comparable<T>> extends ExprComparable<T>{
	 /**
     * Create a {@code this > right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; right
     */
    default BooleanExpression after(T right) {
        return gt(right);
    }

    /**
     * Create a {@code this > right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; right
     */
    default BooleanExpression after(Expression<T> right) {
        return gt(right);
    }

    /**
     * Create a {@code this < right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; right
     */
    default BooleanExpression before(T right) {
        return lt(right);
    }

    /**
     * Create a {@code this < right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; right
     */
    default BooleanExpression before(Expression<T> right) {
        return lt(right);
    }

}
