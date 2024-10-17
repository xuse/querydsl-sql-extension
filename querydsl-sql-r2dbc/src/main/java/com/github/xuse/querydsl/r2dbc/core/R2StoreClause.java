package com.github.xuse.querydsl.r2dbc.core;

import org.jetbrains.annotations.Nullable;

import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

public interface R2StoreClause<C extends R2StoreClause<C>> extends R2DMLClause<C> {

    /**
     * Add a value binding
     *
     * @param <T>
     * @param path path to be updated
     * @param value value to set
     * @return the current object
     */
    <T> C set(Path<T> path, @Nullable T value);

    /**
     * Add an expression binding
     *
     * @param <T>
     * @param path path to be updated
     * @param expression binding
     * @return the current object
     */
    <T> C set(Path<T> path, Expression<? extends T> expression);

    /**
     * Bind the given path to null
     *
     * @param path path to be updated
     * @return the current object
     */
    <T> C setNull(Path<T> path);

    /**
     * Returns true, if no bindings have been set, otherwise false.
     *
     * @return true, if empty, false, if not
     */
    boolean isEmpty();
}
