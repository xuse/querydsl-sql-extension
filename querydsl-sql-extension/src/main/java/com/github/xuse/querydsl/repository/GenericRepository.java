package com.github.xuse.querydsl.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.sql.RelationalPath;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public abstract class GenericRepository<T, ID> extends AbstractCrudRepository<T, ID> {
	@Resource
	protected SQLQueryFactory factory;
	
	protected final RelationalPath<T> path;
	
	public GenericRepository() {
		path = initPath();
	}

	@SuppressWarnings("unchecked")
	private RelationalPath<T> initPath() {
		Class<?> c= this.getClass();
		Map<TypeVariable<?>,Type> context=new LinkedHashMap<>();
		while (c != Object.class) {
			Type s = c.getGenericSuperclass();
			if (s instanceof ParameterizedType) {
				ParameterizedType p = (ParameterizedType) s;
				TypeVariable<?>[] vars = c.getSuperclass().getTypeParameters();
				Type[] typeArgs = p.getActualTypeArguments();
				//维护泛型变量上下文
				for (int i = 0; i < vars.length; i++) {
					TypeVariable<?> key = vars[i];
					Type value = typeArgs[i];
					if (value instanceof TypeVariable<?>) {
						value = typeArgs[i] = context.get((TypeVariable<?>) value);
						context.put(key, value);
					} else {
						context.put(key, value);
					}
				}
				if (p.getRawType() == GenericRepository.class) {
					Type gT = typeArgs[0];
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
			log.warn("Query Class not found {}, will generate a dynanamic model.", qClassName);
			return PathCache.get(entity, null);
		}
		RelationalPath<?> result= SQLTypeUtils.getMetaModel(clz);
		if(result==null) {
			throw Exceptions.illegalArgument("No relational path found in {}", clz.getName());
		}
		return (RelationalPath<T>) result;
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
