package com.github.xuse.querydsl.sql.dbmeta;

/**
 * <h2>English:</h2>
 * Enumeration of database object types.
 * <ul>
 * <li>{@link #TABLE} - Database table</li>
 * <li>{@link #SEQUENCE} - Database sequence</li>
 * <li>{@link #VIEW} - Database view</li>
 * <li>{@link #FUNCTION} - User-defined function</li>
 * <li>{@link #PROCEDURE} - Stored procedure</li>
 * </ul>
 * <h2>Chinese:</h2>
 * 枚举数据库对象类型
 * <ul>
 * <li>{@link #TABLE} - 数据库表</li>
 * <li>{@link #SEQUENCE} - 数据库序列</li>
 * <li>{@link #VIEW} - 数据库视图</li>
 * <li>{@link #FUNCTION} - 自定义函数</li>
 * <li>{@link #PROCEDURE} - 存储过程</li>
 * </ul>
 */
public enum ObjectType {
	/**
	 * Table, including various temporary tables.
	 * <p>表，包含各种临时表等
	 */
	TABLE,
	/**
	 * Sequence.
	 * <p>序列
	 */
	SEQUENCE,
	/**
	 * View.
	 * <p>视图
	 */
	VIEW,
	/**
	 * User-defined function.
	 * <p>函数
	 */
	FUNCTION,
	/**
	 * Stored procedure.
	 * <p>存储过程
	 */
	PROCEDURE
}