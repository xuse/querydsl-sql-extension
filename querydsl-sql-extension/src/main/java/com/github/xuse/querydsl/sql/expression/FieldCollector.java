package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FieldCollector implements BindingProvider {
	private List<String> fieldNames;

	@Override
	public List<String> fieldNames() {
		return Collections.singletonList("*");
	}

	@Override
	public int size() {
		return 8;
	}

	@Override
	public List<String> names(Collection<String> fieldOrder) {
		return fieldNames = new ArrayList<>(fieldOrder);
	}

	@Override
	public Class<?> getType(String name,FieldProperty field) {
		Class<?> clz;
		if (field != null) {
			if(field.getField()==null) {
				clz=field.getGetter().getReturnType();
			}else {
				clz = field.getField().getType();	
			}
		} else {
			clz = Object.class;
		}
		return clz;
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}
}
