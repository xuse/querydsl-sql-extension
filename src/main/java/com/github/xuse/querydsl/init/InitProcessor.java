package com.github.xuse.querydsl.init;

import java.util.Date;

import com.github.xuse.querydsl.annotation.init.InitializeData;
import com.github.xuse.querydsl.config.TableInitTask;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.util.ProcessUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitProcessor implements Runnable {
	final SQLQueryFactory factory;
	final SQLMetadataQueryFactory metadata;

	private boolean stopOnError = true;
	private DataInitLog globalRecord = null;

	public InitProcessor(SQLQueryFactory factory) {
		this.factory=factory;
		this.metadata=factory.getMetadataFactory();
	}
	
	@Override
	public void run() {
		TableInitTask task;
		while((task= TableInitTask.pollFrom(factory.getConfiguration()))!=null) {
			try {
				execute(task.table, task.option);
			} catch (Exception ex) {
				// 表初始化工作不会引起启动失败。
				log.error("init {} error.", task.table, ex);
				if(stopOnError) {
					throw ex;
				}
			}	
		}
	}

	private void execute(RelationalPathEx<?> table, ScanOptions option) {
		boolean exists = metadata.existsTable(table.getSchemaAndTable(), null);
		InitializeData initData = table.getInitializeData();
		if (exists) {
			if (option.isAlterExistTable()) {
				metadata.refreshTable(table).dropColumns(option.isAllowDropColumn())
						.dropConstraint(option.isAllowDropConstraint()).dropIndexes(option.isAllowDropIndex())
						.execute();
				if (initData != null && option.getDataInitBehavior().code >= DataInitBehavior.FOR_MODIFIED_TABLE.code) {
					initData(table, option, initData);
				}
			} else {
				if (initData != null && option.getDataInitBehavior().code >= DataInitBehavior.FOR_ALL_TABLE.code) {
					initData(table, option, initData);
				}
			}
		} else {
			if (option.isCreateMissingTable()) {
				metadata.createTable(table).ifExists().execute();
				if (initData != null
						&& option.getDataInitBehavior().code >= DataInitBehavior.FOR_CREATED_TABLE_ONLY.code) {
					initData(table, option, initData);
				}
			}
		}
	}

	private void initData(RelationalPathEx<?> table, ScanOptions option, InitializeData initData) {
		// Check Global flags.
		DataInitLog current = null;
		if (option.isUseDataInitTable()) {
			if (globalRecord == null) {
				initInitLogTable();
				if (globalRecord.getDisabled() > 0) {
					// 全局禁用数据初始化功能，此处仅在第一次加载时输出日志
					log.info("global record of init data table is set to {}, init data feature will be disabled.",
							globalRecord);
					return;
				}
			} else {
				if (globalRecord.getDisabled() > 0) {
					return;
				}
			}

			String tableName = table.getTableName();
			QDataInitLog t = QDataInitLog.dataInitLog;
			current = factory.selectFrom(t).where(t.tableName.eq(tableName)).fetchFirst();
			if (current == null) {
				current = new DataInitLog();
				current.setTableName(tableName);
				factory.insert(t).populate(current).execute();
			}
			if (current.getDisabled() > 0) {
				log.info("init record of  table {} is set to {}, init data feature will be disabled.", tableName,
						current.getDisabled());
				return;
			}
		}
		// 执行数据初始化
		int count = factory.initializeTable(table).applyConfig(initData).execute();

		if (option.isUseDataInitTable()) {
			QDataInitLog t = QDataInitLog.dataInitLog;
			current.setDisabled(1);
			current.setLastInitTime(new Date());
			current.setLastInitUser(getAgent());
			current.setLastInitResult("Success." + String.valueOf(count));
			current.setRecords(count);
			factory.update(t).populate(current, true).execute();
		}
	}

	private String getAgent() {
		return ProcessUtil.getPid() + "@" + ProcessUtil.getHostname() + "(" + ProcessUtil.getLocalIp() + ") OS:"
				+ ProcessUtil.getOSName();
	}

	private void initInitLogTable() {
		QDataInitLog t = QDataInitLog.dataInitLog;
		int count = metadata.createTable(t).ifExists().execute();
		if (count == 0) {
			globalRecord = factory.selectFrom(t).where(t.tableName.eq("*")).fetchFirst();
		}
		if (globalRecord == null) {
			globalRecord = new DataInitLog();
			globalRecord.setTableName("*");
			globalRecord.setLastInitUser(getAgent());
			globalRecord.setLastInitTime(new Date());
			globalRecord.setLastInitResult("create record");
			factory.insert(t).populate(globalRecord).execute();
		}
	}
}