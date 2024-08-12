package com.github.xuse.querydsl.sql.dbmeta;

public interface DatabaseInfo {
	String getDriverName();

	String getDriverVersion();

	String getNamespace();

	int getDefaultTxIsolation();
}
