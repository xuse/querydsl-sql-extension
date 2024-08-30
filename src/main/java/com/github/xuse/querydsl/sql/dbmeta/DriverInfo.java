package com.github.xuse.querydsl.sql.dbmeta;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.github.xuse.querydsl.sql.dialect.DbType;
import com.github.xuse.querydsl.sql.dialect.SchemaPolicy;
import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.Enums;
import com.github.xuse.querydsl.util.JDKEnvironment;
import com.github.xuse.querydsl.util.StringUtils;

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
	
	String url;
	
	DbType dbType;

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
	
	public void setUrl(String url) {
		this.url=url;
		this.dbType=parseDbType(url);
	}

	private DbType parseDbType(String url) {
		if(url.startsWith("jdbc:")) {
			String s=url.substring(5);
			s=StringUtils.substringBefore(s, ":");
			DbType dbType=Enums.valueOf(DbType.class, s, null);
			if(dbType==null) {
				dbType = DbType.ofAlias(s);
			}
			if(dbType!=null) {
				return dbType;
			}
		}
		log.warn("Can not determine dbtype for {}",url);
		return DbType.other;
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder("Database ");
		sb.append(dbType).append(" Connected.");
		sb.append(databaseProductName).append(' ').append(dataProductVersion);
		sb.append(" @").append(url).append('\n').append("JDBC Driver:").append(driverName).append(' ').append(driverVersion);
		sb.append(" in ").append(catalog).append('/').append(schema).append(" Isolation=").append(defaultTxIsolation);
		sb.append(" CurrentTime=[").append(DateFormats.TIME_STAMP_CS.format(getDatabaseTime()));
		sb.append("] jvm=").append(JDKEnvironment.JVM_VERSION).append(" Charset=").append(Charset.defaultCharset()).append(" Locale=").append(Locale.getDefault());
		sb.append(" Timezone=").append(TimeZone.getDefault().toZoneId());
		return sb.toString();
	}
}