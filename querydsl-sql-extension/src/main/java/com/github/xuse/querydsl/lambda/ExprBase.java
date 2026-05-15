package com.github.xuse.querydsl.lambda;

import java.util.Collection;

import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseForEqBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * Base expression interface providing common operations that do not require
 * {@code Comparable} constraint. This allows temporal types like
 * {@code LocalDateTime} (which implements {@code Comparable<ChronoLocalDateTime<?>>}
 * rather than {@code Comparable<LocalDateTime>}) to use these operations.
 *
 * @author Joey
 * @param <T> the type of the expression
 */
@SuppressWarnings("unchecked")
public interface ExprBase<T> extends Expression<T> {

	SimpleExpression<T> mixinBase();

	/**
	 * Create an alias for the expression
	 * @param alias alias
	 * @return alias expression
	 */
	default SimpleExpression<T> as(Path<T> alias) {
		return mixinBase().as(alias);
	}

	/**
	 * Create an alias for the expression
	 * @param alias alias
	 * @return alias expression
	 */
	default SimpleExpression<T> as(String alias) {
		return mixinBase().as(alias);
	}

	/**
	 * Create a {@code this is not null} expression
	 *
	 * @return this is not null
	 */
	default BooleanExpression isNotNull() {
		return mixinBase().isNotNull();
	}

	/**
	 * Create a {@code this is null} expression
	 *
	 * @return this is null
	 */
	default BooleanExpression isNull() {
		return mixinBase().isNull();
	}

	/**
	 * Get the {@code count(this)} expression
	 *
	 * @return count(this)
	 */
	default NumberExpression<Long> count() {
		return mixinBase().count();
	}

	/**
	 * Get the {@code count(distinct this)} expression
	 *
	 * @return count(distinct this)
	 */
	default NumberExpression<Long> countDistinct() {
		return mixinBase().countDistinct();
	}

	/**
	 * Create a {@code this == right} expression
	 *
	 * <p>Use expr.isNull() instead of expr.eq(null)</p>
	 *
	 * @param right rhs of the comparison
	 * @return this == right
	 */
	default BooleanExpression eq(T right) {
		return mixinBase().eq(right);
	}

	/**
	 * Create a {@code this == right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this == right
	 */
	default BooleanExpression eq(Expression<? super T> right) {
		return mixinBase().eq(right);
	}

	/**
	 * Create a {@code this == all right} expression
	 *
	 * @param right right
	 * @return this == all right
	 */
	default BooleanExpression eqAll(CollectionExpression<?, ? super T> right) {
		return mixinBase().eqAll(right);
	}

	/**
	 * Create a {@code this == any right} expression
	 *
	 * @param right right
	 * @return this == any right
	 */
	default BooleanExpression eqAny(CollectionExpression<?, ? super T> right) {
		return mixinBase().eqAny(right);
	}

	/**
	 * Create a {@code this == all right} expression
	 *
	 * @param right right
	 * @return this == all right
	 */
	default BooleanExpression eqAll(SubQueryExpression<? extends T> right) {
		return mixinBase().eqAll(right);
	}

	/**
	 * Create a {@code this == any right} expression
	 *
	 * @param right right
	 * @return this == any right
	 */
	default BooleanExpression eqAny(SubQueryExpression<? extends T> right) {
		return mixinBase().eqAny(right);
	}

	/**
	 * Create a {@code this in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this in right
	 */
	default BooleanExpression in(Collection<? extends T> right) {
		return mixinBase().in(right);
	}

	/**
	 * Create a {@code this in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this in right
	 */
	default BooleanExpression in(T... right) {
		return mixinBase().in(right);
	}

	/**
	 * Create a {@code this in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this in right
	 */
	default BooleanExpression in(CollectionExpression<?, ? extends T> right) {
		return mixinBase().in(right);
	}

	/**
	 * Create a {@code this in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this in right
	 */
	default BooleanExpression in(SubQueryExpression<? extends T> right) {
		return mixinBase().in(right);
	}

	/**
	 * Create a {@code this in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this in right
	 */
	default BooleanExpression in(Expression<? extends T>... right) {
		return mixinBase().in(right);
	}

	/**
	 * Create a {@code this <> right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this != right
	 */
	default BooleanExpression ne(T right) {
		return mixinBase().ne(right);
	}

	/**
	 * Create a {@code this <> right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this != right
	 */
	default BooleanExpression ne(Expression<? super T> right) {
		return mixinBase().ne(right);
	}

	/**
	 * Create a {@code this != all right} expression
	 *
	 * @param right right
	 * @return this != all right
	 */
	default BooleanExpression neAll(CollectionExpression<?, ? super T> right) {
		return mixinBase().neAll(right);
	}

	/**
	 * Create a {@code this != any right} expression
	 *
	 * @param right right
	 * @return this != any right
	 */
	default BooleanExpression neAny(CollectionExpression<?, ? super T> right) {
		return mixinBase().neAny(right);
	}

	/**
	 * Create a {@code this not in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this not in right
	 */
	default BooleanExpression notIn(Collection<? extends T> right) {
		return mixinBase().notIn(right);
	}

	/**
	 * Create a {@code this not in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this not in right
	 */
	default BooleanExpression notIn(T... right) {
		return mixinBase().notIn(right);
	}

	/**
	 * Create a {@code this not in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this not in right
	 */
	default BooleanExpression notIn(CollectionExpression<?, ? extends T> right) {
		return mixinBase().notIn(right);
	}

	/**
	 * Create a {@code this not in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this not in right
	 */
	default BooleanExpression notIn(SubQueryExpression<? extends T> right) {
		return mixinBase().notIn(right);
	}

	/**
	 * Create a {@code this not in right} expression
	 *
	 * @param right rhs of the comparison
	 * @return this not in right
	 */
	default BooleanExpression notIn(Expression<? extends T>... right) {
		return mixinBase().notIn(right);
	}

	/**
	 * Create a case expression builder
	 *
	 * @param other other
	 * @return case expression builder
	 */
	default CaseForEqBuilder<T> when(T other) {
		return mixinBase().when(other);
	}

	/**
	 * Create a case expression builder
	 *
	 * @param other other
	 * @return case expression builder
	 */
	default CaseForEqBuilder<T> when(Expression<? extends T> other) {
		return mixinBase().when(other);
	}
}
