package com.github.xuse.querydsl.sql;

import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.querydsl.sql.mssql.SQLServerQueryFactory;
import com.querydsl.sql.mysql.MySQLQueryFactory;
import com.querydsl.sql.oracle.OracleQueryFactory;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;

/**
 * 增强后的SQLFactory
 * @author Administrator
 *
 */
public interface ISQLFactoryEx {
	/**
	 * 返回QueryDSL原生版本的SQLQueryFactory
	 * @return com.querydsl.sql.SQLQueryFactory
	 */
	public com.querydsl.sql.SQLQueryFactory asRaw();

	/**
	 * 返回QueryDSL原生版本的MySQL的QueryFactory
	 * @return MySQLQueryFactory
	 */
	public MySQLQueryFactory asMySQL();

	/**
	 * 返回QueryDSL原生版本的SQLServer的QueryFactory
	 * @return SQLServerQueryFactory
	 */ 
	public SQLServerQueryFactory asSQLServer();

	/**
	 * 返回QueryDSL原生版本的Oracle的QueryFactory
	 * @return OracleQueryFactory
	 */
	public OracleQueryFactory asOracle();

	/**
	 * 返回QueryDSL原生版本的PostgreSQL的QueryFactory
	 * @return PostgreSQLQueryFactory
	 */
	public PostgreSQLQueryFactory asPostgreSQL();
	
	/**
	 * @return 返回SQLMetadataQueryFactory对象，这是query-dsl-extension开发的一个DDL操作框架
	 */
	public SQLMetadataQueryFactory getMetadataFactory();

}
