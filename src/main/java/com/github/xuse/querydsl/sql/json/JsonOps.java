package com.github.xuse.querydsl.sql.json;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Operator;

/**
 * 扩展QueryDSL，实现JSON函数
 * 
 * @author jiyi
 *
 */
public enum JsonOps implements Operator {

	/**
	 * 查询是否包含数据
	 */
	JSON_CONTAINS(Boolean.class),

	JSON_CONTAINS_UNDER_PATH(Boolean.class),

	JSON_CONTAINS_PATH(Boolean.class),
	/**
	 * 查询数据
	 */
	JSON_SEARCH(Boolean.class),

	/**
	 * 查询数据
	 */
	JSON_SEARCH_WITH_PATH(Boolean.class),

	/**
	 * 提取数据
	 */
	JSON_EXTRACT(String.class),
	/**
	 * 列出KEY
	 */
	JSON_KEYS(String.class),
	/**
	 * 合并数据
	 */
	JSON_MERGE(String.class),
	/**
	 * 设置数据
	 */
	JSON_SET(String.class),

	/**
	 * 插入数据
	 */
	JSON_INSERT(String.class),
	/**
	 * 替换数据
	 */
	JSON_REPLACE(String.class),
	/**
	 * 创建一个JSON数组
	 */
	JSON_ARRAY(String.class),

	/**
	 * 创建一个JSON数组
	 */
	JSON_OBJECT(String.class),
	/**
	 * 创建JSON字符串，
	 */
	JSON_QUOTE(String.class),
	/**
	 * 向数组尾部追加数据
	 */
	JSON_ARRAY_APPEND(String.class),
	/**
	 * 
	 */
	JSON_ARRAY_INSERT(String.class),
	/**
	 * 从指定位置移除数据
	 */
	JSON_REMOVE(String.class),
	/**
	 * 判断是否重叠 JSON_OVERLAPS() was added in MySQL 8.0.17.
	 */
	JSON_OVERLAPS(Boolean.class),
	/**
	 * 取得节点值
	 */
	JSON_VALUE(String.class),
	/**
	 * 取得Unique运算后的json
	 */
	JSON_UNQUOTE(String.class),
	/**
	 * JSON深度
	 */
	JSON_DEPTH(Integer.class),
	/**
	 * 获得长度<br/>
	 * The length of a document is determined as follows:
	 * The length of a scalar is 1.
	 * The length of an array is the number of array elements.
	 * The length of an object is the number of object members.
	 * The length does not count the length of nested arrays or objects.
	 */
	JSON_LENGTH(Integer.class),
	/**
	 * 获得类型
	 */
	JSON_TYPE(String.class),
	/**
	 * 检查JSON是否有效
	 */
	JSON_VALID(Boolean.class),
	
	/**
	 * 将JSON数值定义为一张动态table
	 */
	JSON_TABLE(Tuple.class),
	/**
	 * Schema检查
	 */
	JSON_SCHEMA_VALID(Boolean.class),
	/**
	 * 检查Schema
	 */
	JSON_SCHEMA_VALIDATION_REPORT(String.class),
	/**
	 * json格式化
	 */
	JSON_PRETTY(String.class),
	/**
	 * 剩余空间检查
	 */
	JSON_STORAGE_FREE(Integer.class),
	/**
	 * 空间查询
	 */
	JSON_STORAGE_SIZE(Integer.class),
	/**
	 * 检查是否数组的成员
	 */
	MEMBER_OF(Boolean.class)
	;

	final Class<?> type;

	JsonOps(Class<?> type) {
		this.type = type;
	}

	@Override
	public Class<?> getType() {
		return type;
	}
}
