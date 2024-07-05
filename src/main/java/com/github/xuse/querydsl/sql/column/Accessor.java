package com.github.xuse.querydsl.sql.column;

import java.lang.annotation.Annotation;

public interface Accessor {

	<T extends Annotation> T getAnnotation(Class<T> clz);

	Class<?> getType();

	String getName();

	void set(Object bean, Object value);

}
