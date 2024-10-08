package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD })
@Retention(RUNTIME)
public @interface Order {
	/**
	 * @return 配置一个字段名，用于描述本排序字段是ASC还是DESC。
	 * 如果目标字段为boolean类型，则true表示 ASC， false表示DESC。
	 * 如果目标字段为String类型，则需要值为 asc/desc （无视大小写），其他值会被忽略。
	 */
	String sortField() default "";
	
}
