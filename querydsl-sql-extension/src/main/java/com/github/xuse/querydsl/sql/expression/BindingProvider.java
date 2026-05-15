package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.querydsl.core.types.Path;

/**
 * 提供查询结果到Java Bean之间的字段绑定信息。
 * <p>
 * 该接口用于在ASM动态生成Bean访问器时，确定结果集中的列与目标Bean属性之间的映射关系。
 * 实现类需要提供字段名称列表和对应的Java类型信息，以便框架生成高效的属性赋值代码。
 * </p>
 *
 * @see ListPathBindings
 * @see RelationalPathBindings
 */
public interface BindingProvider {

	/**
	 * 获取绑定的字段名称列表。
	 * <p>
	 * 返回的列表用于形成缓存键（cache key），确保具有标志性即可，
	 * 不同的绑定组合应返回不同的字段名列表。
	 * </p>
	 *
	 * @return 字段名称列表，用于标识当前绑定组合
	 */
	List<String> fieldNames();

	/**
	 * 获取绑定字段的数量。
	 * <p>
	 * 用于性能优化场景（如预分配集合容量），不要求与实际字段数完全一致。
	 * </p>
	 *
	 * @return 绑定字段的近似数量
	 */
	int size();

	/**
	 * 根据目标Bean的字段顺序，返回实际需要绑定的字段名称列表。
	 * <p>
	 * 该方法允许实现类根据目标对象的属性定义来调整绑定顺序或过滤无效字段。
	 * </p>
	 *
	 * @param fieldOrder 目标Bean中字段名到 {@link FieldProperty} 的映射
	 * @return 经过排序或过滤后的字段名称列表
	 */
	List<String> names(Map<String, FieldProperty> fieldOrder);

	/**
	 * 获取指定字段的Java类型。
	 * <p>
	 * 用于在生成Bean访问器时确定属性的目标类型，以便进行正确的类型转换。
	 * </p>
	 *
	 * @param name     字段名称
	 * @param property 目标Bean中对应的字段属性信息，可能为 {@code null}
	 * @return 字段对应的Java类型，如果无法确定则返回 {@code null}
	 */
	Class<?> getType(String name, FieldProperty property);

	/**
	 * 基于显式指定的 {@link Path} 列表的绑定提供者。
	 * <p>
	 * 适用于查询中明确指定了投影列的场景，字段名和类型直接从 Path 元数据中提取。
	 * </p>
	 */
	public static class ListPathBindings implements BindingProvider {
		private final List<String> fieldNames = new ArrayList<>();
		private final Map<String, Class<?>> types = new HashMap<>();

		/**
		 * 根据查询投影的列路径列表构造绑定信息。
		 *
		 * @param columns 查询投影中的列路径列表
		 */
		public ListPathBindings(List<Path<?>> columns) {
			for (Path<?> p : columns) {
				String name = p.getMetadata().getName();
				fieldNames.add(name);
				types.put(name, p.getType());
			}
		}

		@Override
		public List<String> fieldNames() {
			return fieldNames;
		}

		@Override
		public int size() {
			return fieldNames.size();
		}

		@Override
		public List<String> names(Map<String, FieldProperty> fieldOrder) {
			return fieldNames;
		}

		@Override
		public Class<?> getType(String name, FieldProperty property) {
			return types.get(name);
		}
	}

	/**
	 * 基于 {@link RelationalPathEx} 表模型的绑定提供者。
	 * <p>
	 * 适用于查询整张表（select *）的场景，字段名和类型从表模型的列定义中获取。
	 * </p>
	 */
	public static class RelationalPathBindings implements BindingProvider {
		private final RelationalPathEx<?> table;
		private final List<String> fieldNames = new ArrayList<>();

		/**
		 * 根据表模型构造绑定信息，从表的所有列中提取字段名。
		 *
		 * @param b 表模型（RelationalPathEx 实例）
		 */
		public RelationalPathBindings(RelationalPathEx<?> b) {
			this.table = b;
			for (Path<?> p : b.getColumns()) {
				fieldNames.add(p.getMetadata().getName());
			}
		}

		@Override
		public List<String> fieldNames() {
			return fieldNames;
		}

		@Override
		public int size() {
			return fieldNames.size();
		}

		@Override
		public List<String> names(Map<String, FieldProperty> fieldOrder) {
			return fieldNames;
		}

		@Override
		public Class<?> getType(String name, FieldProperty property) {
			Path<?> path = table.getColumn(name);
			if (path != null) {
				return path.getType();
			}
			return null;
		}
	}
}
