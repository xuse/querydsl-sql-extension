package com.github.xuse.querydsl.annotation.dbdef;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Types;

@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface ColumnSpec {

	/**
	 * @return 数据库列名称
	 */
	String name() default "";
	
	/**
	 * @return 数据库字段类型
	 * @see java.sql.Types
	 */
	int type() default Types.NULL;

	/**
	 * @return 字段长度。对于time和timestamp，含义的秒以下的位数精度。
	 * 
	 */
	int size() default -1;

	/**
	 * @return 小数位数
	 */
	int digits() default -1;
	
	/**
	 * @return 允许为null
	 */
	boolean nullable() default true;

	/**
	 * 如果是数值类型，无符号
	 */
	boolean unsigned() default false;
	
	/**
	 * @return 缺省值表达式 
	 */
	String defaultValue() default "";	
	
	/**
	 * 是否自增，不同数据库对自增实现规格不同，一般只有1个列可以自增。
	 */
	boolean autoIncrement() default false;
	
	/**
	 * 尚未实现
	 * not implemented.
	 */
	boolean updatable() default true;
	
	/**
	 * @return not implemented.
	 */
	boolean insertable() default true;
	
}
