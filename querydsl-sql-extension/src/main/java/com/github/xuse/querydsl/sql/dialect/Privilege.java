package com.github.xuse.querydsl.sql.dialect;

public enum Privilege {
	/**
	 * See <a href="https://dev.mysql.com/doc/refman/5.7/en/grant.html#grant-overview">...</a>
	 * Enable database and table creation. Levels: Global, database, table.
	 */
	CREATE,
	/**
	 * Enable databases, tables, and views to be dropped. Levels: Global, database, table.
	 */
	DROP,
	/**
	 * 	Enable use of ALTER TABLE. Levels: Global, database, table.
	 */
	ALTER,
	
	/**
	 * Enable indexes to be created or dropped. Levels: Global, database, table.
	 */
	INDEX,
	
	/**
	 * Enable foreign key creation. Levels: Global, database, table, column.
	 */
	REFERENCES,
	
	CREATE_TEMPORARY_TABLES,
	
	CREATE_VIEW,
	/**
	 * Enable privileges to be granted to or removed from other accounts. Levels: Global, database, table, routine, proxy.
	 */
	GRANT_OPTION
}
