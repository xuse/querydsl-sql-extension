package com.github.xuse.querydsl.sql.expression;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldProperty {
	private final Method getter;
	private final Method setter;
	private final Field field;

	public FieldProperty(Method getter,Method setter, Field field) {
		this.getter=getter;
		this.setter=setter;
		this.field=field;
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
}
