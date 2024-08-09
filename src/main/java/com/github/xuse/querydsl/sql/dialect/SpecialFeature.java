package com.github.xuse.querydsl.sql.dialect;

import com.querydsl.core.types.Operator;

/**
 * 从我的上一个框架复制来的代码。大部分都还没有用到，后续将清理掉不再使用的特性
 */

public enum SpecialFeature implements Operator{
	/**
	 * Oracle兼容用
	 */
	HAS_CRLF_IN_DEFAULT_VALUE_EXPRESSION,
	
	/**
	 * 在执行ALTER TABLE语句的时候一次只能操作一个列 (Derby)
	 */
	ONE_COLUMN_IN_SINGLE_DDL,
	
	/**
	 * 支持分区表
	 */
	PARTITION_SUPPORT
	;

	@Override
	public Class<?> getType() {
		return null;
	}
	
}
