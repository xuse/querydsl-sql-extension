package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 字段收集型绑定提供者，用于在未明确指定投影列时自动收集目标Bean的所有字段。
 * <p>
 * 当查询未显式指定列映射时（即 select *），该实现会根据目标Bean的属性定义
 * 动态确定需要绑定的字段。{@link #fieldNames()} 返回通配符 {@code "*"}，
 * 表示绑定所有可用字段；实际的字段列表在 {@link #names(Map)} 中根据目标Bean的
 * 字段顺序动态生成。
 * </p>
 * <p>
 * 类型信息直接从目标Bean的字段声明或getter方法返回值中获取，
 * 而非从查询表达式中推断。
 * </p>
 *
 * @see BindingProvider
 */
final class FieldCollector implements BindingProvider {

	/** 通配符标记，表示绑定目标Bean的所有字段。 */
	static final List<String> ALL_FIELDS = Collections.singletonList("*");

	private List<String> fieldNames;

	/**
	 * {@inheritDoc}
	 * <p>
	 * 返回通配符列表 {@code ["*"]}，表示该绑定提供者不限定具体字段，
	 * 所有缓存键共享同一标识。
	 * </p>
	 */
	@Override
	public List<String> fieldNames() {
		return ALL_FIELDS;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 返回估算值 8，用于集合预分配。由于实际字段数在构造时未知，
	 * 此处使用一个合理的默认值。
	 * </p>
	 */
	@Override
	public int size() {
		return 8;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 根据目标Bean的字段定义动态返回所有属性名称，顺序由 {@code fieldOrder} 的键集合决定。
	 * </p>
	 */
	@Override
	public List<String> names(Map<String, FieldProperty> fieldOrder) {
		return new ArrayList<>(fieldOrder.keySet());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 从目标Bean的字段属性中获取类型信息。优先使用字段声明类型，
	 * 若字段不存在则使用getter方法的返回类型。如果属性信息为 {@code null}，
	 * 则回退为 {@link Object} 类型。
	 * </p>
	 */
	@Override
	public Class<?> getType(String name, FieldProperty field) {
		Class<?> clz;
		if (field != null) {
			if (field.getField() == null) {
				clz = field.getGetter().getReturnType();
			} else {
				clz = field.getField().getType();
			}
		} else {
			clz = Object.class;
		}
		return clz;
	}

	/**
	 * 获取已收集的字段名称列表。
	 *
	 * @return 字段名称列表，可能为 {@code null}（如果尚未被外部设置）
	 */
	public List<String> getFieldNames() {
		return fieldNames;
	}
}
