package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.TemporalExpression;

/**
 * Provides temporal comparison operations without requiring the strict
 * {@code T extends Comparable<T>} constraint. This allows types like
 * {@code java.time.LocalDateTime} (which implements
 * {@code Comparable<ChronoLocalDateTime<?>>} rather than
 * {@code Comparable<LocalDateTime>}) to be used with temporal lambda columns.
 *
 * @param <T> the temporal type
 */
public interface ExprTemporal<T extends Comparable<?>> extends ExprBase<T> {

	@SuppressWarnings("rawtypes")
	ComparableExpression mixin();

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	default SimpleExpression<T> mixinBase() {
		return (SimpleExpression) mixin();
	}

	/**
	 * Create an OrderSpecifier for ascending order of this expression
	 *
	 * @return ascending order by this
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default OrderSpecifier<T> asc() {
		return ((TemporalExpression) mixin()).asc();
	}

	/**
	 * Create an OrderSpecifier for descending order of this expression
	 *
	 * @return descending order by this
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default OrderSpecifier<T> desc() {
		return ((TemporalExpression) mixin()).desc();
	}

	/**
	 * Create a {@code this > right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &gt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression after(T right) {
		return ((ComparableExpression) mixin()).gt(ConstantImpl.create(right));
	}

	/**
	 * Create a {@code this > right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &gt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression after(Expression<T> right) {
		return ((ComparableExpression) mixin()).gt(right);
	}

	/**
	 * Create a {@code this < right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &lt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression before(T right) {
		return ((ComparableExpression) mixin()).lt(ConstantImpl.create(right));
	}

	/**
	 * Create a {@code this < right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &lt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression before(Expression<T> right) {
		return ((ComparableExpression) mixin()).lt(right);
	}

	/**
	 * Create a {@code this > right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &gt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression gt(T right) {
		return ((ComparableExpression) mixin()).gt(ConstantImpl.create(right));
	}

	/**
	 * Create a {@code this > right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &gt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression gt(Expression<T> right) {
		return ((ComparableExpression) mixin()).gt(right);
	}

	/**
	 * Create a {@code this >= right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &gt;= right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression goe(T right) {
		return ((ComparableExpression) mixin()).goe(ConstantImpl.create(right));
	}

	/**
	 * Create a {@code this >= right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &gt;= right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression goe(Expression<T> right) {
		return ((ComparableExpression) mixin()).goe(right);
	}

	/**
	 * Create a {@code this < right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &lt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression lt(T right) {
		return ((ComparableExpression) mixin()).lt(ConstantImpl.create(right));
	}

	/**
	 * Create a {@code this < right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &lt; right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression lt(Expression<T> right) {
		return ((ComparableExpression) mixin()).lt(right);
	}

	/**
	 * Create a {@code this <= right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &lt;= right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression loe(T right) {
		return ((ComparableExpression) mixin()).loe(ConstantImpl.create(right));
	}

	/**
	 * Create a {@code this <= right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this &lt;= right
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression loe(Expression<T> right) {
		return ((ComparableExpression) mixin()).loe(right);
	}

	/**
	 * Create a {@code this between from and to} expression
	 *
	 * <p>Is equivalent to {@code from <= this <= to}</p>
	 *
	 * @param from inclusive start of range
	 * @param to inclusive end of range
	 * @return this between from and to
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression between(T from, T to) {
		return ((ComparableExpression) mixin()).between(ConstantImpl.create(from), ConstantImpl.create(to));
	}

	/**
	 * Create a {@code this between from and to} expression
	 *
	 * <p>Is equivalent to {@code from <= this <= to}</p>
	 *
	 * @param from inclusive start of range
	 * @param to inclusive end of range
	 * @return this between from and to
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	default BooleanExpression between(Expression<T> from, Expression<T> to) {
		return ((ComparableExpression) mixin()).between(from, to);
	}

	/**
	 * Create a {@code this not between from and to} expression
	 *
	 * <p>Is equivalent to {@code this < from || this > to}</p>
	 *
	 * @param from inclusive start of range
	 * @param to inclusive end of range
	 * @return this not between from and to
	 */
	default BooleanExpression notBetween(T from, T to) {
		return between(from, to).not();
	}

	/**
	 * Create a {@code this not between from and to} expression
	 *
	 * <p>Is equivalent to {@code this < from || this > to}</p>
	 *
	 * @param from inclusive start of range
	 * @param to inclusive end of range
	 * @return this not between from and to
	 */
	default BooleanExpression notBetween(Expression<T> from, Expression<T> to) {
		return between(from, to).not();
	}
}
