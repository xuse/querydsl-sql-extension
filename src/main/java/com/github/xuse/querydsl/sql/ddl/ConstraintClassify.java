package com.github.xuse.querydsl.sql.ddl;

/**
 * 描述约束和索引的处理行为
 * @author Joey
 *
 */
public enum ConstraintClassify {
	/**
	 * 忽略，当前框架不处理
	 */
	IGNORE,
	/**
	 * 列相关约束
	 */
	COLUMNS,
	
	/**
	 * 列相关约束
	 */
	INDEX_COLUMNS,
	/**
	 * 多表外键，当前框架暂不处理
	 */
	REF,
	/**
	 * CHECK类约束
	 */
	CHECK

}
