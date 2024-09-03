package com.github.xuse.querydsl.sql.dialect;

import java.sql.Types;
import java.util.Date;

import com.github.xuse.querydsl.init.DDLLockUtils;
import com.github.xuse.querydsl.init.DataInitLog;
import com.github.xuse.querydsl.init.QDataInitLog;
import com.github.xuse.querydsl.sql.DynamicRelationalPath;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.querydsl.core.types.Path;
import com.querydsl.sql.ColumnMetadata;

import lombok.extern.slf4j.Slf4j;

/**
 * 检查是否具有完整的Create table, alter table等权限
 *
 *
 */
@Slf4j
public final class SimpleDetector implements PrivilegeDetector {
	private static final String PERMISSION_ENTRY_NAME = "#ddl_detector";

	/**
	 * 测试用表名，会尝试执行一个建表动作,如果建表成功，证明当前数据库用户拥有DDL权限。 此配置项一般无需修改。
	 */
	private final String detectorTable = "querydsl_opevent_log";

	private boolean executeCheck(SQLQueryFactory factory) {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		TableRouting routing = TableRouting.rename(detectorTable);
		try {
			if (metadata.existsTable(table.getSchemaAndTable(), routing)) {
				log.info("table {} exists, try to truncate.", detectorTable);
				metadata.truncate(table).withRouting(routing).execute();
			} else {
				log.info("table {} not exist, try to create.", detectorTable);
				metadata.createTable(table).withRouting(routing).ifExists().execute();
			}
		} catch (Exception e) {
			log.error("DDL Detect failure! ALL ddl features will be disabled.", e);
			return false;
		}
		return true;
	}

	private int updateExistResult(SQLQueryFactory factory,boolean result) {
		QDataInitLog t = QDataInitLog.dataInitLog;
		int value = result ? 1 : 2;
		int record = (int) factory.update(t).set(t.disabled, value).set(t.lastInitTime, new Date())
				.where(t.tableName.eq(PERMISSION_ENTRY_NAME), t.disabled.eq(0)).execute();
		log.info("update #ddl_detector set to {}, records={}", result, record);
		return record;
	}

	private DataInitLog getExistResult(SQLQueryFactory factory) {
		QDataInitLog t = QDataInitLog.dataInitLog;
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		if (metadata.existsTable(t.getSchemaAndTable(), null)) {
			DataInitLog record = factory.selectFrom(t).where(t.tableName.eq(PERMISSION_ENTRY_NAME)).fetchFirst();
			if (record == null) {
				try {
					record = new DataInitLog();
					record.setTableName(PERMISSION_ENTRY_NAME);
					record.setDisabled(0);
					record.setLastInitUser(DDLLockUtils.getAgent());
					record.setLastInitTime(new Date());
					record.setLastInitResult("create record");
					factory.insert(t).populate(record).execute();
				} catch (Exception e) {
					log.warn("insert detector record error:", e);
					record = null;
				}
			}
			return record;
		}
		return null;
	}

	@Override
	public boolean check(SQLQueryFactory factory, Privilege... privileges) {
		DataInitLog existResult= getExistResult(factory);
		if (existResult == null) {
			boolean result = executeCheck(factory);
			return result;
		} else if (existResult.getDisabled() == 0) {
			boolean result = executeCheck(factory);
			updateExistResult(factory,result);
			return result;
		}
		return existResult.getDisabled()==1;
	}
	

	private final DynamicRelationalPath table;

	{
		table = new DynamicRelationalPath("t1", null, "querydsl_opevent_log");
		Path<String> name = table
				.addColumn(String.class, ColumnMetadata.named("name").ofType(Types.VARCHAR).notNull().withSize(32))
				.build();
		table.createPrimaryKey(name);
	}
}
