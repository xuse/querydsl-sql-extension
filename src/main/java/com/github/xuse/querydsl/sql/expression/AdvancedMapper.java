package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.github.xuse.querydsl.util.ArrayListMap;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.Primitives;
import com.github.xuse.querydsl.util.StringUtils;
import com.google.common.collect.Maps;
import com.querydsl.core.QueryException;
import com.querydsl.core.types.Path;
import com.querydsl.core.util.ReflectionUtils;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.Mapper;

/**
 * A Mapper that using dynamic codec class to extract values from the entity-bean.
 * 
 * @see Mapper
 * 
 * @author Joey
 */
public class AdvancedMapper implements Mapper<Object> {
	/**
	 * Singleton instance
	 */
	public static AdvancedMapper INSTANCE = new AdvancedMapper();

	private final boolean withNullBindings;

	protected AdvancedMapper() {
		this.withNullBindings = false;
	}

	/*
	 * 
	 */
	@Override
	public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Object bean) {
		if (entity instanceof IRelationPathEx && entity.getType().isAssignableFrom(bean.getClass())) {
			// 可以采用优化方案构造
			return createMapOptimized((IRelationPathEx) entity, bean);
		} else {
			return createMap0(entity, bean);
		}
	}

	/**
	 * Create the property map using ASM generated class.
	 * @param entity
	 * @param bean
	 * @return
	 */
	private Map<Path<?>, Object> createMapOptimized(IRelationPathEx entity, Object bean) {
		List<Path<?>> path = entity.getColumns();
		BeanCodec bc = entity.getBeanCodec();
		Field[] fields = bc.getFields();
		Object[] values = bc.values(bean);
//		entity
		int len = path.size();
		List<Entry<Path<?>, Object>> data = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			Object value = values[i];
			if (!isNullValue(fields[i], value)) {
				data.add(new Entry<>(path.get(i), value));
//			} else if (withNullBindings && ) {
//				data.add(new Entry<>(path.get(i), com.querydsl.sql.types.Null.DEFAULT));
			}
		}
		return ArrayListMap.wrap(data);
	}

	private Map<Path<?>, Object> createMap0(RelationalPath<?> entity, Object bean) {
		try {
			Map<Path<?>, Object> values = Maps.newLinkedHashMap();
			Class<?> beanClass = bean.getClass();
			Map<String, Path<?>> columns = getColumns(entity);
			for (Map.Entry<String, Path<?>> entry : columns.entrySet()) {
				Path<?> path = entry.getValue();
				Field beanField = ReflectionUtils.getFieldOrNull(beanClass, entry.getKey());
				if (beanField != null && !Modifier.isStatic(beanField.getModifiers())) {
					beanField.setAccessible(true);
					Object propertyValue = beanField.get(bean);
					if (!isNullValue(beanField, propertyValue)) {
						values.put(path, propertyValue);
					} else if (withNullBindings && !isPrimaryKeyColumn(entity, path)) {
						values.put(path, com.querydsl.sql.types.Null.DEFAULT);
					}
				}
			}
			return values;
		} catch (IllegalAccessException e) {
			throw new QueryException(e);
		}
	}

	private boolean isNullValue(Field field, Object propertyValue) {
		UnsavedValue annotation = field.getAnnotation(UnsavedValue.class);
		Class<?> clz = field.getType();
		if (annotation != null) {
			return parseValue(clz, annotation.value()).test(propertyValue);
		}
		if (clz.isPrimitive()) {
			return Primitives.defaultValueOfPrimitive(clz).equals(propertyValue);
		} else {
			return propertyValue == null;
		}
	}

	private Predicate<Object> parseValue(Class<?> containerType, String value) {
		// int 226
		// short 215
		// long 221
		// boolean 222
		// float 219
		// double 228
		// char 201
		// byte 237
		Object condition = null;
		if (containerType.isPrimitive()) {
			String s = containerType.getName();
			switch (s.charAt(1) + s.charAt(2)) {
			case 226:
				condition = StringUtils.toInt(value, 0);
				break;
			case 215:
				condition = StringUtils.toInt(value, 0);
				break;
			case 221:
				condition = StringUtils.toLong(value, 0L);
				break;
			case 222:
				condition = StringUtils.toBoolean(value, false);
				break;
			case 219:
				condition = StringUtils.toFloat(value, 0f);
				break;
			case 228:
				condition = StringUtils.toDouble(value, 0d);
				break;
			case 201:
				if (value.length() == 0) {
					condition = (char) 0;
				} else {
					condition = value.charAt(0);
				}
				break;
			case 237:
				condition = (byte) StringUtils.toInt(value, 0);
				break;
			default:
			}
		} else if ("null".equalsIgnoreCase(value)) {
			return Null;
		} else if (UnsavedValue.MinusNumber.equals(value)) {
			return MinusNumber;
		} else if (UnsavedValue.ZeroAndMinus.equals(value)) {
			return ZeroAndMinus;
		} else if (UnsavedValue.NullOrEmpty.equals(value)) {
			return NullOrEmpty;
		} else if (String.class == containerType) {
			condition = value;
		} else {
			throw Exceptions.illegalArgument("Unsupport type [{}] for annotation @UnsavedValue.", containerType);
		}
		return condition == null ? Null : new ConstantFilter(condition);
	}

	private static class ConstantFilter implements Predicate<Object> {
		private Object object1;

		ConstantFilter(Object obj) {
			this.object1 = obj;
		}

		public boolean test(Object object2) {
			if (object1 == object2) {
				return true;
			}
			if ((object1 == null) || (object2 == null)) {
				return false;
			}
			return object1.equals(object2);
		}
	}

	private static final Predicate<Object> Null = new Predicate<Object>() {
		public boolean test(Object obj) {
			return obj == null;
		}
	};

	private static final Predicate<Object> NullOrEmpty = new Predicate<Object>() {
		public boolean test(Object obj) {
			if (obj == null)
				return true;
			return String.valueOf(obj).length() == 0;
		}
	};

	private static final Predicate<Object> MinusNumber = new Predicate<Object>() {
		public boolean test(Object obj) {
			if (obj == null)
				return true;
			if (obj instanceof Number) {
				return ((Number) obj).longValue() < 0;
			} else {
				return false;
			}
		}
	};

	private static final Predicate<Object> ZeroAndMinus = new Predicate<Object>() {
		public boolean test(Object obj) {
			if (obj == null)
				return true;
			if (obj instanceof Number) {
				return ((Number) obj).longValue() <= 0;
			} else {
				return false;
			}
		}
	};

	protected Map<String, Path<?>> getColumns(RelationalPath<?> path) {
		Map<String, Path<?>> columns = Maps.newLinkedHashMap();
		for (Path<?> column : path.getColumns()) {
			columns.put(column.getMetadata().getName(), column);
		}
		return columns;
	}

	protected boolean isPrimaryKeyColumn(RelationalPath<?> parent, Path<?> property) {
		return parent.getPrimaryKey() != null && parent.getPrimaryKey().getLocalColumns().contains(property);
	}
}
