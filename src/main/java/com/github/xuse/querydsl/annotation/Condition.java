package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.querydsl.core.types.Ops;

/**
 * 用于辅助描述条件类上的字段
 * 
 * @author jiyi
 *
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface Condition {
	Ops value() default Ops.EQ;

}
