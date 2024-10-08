package com.github.xuse.querydsl.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.github.xuse.querydsl.init.ScanOptions;
import com.github.xuse.querydsl.init.TableInitTask;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dbmeta.DriverInfo;
import com.github.xuse.querydsl.sql.dialect.DefaultSQLTemplatesEx;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.github.xuse.querydsl.sql.support.DistributedLockProvider;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.SnowflakeIdWorker;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLExceptionTranslator;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SchemaAndTable;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;
import com.querydsl.sql.types.Type;

/**
 * This is a configuration project based on extended QueryDSL. During the
 * initialization phase, it allows you to configure various framework behaviors.
 * <p>
 * 在QueryDSL原生基础上扩展的配置项目。在初始化阶段可以设置各种框架的行为。
 */
public class ConfigurationEx {

	public static boolean FREE_PRIMITIVE = false;
	
	
	private static final Logger log = LoggerFactory.getLogger(ConfigurationEx.class);
	
	final Set<RelationalPath<?>> registededRelations = new HashSet<>();

	/**
	 * configuration of the original Querydsl.
	 * <p>
	 * QueryDSL配置
	 */
	private final Configuration configuration;

	/**
	 * Slow SQL Query Threshold: - When using
	 * `com.github.xuse.querydsl.sql.log.QueryDSLSQLListener`, if the SQL execution
	 * time exceeds this threshold (in milliseconds), it will be logged as an error.
	 * <p>
	 * 慢SQL判断阈值。
	 * 在使用com.github.xuse.querydsl.sql.log.QueryDSLSQLListener的场景下，如果SQL执行时间超过这个数值（毫秒）
	 * 就会记录为错误日志
	 */
	private long slowSqlWarnMillis = 10000;

	/**
	 * The default query timeout period.
	 * <p>
	 * 默认的查询最大超时时间
	 */
	private int defaultQueryTimeout;

	/**
	 * 需要显式指定schema的表。 原框架在方言中指定了是否要携带schema进行where查询，但是没有表维度的schema指定。
	 */
	private final Set<RelationalPath<?>> withSchemas = new HashSet<>();

	/**
	 * <p>
	 * 达到最大maxRows后按错误日志记录
	 */
	private Level levelOfReachMaxRows = Level.ERROR;

	/**
	 * Allow the deletion and re-creation of tables, or not.
	 * <p>
	 * 允许删除表重建
	 */
	private boolean allowTableDropAndCreate = false;

	/**
	 * How many objects of the log prints out in a batch processing operations
	 * <p>
	 * batch操作时日志最多打印条数
	 */
	private int maxRecordsLogInBatch = 5;

	/**
	 * 数据初始化相关配置：在包扫描后执行哪些数据库初始化动作。
	 */
	private final ScanOptions scanOptions = new ScanOptions();

	/**
	 * Distributed Lock Provider. During system startup or when executing DDL,
	 * distributed locks can be used to prevent multiple instances from concurrently
	 * operating on the database. If this item is not configured and distributed
	 * locks are required, the framework will use a default database table as the
	 * distributed lock.
	 * <h2>中文</h2> 分布式锁提供器。 在系统启动或执行DDL时，可以使用分布式锁防止多个实例并发操作数据库。
	 * 如果不设置此项，而使用时由要求使用分布式锁，框架会使用默认的数据库表作为分布式锁。
	 */
	private DistributedLockProvider distributedLockProvider;

	private final Map<String, String> dialectMapping = IOUtils
			.loadProperties(this.getClass().getResource("/META-INF/dialect_mapping"));

	private transient volatile SQLTemplatesEx template;

	transient volatile boolean noDDLPermission = false;

	private LetterCase letterCase;

	private static Field quoteStrField;

	/*
	 * 在包扫描时识别到的数据库初始化任务，由于当时没有SQLQueryFactory实例化无法执行，故将初始化任务缓存起来，以便后续执行
	 */
	final BlockingQueue<TableInitTask> initTasks = new LinkedBlockingQueue<>();

	/**
	 * The database driver information.
	 */
	protected volatile DriverInfo driverInfo;

