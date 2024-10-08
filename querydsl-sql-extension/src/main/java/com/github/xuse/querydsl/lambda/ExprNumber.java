package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

public interface ExprNumber<T extends Number & Comparable<T>> extends ExprComparable<T>{
	NumberExpression<T> mixinNumber();

	/**
	 * Create a cast to String expression
	 *
	 * @see java.lang.Object#toString()
	 * @return string representation
	 */
	default StringExpression stringValue() {
		return mixinNumber().stringValue();
	}

	/**
	 * Create a {@code abs(this)} expression
	 *
	 * <p>
	 * Returns the absolute value of this expression
	 * </p>
	 *
	 * @return abs(this)
	 */
	default NumberExpression<T> abs() {
		return mixinNumber().abs();
	}

	/**
	 * Create a {@code this + right} expression
	 *
	 * <p>
	 * Returns the sum of this and right
	 * </p>
	 *
	 * @param right rhs of expression
	 * @return this + right
	 */
	default <N extends Number & Comparable<?>> NumberExpression<T> add(Expression<N> right) {
		return mixinNumber().add(right);
	}

	/**
	 * Create a {@code this + right} expression
	 *
	 * <p>
	 * Get the sum of this and right
	 * </p>
	 *
	 * @param right rhs of expression
	 * @return this + right
	 */
	default <N extends Number & Comparable<N>> NumberExpression<T> add(N right) {
		return mixinNumber().add(right);
	}

	/**
	 * Create a {@code avg(this)} expression
	 *
	 * <p>
	 * Get the average value of this expression (aggregation)
	 * </p>
	 *
	 * @return avg(this)
	 */
	default NumberExpression<Double> avg() {
		return mixinNumber().avg();
	}

	/**
	 * Create a {@code cast(this as byte)} expression
	 *
	 * <p>
	 * Get the byte expression of this numeric expression
	 * </p>
	 *
	 * @return this.byteValue()
	 * @see java.lang.Number#byteValue()
	 */
	default NumberExpression<Byte> byteValue() {
		return mixinNumber().byteValue();
	}

	default <A extends Number & Comparable<? super A>> NumberExpression<A> castToNum(Class<A> type) {
		return mixinNumber().castToNum(type);
	}

	/**
	 * Create a {@code ceil(this)} expression
	 *
	 * <p>
	 * Returns the smallest (closest to negative infinity) {@code double} value that
	 * is greater than or equal to the argument and is equal to a mathematical
	 * integer
	 * </p>
	 *
	 * @return ceil(this)
	 * @see java.lang.Math#ceil(double)
	 */
	default NumberExpression<T> ceil() {
		return mixinNumber().ceil();
	}

	/**
	 * Create a {@code this / right} expression
	 *
	 * <p>
	 * Get the result of the operation this / right
	 * </p>
	 *
	 * @param right
	 * @return this / right
	 */
	default <N extends Number & Comparable<?>> NumberExpression<T> divide(Expression<N> right) {
		return mixinNumber().divide(right);
	}

	/**
	 * Create a {@code this / right} expression
	 *
	 * <p>
	 * Get the result of the operation this / right
	 * </p>
	 *
	 * @param right
	 * @return this / right
	 */
	default <N extends Number & Comparable<?>> NumberExpression<T> divide(N right) {
		return mixinNumber().divide(right);
	}

	/**
	 * Create a {@code cast(this as double)} expression
	 *
	 * <p>
	 * Get the double expression of this numeric expression
	 * </p>
	 *
	 * @return this.doubleValue()
	 * @see java.lang.Number#doubleValue()
	 */
	default NumberExpression<Double> doubleValue() {
		return castToNum(Double.class);
	}

	/**
	 * Create a {@code cast(this as double)} expression
	 *
	 * <p>
	 * Get the float expression of this numeric expression
	 * </p>
	 *
	 * @return this.floatValue()
	 * @see java.lang.Number#floatValue()
	 */
	default NumberExpression<Float> floatValue() {
		return castToNum(Float.class);
	}

	/**
	 * Create a {@code floor(this)} expression
	 *
	 * <p>
	 * Returns the largest (closest to positive infinity) {@code double} value that
	 * is less than or equal to the argument and is equal to a mathematical integer.
	 * </p>
	 *
	 * @return floor(this)
	 * @see java.lang.Math#floor(double)
	 */
	default NumberExpression<T> floor() {
		return mixinNumber().floor();
	}

