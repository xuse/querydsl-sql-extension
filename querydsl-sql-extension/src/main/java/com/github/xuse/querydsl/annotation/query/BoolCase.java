package com.github.xuse.querydsl.annotation.query;

import com.querydsl.core.types.Ops;

public @interface BoolCase {
	boolean is() default false;
	/**
	 * 运算符
	 */
	Ops ops() default Ops.EQ;
	/**
	 * 可以转换为指定字段类型的数值。如为多值用逗号分隔。
	 */
	String value() default "";
	
	/**
	 * 复杂条件直接写SQL表达式，使用表达式时运算符不生效，SQL需要全部写在表达式中。
	 */
	String expression() default "";
}
