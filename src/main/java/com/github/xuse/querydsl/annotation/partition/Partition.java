package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Partition {
	String name();
	
	String value();

	/**
	 * @return 在Postgresql上， Range需要指定from 
	 */
	String from() default "";
}