	/**
	 * Create a {@code this > right} expression
	 *
	 * @param <A>
	 * @param right rhs of the comparison
	 * @return {@code this > right}
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	default <A extends Number & Comparable<?>> BooleanExpression gt(A right) {
		return mixinNumber().gt(right);
	}

	/**
	 * Create a {@code this.intValue()} expression
	 *
	 * <p>
	 * Get the int expression of this numeric expression
	 * </p>
	 *
	 * @return this.intValue()
	 * @see java.lang.Number#intValue()
	 */
	default NumberExpression<Integer> intValue() {
		return castToNum(Integer.class);
	}

	/**
	 * Create a {@code this like str} expression
	 *
	 * @param str rhs
	 * @return this like str
	 */
	default BooleanExpression like(String str) {
		return Expressions.booleanOperation(Ops.LIKE, stringValue(), ConstantImpl.create(str));
	}

	/**
	 * Create a {@code this like str} expression
	 *
	 * @param str
	 * @return this like str
	 */
	default BooleanExpression like(Expression<String> str) {
		return Expressions.booleanOperation(Ops.LIKE, stringValue(), str);
	}

	/**
	 * Create a {@code this.longValue()} expression
	 *
	 * <p>
	 * Get the long expression of this numeric expression
	 * </p>
	 *
	 * @return this.longValue()
	 * @see java.lang.Number#longValue()
	 */
	default NumberExpression<Long> longValue() {
		return castToNum(Long.class);
	}

	/**
	 * Create a {@code mod(this, num)} expression
	 *
	 * @param num
	 * @return mod(this, num)
	 */
	default NumberExpression<T> mod(Expression<T> num) {
		return mixinNumber().mod(num);
	}

	/**
	 * Create a {@code mod(this, num)} expression
	 *
	 * @param num
	 * @return mod(this, num)
	 */
	default NumberExpression<T> mod(T num) {
		return mixinNumber().mod(num);
	}

	/**
	 * Create a {@code this * right} expression
	 *
	 * <p>
	 * Get the result of the operation this * right
	 * </p>
	 *
	 * @param right
	 * @return this * right
	 */
	default <N extends Number & Comparable<?>> NumberExpression<T> multiply(Expression<N> right) {
		return mixinNumber().multiply(right);
	}

	/**
	 * Create a {@code this * right} expression
	 *
	 * <p>
	 * Get the result of the operation this * right
	 * </p>
	 *
	 * @param right
	 * @return this * right
	 */
	default <N extends Number & Comparable<N>> NumberExpression<T> multiply(N right) {
		return mixinNumber().multiply(right);
	}

	/**
	 * Create a {@code this * -1} expression
	 *
	 * <p>
	 * Get the negation of this expression
	 * </p>
	 *
	 * @return this * -1
	 */
	default NumberExpression<T> negate() {
		return mixinNumber().negate();
	}

	/**
	 * Create a {@code round(this)} expression
	 *
	 * <p>
	 * Returns the closest {@code int} to this.
	 * </p>
	 *
	 * @return round(this)
	 * @see java.lang.Math#round(double)
	 * @see java.lang.Math#round(float)
	 */
	default NumberExpression<T> round() {
		return mixinNumber().round();
	}

	/**
	 * Create a {@code this.shortValue()} expression
	 *
	 * <p>
	 * Get the short expression of this numeric expression
	 * </p>
	 *
	 * @return this.shortValue()
	 * @see java.lang.Number#shortValue()
	 */
	default NumberExpression<Short> shortValue() {
		return castToNum(Short.class);
	}

	/**
	 * Create a {@code sqrt(this)} expression
	 *
	 * <p>
	 * Get the square root of this numeric expressions
	 * </p>
	 *
	 * @return sqrt(this)
	 */
	default NumberExpression<Double> sqrt() {
		return mixinNumber().sqrt();
	}

	/**
	 * Create a {@code this - right} expression
	 *
	 * <p>
	 * Get the difference of this and right
	 * </p>
	 *
	 * @param right
	 * @return this - right
	 */
	default <N extends Number & Comparable<?>> NumberExpression<T> subtract(Expression<N> right) {
		return mixinNumber().subtract(right);
	}

	/**
	 * Create a {@code this - right} expression
	 *
	 * <p>
	 * Get the difference of this and right
	 * </p>
	 *
	 * @param right
	 * @return this - right
	 */
	default <N extends Number & Comparable<?>> NumberExpression<T> subtract(N right) {
		return mixinNumber().subtract(right);
	}

	/**
	 * Create a {@code sum(this)} expression
	 *
	 * <p>
	 * Get the sum of this expression (aggregation)
	 * </p>
	 *
	 * @return sum(this)
	 */
	default NumberExpression<T> sum() {
		return mixinNumber().sum();
	}
}