	static {
		try {
			quoteStrField = SQLTemplates.class.getDeclaredField("quoteStr");
			quoteStrField.setAccessible(true);
		} catch (Exception e) {
			log.error("get field 'quoteStr' error, from {}", SQLTemplates.class);
		}
	}

	public int getMaxRecordsLogInBatch() {
		return maxRecordsLogInBatch;
	}

	public void setMaxRecordsLogInBatch(int maxRecordsLogInBatch) {
		this.maxRecordsLogInBatch = maxRecordsLogInBatch;
	}

	public Configuration get() {
		return configuration;
	}

	/**
	 * A global switch is used to control whether the deletion and rebuilding of
	 * tables is allowed during table creation. It is disabled by default.
	 * <p>
	 * 全局开关用于控制在建表时是否允许删除表再重建的行为。默认关闭。
	 * @return ConfigurationEx
	 */
	public ConfigurationEx allowTableDropAndCreate() {
		allowTableDropAndCreate = true;
		return this;
	}

	public String getDataInitFileSuffix() {
		return scanOptions.getDataInitFileSuffix();
	}

	public void setDataInitFileSuffix(String dataInitFileSuffix) {
		this.scanOptions.setDataInitFileSuffix(dataInitFileSuffix);
	}

	/**
	 * get the options of database operation after class scanning.
	 * <p>
	 * 获得配置，该配置可以定义类扫描后的数据库操作行为。
	 *
	 * @return ScanOptions
	 */
	public ScanOptions getScanOptions() {
		return scanOptions;
	}

	private synchronized SQLTemplatesEx initTemplateEx() {
		if (this.template != null) {
			return this.template;
		}
		SQLTemplates templates = configuration.getTemplates();
		if (templates instanceof SQLTemplatesEx) {
			return this.template = (SQLTemplatesEx) templates;
		}
		String clz = dialectMapping.get(templates.getClass().getName());
		if (StringUtils.isNotEmpty(clz)) {
			try {
				Class<?> c = Class.forName(clz);
				Constructor<?> constructor = c.getConstructor(SQLTemplates.class);
				SQLTemplatesEx templateExt = (SQLTemplatesEx) constructor.newInstance(templates);
				log.info("Using {} as the extension SQLTemplates of current database.", clz);
				this.template = templateExt;
				templateExt.init(templates);
				return templateExt;
			} catch (Exception e) {
				log.error("Load SQLTemplateExt [{}] failure.", clz, e);
			}
		}
		// 如果当前方言（SQLTemplates）类没有实现SQLTemplatesExt，那么就用默认方言
		return this.template = new DefaultSQLTemplatesEx(templates);
	}

	public ConfigurationEx(SQLTemplates templates) {
		this.configuration = new Configuration(templates);
		initTemplateEx();
	}

	public String getQuoteString() {
		try {
			String s = (String) quoteStrField.get(configuration.getTemplates());
			return s;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw Exceptions.illegalState(e);
		}
	}

	/**
	 * Got the database dialect.
	 * <p>
	 * 
	 * @return 获得扩展方言对象
	 */
	public SQLTemplatesEx getTemplates() {
		if (this.template != null) {
			return this.template;
		}
		return initTemplateEx();
	}

	/**
     * Add a listener
     * @param listener listener
	 */
	public void addListener(SQLListener listener) {
		configuration.addListener(listener);
	}

	/**
	 * Initialize a Snowflake ID generator
	 * <p>
	 * 初始化Snowflake id生成器。
	 *
	 * @param workerId     worker Id
	 * @param datacenterId datacenter Id
	 */
	public void initSnowflake(int workerId, int datacenterId) {
		SnowflakeIdManager.init(workerId, datacenterId);
	}

	public SnowflakeIdWorker getSnowflakeWorker() {
		return SnowflakeIdManager.getInstance();
	}

	public void registerPrintSchemas(RelationalPath<?> path) {
		this.withSchemas.add(path);
	}

	/**
	 * @deprecated use {@link #registerRelation(RelationalPathEx)}
	 * @param table
	 */
	public void registerType(RelationalPathEx<?> table) {
		registerRelation(table);
	}
	
