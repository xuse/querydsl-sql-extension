package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.UnsavedValuePredicateFactory;
import com.github.xuse.querydsl.util.ArrayListMap;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.QueryException;
import com.querydsl.core.types.Expression;
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
public class AdvancedMapper {
	/**
	 * Singleton instance
	 * 空值绑定会被跳过，因此在Batch模式下根据参数中的空值不同，很容易生成多组SQL语句。
	 * 当多组SQL执行executeWithKey时，会抛出异常，因此执行批量插入并且获取Key时，不能使用这种Mapper。
	 */
	public static AdvancedMapper INSTANCE = new AdvancedMapper(0);
	
	/**
	 * 空值绑定会向Statement中执行setNull(index, sqlType). 适合于批量模式下统一SQL语句。
	 * 但是对于带有自生成如自增键、缺省值等字段，尝试写入NULL可能导致异常。
	 */
	public static AdvancedMapper INSTANCE_NULLS_BINGIND = ofNullsBingding(0);
	
	
	private int scenario;
	
	public static final int SCENARIO_INSERT=1;
	
	public static final int SCENARIO_UPDATE=2;
	
	
	public static final AdvancedMapper ofNullsBingding(int scenario) {
		return new AdvancedMapper(scenario) {
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
	}
	
	/**
	 * 直接在SQL语句的Value区域写入DEFAULT关键字。在某些数据库上有用。
	 */	
	public static final AdvancedMapper ofNullsAsDefaultBingding(int scenario) {
		return new AdvancedMapper(scenario) {
			protected void processNullBindings(ColumnMapping metadata, List<Entry<Path<?>, Object>> data,Path<?> path) {
				data.add(new Entry<>(path, Expressions.template(path.getType(), "DEFAULT")));
			}
			protected void processNullBindings(RelationalPath<?> entity,List<Entry<Path<?>, Object>> data, Path<?> path) {
				data.add(new Entry<>(path, Expressions.template(path.getType(), "DEFAULT")));
			}	
		};
	}
	
	public AdvancedMapper(int scenario) {
		this.scenario=scenario;
	}
	
	@SuppressWarnings("rawtypes")
	public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Object bean, ConfigurationEx config) {
		if (entity instanceof IRelationPathEx && entity.getType().isAssignableFrom(bean.getClass())) {
			return createMapOptimized((IRelationPathEx) entity, bean,config);
		} else {
			return createMap0(entity, bean,config);
		}
	}

	/**
	 * Create the property map using ASM generated class.
	 * @param entity
	 * @param bean
	 * @return
	 */
	@SuppressWarnings({"rawtypes","unchecked"}) 
	private Map<Path<?>, Object> createMapOptimized(IRelationPathEx entity, Object bean, ConfigurationEx config) {
		List<Path<?>> path = entity.getColumns();
		BeanCodec bc = entity.getBeanCodec();
		Object[] values = bc.values(bean);
		int len = path.size();
		List<Entry<Path<?>, Object>> data = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			Object value = values[i];
			Path<?> p=path.get(i);
			ColumnMapping metadata=entity.getColumnMetadata(p);
			// 如果具有自动生成类型
			if (metadata.getGenerated() != null) {
				value = asAutoValue(metadata.getGenerated(), metadata, value,scenario,config);
			}	
			if (!isNullValue(metadata, value)) {
				data.add(new Entry<>(p, value));
			} else{
				//处理空值是否绑定
				processNullBindings(metadata,data,p);
				
			}
		}
		return ArrayListMap.wrap(data);
	}
	
	//根据自动生成值注解返回要写入的自动数据
	/**
	 * @param generateDef
	 * @param metadata 可能为NULL。当用户使用非默认的bean进行数据库操作时，该字段可能为null
	 * @param value
	 * @return
	 */
	public static Object asAutoValue(AutoGenerated generateDef, ColumnMapping metadata, Object value, int scenario, ConfigurationEx config) {
		//如果已经设置了值，并且不覆盖，那么就采用输入的原值
		if(value!=null && !generateDef.overwrite()) {
			return value;
		}
		switch(generateDef.value()) {
		case CREATED_TIMESTAMP:
			if(scenario==SCENARIO_INSERT){
				return Expressions.currentTimestamp();
			}
			break;
		case UPDATED_TIMESTAMP:
			if(scenario>0){
				return Expressions.currentTimestamp();
			}
			break;
		case GUID36:
			if(scenario==SCENARIO_INSERT){
				UUID uuid = UUID.randomUUID();
				return uuid.toString();
			}
			break;
		case GUID32:
			if(scenario==SCENARIO_INSERT){
				UUID uuid = UUID.randomUUID();
				return uuid2String32(uuid.getMostSignificantBits(),uuid.getLeastSignificantBits());
			}
		case SNOWFLAKE:
			if(scenario==SCENARIO_INSERT){
				if(config.getSnowflakeWorker()!=null){
					return config.getSnowflakeWorker().nextId();
				}else {
					throw Exceptions.illegalArgument("Please init snowflake workerId and datacenterId for @AutoGenerated type 'SNOWFLAKE'!");
				}
			}
		default:
			throw new IllegalArgumentException("Unsupported auto-generate type:"+generateDef.value());
		}
		return value;
	}

	private static String uuid2String32(long mostSigBits, long leastSigBits) {
		return (digits(mostSigBits >> 32, 8) +  digits(mostSigBits >> 16, 4) + digits(mostSigBits, 4) 
				+ digits(leastSigBits >> 48, 4) + digits(leastSigBits, 12));
	}

	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}
	    
	protected void processNullBindings(ColumnMapping metadata, List<Entry<Path<?>, Object>> data,Path<?> p) {
	}
	protected void processNullBindings(RelationalPath<?> entity,List<Entry<Path<?>, Object>> data, Path<?> path) {
	}

	private Map<Path<?>, Object> createMap0(RelationalPath<?> entity, Object bean, ConfigurationEx config) {
		try {
			Class<?> beanClass = bean.getClass();
			Map<String, Path<?>> columns = getColumns(entity);
			List<Entry<Path<?>, Object>> data = new ArrayList<>(columns.size());
			for (Map.Entry<String, Path<?>> entry : columns.entrySet()) {
				Path<?> path = entry.getValue();
				//性能比较差
				Field beanField = ReflectionUtils.getFieldOrNull(beanClass, entry.getKey());
				if (beanField != null && !Modifier.isStatic(beanField.getModifiers())) {
					beanField.setAccessible(true);
					Object propertyValue = beanField.get(bean);
					
					AutoGenerated anno=beanField.getAnnotation(AutoGenerated.class);
					if(anno!=null) {
						propertyValue = asAutoValue(anno, null, propertyValue,scenario,config);
					}
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
		UnsavedValue anno= field.getAnnotation(UnsavedValue.class);
		return UnsavedValuePredicateFactory.create(field.getType(),anno==null?null:anno.value()).test(propertyValue);
	}
	
	public static boolean isNullValue(ColumnMapping columnMetadata, Object value) {
        if (value instanceof Expression<?>) {
        	return false;
        }
		return columnMetadata.isUnsavedValue(value);
	}

	protected Map<String, Path<?>> getColumns(RelationalPath<?> path) {
		Map<String, Path<?>> columns = new LinkedHashMap<>();
		for (Path<?> column : path.getColumns()) {
			columns.put(column.getMetadata().getName(), column);
		}
		return columns;
	}

	protected static boolean isPrimaryKeyColumn(RelationalPath<?> parent, Path<?> property) {
		return parent.getPrimaryKey() != null && parent.getPrimaryKey().getLocalColumns().contains(property);
	}
}
