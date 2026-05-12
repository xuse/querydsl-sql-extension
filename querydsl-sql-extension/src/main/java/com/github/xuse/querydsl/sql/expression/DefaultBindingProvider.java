package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Expression;

/**
 * 基于表达式映射（Expression Map）的默认绑定提供者。
 * <p>
 * 适用于查询中使用了自定义表达式绑定的场景，例如通过别名映射将查询列绑定到目标Bean属性。
 * 字段名从映射的键集合中提取，类型从对应的 {@link Expression} 中获取。
 * </p>
 *
 * @see BindingProvider
 */
final class DefaultBindingProvider implements BindingProvider {
	private final Map<String, ? extends Expression<?>> bindings;
	private final List<String> fieldNames;

	/**
	 * 根据表达式绑定映射构造绑定提供者。
	 *
	 * @param map 字段名到查询表达式的映射，键为目标Bean的属性名，值为对应的查询表达式
	 */
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
	public List<String> names(Map<String, FieldProperty> fieldOrder) {
		return fieldNames;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 从绑定映射中查找对应表达式，返回其声明的Java类型。
	 * </p>
	 */
	@Override
	public Class<?> getType(String prop, FieldProperty property) {
		Expression<?> expr = bindings.get(prop);
		return expr == null ? null : expr.getType();
	}
}
