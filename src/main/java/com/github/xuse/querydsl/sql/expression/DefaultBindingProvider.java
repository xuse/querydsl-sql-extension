package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Expression;

public class DefaultBindingProvider implements BindingProvider{
	private final Map<String, Expression<?>> bindings;
	private final List<String> fieldNames;

	public DefaultBindingProvider(Map<String, Expression<?>> map) {
		this.bindings = map;
		this.fieldNames = new ArrayList<>(bindings.keySet());
	}

	@Override
	public List<String> fieldNames() {
		return fieldNames; 
	}

	@Override
	public int size() {
		return bindings.size();
	}

	@Override
	public List<String> names(Map<String, FieldProperty> fieldOrder) {
		return fieldNames;
	}

	@Override
	public Expression<?> get(String property) {
		return bindings.get(property);
	}

	
}
