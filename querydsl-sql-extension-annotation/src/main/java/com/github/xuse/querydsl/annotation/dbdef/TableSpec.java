package com.github.xuse.querydsl.annotation.dbdef;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于表的注解
 *
 * @author Joey
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface TableSpec {

	/**
	 * @return namespace
	 */
	String schema() default "";

	/**
	 * @return 表名
	 */
	String name() default "";

	/**
	 * @return 主键字段，此处填写Java字段名（Path）
	 */
	String[] primaryKeys() default {};

	/**
	 * @return 索引和UNIQUE
	 */
	Key[] keys() default {};

	/**
	 * @return 检查约束
	 */
	Check[] checks() default {};

	/**
	 *  @return 字符集
	 */
	String collate() default "";
	
	/**
	 * @return 短别名
	 */
	String alias() default "";
	
	/**
	 * @return 自增值开始于，0表示不指定，采用默认。
	 */
	int autoIncrementStartAt() default 0;
}
