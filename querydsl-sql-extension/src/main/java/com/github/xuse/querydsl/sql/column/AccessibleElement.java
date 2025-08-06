package com.github.xuse.querydsl.sql.column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public interface AccessibleElement {

	<T extends Annotation> T getAnnotation(Class<T> clz);

	Class<?> getType();

	String getName();

	void set(Object bean, Object value);

	static AccessibleElement ofField(Field field) {
		if(Modifier.isFinal(field.getModifiers())) {
			return new ReadOnlyField(field);
		}else {
			return new FieldImpl(field);
		}
	}

	Type getGenericType();
}
