package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 描述一个类文件为查询表单类。
 * @author Joey
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ConditionBean {

	/**
	 * @return 指定Limit字段名
	 */
	String limitField() default "";

	/**
	 * @return 指定Offset字段名
	 */
	String offsetField() default "";
	
	/**
	 * @return 指定一个字段名，该字段为boolean类型，记录本次查询是否需要返回记录总数。
	 */
	String isRequireTotalField() default "";
}
