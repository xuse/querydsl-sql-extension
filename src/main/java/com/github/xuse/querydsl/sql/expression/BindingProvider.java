package com.github.xuse.querydsl.sql.expression;

import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Expression;

public interface BindingProvider {

	/**
	 * @return 用于形成cacheKey。确保标志性即可
	 */

	List<String> fieldNames();

	/**
	 * 
	 * @return 优化性能用，不要求准确
	 */
	int size();

	List<String> names(Map<String, FieldProperty> fieldOrder);

	Expression<?> get(String property);
}
