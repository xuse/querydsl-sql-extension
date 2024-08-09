package com.github.xuse.querydsl.sql.expression;

import java.util.Collection;
import java.util.List;

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

	List<String> names(Collection<String> fieldOrder);

	Class<?> getType(String name,FieldProperty property);
}
