package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>English:</h2>
 * For scenarios where there are fixed combination queries (e.g., several fields passed from a frontend page),
 * some fields may be empty (not used as filter conditions). All fields with valid values participate in query filtering.
 * Define a Bean to fix the query conditions, and use @Condition to configure the operator for each condition.
 * <h2>Chinese:</h2>
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
	 * @return The corresponding query field. Defaults to auto-matching by the same field name, so no configuration is needed.
	 * Only set this when the field name in the condition class differs from the entity mapping class.
	 * <p>对应的查询字段，默认是同名字段自动匹配，因此无需设置。
	 * 只有当查询条件类中字段名和数据表映射类的字段名不同时，此处才需要设置；同时还需要设置@ConditionBean中的additional。
	 */
	String path() default "";
	
	/**
	 * In some cases, multiple fields need to match user input, e.g., User.phone=? OR User.email=?. Additional matching fields can be added here.
	 * <p>某些场合需要多个字段匹配用户输入条件，例如  User.phone=? OR User.email=?，需要增加其他匹配字段
	 */
	String[] otherPaths() default{};

	/**
	 * @return 当未设置值(为null)时，忽略该条件
	 */
	boolean ignoreUnsavedValue() default true;
}
