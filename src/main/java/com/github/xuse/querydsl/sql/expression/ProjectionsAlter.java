package com.github.xuse.querydsl.sql.expression;

import java.util.LinkedHashMap;
import java.util.Map;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.RelationalPath;

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
	

    public static <T> FactoryExpression<T> createProjection(RelationalPath<T> path) {
        if (path.getType().equals(path.getClass())) {
            throw new IllegalArgumentException("RelationalPath based projection can only be used with generated Bean types");
        }
        try {
            // ensure that empty constructor is available
            path.getType().getConstructor();
            return createBeanProjection(path);
        } catch (NoSuchMethodException e) {
            // fallback to constructor projection
            return createConstructorProjection(path);
        }
    }
    
    public static <T> QBeanEx<T> createBeanProjection(RelationalPath<T> path) {
        Map<String,Expression<?>> bindings = new LinkedHashMap<String,Expression<?>>();
        for (Path<?> column : path.getColumns()) {
            bindings.put(column.getMetadata().getName(), column);
        }
        if (bindings.isEmpty()) {
            throw new IllegalArgumentException("No bindings could be derived from " + path);
        }
        return new QBeanEx<T>((Class<? extends T>) path.getType(), bindings);
    }

    private static <T> FactoryExpression<T> createConstructorProjection(RelationalPath<T> path) {
        Expression<?>[] exprs = path.getColumns().toArray(new Expression[path.getColumns().size()]);
        return Projections.<T>constructor((Class<? extends T>) path.getType(), exprs);
    }

	private ProjectionsAlter() {
	}
}
