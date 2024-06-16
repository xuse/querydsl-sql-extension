package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 描述一个类文件为查询条件类
 * @author jiyi
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ConditionBean {
	
	/**
	 * 指定Limit字段名
	 */
	String limitField() default "";

	/**
	 * 指定Offset字段名
	 */
	String offsetField() default "";
}
