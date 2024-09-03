package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Partition {
	String name();
	
	String value();

	/**
	 * In PostgreSQL, ranged partitioning requires a start and end of the range. In
	 * MySQL, only the upper boundary needs to be specified, so this property will
	 * be ignored in MySQL.
	 * 
	 * @return 在PostgreSql上， Ranged
	 *         partition需要一个范围的起始和结束。而在MySQL上只需要指定上结束边界，因此该属性在MySQL上将被忽略。
	 */
	String from() default "";
}
