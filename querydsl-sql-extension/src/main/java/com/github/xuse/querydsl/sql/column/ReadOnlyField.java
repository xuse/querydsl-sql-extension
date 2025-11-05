package com.github.xuse.querydsl.sql.column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ReadOnlyField implements AccessibleElement{
	private final Field field;
	
	ReadOnlyField(Field field){
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
		log.error("Field [{}] is read only. setting value [{}] is ignored.", field,value);
	}

	@Override
	public String toString() {
		return field.toString();
	}

	@Override
	public Type getGenericType() {
		return field.getGenericType();
	}
}
