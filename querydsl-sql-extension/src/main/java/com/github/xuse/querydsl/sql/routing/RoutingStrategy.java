package com.github.xuse.querydsl.sql.routing;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.SchemaAndTable;

/**
 * This extension provides basic support for simple sharding scenarios. It does
 * not offer business functionalities for sharding operations but focuses on
 * dynamically modifying table names and schema names.
 * <p>
 * Supported features:
 * <ul>
 * <li>[SUPPORT] Dynamically modifies table names and schema names in SQL
 * statements.</li>
 * </ul>
 * <p>
 * The following sharding scenarios are NOT supported and will not be added in
 * the future. They are excluded to adhere to the KISS principle and maintain
 * framework simplicity:
 * <ul>
 * <li>[NOT SUPPORT] Manage multiple data sources and select targets from
 * them</li>
 * <li>[NOT SUPPORT] Rewrite AST for cross-table queries using UNION/UNION
 * ALL</li>
 * <li>[NOT SUPPORT] Reorder results from multiple ResultSets</li>
 * <li>[NOT SUPPORT] Replace table joins with Cartesian product joins</li>
 * </ul>
 * <p>
 * There are two common approaches for sharding implementations:
 * <ol>
 * <li>Handled by the upper-layer business layer</li>
 * <li>Handled at JDBC or lower levels</li>
 * </ol>
 * <p>
 * The latter approach does not require this framework. For upper-layer business
 * sharding, implement the RoutingStrategy interface as needed.
 * <p>
 * <h3>Chinese</h3>
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
 * 上层业务封装分表功能时，可以自行实现接口RoutingStrategy。
 */
public interface RoutingStrategy {
	/**
	 * The default Routing Strategy.
	 * <p>
	 * 默认实现策略，标准的表名.
	 */
	static RoutingStrategy DEFAULT = new RoutingStrategy() {
		/**
		 * 根据配置策略获取修改后的Schema和表名.
		 * <p>
		 * 根据给定的原始Schema和表名以及配置信息，返回经过路由策略处理后的Schema和表名.
		 *
		 * @param schemaAndTable  原始的Schema和表名
		 *                        <p>
		 *                        Original schema and table name
		 * @param configurationEx 配置信息对象
		 *                        <p>
		 *                        Configuration object containing routing rules
		 * @return 根据策略修改后的Schema和表名
		 *         <p>
		 *         Schema and table name modified according to the strategy
		 */
		public SchemaAndTable getOverride(SchemaAndTable schemaAndTable, ConfigurationEx configurationEx) {
			return configurationEx.getOverride(schemaAndTable);
		}
	};

	SchemaAndTable getOverride(SchemaAndTable schemaAndTable, ConfigurationEx configurationEx);
}
