package com.github.xuse.querydsl.sql.ddl;

import com.querydsl.sql.RelationalPathBase;

/**
 * 用于进行数据库表生成和维护
 * 
 * @author jiyi
 *
 */
public interface SQLMetadataQueryFactory {

	/**
	 * 创建表
	 * 
	 * @param <T>
	 * @param t
	 */
	<T> CreateTableQuery createTable(RelationalPathBase<T> path);

	/**
	 * 删除表
	 * 
	 * @param <T>
	 * @param t
	 */
	<T> DropTableQuery dropTable(RelationalPathBase<T> path);

	/**
	 * 更新表
	 * 
	 * @param <T>
	 * @param t
	 */
	<T> AlterTableQuery alterTable(RelationalPathBase<T> path);

	/**
	 * 创建索引
	 * 
	 * @param t
	 * @return
	 */
	<T> CreateIndexQuery createIndex(RelationalPathBase<T> path);
	
	/**
	 * 删除索引
	 * @param <T>
	 * @param t
	 * @return
	 */
	<T> DropIndexQuery dropIndex(RelationalPathBase<T> path);
	
	
	/**
	 * 创建约束
	 * @param t
	 * @return
	 */
	<T> CreateConstraintQuery createContraint(RelationalPathBase<T> path);

	
	/**
	 * 删除约束 
	 * @param <T>
	 * @param t
	 * @return
	 */
	<T> DropConstraintQuery dropConstraint(RelationalPathBase<T> path);
}
