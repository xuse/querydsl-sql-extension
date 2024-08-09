package com.github.xuse.querydsl.repository;

import java.util.List;
import java.util.function.Consumer;

import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.Path;

/**
 * <h1>Chinese:</h1>
 * 基于QueryDSL封装的通用CRUD仓库。
 * 如果是复合主键，可以使用java.util.List、com.mysema.commons.lang.Pair中的任意一种容器来传递主键字段。但要注意其顺序必须和数据库主键定义的顺序一致。
 * <h1>English:</h1>
 * A general repository implementation for accessing entity.
 * @author Joey
 * @param <T>  type of target
 * @param <ID> type of primary key.
 *             If it is a composite primary key, you can use any one of the containers such as `java.util.List`  or
 *             `com.mysema.commons.lang.Pair` to pass the values.
 *             However, please note that their order must be consistent with the order defined in the database primary key.
 */
public interface CRUDRepository<T, ID> {

	/**
	 * <h2>Chinese:</h2>
	 * 按数据库主键获得实体
	 * <h2>English:</h2>
	 * load entity by primary key.
	 * 
	 * @param key 主键的值 / value of primary key
	 * @return null if record is not exist.
	 */
	T load(ID key);
	
	/**
	 * 
	 * <h2>Chinese:</h2>
	 * 按数据库主键获得实体
	 * <h2>English:</h2>
	 * load entity by primary keys.
	 * @param keys 主键列表 / values of primary key
	 * @return 批量获得记录
	 */
	List<T> loadBatch(List<ID> keys);

	/**
	 * <h2>Chinese:</h2>
	 * 按示例查询对象检索记录。
	 * 
	 * <h2>English:</h2>
	 * Find records by an example bean.
	 * @param example example bean.
	 * @return result list 
	 */
	List<T> findByExample(T example);

	/**
	 * <h2>Chinese:</h2>
	 * 根据用户自行填写的查询条件检索记录。
	 * <h2>English:</h2>
	 * Retrieve records based on query conditions filled in by the user.
	 * @param consumer  函数对象用于填入查询条件 / functional object to populate conditions.
	 * @return result list 
	 */
	List<T> find(Consumer<SQLQueryAlter<T>> consumer);

	
	List<T> find(QueryWrapper<T,?> wrapper);
	
	
	Pair<Integer,List<T>> findAndCount(QueryWrapper<T,?> wrapper);
	
	
	Pair<Integer,List<T>> findAndCount(QueryWrapper<T,?> wrapper, int limit, int offset);
	
	
	/**
	 * <h2>Chinese:</h2>
	 * 插入一条数据
	 * <h2>English:</h2>
	 * Insert one record.
	 * @param t 插入记录对象
	 * @return 自动生成的主键。
	 */
	ID insert(T t);

	/**
	 * <h2>Chinese:</h2>
	 * 批量插入数据
	 * <h2>English:</h2>
	 * @param ts 插入记录对象列表 / list of records.
	 * @return 写入记录数 / count of records inserted.
	 */
	int insertBatch(List<T> ts);

	/**
	 * <h2>Chinese:</h2>
	 * 按主键删除记录
	 * <h2>English:</h2>
	 * 
	 * @param key 主键值 / value of primary key 
	 * @return 删除记录数 / count of records deleted.
	 */
	int delete(ID key);
	
	
	/**
	 * 按传入的Wrapper条件删除记录
	 * @param wrapper 条件封装
	 * @return 删除记录数 / count of records deleted.
	 */
	int delete(QueryWrapper<T,?> wrapper);

	/**
	 * <h2>Chinese:</h2>
	 *  根据用户自行填写的查询条件删除记录
	 * <h2>English:</h2>
	 * @param consumer  函数对象用于填入条件 / functional object to populate conditions.
	 * @return 删除记录数 / count of records deleted. 
	 */
	int delete(Consumer<SQLDeleteClauseAlter> consumer);

	/**
	 * <h2>Chinese:</h2>
	 * 按示例对象删除记录。
	 * <h2>English:</h2>
	 * delete records by a example object.
	 * @param t 示例对象 / example object
	 * @return 删除记录数 / count of records deleted. 
	 */
	int deleteByExample(T t);

	/**
	 * <h2>Chinese:</h2>
	 * 按主键更新对象
	 * <h2>English:</h2>
	 * Update a record by primary key.
	 * 
	 * @param key 主键的值 / value of primary key
	 * @param t   object
	 * @return 更新记录数 / records affected.
	 */
	int update(ID key, T t);
	
	
	/**
	 * 按传入的对象更新记录
	 * @param t   object
	 * @param key 更新的条件封装
	 * @return 更新记录数 / records affected. 
	 */
	int update(T t,QueryWrapper<T,?> key);

	/**
	 * <h2>Chinese:</h2>
	 * 按指定的几个字段作为Where条件，更新该对象中其他的字段。
	 * <h2>English:</h2>
	 * Update a record. the column in where condition are assigned as the second
	 * parameter.
	 * 
	 * @param t       object
	 * @param bizKeys 用于where条件的字段 / the columns in where condition.
	 * @return 写入记录数 / records affected.
	 */
	int updateByKeys(T t, Path<?>... bizKeys);

	/**
	 * <h2>Chinese:</h2>
	 * 自行拼装条件进行数据库更新
	 * <h2>English:</h2>
	 * Assemble conditions independently to update the database.
	 * 
	 * @param consumer 函数对象，用于填充条件和更新字段 / Function object, used to populate conditions and update fields.
	 * @return 写入记录数 / records affected.
	 */
	int update(Consumer<SQLUpdateClauseAlter> consumer);

	/**
	 * <h2>Chinese:</h2>
	 * 自行拼装条件进行Count查询
	 * <h2>English:</h2>
	 * @param consumer 函数对象，用于填充条件 / Function object, used to populate conditions.
	 * @return 查询记录数 / count of records.
	 */
	int count(Consumer<SQLQueryAlter<T>> consumer);
	

	/**
	 * <h2>Chinese:</h2>
	 * 按传入的Wrapper条件查询数量
	 * <h2>English:</h2>
	 * @param wrapper 查询条件封装
	 * @return 查询记录数 / count of records.
	 */
	int count(QueryWrapper<T,?> wrapper);

	/**
	 * <h2>Chinese:</h2>
	 * 根据示例对象进行Count查询
	 * <h2>English:</h2>
	 * Calculate the number of records that meet the criteria based on the example object.
	 * 
	 * @param example 示例对象 / the example object.
	 * @return 查询记录数 / count of records.
	 */
	int countByExample(T example);

	/**
	 * <h2>Chinese:</h2>
	 * 创建一个通用的查询构建器。
	 * <h2>English:</h2>
	 * create a general query builder.
	 * 
	 * @return  查询构建器 / WhereBuilder
	 * @see QueryExecutor 
	 */
	QueryExecutor<T> query();
	
	/**
	 * 传入一个带有@ConditionBean注解的类，使用该对象作为查询条件
	 * @param <X> the type of condition bean.
	 * @param conditionBean conditionBean 
	 * @return SQLQueryAlter 
	 */
	<X> Pair<Integer,List<T>> findByCondition(X conditionBean);
}
