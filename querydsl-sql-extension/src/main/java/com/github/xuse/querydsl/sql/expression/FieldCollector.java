package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class FieldCollector implements BindingProvider {
	static final List<String> ALL_FIELDS = Collections.singletonList("*");
	
	private List<String> fieldNames;

	@Override
	public List<String> fieldNames() {
		return ALL_FIELDS;
	}

	@Override
	public int size() {
		return 8;
	}

	@Override
	public List<String> names(Map<String, FieldProperty> fieldOrder) {
		return new ArrayList<>(fieldOrder.keySet());
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
