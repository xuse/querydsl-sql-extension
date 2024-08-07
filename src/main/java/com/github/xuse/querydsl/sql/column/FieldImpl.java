package com.github.xuse.querydsl.sql.column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

final class FieldImpl implements AccessibleElement{
	private final Field field;
	
	FieldImpl(Field field){
		this.field=field;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> clz) {
		return field.getAnnotation(clz);
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public void set(Object bean, Object value) {
		try {
			field.set(bean, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("field set error. " + field + " = " + value, e);
		}
	}

	@Override
	public String toString() {
		return field.toString();
	}
}
