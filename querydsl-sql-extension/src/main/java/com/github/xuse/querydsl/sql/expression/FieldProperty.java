package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.xuse.querydsl.util.Primitives;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FieldProperty {
	private final Method getter;
	private final Method setter;
	private final Field field;
	//not the type of field, but the type of path(expression in bindings)
	//数据库查出类型（通过Mapping）和实际field类型可能不同。
	private Class<?> bindingType;

	public FieldProperty(Method getter, Method setter, Field field) {
		this.getter = getter;
		this.setter = setter;
		this.field = field;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	public Field getField() {
		return field;
	}

	@Override
	public String toString() {
		return "getter=" + getter + ", setter=" + setter + ", field=" + field;
	}

	public Class<?> getBindingType() {
		return bindingType;
	}

	public void setBindingType(Class<?> bindingType) {
		if(field!=null) {
			if(isNotAssignableFrom(bindingType)) {
				log.warn("Data type incompatible between field [{}] and expression type {}",field,bindingType);
			}
		}
		this.bindingType = bindingType;
	}

	private boolean isNotAssignableFrom(Class<?> bindingType) {
		Class<?> type=Primitives.toWrapperClass(field.getType());
		return !type.isAssignableFrom(bindingType);
	}

	public String getName() {
		return field.getName();
	}
}
