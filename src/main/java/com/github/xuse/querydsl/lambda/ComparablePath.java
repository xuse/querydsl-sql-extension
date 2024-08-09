package com.github.xuse.querydsl.lambda;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseForEqBuilder;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.LiteralExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * To provide features as {@link LiteralExpression}.
 * @author Joey
 * @param <T> the type of path.
 */
@SuppressWarnings("unchecked")
public interface ComparablePath<T extends Comparable<T>> extends Expression<T> {
	ComparableExpression<T> mixin();
	
    /**
     * Create an OrderSpecifier for ascending order of this expression
     *
     * @return ascending order by this
     */
    default OrderSpecifier<T> asc() {
        return mixin().asc();
    }

    /**
     * Create a {@code coalesce(this, expr)} expression
     *
     * @param expr additional argument
     * @return coalesce
     */
    default ComparableExpressionBase<T> coalesce(Expression<T> expr) {
        return mixin().coalesce(expr);
    }

    /**
     * Create a {@code coalesce(this, exprs...)} expression
     *
     * @param exprs additional arguments
     * @return coalesce
     */
    default ComparableExpressionBase<T> coalesce(Expression<?>...exprs) {
    	return mixin().coalesce(exprs);
    }

    /**
     * Create a {@code coalesce(this, arg)} expression
     *
     * @param arg additional argument
     * @return coalesce
     */
    default ComparableExpressionBase<T> coalesce(T arg) {
    	return mixin().coalesce(arg);
    }

