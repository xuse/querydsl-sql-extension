package com.github.xuse.querydsl.sql;

import com.github.xuse.querydsl.init.TableDataInitializer;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.extension.ExtensionQueryFactory;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.mssql.SQLServerQueryFactory;
import com.querydsl.sql.oracle.OracleQueryFactory;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;

/**
 * 增强后的SQLFactory
 * @author Administrator
 */
public interface SQLFactoryExtension {

	/**
	 *  返回QueryDSL原生版本的SQLQueryFactory
	 *  @return com.querydsl.sql.SQLQueryFactory
	 */
	com.querydsl.sql.SQLQueryFactory asRaw();

	/**
	 * 返回QueryDSL原生版本的MySQL的QueryFactory.
	 * 可以使用一些MySQL特有的语法，比如
	 * <ul><li>MySQLQueryFactory#insertIgnore</li>
	 * <li>MySQLQueryFactory#insertOnDuplicateKeyUpdate</li>
	 * </ul>
	 * 等功能。
	 * @return MySQLQueryFactory
	 */
	MySQLQueryFactory2 asMySQL();

	/**
	 *  返回QueryDSL原生版本的SQLServer的QueryFactory
	 *  @return SQLServerQueryFactory
	 */
	SQLServerQueryFactory asSQLServer();

	/**
	 *  返回QueryDSL原生版本的Oracle的QueryFactory
	 *  @return OracleQueryFactory
	 */
	OracleQueryFactory asOracle();

	/**
	 *  返回QueryDSL原生版本的PostgreSQL的QueryFactory
	 *  @return PostgreSQLQueryFactory
	 */
	PostgreSQLQueryFactory asPostgreSQL();

	/**
	 *  @return 返回SQLMetadataQueryFactory对象，这是query-dsl-extension开发的一个DDL操作框架
	 */
	SQLMetadataQueryFactory getMetadataFactory();

	TableDataInitializer initializeTable(RelationalPath<?> table);
	
	/**
	 * 供扩展使用，用于封装出各种符合用户自己习惯的QueryFactory
	 * @param <T> the type of query factory.
	 * @param clz the type of query factory.
	 * @return ExtensionQueryFactory
	 */
	<T extends ExtensionQueryFactory> T asExtension(Class<T> clz);
}
