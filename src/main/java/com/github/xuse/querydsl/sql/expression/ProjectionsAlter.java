package com.github.xuse.querydsl.sql.expression;

import java.util.Map;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

public class ProjectionsAlter {
	/**
	 * Create a Bean populating projection for the given type and expressions
	 *
	 * <p>
	 * Example
	 * </p>
	 * 
	 * <pre>
	 * UserDTO dto = query.select(Projections.bean(UserDTO.class, user.firstName, user.lastName));
	 * </pre>
	 *
	 * @param <T>   type of projection
	 * @param type  type of the projection
	 * @param exprs arguments for the projection
	 * @return factory expression
	 */
	public static <T> QBeanEx<T> bean(Class<? extends T> type, Expression<?>... exprs) {
		return new QBeanEx<T>(type, exprs);
	}

	/**
	 * Create a Bean populating projection for the given type and expressions
	 *
	 * @param <T>   type of projection
	 * @param type  type of the projection
	 * @param exprs arguments for the projection
	 * @return factory expression
	 */
	public static <T> QBeanEx<T> bean(Path<? extends T> type, Expression<?>... exprs) {
		return new QBeanEx<T>(type.getType(), exprs);
	}

	/**
	 * Create a Bean populating projection for the given type and bindings
	 *
	 * @param <T>      type of projection
	 * @param type     type of the projection
	 * @param bindings property bindings
	 * @return factory expression
	 */
	public static <T> QBeanEx<T> bean(Path<? extends T> type, Map<String, ? extends Expression<?>> bindings) {
		return new QBeanEx<T>(type.getType(), bindings);
	}

	/**
	 * Create a Bean populating projection for the given type and bindings
	 *
	 * @param <T>      type of projection
	 * @param type     type of the projection
	 * @param bindings property bindings
	 * @return factory expression
	 */
	public static <T> QBeanEx<T> bean(Class<? extends T> type, Map<String, ? extends Expression<?>> bindings) {
		return new QBeanEx<T>(type, bindings);
	}
	

	private ProjectionsAlter() {
	}
}
