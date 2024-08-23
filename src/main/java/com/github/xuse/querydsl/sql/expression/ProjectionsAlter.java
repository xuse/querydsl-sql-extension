package com.github.xuse.querydsl.sql.expression;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.group.QPair;
import com.querydsl.core.types.ArrayConstructorExpression;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QList;
import com.querydsl.sql.Beans;
import com.querydsl.sql.RelationalPath;

public class ProjectionsAlter {
	
	@SuppressWarnings("unchecked")
	public static <T> QBeanEx<T> bean(Class<? extends T> type, RelationalPath<?> beanPath) {
		if (type == beanPath.getType()) {
			 Expression<?> expr=beanPath.getProjection();
			if(expr instanceof QBeanEx) {
				return (QBeanEx<T>)expr;
			}
		}
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		for (Path<?> p : beanPath.getColumns()) {
			bindings.put(p.getMetadata().getName(), p);
		}
		return new QBeanEx<T>(type, bindings);
	}

	/**
	 *  Create a Bean populating projection for the given type and expressions
	 *  <p>
	 *  Example
	 *  </p>
	 *
	 *  <pre>
	 *  UserDTO dto = query.select(Projections.bean(UserDTO.class, user.firstName, user.lastName));
	 *  </pre>
	 *
	 *  @param <T>   type of projection
	 *  @param type  type of the projection
	 *  @param exprs arguments for the projection
	 *  @return factory expression
	 */
	public static <T> QBeanEx<T> bean(Class<? extends T> type, Expression<?>... exprs) {
		return new QBeanEx<T>(type, exprs);
	}

	/**
	 *  Create a Bean populating projection for the given type and expressions
	 *
	 *  @param <T>   type of projection
	 *  @param type  type of the projection
	 *  @param exprs arguments for the projection
	 *  @return factory expression
	 */
	public static <T> QBeanEx<T> bean(Path<? extends T> type, Expression<?>... exprs) {
		return new QBeanEx<T>(type.getType(), exprs);
	}

	/**
	 *  Create a Bean populating projection for the given type and bindings
	 *
	 *  @param <T>      type of projection
	 *  @param type     type of the projection
	 *  @param bindings property bindings
	 *  @return factory expression
	 */
	public static <T> QBeanEx<T> bean(Path<? extends T> type, Map<String, ? extends Expression<?>> bindings) {
		return new QBeanEx<T>(type.getType(), bindings);
	}

	/**
	 *  Create a Bean populating projection for the given type and bindings
	 *
	 *  @param <T>      type of projection
	 *  @param type     type of the projection
	 *  @param bindings property bindings
	 *  @return factory expression
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
		Map<String, Expression<?>> bindings = new LinkedHashMap<String, Expression<?>>();
		for (Path<?> column : path.getColumns()) {
			bindings.put(column.getMetadata().getName(), column);
		}
		if (bindings.isEmpty()) {
			throw new IllegalArgumentException("No bindings could be derived from " + path);
		}
		return new QBeanEx<T>(path.getType(), bindings);
	}

	private static <T> FactoryExpression<T> createConstructorProjection(RelationalPath<T> path) {
		Expression<?>[] exprs = path.getColumns().toArray(new Expression[path.getColumns().size()]);
		return Projections.constructor(path.getType(), exprs);
	}

	protected ProjectionsAlter() {
	}

	/**
	 * Create a constructor invocation projection for the given type and expressions
	 *
	 * <p>Example</p>
	 * <pre>
	 * UserDTO dto = query.singleResult(
	 *     Projections.constructor(UserDTO.class, user.firstName, user.lastName));
	 * </pre>
	 *
	 * @param <T> type projection
	 * @param type type of the projection
	 * @param exprs arguments for the projection
	 * @return factory expression
	 */
	public static <T> ConstructorExpression<T> constructor(Class<? extends T> type, Expression<?>... exprs) {
		return Projections.constructor(type, exprs);
	}

	/**
	 * Create a constructor invocation projection for given type, parameter types and expressions
	 *
	 * @param type type of the projection
	 * @param paramTypes constructor parameter types
	 * @param exprs constructor parameters
	 * @param <T> type of projection
	 * @return factory expression
	 */
	public static <T> ConstructorExpression<T> constructor(Class<? extends T> type, Class<?>[] paramTypes, Expression<?>... exprs) {
		return Projections.constructor(type, paramTypes, exprs);
	}

	/**
	 * Create a constructor invocation projection for given type, parameter types and expressions
	 *
	 * @param type type of the projection
	 * @param paramTypes constructor parameter types
	 * @param exprs constructor parameters
	 * @param <T> type of projection
	 * @return factory expression
	 */
	public static <T> ConstructorExpression<T> constructor(Class<? extends T> type, Class<?>[] paramTypes, List<Expression<?>> exprs) {
		return Projections.constructor(type, paramTypes, exprs);
	}
	
	 /**
     * Create a typed array projection for the given type and expressions
     *
     * @param <T> type of projection
     * @param type type of the projection
     * @param exprs arguments for the projection
     * @return factory expression
     */
    @SafeVarargs
	public static <T> ArrayConstructorExpression<T> array(Class<T[]> type, Expression<T>... exprs) {
        return new ArrayConstructorExpression<T>(type, exprs);
    }
    
	public static ArrayConstructorExpression<Object> array(Expression<?>... exprs) {
        return new ArrayConstructorExpression<>(exprs);
    }
    
    public static QStringObjMap stringMap(Expression<?>... exprs){
    	return new QStringObjMap(exprs);
    }

    public static QList list(Expression<?>... exprs){
    	return Projections.list(exprs);
    }
    
    public static FactoryExpressionBase<Beans> beans(RelationalPath<?>... tables){
    	return new QBeansContinuous(tables);
    }
    
    public static QBeansDiscontinuous.Builder beansBuilder(){
    	return QBeansDiscontinuous.builder();
    }
    
    public static QAliasBeansDiscontinuous.Builder aliasBeansBuilder(RelationalPath<?>... tables){
    	return QAliasBeansDiscontinuous.builder();
    }
    
    
    public static <K,V> QPair<K,V> pair(Expression<K> expr1,Expression<V> expr2){
    	return new QPair<>(expr1,expr2);
    }
}
