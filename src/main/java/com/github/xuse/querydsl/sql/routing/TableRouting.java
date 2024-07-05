package com.github.xuse.querydsl.sql.routing;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.RoutingStrategy;
import com.querydsl.sql.SchemaAndTable;

/**
 * RoutingStrategy 的常用实现，一般来说不需要跨Schema进行路由。所以就变成了表名调整。
 */
public abstract class TableRouting implements RoutingStrategy {

	@Override
	public SchemaAndTable getOverride(SchemaAndTable schemaAndTable, ConfigurationEx configurationEx) {
		schemaAndTable = configurationEx.getOverride(schemaAndTable);
		return new SchemaAndTable(schemaAndTable.getSchema(), adjustTable(schemaAndTable.getTable()));
	}

	protected abstract String adjustTable(String table);

	/**
	 * 分表场合：为表名添加指定后缀
	 * @param suffix
	 * @return TableRouting
	 */
	public static final TableRouting suffix(String suffix) {
		final String value = StringUtils.trimToEmpty(suffix);
		return new TableRouting() {
			@Override
			protected String adjustTable(String table) {
				return table + value;
			}
		};
	}

	/**
	 * 分表场合：为表名添加指定前缀
	 * @param prefix
	 * @return TableRouting
	 */
	public static final TableRouting prefix(String prefix) {
		final String value = StringUtils.trimToEmpty(prefix);
		return new TableRouting() {
			@Override
			protected String adjustTable(String table) {
				return value + table;
			}
		};
	}
	
	/**
	 * 有多张表需要指定不同的表名修改策略的场合。使用Builder进行构造 
	 * @return Builder.
	 */
	public static final TableRoutingBuilder builder() {
		return new TableRoutingBuilder();
	}
}
