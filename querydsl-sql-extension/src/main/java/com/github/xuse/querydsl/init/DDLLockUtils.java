package com.github.xuse.querydsl.init;

import java.util.Date;

import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.util.ProcessUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DDLLockUtils {
	public static String getAgent() {
		return ProcessUtil.getPid() + "@" + ProcessUtil.getHostname() + "(" + ProcessUtil.getLocalIp() + ") OS:"
				+ ProcessUtil.getOSName();
	}
	
	 public static DataInitLog initInitLogTable(SQLQueryFactory factory) {
		QDataInitLog t = QDataInitLog.dataInitLog;
		SQLMetadataQueryFactory metadata= factory.getMetadataFactory();
		try {
			if(!factory.getConfiguration().isMissDDLPermissions()) {
				metadata.createTable(t).ifExists().execute();	
			}
		}catch(Exception e) {
			//建表失败情况下，日志表功能禁用.
			log.error("create table {} failure. init log table feature disabled.",t.getSchemaAndTable(),e);
			return null;
		}
		try {
			DataInitLog globalRecord = factory.selectFrom(t).where(t.tableName.eq("*")).fetchFirst();
			if (globalRecord == null) {
				globalRecord = new DataInitLog();
				globalRecord.setTableName("*");
				globalRecord.setLastInitUser(getAgent());
				globalRecord.setLastInitTime(new Date());
				globalRecord.setLastInitResult("create record");
				factory.insert(t).populate(globalRecord).execute();
			}	
			return globalRecord;
		}catch(Exception e) {
			//读写数据错误情况下，日志表功能禁用
			log.error("Global Initlog record load failure. init log table feature disabled.", e);
			return null;
		}
	}
}