    /**
     * Create a {@code coalesce(this, args...)} expression
     *
     * @param args additional arguments
     * @return coalesce
     */
    default ComparableExpressionBase<T> coalesce(T... args) {
    	return mixin().coalesce(args);
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other other
     * @return nullif(this, other)
     */
    default ComparableExpression<T> nullif(T other) {
        return mixin().nullif(other);
    }

    /**
     * Create an OrderSpecifier for descending order of this expression
     *
     * @return descending order by this
     */
    default OrderSpecifier<T> desc() {
        return mixin().desc();
    }

    /**
     * Create an alias for the expression
     * @param alias alias
     * @return alias expression
     */
    default SimpleExpression<T> as(Path<T> alias) {
        return mixin().as(alias);
    }

    /**
     * Create an alias for the expression
     *@param alias alias
     * @return alias expression
     */
    default SimpleExpression<T> as(String alias) {
        return mixin().as(alias);
    }

    /**
     * Create a {@code this is not null} expression
     *
     * @return this is not null
     */
    default BooleanExpression isNotNull() {
        return mixin().isNotNull();
    }

    /**
     * Create a {@code this is null} expression
     *
     * @return this is null
     */
    default BooleanExpression isNull() {
    	return mixin().isNull();
    }

    /**
     * Get the {@code count(this)} expression
     *
     * @return count(this)
     */
    default NumberExpression<Long> count() {
        return mixin().count();
    }


    /**
     * Get the {@code count(distinct this)} expression
     *
     * @return count(distinct this)
     */
    default NumberExpression<Long> countDistinct() {
    	return mixin().countDistinct();
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
        return mixin().eq(right);
    }

    /**
     * Create a {@code this == right} expression
     *
     * @param right rhs of the comparison
     * @return this == right
     */
    default BooleanExpression eq(Expression<? super T> right) {
        return mixin().eq(right);
    }

    /**
     * Create a {@code this == all right} expression
     *
     * @param right right
     * @return this == all right
     */
    default BooleanExpression eqAll(CollectionExpression<?, ? super T> right) {
    	return mixin().eqAll(right);
    }

    /**
     * Create a {@code this == < right} expression
     *
     * @param right right
     * @return this == any right
     */
    default BooleanExpression eqAny(CollectionExpression<?, ? super T> right) {
    	return mixin().eqAny(right);
    }

    /**
     * Create a {@code this == all right} expression
     *
     * @param right right
     * @return this == all right
     */
    default BooleanExpression eqAll(SubQueryExpression<? extends T> right) {
    	return mixin().eqAll(right);
    }

    /**
     * Create a {@code this == any right} expression
     *
     * @param right right
     * @return this == any right
     */
    default BooleanExpression eqAny(SubQueryExpression<? extends T> right) {
    	return mixin().eqAny(right);
    }


    /**
     * Create a {@code this in right} expression
     *
     * @param right rhs of the comparison
     * @return this in right
     */
    default BooleanExpression in(Collection<? extends T> right) {
    	return mixin().in(right); 
    }

    /**
     * Create a {@code this in right} expression
     *
     * @param right rhs of the comparison
     * @return this in right
     */
    default BooleanExpression in(T... right) {
    	return mixin().in(right);
    }

    /**
     * Create a {@code this in right} expression
     *
     * @param right rhs of the comparison
     * @return this in right
     */
    default BooleanExpression in(CollectionExpression<?,? extends T> right) {
        return mixin().in(right);
    }

    /**
     * Create a {@code this in right} expression
     *
     * @param right rhs of the comparison
     * @return this in right
     */
    default BooleanExpression in(SubQueryExpression<? extends T> right) {
        return mixin().in(right);
    }

    /**
     * Create a {@code this in right} expression
     *
     * @param right rhs of the comparison
     * @return this in right
     */
    default BooleanExpression in(Expression<? extends T>... right) {
        return mixin().in(right);
    }

    /**
     * Create a {@code this <> right} expression
     *
     * @param right rhs of the comparison
     * @return this != right
     */
    default BooleanExpression ne(T right) {
        return mixin().ne(right);
    }

    /**
     * Create a {@code this <> right} expression
     *
     * @param right rhs of the comparison
     * @return this != right
     */
    default BooleanExpression ne(Expression<? super T> right) {
        return mixin().ne(right);
    }

    /**
     * Create a {@code this != all right} expression
     *
     * @param right right
     * @return this != all right
     */
    default BooleanExpression neAll(CollectionExpression<?, ? super T> right) {
        return mixin().neAll(right);
    }

    /**
     * Create a {@code this != any right} expression
     *
     * @param right right
     * @return this != any right
     */
    default BooleanExpression neAny(CollectionExpression<?, ? super T> right) {
        return mixin().neAny(right);
    }

    /**
     * Create a {@code this not in right} expression
     *
     * @param right rhs of the comparison
     * @return this not in right
     */
    default BooleanExpression notIn(Collection<? extends T> right) {
    	return mixin().notIn(right);
    }

    /**
     * Create a {@code this not in right} expression
     *
     * @param right rhs of the comparison
     * @return this not in right
     */
	default BooleanExpression notIn(T... right) {
    	return mixin().notIn(right);
    }

    /**
     * Create a {@code this not in right} expression
     *
     * @param right rhs of the comparison
     * @return this not in right
     */
    default BooleanExpression notIn(CollectionExpression<?,? extends T> right) {
    	return mixin().notIn(right);
    }

    /**
     * Create a {@code this not in right} expression
     *
     * @param right rhs of the comparison
     * @return this not in right
     */
    default BooleanExpression notIn(SubQueryExpression<? extends T> right) {
        return mixin().notIn(right);
    }

    /**
     * Create a {@code this not in right} expression
     *
     * @param right rhs of the comparison
     * @return this not in right
     */
    default BooleanExpression notIn(Expression<? extends T>... right) {
        return mixin().notIn(right);
    }

    /**
     * Create a {@code nullif(this, other)} expression
     * @param other other
     * @return nullif(this, other)
     */
    default ComparableExpression<T> nullif(Expression<T> other) {
    	 return mixin().nullif(other);
    }
    
    /**
     * Create a case expression builder
     *
     * @param other other
     * @return case expression builder
     */
    default CaseForEqBuilder<T> when(T other) {
        return mixin().when(other); 
    }

    /**
     * Create a case expression builder
     *
     * @param other other
     * @return case expression builder
     */
    default CaseForEqBuilder<T> when(Expression<? extends T> other) {
        return mixin().when(other);
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
    default BooleanExpression between(T from, T to) {
    	return mixin().between(from, to);
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
    default BooleanExpression between(@Nullable Expression<T> from, @Nullable Expression<T> to) {
    	return mixin().between(from, to);
    }

    /**
     * Create a {@code this not between from and to} expression
     *
     * <p>Is equivalent to {@code this < from || this > to}</p>
     *
     * @param from inclusive start of range
     * @param to inclusive end of range
     * @return this not between from and to
     */
    default BooleanExpression notBetween(T from, T to) {
        return mixin().between(from, to).not();
    }

    /**
     * Create a {@code this not between from and to} expression
     *
     * <p>Is equivalent to {@code this < from || this > to}</p>
     *
     * @param from inclusive start of range
     * @param to inclusive end of range
     * @return this not between from and to
     */
    default BooleanExpression notBetween(Expression<T> from, Expression<T> to) {
        return mixin().between(from, to).not();
    }

    /**
     * Create a {@code this > right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression gt(T right) {
        return mixin().gt(right);
    }

    /**
     * Create a {@code this > right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression gt(Expression<T> right) {
    	 return mixin().gt(right);
    }

    /**
     * Create a {@code this > all right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; all right
     */
    default BooleanExpression gtAll(CollectionExpression<?, ? super T> right) {
    	 return mixin().gtAll(right);
    }

    /**
     * Create a {@code this > any right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; any right
     */
    default BooleanExpression gtAny(CollectionExpression<?, ? super T> right) {
    	 return mixin().gtAny(right);
    }

    /**
     * Create a {@code this > all right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; all right
     */
    default BooleanExpression gtAll(SubQueryExpression<? extends T> right) {
    	return mixin().gtAll(right);
    }

    /**
     * Create a {@code this > any right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt; any right
     */
    default BooleanExpression gtAny(SubQueryExpression<? extends T> right) {
    	 return mixin().gtAny(right);
    }

    /**
     * Create a {@code this >= right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt;= right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression goe(T right) {
    	 return mixin().goe(right);
    }

    /**
     * Create a {@code this >= right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt;= right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression goe(Expression<T> right) {
    	return mixin().goe(right);
    }

    /**
     * Create a {@code this >= all right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt;= all right
     */
    default BooleanExpression goeAll(CollectionExpression<?, ? super T> right) {
    	return mixin().goeAll(right);
    }

    /**
     * Create a {@code this >= any right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt;= any right
     */
    default BooleanExpression goeAny(CollectionExpression<?, ? super T> right) {
    	return mixin().goeAny(right);
    }

    /**
     * Create a {@code this >= all right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt;= all right
     */
    default BooleanExpression goeAll(SubQueryExpression<? extends T> right) {
    	return mixin().goeAll(right);
    }

    /**
     * Create a {@code this >= any right} expression
     *
     * @param right rhs of the comparison
     * @return this &gt;= any right
     */
    default BooleanExpression goeAny(SubQueryExpression<? extends T> right) {
    	return mixin().goeAny(right);
    }

    /**
     * Create a {@code this < right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression lt(T right) {
        return lt(ConstantImpl.create(right));
    }

    /**
     * Create a {@code this < right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression lt(Expression<T> right) {
    	return mixin().lt(right);
    }

    /**
     * Create a {@code this < all right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; all right
     */
    default BooleanExpression ltAll(CollectionExpression<?, ? super T> right) {
    	return mixin().ltAll(right);
    }

    /**
     * Create a {@code this < any right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; any right
     */
    default BooleanExpression ltAny(CollectionExpression<?, ? super T> right) {
    	return mixin().ltAny(right);
    }

    /**
     * Create a {@code this < all right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; all right
     */
    default BooleanExpression ltAll(SubQueryExpression<? extends T> right) {
    	return mixin().ltAll(right);
    }

    /**
     * Create a {@code this < any right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt; any right
     */
    default BooleanExpression ltAny(SubQueryExpression<? extends T> right) {
    	return mixin().ltAny(right);
    }

    /**
     * Create a {@code this <= right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt;= right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression loe(T right) {
    	return mixin().loe(right);
    }

    /**
     * Create a {@code this <= right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt;= right
     * @see java.lang.Comparable#compareTo(Object)
     */
    default BooleanExpression loe(Expression<T> right) {
    	return mixin().loe(right);
    }

    /**
     * Create a {@code this <= all right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt;= all right
     */
    default BooleanExpression loeAll(CollectionExpression<?, ? super T> right) {
    	return mixin().loeAll(right);
    }

    /**
     * Create a {@code this <= any right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt;= any right
     */
    default BooleanExpression loeAny(CollectionExpression<?, ? super T> right) {
    	return mixin().loeAny(right);
    }

    /**
     * Create a {@code this <= all right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt;= all right
     */
    default BooleanExpression loeAll(SubQueryExpression<? extends T> right) {
    	return mixin().loeAll(right);
    }

    /**
     * Create a {@code this <= any right} expression
     *
     * @param right rhs of the comparison
     * @return this &lt;= any right
     */
    default BooleanExpression loeAny(SubQueryExpression<? extends T> right) {
    	return mixin().loeAny(right);
    }

    /**
     * Create a {@code min(this)} expression
     *
     * <p>Get the minimum value of this expression (aggregation)</p>
     *
     * @return min(this)
     */
    default ComparableExpression<T> min() {
        return mixin().min();
    }

    /**
     * Create a {@code max(this)} expression
     *
     * <p>Get the maximum value of this expression (aggregation)</p>
     *
     * @return max(this)
     */
    default ComparableExpression<T> max() {
    	return mixin().max();
    }
}