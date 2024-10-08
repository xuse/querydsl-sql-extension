package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.sql.DynamicRelationalPath;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.collection.ArrayListMap;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.Mapper;

public class TupleMapper extends AbstractMapperSupport implements Mapper<Tuple> {
	private final int scenario;
	
	private final boolean ignoreKeys;
	
	public String name = "";
	
	public TupleMapper(int scenario, boolean ignoreKeys) {
		this.scenario = scenario;
		this.ignoreKeys = ignoreKeys;
	}
	
	public TupleMapper name(String name) {
		this.name=name;
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Map<Path<?>, Object> createMap(RelationalPath<?> path, Tuple tuple) {
		DynamicRelationalPath entity=(DynamicRelationalPath)path;
		List<Path<?>> paths=entity.getColumns();
		int len=paths.size();
		List<Entry<Path<?>, Object>> data = new ArrayList<>(len);
		Object[] values=tuple.toArray();
		for (int i = 0; i < len; i++) {
			Path<?> p = paths.get(i);
			Object value = values[i];
			ColumnMapping metadata = entity.getColumnMetadata(p);
			if ((scenario == SCENARIO_UPDATE && metadata.isNotUpdate())
					|| (scenario == SCENARIO_INSERT && metadata.isNotInsert())) {
				continue;
			}
			if(isIgnoredKeyColumn(p)) {
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
	
	private boolean isIgnoredKeyColumn(Path<?> p) {
		return ignoreKeys && isKeyColumn(p);
	}
	
	public static final TupleMapper ofNullsAsDefaultBinding(int scenario,boolean ignoreKeys) {
		return new TupleMapper(scenario,ignoreKeys) {
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

	public static final TupleMapper ofNullsBinding(int scenario, boolean ignoreKeys) {
		return new TupleMapper(scenario,ignoreKeys) {
			protected void processNullBindings(Path<?> path, List<Entry<Path<?>, Object>> data,
					ColumnMapping metadata) {
				if (!isKeyColumn(path)) {
					data.add(new Entry<>(path, com.querydsl.sql.types.Null.DEFAULT));
				}
			}
		};
	}

}
