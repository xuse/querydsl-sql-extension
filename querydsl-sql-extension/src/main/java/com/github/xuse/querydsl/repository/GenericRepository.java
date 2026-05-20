package com.github.xuse.querydsl.repository;

import javax.annotation.Resource;

import com.github.xuse.querydsl.sql.SQLQueryFactory;
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
	private SQLQueryFactory factory;
	
	protected final RelationalPath<T> path;
	
	public GenericRepository() {
		path = calcPathType();
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
