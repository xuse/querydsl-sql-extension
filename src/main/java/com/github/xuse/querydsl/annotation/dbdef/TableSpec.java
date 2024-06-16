package com.github.xuse.querydsl.annotation.dbdef;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于表的注解
 * 
 * @author jiyi
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface TableSpec {
	/**
	 * Namespace
	 */
	String schema() default "";

	/**
	 * 表名
	 */
	String name() default "";

	/**
	 * 主键字段
	 */
	String[] primaryKeyPath() default {};

	/**
	 * 索引和UNIQUE
	 */
	Key[] keys() default {};

	/**
	 * 检查约束
	 */
	Check[] checks() default {};

	/**
	 * @return 字符集
	 */
	String collate() default "";

}
