package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.annotation.query.PathBind;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.TypeUtils;
import com.github.xuse.querydsl.util.Util;
import com.github.xuse.querydsl.util.collection.ArrayListMap;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.Mapper;

/**
 * <h2>English:</h2>
 * A decorator Mapper that supports populating Insert/Update statements from a DTO
 * whose field names or types differ from the target table entity.
 * <p>
 * Uses {@link PathBind} annotations on the DTO class to:
 * <ul>
 *   <li>Map DTO field names to entity field names (via {@link PathBind#value()})</li>
 *   <li>Skip read-only fields (via {@link PathBind#writable()})</li>
 *   <li>Apply write type conversion (via {@link PathBind#writeConverter()} or
 *       {@link PathBind#writeConverterRef()})</li>
 * </ul>
 * </p>
 *
 * <h2>Chinese:</h2>
 * 装饰器 Mapper，支持从字段名或类型与目标表实体不同的 DTO 填充 Insert/Update 语句。
 * <p>
 * 通过 DTO 类上的 {@link PathBind} 注解：
 * <ul>
 *   <li>将 DTO 字段名映射到实体字段名（通过 {@link PathBind#value()}）</li>
 *   <li>跳过只读字段（通过 {@link PathBind#writable()}）</li>
 *   <li>应用写入类型转换（通过 {@link PathBind#writeConverter()} 或
 *       {@link PathBind#writeConverterRef()}）</li>
 * </ul>
 * </p>
 *
 * <p>Usage:</p>
 * <pre>
 * ConvertMapper mapper = ConvertMapper.of(FooDTO.class);
 * factory.insert(fooPath).populate(fooDTO, mapper).execute();
 * factory.update(fooPath).populate(fooDTO, mapper, true).execute();
 * </pre>
 *
 * @author Joey
 */
public class ConvertMapper implements Mapper<Object> {

	private final Class<?> dtoType;

	/**
	 * Cached mapping info: DTO field index → table column name + write converter.
	 * null element means the field is skipped (not writable or no mapping).
	 */
	private volatile FieldMapping[] mappings;

	/**
	 * The BeanCodec for the DTO class (full, cached).
	 */
	private volatile BeanCodec dtoCodec;

	/**
	 * Create a ConvertMapper for the given DTO class.
	 *
	 * @param dtoType the DTO class with optional {@link PathBind} annotations
	 */
	public ConvertMapper(Class<?> dtoType) {
		this.dtoType = dtoType;
	}

	/**
	 * Factory method.
	 *
	 * @param dtoType the DTO class
	 * @return a new ConvertMapper instance
	 */
	public static ConvertMapper of(Class<?> dtoType) {
		return new ConvertMapper(dtoType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Object bean) {
		RelationalPathEx<?> path = RelationalPathExImpl.toRelationPathEx(entity);
		ensureInitialized();

		// Extract all values from DTO using its BeanCodec
		Object[] values = dtoCodec.values(bean);

		// Build the result map: table Path → converted value
		List<Entry<Path<?>, Object>> data = new ArrayList<>();
		for (int i = 0; i < mappings.length; i++) {
			FieldMapping mapping = mappings[i];
			if (mapping == null) {
				continue; // Skipped: not writable or no mapping
			}
			Path<?> columnPath = path.getColumn(mapping.targetColumnName);
			if (columnPath == null) {
				continue; // Table doesn't have this column
			}
			Object value = values[i];
			Function converter = mapping.writeConverter;
			if (converter != null) {
				value = converter.apply(value);
			}
			if (value != null) {
				data.add(new Entry<>(columnPath, value));
			}
		}
		return ArrayListMap.wrap(data);
	}

	private void ensureInitialized() {
		if (mappings == null) {
			synchronized (this) {
				if (mappings == null) {
					buildMappings();
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void buildMappings() {
		BeanCodec codec = BeanCodecManager.getInstance().getCodec(dtoType);
		Property[] fields = codec.getFields();
		FieldMapping[] result = new FieldMapping[fields.length];

		for (int i = 0; i < fields.length; i++) {
			Property field = fields[i];
			String fieldName = field.getName();
			PathBind pathBind = field.getAnnotation(PathBind.class);

			// Check writable flag
			if (pathBind != null && !pathBind.writable()) {
				result[i] = null; // Read-only, skip on write
				continue;
			}

			// Determine target column name
			String targetColumnName = (pathBind != null) ? pathBind.value() : fieldName;

			// Determine write converter
			Function writeConverter = resolveWriteConverter(pathBind, field);

			result[i] = new FieldMapping(targetColumnName, writeConverter);
		}

		this.dtoCodec = codec;
		this.mappings = result;
	}

	@SuppressWarnings("rawtypes")
	private Function resolveWriteConverter(PathBind pathBind, Property field) {
		if (pathBind == null) {
			return null;
		}
		// Check writeConverter class
		Class<? extends Function> writeClass = pathBind.writeConverter();
		if (writeClass != Function.class) {
			return (Function) TypeUtils.newInstance(writeClass);
		}
		// Check writeConverterRef
		String ref = pathBind.writeConverterRef();
		if (!ref.isEmpty()) {
			return Util.getStaticFunctionField(dtoType, ref, field.getName());
		}
		// No explicit write converter
		return null;
	}

	private static class FieldMapping {
		final String targetColumnName;
		@SuppressWarnings("rawtypes")
		final Function writeConverter;

		@SuppressWarnings("rawtypes")
		FieldMapping(String targetColumnName, Function writeConverter) {
			this.targetColumnName = targetColumnName;
			this.writeConverter = writeConverter;
		}
	}
}
