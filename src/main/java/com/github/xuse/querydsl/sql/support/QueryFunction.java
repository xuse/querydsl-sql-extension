package com.github.xuse.querydsl.sql.support;

import java.sql.SQLException;

/**
 * Like java.util.function.Function, but can throw SQLException;
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface QueryFunction<T,R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws SQLException
     */
	R apply(T t) throws SQLException;
}
