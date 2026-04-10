package com.github.xuse.querydsl.annotation.dbdef;

/**
 * <h2>English:</h2> Add user-defined SQL fragments for specific databases in column definitions.
 * <h2>Chinese:</h2> 针对特定数据库，在定义上添加用户自定义片段
 */
public @interface SpecialSpec {
	/**
	 * <h2>English:</h2> This definition is limited to which database.
	 * <h2>Chinese:</h2> 该定义仅限于哪个数据库，
	 * @See com.github.xuse.querydsl.sql.dialect.DbType
	 */
	String dbType() default "";
	
	/**
	 * <h2>English:</h2> A SQL fragment for the column definition, placed after the DEFAULT value
	 * and before the COMMENT. For databases that do not support COMMENT on columns,
	 * it is placed at the end of the column definition.
	 * <h2>Chinese:</h2>
	 * @return 列上的自定义SQL片段，位于DEFAULT值定义之后、COMMENT定义之前。
	 *         对于不支持列COMMENT的数据库，该片段位于列定义的末尾。
	 */
	String value();
}
