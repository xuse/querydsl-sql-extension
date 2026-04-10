package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>English:</h2> Experimental feature. Can be placed on @ConditionBean fields to map UI conditions to database conditions.
 * <h2>Chinese:</h2> 试验性功能，允许加在@ConditionBean的字段上用于映射界面条件与数据库条件
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface When {
	String path() default "";
	StringCase[] value() default{};
	BoolCase[] forBool() default{};
	IntCase[] forInt() default{};
	boolean ignoreIfNoMatchCase() default true; 
}
