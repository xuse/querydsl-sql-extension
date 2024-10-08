package com.github.xuse.querydsl.sql.routing;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.SchemaAndTable;

/**
 * 该扩展为最简单的分表场景提供一些支持。不提供分表操作业务功能，仅针对表名动态变化的诉求做一些支持。
 * 支持的特性
 * <ul>
 * <li>[SUPPORT] 对SQL中的表名和Schema名称进行修改。</li>
 * </ul>
 * <p>
 * 不支持以下分表场景，今后也不会添加这些功能。不是做不了，而是违反了KISS原则，不适合在此框架中完成。
 * <ul>
 * <li>[NOT SUPPORT] 管理多数据源，并在多数据源中选择目标</li>
 * <li>[NOT SUPPORT] 改写AST，在多个表中查询，UNION或UNION ALL查询</li>
 * <li>[NOT SUPPORT] 在多个ResultSet中进行重排序。</li>
 * <li>[NOT SUPPORT] 将表Join换为笛卡尔积的相互Join</li>
 * </ul>
 * <p>
 * 分表方案无非是两种
 * 1. 上层业务层自行处理。
 * 2. JDBC或以下层次处理，
 * <p>
 * 后者无需使用本框架任何功能。
 * 上层业务封装分表功能时，可以使用RoutingStrategy作为工具，自行开发。
 */
public interface RoutingStrategy {

	static RoutingStrategy DEFAULT = new RoutingStrategy() {
		public SchemaAndTable getOverride(SchemaAndTable schemaAndTable, ConfigurationEx configurationEx) {
			return configurationEx.getOverride(schemaAndTable);
		}
	};

	SchemaAndTable getOverride(SchemaAndTable schemaAndTable, ConfigurationEx configurationEx);
}
