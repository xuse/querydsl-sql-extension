package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FieldCollector implements BindingProvider {
	private Map<String, FieldProperty> values;

	@Override
	public List<String> fieldNames() {
		return Arrays.asList("*");
	}

	@Override
	public int size() {
		return 8;
	}

	@Override
	public List<String> names(Map<String, FieldProperty> fieldOrder) {
		values = fieldOrder;
		return new ArrayList<>(fieldOrder.keySet());
	}

	@Override
	public Expression<?> get(String property) {
		FieldProperty field = values.get(property);
		Class<?> clz;
		if (field != null) {
			if(field.getField()==null) {
				log.error("property "+property+"不存在");
				clz=field.getGetter().getReturnType();
			}else {
				clz = field.getField().getType();	
			}
			
		} else {
			clz = Object.class;
		}
		return Expressions.simplePath(clz, property);
	}
}
