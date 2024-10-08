package com.github.xuse.querydsl.init;

import com.github.xuse.querydsl.config.ConfigurationEx;

import lombok.Getter;

/**
 * <h2>Database Operation Control Accompanied with the Entity Scanning</h2>
 * The complete database initialization operation includes the
 * following functionalities, with the option to disable some of them:
 * <ul>
 * <li>If the table is missing in the database, create the table.</li>
 * <li>If the table exists in the database, compare with the Java model. Add
 * missing columns, indexes, and constraints.</li>
 * <li>If the table exists in the database, compare with the Java model. Delete
 * redundant columns, indexes, and constraints (each of these can be
 * individually controlled).</li>
 * <li>Load the initial record file for the table, compare with the existing
 * rows in the database, and merge the data. If the table is empty, batch insert
 * the initial data into the table.</li>
 * <li>If a table partitioning strategy is configured, partitions will be
 * created simultaneously when the table is created for the first time. However,
 * for existing tables, the partition settings will not be updated. To maintain
 * table partitions, please call the relevant APIs manually.</li>
 * </ul>
 * <h2>[中文]实体扫描后的数据库操作控制</h2>
 * 实体扫描功能附带的数据库操作控制选项。完整的数据库初始化操作具有以下功能，可以选择禁用其中的部分功能。
 * <ul>
 * <li>如果数据库中缺少该表，则创建表</li>
 * <li>如果数据库中存在该表，则对比Java模型。增加缺少的列、索引、约束。</li>
 * <li>如果数据库中存在该表，则对比Java模型。删除多余列、索引、约束（每项可单独开关）</li>
 * <li>加载表初始记录文件，对比数据库中已有的行，进行数据合并。如果是张空表，则批量将初始数据写入该表。</li>
 * <li>如果配置了表分区策略，首次建表时会同时创建分区，但对于已存在的表，不会去更新其分区设置，要维护数据表分区请自行调用相关API</li>
 * </ul>
 */
@Getter
public class ScanOptions {

	private static final String DEFAULT_DISTRIBUTED_LOCK_NAME = "lock#table_initialize";

	public static final ScanOptions DEFAULT = new ScanOptions();

	// 是否创建不存在的表
	private boolean createMissingTable = true;

	// 是否更改已有表的结构
	private boolean alterExistTable = true;

	// (alterExistTable=true的情况下)是否允许删除已有表的列
	private boolean allowDropColumn;

	// (alterExistTable=true的情况下)是否允许删除已有表的索引
	private boolean allowDropIndex;

	// (alterExistTable=true的情况下)是否允许删除已有表的约束
	private boolean allowDropConstraint;

	/**
	 * Data Initialization Feature: Use a record table to log initialization states.
	 * <h1>Function 1: Log whether each table has been initialized.</h1> Once
	 * initialized, it is recorded, and subsequent data initialization for that
	 * table will not occur again.
	 * The `is_disabled` field in the data table has the following meanings:
	 * <ul>
	 * <li>`0` The table has not been initialized.</li>
	 * <li>`1` The table has been initialized and will not be initialized again.
	 * This value is automatically set to `1` after each initialization. If you need
	 * to reinitialize, you must manually change it to `0`.</li>
	 * </ul>
	 * If this feature is not enabled, the service will attempt to compare and write
	 * initialization data every time it starts. This can be modified by changing
	 * the `ScanOption` object during startup configuration.
	 * <h1>Function 2: Provide a global data initialization switch.</h1>
	 * The record with `table_name='*'` and setting `is_disabled = 1` can disable
	 * the data initialization function for all tables.
	 * <h1>Function 3: Provide a distributed lock function in default.</h1>
	 * Every time before creating DDL for data tables and performing DML for table
	 * data initialization, an attempt will be made to acquire a distributed lock
	 * (`table_name=lock#table_initialize`). Processes that do not acquire the lock
	 * will not perform database operations. The maximum effective lock period is 5
	 * minutes, If a process that has acquired the lock is abnormally killed, other
	 * services can attempt to acquire the lock again after 5 minutes.
	 * If this feature is not enabled, the service will attempt to compare database
	 * structures and initialization data every time it starts. If inconsistencies
	 * are found, it may attempt to modify the database.
	 * Therefore, it is strongly recommended to enable this feature if the service
	 * has multiple instances.
	 * <h2>[中文]</h2> 数据初始化功能：用一张记录表记录初始化状态。
	 * <h1>作用一：记录每张表是否经过了初始化，经过初始化后记录下来，后续就不会再对该表做数据初始化</h1> 数据表中的is_disabled字段含义如下
	 * <ul><li>0 该表尚未初始化，</li>
	 * <li>1 该表已经初始化，不会再次进行初始化，每次初始化完成后会自动将值设置为1，如您需要再次进行初始化需要手工修改为0。</li>
	 * </ul>
	 * <p>
	 * 如果没有开启此功能，那么服务每次启动都会尝试比较初始化数据并写入。除非修改配置变更启动时的ScanOption对象。
	 * </p>
	 * <h1>作用二：提供了一个全局的数据初始化功能开关</h1> table_name='*'这条记录，set is_disabled =
	 * 1，可以禁用所有表的数据初始化功能。
	 * <h1>作用三：提供了默认的分布式锁功能</h1>
	 * 每次进行数据表创建DDL和表数据初始化DML之前，会尝试获得分布式锁。（table_name=lock#table_initialize）
	 * 未获得锁的进程不会进行数据库操作。每次加锁的最大有效期是5分钟，如果某进程在获得了锁之后被异常KILL，其他服务在5分钟后可以再次争抢。
	 * <p>
	 * 如果没有开启此功能，那么服务每次启动都会尝试对比数据库结构和初始化数据，如果发生不一致则可能去修改数据库。分布式场景下，可能造成不确定后果，
	 * 故如果服务有多个实例，强烈建议开启此开关。
	 */
	private boolean useDataInitTable = true;

