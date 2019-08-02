package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.UnsavedValuePredicateFactory;
import com.github.xuse.querydsl.util.ArrayListMap;
import com.github.xuse.querydsl.util.Entry;
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
		Object[] values = bc.values(bean);
//		entity
		int len = path.size();
		List<Entry<Path<?>, Object>> data = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			Object value = values[i];
			Path<?> p=path.get(i);
			if (!isNullValue(entity.getColumnMetadata(p), value)) {
				data.add(new Entry<>(p, value));
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
		return UnsavedValuePredicateFactory.create(field.getType(), field.getAnnotation(UnsavedValue.class)).test(propertyValue);
	}
	
	private boolean isNullValue(ColumnMapping columnMetadata, Object value) {
		return columnMetadata.isUnsavedValue(value);
	}

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
