package com.github.xuse.querydsl.sql.expression;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.Column;

public interface Property {

	String getName();

	<T extends Annotation> T getAnnotation(Class<T> clz);

	Class<?> getType();

	Type getGenericType();

	int getModifiers();
	
	default String properName() {
		String name;
		Column c=getAnnotation(Column.class);
		ColumnSpec cs=getAnnotation(ColumnSpec.class);
		if(cs!=null && StringUtils.isNotEmpty(cs.name())) {
			name=cs.name();
		}else if(c!=null && StringUtils.isNotEmpty(c.value())) {
			name=c.value();
		}else {
			name=getName();
		}
		return name;
	}
}
