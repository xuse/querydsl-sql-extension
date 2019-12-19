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
	/**
	 * 运算操作符
	 * @return
	 */
	Ops value() default Ops.EQ;
	
	/**
	 * 对应的查询字段，默认是同名字段自动匹配，因此无需设置。
	 * 只有当查询条件类中字段名和数据表映射类的字段名不同时，此处才需要设置；同时还需要设置@ConditionBean中的additional。
	 * @return
	 */
	String name() default "";

}
