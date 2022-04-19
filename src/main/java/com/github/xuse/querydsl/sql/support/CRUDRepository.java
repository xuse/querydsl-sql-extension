package com.github.xuse.querydsl.sql.support;

import java.util.List;
import java.util.function.Consumer;

import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.support.Where.WhereBuilder;

/**
 * 基于QueryDSL封装的通用CRUD仓库
 * @author jiyi
 *
 * @param <T>
 * @param <ID>
 */
public interface CRUDRepository<T, ID> {

	T load(ID key);

	List<T> findByExample(T t);
	
	List<T> find(Consumer<SQLQueryAlter<T>> consumer);
	
	T load(Where<T> where);

	ID insert(T t);
	
	int insertBatch(List<T> ts);

	int delete(ID key);
	
	int delete(Consumer<SQLDeleteClauseAlter> consumer);

	int deleteByExample(T t);

	int update(ID key, T t);

	int update(Consumer<SQLUpdateClauseAlter> consumer);

	int count(Consumer<SQLQueryAlter<T>> consumer);
	
	int count(Where<T> where);
	
	WhereBuilder<T> query();
}
