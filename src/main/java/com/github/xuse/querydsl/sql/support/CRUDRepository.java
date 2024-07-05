package com.github.xuse.querydsl.sql.support;

import java.util.List;
import java.util.function.Consumer;

import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.support.Where.WhereBuilder;
import com.querydsl.core.types.Path;

/**
 * 基于QueryDSL封装的通用CRUD仓库
 * @author Joey
 *
 * @param <T>
 * @param <ID> type of primary key 
 */
public interface CRUDRepository<T, ID> {
	/**
	 * load entity by primary key.
	 * @param key
	 * @return null if record is not exist.
	 */
	T load(ID key);
	
	/**
	 * Find records by a example bean.
	 * @param t
	 * @return list
	 */
	List<T> findByExample(T example);
	
	List<T> find(Consumer<SQLQueryAlter<T>> consumer);

	ID insert(T t);
	
	int insertBatch(List<T> ts);

	int delete(ID key);
	
	int delete(Consumer<SQLDeleteClauseAlter> consumer);

	int deleteByExample(T t);

	int update(ID key, T t);
	
	int updateByKeys(T obj, Path<?>... bizKeys);

	int update(Consumer<SQLUpdateClauseAlter> consumer);
	
	int count(Consumer<SQLQueryAlter<T>> consumer);
	
	int countByExample(T example);
	
	WhereBuilder<T> query();
}
