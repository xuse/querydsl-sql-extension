package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Expression;

final class DefaultBindingProvider implements BindingProvider{
	private final Map<String, ? extends Expression<?>> bindings;
	private final List<String> fieldNames;

	public DefaultBindingProvider(Map<String, ? extends Expression<?>> map) {
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
	public List<String> names(Collection<String> fieldOrder) {
		return fieldNames;
	}

	@Override
	public Class<?> getType(String prop,FieldProperty property) {
		Expression<?> expr= bindings.get(prop);
		return expr==null? null: expr.getType();
	}

	
}
