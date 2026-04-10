package com.github.xuse.querydsl.annotation.query;

public @interface IntCase {
	int is() default 0;
	/**
	 * The operator / 运算符
	 */
	Ops ops() default Ops.EQ;
	/**
	 * A value convertible to the target field type. Use comma to separate multiple values.
	 * <p>可以转换为指定字段类型的数值。如为多值用逗号分隔。
	 */
	String value() default "";
	
	/**
	 * For complex conditions, write the SQL expression directly. When using an expression, the operator is ignored.
	 * <p>复杂条件直接写SQL表达式，使用表达式时运算符不生效，SQL需要全部写在表达式中。
	 */
	String expression() default "";
}
