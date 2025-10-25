package com.github.xuse.querydsl.util.function;

import java.sql.SQLException;

/**
 * Like java.util.function.Function, but can throw SQLException;
 * @param <T> type of target.
 * @param <R> type of the result.
 */
@FunctionalInterface
public interface QueryFunction<T, R> {

	/**
	 * Applies this function to the given argument.
	 * @param t the function argument
	 * @return the function result
	 * @throws SQLException If encounter SQLException
	 */
	R apply(T t) throws SQLException;
}
