package com.github.xuse.querydsl.annotation.dbdef;

/**
 * 注解，定义一个数据库约束检查
 * <p>
 * Annotation: Define a database check constraint
 */
public @interface Check {
	/**
	 * @return 名称/name
	 */
	String name() default "";

	/**
	 * @return check约束的数据库表达式 / Database expression for check constraint
	 */
	String value();
}
