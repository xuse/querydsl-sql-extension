package com.github.xuse.querydsl.annotation.dbdef;

import com.querydsl.core.types.ConstraintType;

public @interface Key {
	/**
	 * @return 名称
	 */
	String name() default "";
	/**
	 * @return 索引类型
	 */
	ConstraintType type() default ConstraintType.KEY;
	/**
	 * @return 字段
	 */
	String[] path();
}