	/**
	 *  数据表数据始化行为
	 *  Data Table Initialization Behavior
	 *  @see DataInitBehavior
	 */
	private DataInitBehavior dataInitBehavior = DataInitBehavior.FOR_CREATED_TABLE_ONLY;

	/**
	 *  Data Initialization Feature: Data File Suffix
	 *  数据初始化功能：数据文件后缀，此配置一般无需修改
	 */
	private String dataInitFileSuffix = ".csv";

	/**
	 *  通过一个简单无风险的DDL语句嗅探是否可以执行DDL
	 */
	private boolean ddlPermissionDetect = false;

	/**
	 *  如果原定需要执行DDL，但没有权限时：false抛出异常；true:仅日志警告。 If the original plan requires
	 *  executing DDL but there is no permission: - `false`: Throw an exception. -
	 *  `true`: Log a warning only.
	 */
	private boolean ignoreIfNoPermission = true;
	
	/**
	 * 使用分布式锁。默认值为null，
	 * 表示如果设置了{@link ConfigurationEx#setExternalDistributedLockProvider(com.github.xuse.querydsl.sql.support.DistributedLockProvider)}或者启用了useDataInitTable后，则使用分布式锁。
	 * 否则不使用。
	 */
	private Boolean useDistributedLock = null;

	/**
	 *  Distributed lock timeout.
	 *  <p>
	 *  分布式锁超时时间。
	 *  此配置一般无需修改
	 */
	private int lockExpireMinutes = 5;

	/**
	 *  The name of the distributed lock, by default, uses {@link #DEFAULT_DISTRIBUTED_LOCK_NAME}. This configuration usually does not need to be modified.
	 *  <p>
	 *  分布式锁的名称，默认使用 {@link #DEFAULT_DISTRIBUTED_LOCK_NAME}。
	 *  此配置一般无需修改。
	 */
	private String lockName = DEFAULT_DISTRIBUTED_LOCK_NAME;

	public static ScanOptions getDefault() {
		return DEFAULT;
	}

	public ScanOptions setCreateMissingTable(boolean createMissingTable) {
		this.createMissingTable = createMissingTable;
		return this;
	}

	public ScanOptions setAlterExistTable(boolean alterExistTable) {
		this.alterExistTable = alterExistTable;
		return this;
	}

	public ScanOptions setAllowDropColumn(boolean allowDropColumn) {
		this.allowDropColumn = allowDropColumn;
		return this;
	}

	public ScanOptions setAllowDropIndex(boolean allowDropIndex) {
		this.allowDropIndex = allowDropIndex;
		return this;
	}

	public ScanOptions setAllowDropConstraint(boolean allowDropConstraint) {
		this.allowDropConstraint = allowDropConstraint;
		return this;
	}

	public ScanOptions setDataInitBehavior(DataInitBehavior dataInitBehavior) {
		this.dataInitBehavior = dataInitBehavior;
		return this;
	}

	public ScanOptions setDataInitFileSuffix(String dataInitFileSuffix) {
		this.dataInitFileSuffix = dataInitFileSuffix;
		return this;
	}

	public ScanOptions setUseDataInitTable(boolean useDataInitTable) {
		this.useDataInitTable = useDataInitTable;
		return this;
	}

	/**
	 *  设置开关，允许修改表以及删除表中的字段等。
	 *
	 *  @return this
	 */
	public ScanOptions allowDrops() {
		this.alterExistTable = true;
		this.allowDropColumn = true;
		this.allowDropIndex = true;
		this.allowDropConstraint = true;
		return this;
	}

	/**
	 *  设置开关，禁止DDL（建表与修改表）执行。
	 *
	 *  @return this
	 */
	public ScanOptions disableDDL() {
		this.createMissingTable = false;
		this.alterExistTable = false;
		return this;
	}

	/**
	 *  设置开关，禁止数据初始化
	 *
	 *  @return ScanOptions
	 */
	public ScanOptions disableDataInitialize() {
		this.dataInitBehavior = DataInitBehavior.NONE;
		return this;
	}

	/**
	 *  禁止扫描时的一切数据库操作。
	 *
	 *  @return ScanOptions
	 */
	public ScanOptions disableAllDatabaseOperation() {
		this.useDataInitTable = false;
		disableDDL();
		return disableDataInitialize();
	}

	public ScanOptions useDataInitTable(boolean flag) {
		this.useDataInitTable = flag;
		return this;
	}

	public ScanOptions detectPermissions(boolean flag) {
		this.ddlPermissionDetect = flag;
		return this;
	}

	@Override
	public String toString() {
		return "ScanOptions [createMissingTable=" + createMissingTable + ", alterExistTable=" + alterExistTable + ", allowDropColumn=" + allowDropColumn + ", allowDropIndex=" + allowDropIndex + ", allowDropConstraint=" + allowDropConstraint + ", dataInitBehavior=" + dataInitBehavior + ", dataInitFileSuffix=" + dataInitFileSuffix + ", useDataInitTable=" + useDataInitTable + "]";
	}

	public ScanOptions setIgnoreIfNoPermission(boolean ignoreIfNoPermission) {
		this.ignoreIfNoPermission = ignoreIfNoPermission;
		return this;
	}

	public ScanOptions setLockExpireMinutes(int dblockExpireMinutes) {
		this.lockExpireMinutes = dblockExpireMinutes;
		return this;
	}

	public ScanOptions setLockName(String dblockName) {
		this.lockName = dblockName;
		return this;
	}

	public ScanOptions useDistributedLock(boolean flag) {
		this.useDistributedLock=flag;
		return this;
	}
}
