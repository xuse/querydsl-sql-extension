package com.github.xuse.querydsl.sql.dialect;

import com.github.xuse.querydsl.util.ArrayUtils;

public enum DbType {
	mysql,
	mariadb, 
	oracle, 
	db2,
	h2,
	hsql,
	sqlite, 
	postgresql, 
	sqlserver2005("SQLServer2005"),
	sqlserver("SQLServer"), 
	dm("达梦"), 
	xugu("虚谷"), 
	phoenix("Phoenix HBase"),
	zenith("Gauss"), 
	clickhouse,
	gbase8s("南大通用/Gbase8S","gbasedbt","gbasedbt_sqli"),
	gbase8c("南大通用 GBase 8c"),
	sinodb("星瑞格"),
	oscar("神通"),
	sybase("Sybase ASE"),
	oceanbase,
	Firebird,
	highgo("瀚高"),
	cubrid,
	sundb,
	hana("SAP_HANA"),
	impala, 
	vertica, 
	xcloud("行云"),
	redshift,
	openGauss("华为 opengauss"),
	gaussdb, 
	TDengine("taos", "taos-rs"),
	informix, 
	uxdb("优炫"),
	lealone,
	trino, 
	presto, 
	jtds("SQLServer And Sybase driver"), 
	derby, 
	hive, 
	kingbase("人大金仓","kingbase8"), 
	odps,
	teradata, 
	edb, 
	kylin, 
	ads,
	elastic_search, 
	hbase, 
	drds, 
	blink, 
	antspark, 
	oceanbase_oracle, 
	polardb, 
	ali_oracle,
	mock, 
	greenplum, 
	tidb,
	tydb,
	starrocks,
	ingres, 
	cloudscape, 
	timesten, 
	as400, 
	sapdb, 
	kdb, 
	log4jdbc,
	firebirdsql, 
	JSQLConnect,
	JTurbo, 
	interbase, 
	pointbase, 
	edbc,
	mimer,
	other;

	private DbType() {
		name = name();
		this.alias=ArrayUtils.EMPTY_STRING_ARRAY;
	}

	private DbType(String name,String... alias) {
		this.name = name;
		this.alias=alias;
	}
	public final String name;
	
	public final String[] alias;

	public static DbType ofAlias(String s) {
		for(DbType type:values()) {
			if(type.alias.length>0) {
				for(String a:type.alias) {
					if(a.equals(s)) {
						return type;
					}
				}
			}
		}
		return null;
	}
}
