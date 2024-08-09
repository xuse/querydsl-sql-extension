package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Partition {
	String name();
	
	String value();
}
