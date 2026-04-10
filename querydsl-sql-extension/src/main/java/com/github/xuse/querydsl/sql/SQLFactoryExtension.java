package com.github.xuse.querydsl.sql;

import com.github.xuse.querydsl.init.TableDataInitializer;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.extension.ExtensionQueryFactory;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.mssql.SQLServerQueryFactory;
import com.querydsl.sql.oracle.OracleQueryFactory;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;

/**
 * <h2>English:</h2> Enhanced SQLFactory interface.
 * <h2>Chinese:</h2> 增强后的SQLFactory接口
 * @author Administrator
 */
public interface SQLFactoryExtension {

	/**
	 * Return the raw QueryDSL SQLQueryFactory.
	 * <p>返回QueryDSL原生版本的SQLQueryFactory
	 *  @return com.querydsl.sql.SQLQueryFactory
	 */
	com.querydsl.sql.SQLQueryFactory asRaw();

	/**
	 * Return the MySQL-specific QueryFactory. Supports MySQL-specific syntax such as:
	 * <ul><li>MySQLQueryFactory#insertIgnore</li>
	 * <li>MySQLQueryFactory#insertOnDuplicateKeyUpdate</li>
	 * </ul>
	 * <p>返回MySQL专用的QueryFactory，可以使用MySQL特有的语法。
	 * @return MySQLQueryFactory
	 */
	MySQLQueryFactory2 asMySQL();

	/**
	 * Return the SQLServer-specific QueryFactory.
	 * <p>返回SQLServer专用的QueryFactory
	 *  @return SQLServerQueryFactory
	 */
	SQLServerQueryFactory asSQLServer();

	/**
	 * Return the Oracle-specific QueryFactory.
	 * <p>返回Oracle专用的QueryFactory
	 *  @return OracleQueryFactory
	 */
	OracleQueryFactory asOracle();

	/**
	 * Return the PostgreSQL-specific QueryFactory.
	 * <p>返回PostgreSQL专用的QueryFactory
	 *  @return PostgreSQLQueryFactory
	 */
	PostgreSQLQueryFactory asPostgreSQL();

	/**
	 * Return the SQLMetadataQueryFactory, a DDL operation framework provided by querydsl-sql-extension.
	 * <p>返回SQLMetadataQueryFactory对象，这是querydsl-sql-extension提供的DDL操作框架
	 *  @return 返回SQLMetadataQueryFactory对象
	 */
	SQLMetadataQueryFactory getMetadataFactory();

	TableDataInitializer initializeTable(RelationalPath<?> table);
	
	/**
	 * For extension use, to wrap into various QueryFactory implementations that suit user preferences.
	 * <p>供扩展使用，用于封装出符合用户自己习惯的QueryFactory
	 * @param <T> the type of query factory.
	 * @param clz the type of query factory.
	 * @return ExtensionQueryFactory
	 */
	<T extends ExtensionQueryFactory> T asExtension(Class<T> clz);
}
