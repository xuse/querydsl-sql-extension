package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.sql.Mappers;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.expression.BindingProvider.RelationalPathBindings;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.collection.ArrayListMap;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.Mapper;

/**
 * A Mapper that using dynamic codec class to extract values from the
 * entity-bean. This class has many variations and is used to define different
 * database write behaviors for null fields in Beans. When you save a java
 * object into database. and the field value is null ——
 *
 * <ol>
 * <li>null is write as null: set a NULL value to the column in database.</li>
 * <li>null is write as 'DEFAULT':SET column=DEFAULT in database. `DEFAULT` is a
 * database keyword that references the default value defined for a column
 * during table creation.</li>
 * <li>null is ignored: the field that has null value will be ignored in the
 * final SQL, It means will leave the original value of database along.</li>
 * </ol>
 * Finally, it should be noted that null values do not only refer to `null` in
 * Java, but also include other user-defined {@link UnsavedValue}.
 *
 * <h2>中文</h2> 用于为Bean中的null字段定义不同的数据库写入行为。 Object中的null在写入数据库时，一般有以下三种差异
 * <ol>
 * <li>null写作null：在数据库中将该列设置为NULL值。</li>
 * <li>null写作 'DEFAULT'：在数据库中执行 `SET column=DEFAULT`。`DEFAULT`
 * 是一个数据库关键词，引用了在表创建期间为列定义的默认值。</li>
 * <li>null值被忽略：最后的SQL语句中将忽略具有空值的字段，这意味着将保留数据库中的原始值。这一般发生在选择性更新中。</li>
 * </ol>
 * 最后要注意的是，null值并不仅仅指java中的null，也包含其他用户定义的 {@link UnsavedValue}
 *
 * @see Mapper
 * @see UnsavedValue
 */
public class AdvancedMapper extends AbstractMapperSupport implements Mapper<Object> {

	private final int scenario;

	public String name = "";

	/**
	 * Create a Mapper Object, which will have `null` values written to the values
	 * Map (if it is not a column in the primary key).
	 *
	 * 非主键字段，将会在值的Map中写入Null值。 简单来说，即是null is written as null.
	 * 
	 * @param scenario scenario
	 * @return AdvancedMapper
	 */
	public static final AdvancedMapper ofNullsBinding(int scenario) {
		return new AdvancedMapper(scenario) {

			protected void processNullBindings(Path<?> path, List<Entry<Path<?>, Object>> data,
					ColumnMapping metadata) {
				if (!isKeyColumn(path)) {
					data.add(new Entry<>(path, com.querydsl.sql.types.Null.DEFAULT));
				}
			}
		};
	}

	/**
	 * In some databases, you can use the `DEFAULT` keyword directly in the `VALUES`
	 * area of an SQL statement. This allows you to insert the column's default
	 * value as defined at table creation by simply writing `DEFAULT` instead of
	 * specifying an explicit value.
	 *
	 * 直接在SQL语句的Value区域写入DEFAULT关键字。在某些数据库上有用。 简单来说，即是null is written as 'DEFAULT'。
	 * 一般用于插入数据的场合
	 *
	 * @param scenario scenario
	 * @return AdvancedMapper
	 */
	public static final AdvancedMapper ofNullsAsDefaultBinding(int scenario) {
		return new AdvancedMapper(scenario) {

			protected void processNullBindings(Path<?> path, List<Entry<Path<?>, Object>> data,
					ColumnMapping metadata) {
				if (isKeyColumn(path)) {
					return;
				}
				if (metadata != null && !metadata.isNullable()) {
					// set use the database default.
					data.add(new Entry<>(path, Expressions.template(path.getType(), "DEFAULT")));
				} else {
					// set as null
					data.add(new Entry<>(path, com.querydsl.sql.types.Null.DEFAULT));
				}
			}
		};
	}

	public AdvancedMapper(int scenario) {
		this.scenario = scenario;
	}

	public AdvancedMapper name(String name) {
		this.name = name;
		return this;
	}

	public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Object bean) {
		RelationalPathEx<?> path = RelationalPathExImpl.toRelationPathEx(entity);
		BeanCodec bc = getBeanCodec(path, bean);
		return createMapOptimized(path, bean, bc);
	}

	public static BeanCodec getBeanCodec(RelationalPathEx<?> entity, Object bean) {
		if (entity.getType().isInstance(bean)) {
			return entity.getBeanCodec();
		} else {
			return BeanCodecManager.getInstance().getCodec(bean.getClass(), new RelationalPathBindings(entity));
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Create the property map using ASM generated class.
	 *
	 * @param entity entity
	 * @param bean   bean
	 * @return 映射路径对象
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<Path<?>, Object> createMapOptimized(RelationalPathEx entity, Object bean, BeanCodec bc) {
		List<Path<?>> path = entity.getColumns();
		Object[] values = bc.values(bean);
		int len = path.size();
		List<Entry<Path<?>, Object>> data = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			Object value = values[i];
			Path<?> p = path.get(i);
			ColumnMapping metadata = entity.getColumnMetadata(p);
			if ((scenario == Mappers.SCENARIO_UPDATE && metadata.isNotUpdate())
					|| (scenario == Mappers.SCENARIO_INSERT && metadata.isNotInsert())) {
				continue;
			}
			boolean nullValue = isUnsavedValue(metadata, value);
			// 如果具有自动生成类型
			AutoGenerated generated;
			if ((generated = metadata.getGenerated()) != null && (generated.overwrite() || nullValue)) {
				Object autoValue = asAutoValue(generated, metadata, scenario);
				if (autoValue != null) {
					value = autoValue;
					nullValue = false;
					if (generated.writeback()) {
						metadata.writeback(bean, value);
					}
				}
			}
			if (nullValue) {
				processNullBindings(p, data, metadata);
			} else {
				// 处理空值是否绑定
				data.add(new Entry<>(p, value));
			}
		}
		return ArrayListMap.wrap(data);
	}
}
