package com.github.xuse.querydsl.sql;

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
	 * 
	 * @return
	 */
	public com.querydsl.sql.SQLQueryFactory asRaw();

	/**
	 * 返回MySQL的QueryFactory
	 * @return
	 */
	public MySQLQueryFactory asMySQL();

	/**
	 * 返回SQLServer的QueryFactory
	 * @return
	 */
	public SQLServerQueryFactory asSQLServer();

	/**
	 * 返回Oracle的QueryFactory
	 * @return
	 */
	public OracleQueryFactory asOracle();

	/**
	 * 返回PostgreSQL的QueryFactory
	 * @return
	 */
	public PostgreSQLQueryFactory asPostgreSQL();

}
