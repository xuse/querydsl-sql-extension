package com.github.xuse.querydsl.annotation;

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
	 * @return
	 */
	String limitField() default "";

	/**
	 * 指定Offset字段名
	 * @return
	 */
	String offsetField() default "";
	
	/**
	 * 指定和被查询Bean不同的额外查询字段
	 * @return
	 */
	String[] additional() default{};
	
}
