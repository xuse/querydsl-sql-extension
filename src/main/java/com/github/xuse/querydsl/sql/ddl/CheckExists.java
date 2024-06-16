package com.github.xuse.querydsl.sql.ddl;

public enum CheckExists {
	/**
	 * 不检查
	 */
	NOT_CHECK,
	/**
	 * 如果对象已存在，放弃操作
	 */
	IGNORE,
	
	/**
	 * 如果对象已存在，抛出异常终端整个过程
	 */
	ABORT,
	/**
	 * 如果对象已存在，删除后重新创建（注意，如果对象为表，可能会造成数据丢失）
	 */
	DROP_CREATE,
	/**
	 * 对比，并尝试用Alter语句去修改，有较多因素影响下可能会失败。
	 */
	MERGE

}
