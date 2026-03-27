package com.github.xuse.querydsl.annotation.dbdef;

/**
 * 针对特定数据库，在定义上添加用户自定义片段
 */
public @interface SpecialSpec {
	/**
	 * 该定义仅限于哪个数据库，
	 * @See com.github.xuse.querydsl.sql.dialect.DbType
	 */
	String dbType() default "";
	
	/**
	 * @return 可定义列上SQL片段，该片段会位于"DEFAULT"值定义之后，"COMMENT"定义之前。
	 *         对于列上不支持COMMENT定义的数据库，则位于列定义的最后。
	 */
	String value();
}
