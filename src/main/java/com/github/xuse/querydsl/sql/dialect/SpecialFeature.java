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
	 * 在执行ALTER TABLE语句的时候一次可以操作多个列 (MySQL And Oracle)
	 */
	MULTI_COLUMNS_IN_ALTER_TABLE,
	
	/**
	 * 支持分区表
	 */
	PARTITION_SUPPORT,
	
	/**
	 * 独立的COMMENT语句 (postgres)
	 */
	INDEPENDENT_COMMENT_STATEMENT,
	
	INDEPENDENT_PARTITION_CREATION,
	
	NO_PRIMARY_KEY_ON_PARTITION_TABLE
	;
	
	@Override
	public Class<?> getType() {
		return null;
	}
	
}
