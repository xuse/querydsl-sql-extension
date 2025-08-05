package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.querydsl.core.types.Path;

public interface BindingProvider {

	/**
	 * @return 用于形成cacheKey。确保标志性即可
	 */

	List<String> fieldNames();

	/**
	 * 
	 * @return 优化性能用，不要求准确
	 */
	int size();

	List<String> names(Map<String, FieldProperty> fieldOrder);

	Class<?> getType(String name,FieldProperty property);
	
	
	
	public static class ListPathBindings implements BindingProvider{
		private final List<String> fieldNames = new ArrayList<>();
		private final Map<String,Class<?>> types=new HashMap<>();
		
		public ListPathBindings(List<Path<?>> columns) {
			for(Path<?> p:columns) {
				String name=p.getMetadata().getName();
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
	public static class RelationalPathBindings implements BindingProvider{
		private final RelationalPathEx<?> table;
		private final List<String> fieldNames = new ArrayList<>();
		
		public RelationalPathBindings(RelationalPathEx<?> b) {
			this.table=b;
			for(Path<?> p:b.getColumns()) {
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
			Path<?> path=table.getColumn(name);
			if(path!=null) {
				return path.getType();
			}
			return null;
		}
	}
}