	/**
	 * Register a table mapping instance. Identify the information in the table
	 * definition by scanning the annotations on the object.
	 * <p>
	 * 注册一个表映射实例。通过扫描该对象上的注解，来识别表定义中的信息。
	 *
	 * @param table path
	 * @return true if registered.
	 */
	public boolean registerRelation(RelationalPathEx<?> table) {
		if(registededRelations.add(table)) {
			PathCache.register(table);
			for (Path<?> p : table.getColumns()) {
				ColumnMapping c = table.getColumnMetadata(p);
				Type<?> customType = c.getCustomType();
				if (customType != null) {
					configuration.register(table.getTableName(), c.getColumn().getName(), customType);
					log.info("Column [{}.{}] is registered to:{}", table.getTableName(), c.getColumn().getName(),customType);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Register a custom data type mapping.
	 * <p>
	 * 注册一个自定义数据类型映射
	 *
	 * @param type the mapping type. 映射类型.
	 */
	public void register(Type<?> type) {
		configuration.register(type);
	}

	/**
	 * Register a custom data type mapping for a column of table.
	 * <p>
	 * 为某个表的某个字段注册一个自定义类型映射。一般是用于Bean写入和查询。
	 *
	 * @param table  the table name. 表名
	 * @param column the column name. 列名
	 * @param type   the type mapping. 映射类型
	 */
	public void register(String table, String column, Type<?> type) {
		configuration.register(table, column, type);
	}

	/**
	 * @see Configuration#registerType(String, Class)
	 * @param typeName typeName
	 * @param clazz    clazz
	 */
	public void registerType(String typeName, Class<?> clazz) {
		configuration.registerType(typeName, clazz);
	}

	public long getSlowSqlWarnMillis() {
		return slowSqlWarnMillis;
	}

	/**
	 * Set the exception translator
	 *
	 * @param exceptionTranslator exception translator
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		if (exceptionTranslator != null)
			configuration.setExceptionTranslator(exceptionTranslator);
	}

	/**
	 * Set table name/column name case rules. Generally, MySQL uses lowercase and is
	 * case-insensitive, while Oracle uses uppercase and is case-insensitive.
	 * Usually, the system can automatically detect the case rules of the database,
	 * so this method doesn't need to be called. It should only be used for manual
	 * intervention in special scenarios.
	 * <p>
	 * 设置表名/列名大小写规则。一般MySQL为小写且大小写忽略，Oracle为大写且大小写忽略。
	 * 通常，系统能自行识别数据库大小写规则，无需调用此方法。仅当特殊场合下需要人工干预时才可使用。
	 *
	 * @param letterCase letterCase
	 * @return this
	 */
	public synchronized ConfigurationEx nameInCaseOf(LetterCase letterCase) {
		if (initLetterCase(letterCase)) {
			return this;
		} else {
			throw new UnsupportedOperationException("The letter case was already set. case = " + this.letterCase);
		}
	}

	/**
	 * Set a threshold for slow SQL monitoring. When the execution time of an SQL
	 * statement exceeds this value, a warning is logged.
	 * <p>
	 * 设置一个慢SQL监测阈值。当SQL执行耗时大于这个数值时，日志打印警告。
	 * 
	 * @param slowSqlWarnMillis Threshold for slow SQL, in milliseconds. 慢SQL阈值，单位毫秒
	 * @return ConfigurationEx
	 */
	public ConfigurationEx setSlowSqlWarnMillis(long slowSqlWarnMillis) {
		this.slowSqlWarnMillis = slowSqlWarnMillis;
		return this;
	}

	public int getDefaultQueryTimeout() {
		return defaultQueryTimeout;
	}

	/**
	 * Set a global query timeout, corresponding to the JDBC query timeout. After
	 * the query times out, let the database abandon the task to protect the
	 * database from being hung by slow queries.
	 * <p>
	 * 设置通用的查询超时时间，对应JDBC的查询超时，查询超时后让数据库放弃任务，保护数据库不被慢查挂死。
	 *
	 * @param defaultQueryTimeout time unit is second. 超时保护，单位秒。不建议设置5秒内的数值。
	 * @return ConfigurationEx
	 */
	public ConfigurationEx setDefaultQueryTimeout(int defaultQueryTimeout) {
		this.defaultQueryTimeout = defaultQueryTimeout;
		return this;
	}

	/**
	 * Perform package scanning to automatically discover annotations on entity
	 * classes and fields, thereby eliminating the need for individual registration
	 * and configuration of field mappings. The scanned objects are metamodels,
	 * specifically classes prefixed with the letter 'Q', and these classes are
	 * automatically used to find the default Bean classes. It is required that the
	 * metamodel classes extend `com.github.xuse.querydsl.sql.RelationalPathBaseEx`.
	 * <p>
	 * 进行包扫描，自动发现实体类和字段上的注解，从而无需对字段映射等进行单个的注册和配置。
	 * 扫描对象为元模型，即Q字母开头的这些类，并自动根据这些类去寻找到默认的Bean类。
	 *
	 * 要求元模型类必须继承com/github/xuse/querydsl/sql/RelationalPathBaseEx。
	 * <p>
	 *
	 * @param pkgNames pkgNames
	 * @return 扫描实体定义数量
	 */
	public int scanPackages(String... pkgNames) {
		ScanContext context=new ScanContext(this);
		context.scan(pkgNames);
		return context.getCount();
	}


	public boolean isPrintSchema(RelationalPath<?> path) {
		return withSchemas.contains(path);
	}

	public Level getLevelOfReachMaxRows() {
		return levelOfReachMaxRows;
	}

	public ConfigurationEx setLevelOfReachMaxRows(Level levelOfReachMaxRows) {
		this.levelOfReachMaxRows = levelOfReachMaxRows;
		return this;
	}

	public boolean isAllowTableDropAndCreate() {
		return allowTableDropAndCreate;
	}

	public SchemaAndTable getOverride(SchemaAndTable table) {
		LetterCase lc = getTemplates().getDefaultLetterCase();
		if (letterCase == null && lc != null) {
			// 如果用户没有设置表名大小写转换，就使用方言自带的case。
			initLetterCase(lc);
		}
		return configuration.getOverride(table);
	}

	public String getColumnOverride(SchemaAndTable table, String column) {
		LetterCase lc = getTemplates().getDefaultLetterCase();
		if (letterCase == null && lc != null) {
			// 如果用户没有设置表名大小写转换，就使用方言自带的case。
			initLetterCase(lc);
		}
		return configuration.getColumnOverride(table, column);
	}

	private synchronized boolean initLetterCase(LetterCase letterCase) {
		if (this.letterCase == null) {
			this.letterCase = letterCase;
			configuration.setDynamicNameMapping(ChangeLetterCaseNameMapping2.valueOf(letterCase, Locale.getDefault()));
			return true;
		}
		return this.letterCase == letterCase;
	}

	public boolean has(SpecialFeature feature) {
		return getTemplates().supports(feature);
	}
	
	public boolean supports(Operator feature) {
		return getTemplates().supports(feature);
	}

	public void setMissDDLPermissions() {
		this.noDDLPermission = true;
	}

	public boolean isMissDDLPermissions() {
		return noDDLPermission;
	}

	public DistributedLockProvider getExtenalDistributedLockProvider() {
		return distributedLockProvider;
	}

	public void setExternalDistributedLockProvider(DistributedLockProvider extenalDistributedLockProvider) {
		this.distributedLockProvider = extenalDistributedLockProvider;
	}

	public synchronized DistributedLockProvider computeLockProvider(Supplier<DistributedLockProvider> supplier) {
		if (distributedLockProvider != null) {
			return distributedLockProvider;
		}
		DistributedLockProvider provider = supplier.get();
		if (provider != null) {
			this.distributedLockProvider = provider;
		}
		return provider;
	}

	public BiPredicate<Object, Object> getComparePredicate(Path<?> p) {
		if (Date.class.isAssignableFrom(p.getType())) {
			return SQLTypeUtils::compareDate;
		} else {
			return Objects::equals;
		}
	}

	public RuntimeException translate(String queryString, List<Object> constants, SQLException e) {
		return configuration.translate(queryString, constants, e);
	}
}
