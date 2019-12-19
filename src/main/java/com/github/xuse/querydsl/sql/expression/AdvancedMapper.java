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
import com.querydsl.core.types.dsl.Expressions;
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
	 * 空值绑定会被跳过，因此在Batch模式下根据参数中的空值不同，很容易生成多组SQL语句。
	 * 当多组SQL执行executeWithKey时，会抛出异常，因此执行批量插入并且获取Key时，不能使用这种Mapper。
	 */
	public static AdvancedMapper INSTANCE = new AdvancedMapper();
	
	/**
	 * 空值绑定会向Statement中执行setNull(index, sqlType). 适合于批量模式下统一SQL语句。
	 * 但是对于带有自生成如自增键、缺省值等字段，尝试写入NULL可能导致异常。
	 */
	public static AdvancedMapper INSTANCE_NULLS_BINGIND = new AdvancedMapper() {
		protected void processNullBindings(ColumnMapping metadata, List<Entry<Path<?>, Object>> data,Path<?> path) {
			if(!metadata.isPk()){
				data.add(new Entry<>(path, com.querydsl.sql.types.Null.DEFAULT));	
			}
		}
		protected void processNullBindings(RelationalPath<?> entity,List<Entry<Path<?>, Object>> data, Path<?> path) {
			if(!isPrimaryKeyColumn(entity,path)) {
				data.add(new Entry<>(path, com.querydsl.sql.types.Null.DEFAULT));
			}
		}
	};
	
	/**
	 * 直接在SQL语句的Value区域写入DEFAULT关键字。在某些数据库上有用。
	 */
	public static AdvancedMapper INSTANCE_NULLS_DEFAULT = new AdvancedMapper() {
		protected void processNullBindings(ColumnMapping metadata, List<Entry<Path<?>, Object>> data,Path<?> path) {
			data.add(new Entry<>(path, Expressions.template(path.getType(), "DEFAULT")));
		}
		protected void processNullBindings(RelationalPath<?> entity,List<Entry<Path<?>, Object>> data, Path<?> path) {
			data.add(new Entry<>(path, Expressions.template(path.getType(), "DEFAULT")));
		}
	};

	
	protected AdvancedMapper() {
	}

	/*
	 * 
	 */
	@Override
	public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Object bean) {
		if (entity instanceof IRelationPathEx && entity.getType().isAssignableFrom(bean.getClass())) {
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
			ColumnMapping metadata=entity.getColumnMetadata(p);
			if (!isNullValue(metadata, value)) {
				data.add(new Entry<>(p, value));
			} else{
				processNullBindings(metadata,data,p);
				
			}
		}
		return ArrayListMap.wrap(data);
	}
	
	protected void processNullBindings(ColumnMapping metadata, List<Entry<Path<?>, Object>> data,Path<?> p) {
	}
	protected void processNullBindings(RelationalPath<?> entity,List<Entry<Path<?>, Object>> data, Path<?> path) {
	}

	private Map<Path<?>, Object> createMap0(RelationalPath<?> entity, Object bean) {
		try {
			Class<?> beanClass = bean.getClass();
			Map<String, Path<?>> columns = getColumns(entity);
			List<Entry<Path<?>, Object>> data = new ArrayList<>(columns.size());
			for (Map.Entry<String, Path<?>> entry : columns.entrySet()) {
				Path<?> path = entry.getValue();
				Field beanField = ReflectionUtils.getFieldOrNull(beanClass, entry.getKey());
				if (beanField != null && !Modifier.isStatic(beanField.getModifiers())) {
					beanField.setAccessible(true);
					Object propertyValue = beanField.get(bean);
					if (!isNullValue(beanField, propertyValue)) {
						data.add(new Entry<>(path, propertyValue));
					} else {
						processNullBindings(entity,data,path);
					}
				}
			}
			return ArrayListMap.wrap(data);
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

	protected static boolean isPrimaryKeyColumn(RelationalPath<?> parent, Path<?> property) {
		return parent.getPrimaryKey() != null && parent.getPrimaryKey().getLocalColumns().contains(property);
	}
}
