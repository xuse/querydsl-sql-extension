package com.github.xuse.querydsl.sql.dbmeta;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.xuse.querydsl.sql.dialect.SchemaPolicy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public final class DriverInfo implements DatabaseInfo{
	String driverName;

	String driverVersion;

	String databaseProductName;

	String dataProductVersion;

	String schema;

	String catalog;
	
	int defaultTxIsolation;

	final Map<String, String> settings = new LinkedHashMap<>();
	
	SchemaPolicy policy;

	long dbTimeDelta;

	public String getNamespace() {
		return policy.toNamespace(catalog, schema);
	}
	
	public Date getDatabaseTime() {
		return new Date(System.currentTimeMillis() + dbTimeDelta); 
	}

	public void setDbTimeDelta(long dbTimeDelta) {
		this.dbTimeDelta = dbTimeDelta;
		if (Math.abs(dbTimeDelta) > 30000) {
			// 数据库时间和当前系统时间差距在30秒以上时，警告
			log.warn(
					"The time of this server is [{}], but database is [{}]. Please adjust date time via any NTP server.",
					new Date(), getDatabaseTime());
		} else {
			log.info("The timestamp is {}, difference between database and this machine is {}ms.", getDatabaseTime(),
					dbTimeDelta);
		}
	}
}