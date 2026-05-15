package com.github.xuse.querydsl.sql.dml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.Ops;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.BeanCodecManager;
import com.github.xuse.querydsl.sql.expression.ConverterWrappedBean;
import com.github.xuse.querydsl.sql.expression.Property;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPath;

/**
 * Helper for batch DML operations (update/delete) that use @Condition annotations
 * on DTO fields to determine WHERE clause parameters.
 * <p>
 * Fields annotated with @Condition are used as WHERE conditions (excluded from SET).
 * Fields without @Condition participate in SET (for update) or are ignored (for delete).
 * </p>
 *
 * <h2>中文</h2>
 * 批量 DML 操作（update/delete）的辅助类，使用 DTO 字段上的 @Condition 注解
 * 来确定 WHERE 子句参数。
 * <p>
 * 标注 @Condition 的字段用作 WHERE 条件（从 SET 中排除）。
 * 未标注的字段参与 SET（update）或被忽略（delete）。
 * </p>
 */
class DmlConditionHelper {

	private final BeanCodec codec;
	private final List<ConditionSlot> conditionSlots;
	private final Set<String> conditionPathNames;

	/**
	 * @param dtoType    the DTO class
	 * @param entity     the target entity path
	 */
	DmlConditionHelper(Class<?> dtoType, RelationalPath<?> entity) {
		this.codec = BeanCodecManager.getInstance().getCodec(dtoType);
		Property[] fields = codec.getFields();

		Map<String, Path<?>> bindings = new HashMap<>();
		for (Path<?> p : entity.getColumns()) {
			bindings.put(p.getMetadata().getName(), p);
		}

		List<ConditionSlot> slots = new ArrayList<>();
		Set<String> pathNames = new HashSet<>();
		for (int i = 0; i < fields.length; i++) {
			Property field = fields[i];
			Condition condition = field.getAnnotation(Condition.class);
			if (condition != null) {
				String pathName = condition.path();
				if (StringUtils.isEmpty(pathName)) {
					pathName = field.getName();
				}
				Path<?> path = bindings.get(pathName);
				if (path == null) {
					throw Exceptions.illegalArgument(
							"@Condition path [{}] not found in entity [{}]",
							pathName, entity.getType().getName());
				}
				slots.add(new ConditionSlot(i, path, condition.value()));
				pathNames.add(path.getMetadata().getName());
			}
		}
		if (slots.isEmpty()) {
			throw Exceptions.illegalArgument(
					"No @Condition fields found in class [{}]. "
					+ "At least one field must be annotated with @Condition for batch DML.",
					dtoType.getName());
		}
		this.conditionSlots = slots;
		this.conditionPathNames = pathNames;
	}

	/**
	 * Check if a given path is a condition path (should be excluded from SET).
	 */
	boolean isConditionPath(Path<?> path) {
		return conditionPathNames.contains(path.getMetadata().getName());
	}

	/**
	 * Build WHERE predicates from the bean's @Condition fields.
	 * Returns a list of predicates to be applied to the clause.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	com.querydsl.core.types.Predicate[] buildConditions(Object bean) {
		Object actualBean = (bean instanceof ConverterWrappedBean)
				? ((ConverterWrappedBean) bean).getDto() : bean;
		Object[] values = codec.values(actualBean);
		com.querydsl.core.types.Predicate[] predicates = new com.querydsl.core.types.Predicate[conditionSlots.size()];
		for (int i = 0; i < conditionSlots.size(); i++) {
			ConditionSlot slot = conditionSlots.get(i);
			Object value = values[slot.fieldIndex];
			if (value == null) {
				throw Exceptions.illegalArgument(
						"@Condition field at index [{}] has null value. "
						+ "All condition fields must have non-null values for batch DML.",
						slot.fieldIndex);
			}
			SimpleExpression path = (SimpleExpression) slot.path;
			switch (slot.ops) {
			case EQ:
				predicates[i] = path.eq(ConstantImpl.create(value));
				break;
			case GT:
				predicates[i] = ((com.querydsl.core.types.dsl.ComparableExpression) path)
						.gt((Comparable) value);
				break;
			case GOE:
				predicates[i] = ((com.querydsl.core.types.dsl.ComparableExpression) path)
						.goe((Comparable) value);
				break;
			case LT:
				predicates[i] = ((com.querydsl.core.types.dsl.ComparableExpression) path)
						.lt((Comparable) value);
				break;
			case LOE:
				predicates[i] = ((com.querydsl.core.types.dsl.ComparableExpression) path)
						.loe((Comparable) value);
				break;
			default:
				throw Exceptions.illegalArgument(
						"Ops.{} is not supported for batch DML @Condition. Only EQ/GT/GOE/LT/LOE are supported.",
						slot.ops);
			}
		}
		return predicates;
	}

	private static class ConditionSlot {
		final int fieldIndex;
		final Path<?> path;
		final Ops ops;

		ConditionSlot(int fieldIndex, Path<?> path, Ops ops) {
			this.fieldIndex = fieldIndex;
			this.path = path;
			this.ops = ops;
		}
	}
}
