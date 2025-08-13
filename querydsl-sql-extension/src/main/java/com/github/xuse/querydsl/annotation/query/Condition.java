package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import com.querydsl.core.types.Ops;

/**
 * 满足这样一种场景。有一些固定的组合条件查询。（比如从前端页面传入若干字段）其中一些字段可以为空，即不作为过滤条件。凡是传入有效数值的条件，都要参与查询过滤。
 * 为此，可以定义一个Bean，将查询条件固定下来。通过@Condition注解，配置每个条件的运算操作符。
 * @author Joey
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface Condition {

	/**
	 * @return 运算操作符
	 */
	Ops value() default Ops.EQ;

	/**
	 * @return 对应的查询字段，默认是同名字段自动匹配，因此无需设置。
	 * 只有当查询条件类中字段名和数据表映射类的字段名不同时，此处才需要设置；同时还需要设置@ConditionBean中的additional。
	 */
	String path() default "";
	
	/**
	 * 某些场合需要多个字段匹配用户输入条件，例如  User.phone=? OR User.email=?，需要增加其他匹配字段
	 */
	String[] otherPaths() default{};

	/**
	 * @return 当未设置值(为null)时，忽略该条件
	 */
	boolean ignoreUnsavedValue() default true;
}
