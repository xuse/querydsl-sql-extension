package com.github.xuse.querydsl.sql.routing;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.util.StringUtils;
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
	 * For sharding: add a suffix to the table name.
	 * <p>分表场合：为表名添加指定后缀
	 * @param suffix suffix
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
	 * For sharding: add a prefix to the table name.
	 * <p>分表场合：为表名添加指定前缀
	 * @param prefix prefix
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
	 * For sharding: replace the table name entirely.
	 * <p>分表场合：替换表名
	 * @param newName newName
	 * @return TableRouting
	 */
	public static final TableRouting rename(String newName) {
		final String value = StringUtils.trimToEmpty(newName);
		return new TableRouting() {

			@Override
			protected String adjustTable(String table) {
				return value;
			}
		};
	}

	/**
	 * For sharding: find and replace a substring in the table name.
	 * <p>分表场合：查找替换表名中的子串
	 * @param find find
	 * @param replace replace
	 * @return TableRouting
	 */
	public static final TableRouting replaceKey(String find, String replace) {
		return new TableRouting() {

			@Override
			protected String adjustTable(String table) {
				return table.replace(find, replace);
			}
		};
	}

	/**
	 * For scenarios where multiple tables need different routing strategies. Use Builder to construct.
	 * <p>当多张表需要指定不同的表名修改策略时，使用Builder进行构造。
	 *  @return Builder.
	 */
	public static final TableRoutingBuilder builder() {
		return new TableRoutingBuilder();
	}
}
