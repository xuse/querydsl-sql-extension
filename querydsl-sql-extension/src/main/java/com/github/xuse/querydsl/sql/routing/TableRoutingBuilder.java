package com.github.xuse.querydsl.sql.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.RelationalPath;

public class TableRoutingBuilder {
	
	private final Map<String,Function<String,String>> map=new HashMap<>();
	
	public TableRouting build() {
		return new TableRouting() {
			@Override
			protected String adjustTable(String table) {
				if (table == null) {
					return table;
				}
				Function<String,String> function=map.get(table.toLowerCase());
				return function == null ? table : function.apply(table);
			}
		};
	}
	
	public TableRoutingBuilder suffix(RelationalPath<?> table, String suffix) {
		final String value = StringUtils.trimToNull(suffix);
		if(value==null) {
			return this;
		}
		map.put(table.getSchemaAndTable().getTable().toLowerCase(), (s) -> s + value);
		return this;
	}
	
	public TableRoutingBuilder prefix(RelationalPath<?> table, String prefix) {
		final String value = StringUtils.trimToNull(prefix);
		if(value==null) {
			return this;
		}
		map.put(table.getSchemaAndTable().getTable().toLowerCase(), (s) -> value + s);
		return this;
	}
}
