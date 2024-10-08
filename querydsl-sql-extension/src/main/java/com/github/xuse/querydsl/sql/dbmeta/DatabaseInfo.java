package com.github.xuse.querydsl.sql.dbmeta;

import com.github.xuse.querydsl.sql.dialect.DbType;

public interface DatabaseInfo {
	String getDriverName();

	String getDriverVersion();

	String getNamespace();

	int getDefaultTxIsolation();
	
	DbType getDbType();
}
