package com.github.xuse.querydsl.annotation.dbdef;

/**
 * 定义一个约束检查
 */
public @interface Check {
	/**
	 * @return 名称
	 */
	String name() default "";
	
	
	String value();
}
