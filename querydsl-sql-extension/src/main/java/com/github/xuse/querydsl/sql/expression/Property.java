package com.github.xuse.querydsl.sql.expression;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface Property {

	String getName();

	<T extends Annotation> T getAnnotation(Class<T> clz);

	Class<?> getType();

	Type getGenericType();

	int getModifiers();
}
