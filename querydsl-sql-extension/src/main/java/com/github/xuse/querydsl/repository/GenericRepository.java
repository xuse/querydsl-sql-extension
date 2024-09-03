package com.github.xuse.querydsl.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.Resource;

import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.sql.RelationalPath;

/**
 * AbstractCrudRepository requires developers to implement the
 * {@code AbstractCrudRepository.getFactory()} and {@code AbstractCrudRepository.getPath()} methods. To further simplify
 * your code, you can extend this class.
 * <p>
 * AbstractCrudRepository 需要开发者实现{@code AbstractCrudRepository.getFactory()}和实现{@code AbstractCrudRepository.getPath()}两个方法。
 * 如希望进一步简化代码，可以继承此类。
 * 
 * @author Joey
 * @param <T>  the type of entity bean.
 * @param <ID> the type of ID
 */
public abstract class GenericRepository<T, ID> extends AbstractCrudRepository<T, ID> {
	@Resource
	protected SQLQueryFactory factory;
	
	private final RelationalPath<T> path;
	
	public GenericRepository() {
		path = initPath();
	}

	@SuppressWarnings("unchecked")
	private RelationalPath<T> initPath() {
		Class<?> c=this.getClass();
		while (c != Object.class) {
			Type s = this.getClass().getGenericSuperclass();
			if (s instanceof ParameterizedType) {
				ParameterizedType p = (ParameterizedType) s;
				if (p.getRawType() == GenericRepository.class) {
					Type gT = p.getActualTypeArguments()[0];
					if (gT instanceof Class) {
						return getPath((Class<T>) gT);
					}
				}
			}
			c = c.getSuperclass();
		}
		for (Type t : this.getClass().getGenericInterfaces()) {
			if(!(t instanceof ParameterizedType)) {
				continue;
			}
			ParameterizedType p = (ParameterizedType) t;
			if (p.getRawType() != CRUDRepository.class) {
				continue;
			}
			Type gT = p.getActualTypeArguments()[0];
			if (!(gT instanceof Class) ){
				continue;
			}
			Class<T> entity = (Class<T>) gT;
			return getPath(entity);
		}
		throw Exceptions.illegalArgument("No relational path found in {}", getClass().getName());
	}

	@SuppressWarnings("unchecked")
	private RelationalPath<T> getPath(Class<T> entity) {
		String name = entity.getName();
		int index = name.lastIndexOf('.');
		String qClassName = name.substring(0, index + 1) + "Q" + name.substring(index + 1);
		Class<?> clz;
		try {
			clz = entity.getClassLoader().loadClass(qClassName);
		} catch (ClassNotFoundException e) {
			return PathCache.get(entity, null);
		}
		for (Field field : clz.getDeclaredFields()) {
			if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType() == clz) {
				try {
					return (RelationalPath<T>) field.get(null);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw Exceptions.toRuntime(e);
				}
			}
		}
		throw Exceptions.illegalArgument("No relational path found in {}", clz.getName());
	}

	@Override
	protected RelationalPath<T> getPath() {
		return path;
	}

	@Override
	protected SQLQueryFactory getFactory() {
		return factory;
	}
}
