package com.github.xuse.querydsl.init;

import java.util.Date;

import com.github.xuse.querydsl.annotation.InitializeData;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.config.ConfigrationPackageExporter;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dialect.Privilege;
import com.github.xuse.querydsl.sql.support.DbDistributedLockProvider;
import com.github.xuse.querydsl.sql.support.DistributedLock;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Entry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitProcessor {
	final SQLQueryFactory factory;
	final SQLMetadataQueryFactory metadata;
	final ScanOptions option;

	private boolean stopOnError = true;
	private DataInitLog globalRecord = null;
	private DistributedLock lock;

	public InitProcessor(SQLQueryFactory factory, ScanOptions option) {
		this.factory = factory;
		this.metadata = factory.getMetadataFactory();
		this.option = option;
	}

	public void run() {
		int count = 0;
		try {
			TableInitTask task;
			if ((task = ConfigrationPackageExporter.pollFrom(factory.getConfiguration())) != null) {
				if (!doInit()) {
					return;
				}
				execute(task);
				count++;
			}
			while ((task = ConfigrationPackageExporter.pollFrom(factory.getConfiguration())) != null) {
				execute(task);
				count++;
			}
		} finally {
			if (lock != null) {
				lock.unlock();
			}
			log.info("[Init Processor] Database schema init process finished. {} task processed.", count);
		}
	}

	/*
	 * 返回false表示不再执行后续初始化任务
	 */
	private boolean doInit() {
		// 检查有无DDL权限
		if (option.isDdlPermissionDetect()) {
			boolean permission = metadata.hasPrivilege(Privilege.CREATE, Privilege.ALTER);
			if (!permission) {
				factory.getConfiguration().setMissDDLPermissions();
				if(!option.isIgnoreIfNoPermission()) {
					throw new IllegalStateException("There's no Privilege to execute DDL on current database");
				}
			}
		}
		if (option.getUseDistributedLock() == null) {
			lock = getAvailableLock();
		} else if (option.getUseDistributedLock().booleanValue()) {
			lock = getAvailableLock();
			if (lock == null) {
				throw new IllegalStateException("No distributed lock provider found.");
			}
		} else {
			lock = null;
		}
		if (lock != null) {
			if (!lock.tryLock()) {
				Entry<String, Date> lockedBy = lock.lockedBy();
				Assert.notNull(lockedBy);
				log.error("Try get db lock failure, the lock was currently belongs to {}@{}. Init Process will quit.",
						lockedBy.getKey(), lockedBy.getValue());
				return false;
			}
		}
		return true;
	}

	private DistributedLock getAvailableLock() {
		ConfigurationEx configuration = factory.getConfiguration();
		ScanOptions option = this.option;
		// 自动，看当前有什么实现可以用
		if (configuration.getExtenalDistributedLockProvider() != null) {
			return configuration.getExtenalDistributedLockProvider().getLock(option.getLockName(),
					option.getLockExpireMinutes());
		} else if (option.isUseDataInitTable()) {
			return configuration.computeLockProvider(() -> DbDistributedLockProvider.create(factory))
					.getLock(option.getLockName(), option.getLockExpireMinutes());
		}
		return null;
	}

	private void execute(TableInitTask task) {
		try {
			RelationalPathEx<?> table = task.table;
			boolean exists = metadata.existsTable(table.getSchemaAndTable(), null);
			InitializeData initData = table.getInitializeData();
			if (exists) {
				if (option.isAlterExistTable()) {
					modifyExistTable(table, initData);
				} else {
					// 表存在但不允许执行DDL
					if (initData != null && option.getDataInitBehavior().code >= DataInitBehavior.FOR_ALL_TABLE.code) {
						initData(table, initData, false);
					}
				}
			} else {
				// 表不存在但允许执行DDL
				if (option.isCreateMissingTable()) {
					createNewTable(table, initData);
				}
			}
		} catch (Exception ex) {
			log.error("Init table [{}] error.", task.table.getSchemaAndTable(), ex);
			if (stopOnError) {
				throw ex;
			}
		}
	}

	private void createNewTable(RelationalPathEx<?> table, InitializeData initData) {
		metadata.createTable(table).ifExists().execute();
		if (initData != null && option.getDataInitBehavior().code >= DataInitBehavior.FOR_CREATED_TABLE_ONLY.code) {
			initData(table, initData, true);
		}
	}

	private void modifyExistTable(RelationalPathEx<?> table, InitializeData initData) {
		// 表存在且允许修改表
		metadata.refreshTable(table).dropColumns(option.isAllowDropColumn())
				.dropConstraint(option.isAllowDropConstraint()).dropIndexes(option.isAllowDropIndex()).execute();
		if (initData != null && option.getDataInitBehavior().code >= DataInitBehavior.FOR_MODIFIED_TABLE.code) {
			initData(table, initData, false);
		}
	}

	private void initData(RelationalPathEx<?> table, InitializeData initData, boolean isNew) {
		// Check Global flags.
		DataInitLog current = null;
		if (option.isUseDataInitTable()) {
			if (globalRecord == null) {
				globalRecord = DDLLockUtils.initInitLogTable(factory);
				if (globalRecord != null && globalRecord.getDisabled() > 0) {
					// 全局禁用数据初始化功能，此处仅在第一次加载时输出日志
					log.warn("The Global setting of data initialization is set to {}, the function will be DISABLED.",
							globalRecord.getDisabled());
					return;
				}
			} else {
				if (globalRecord.getDisabled() > 0) {
					return;
				}
			}
			if (option.isUseDataInitTable()) {
				current = checkInitLog(table, option);
				if (current != null && current.getDisabled() > 0) {
					log.info(
							"The init-record of table [{}] is set to {}, initialization on this table will be DISABLED.",
							table.getTableName(), current.getDisabled());
					return;
				}
			}
		}
		// 执行数据初始化
		int count = factory.initializeTable(table).applyConfig(initData).isNewTable(isNew).execute();

		if (current != null) {
			QDataInitLog t = QDataInitLog.dataInitLog;
			current.setDisabled(1);
			current.setLastInitTime(new Date());
			current.setLastInitUser(DDLLockUtils.getAgent());
			current.setLastInitResult("Success." + String.valueOf(count));
			current.setRecords(count);
			factory.update(t).populate(current, true).execute();
		}
	}

	private DataInitLog checkInitLog(RelationalPathEx<?> table, ScanOptions option) {
		String tableName = table.getTableName();
		QDataInitLog t = QDataInitLog.dataInitLog;
		DataInitLog current = factory.selectFrom(t).where(t.tableName.eq(tableName)).fetchFirst();
		if (current == null) {
			current = new DataInitLog();
			current.setTableName(tableName);
			factory.insert(t).populate(current).execute();
		}
		return current;
	}

}