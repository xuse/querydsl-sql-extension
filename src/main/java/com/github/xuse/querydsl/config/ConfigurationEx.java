package com.github.xuse.querydsl.config;

import static com.github.xuse.querydsl.sql.expression.AdvancedMapper.SCENARIO_INSERT;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.core.io.Resource;

import com.github.xuse.querydsl.asm.ClassReader;
import com.github.xuse.querydsl.asm.Opcodes;
import com.github.xuse.querydsl.config.util.ClassScanner;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dialect.DefaultSQLTemplatesEx;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.SnowflakeIdWorker;
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
 * 在QueryDSL原生基础上扩展的配置项目
 * 
 * @author jiyi
 *
 */
public class ConfigurationEx {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationEx.class);

	private static final AdvancedMapper FOR_INSERT=new AdvancedMapper(SCENARIO_INSERT);
	
	private static final AdvancedMapper FOR_INSERT_BATCH=AdvancedMapper.ofNullsAsDefaultBingding(SCENARIO_INSERT);
	
	/**
	 * QueryDSL配置
	 */
	private final Configuration configuration;

	/**
	 * 在使用com.github.xuse.querydsl.sql.log.QueryDSLSQLListener的场景下，如果SQL执行时间超过这个数值（毫秒）
	 * 就会记录为错误日志
	 */
	private long slowSqlWarnMillis = 10000;

	/**
	 * 默认的查询最大超时时间
	 */
	private int defaultQueryTimeout;

	/**
	 * 已完成类型注册的Class
	 */
	private final Set<RelationalPathEx<?>> registeredclasses = new HashSet<>();

	/**
	 * 需要使用schema的表。
	 * 原框架在方言中指定了是否要携带schema进行where查询，但是没有表维度的schema指定。
	 */
	private final Set<RelationalPath<?>> withSchemas = new HashSet<>();

	/**
	 * 雪花ID生成器
	 */
	private SnowflakeIdWorker snowflakeWorker;

	/**
	 * 达到最大maxRows后按错误日志记录
	 */
	private Level levelOfReachMaxRows = Level.ERROR;

	/**
	 * 允许删除表重建
	 */
	private boolean allowTableDropAndCreate = false;
	
	/**
	 * 加载资源文件
	 */
	private Map<String,String> dialectMapping=IOUtils.loadProperties(this.getClass().getResource("/META-INF/dialect_mapping"));
	
	private transient volatile SQLTemplatesEx template;
	
	private LetterCase letterCase;
	
	private static Field quoteStrField;
	static {
		try {
			quoteStrField=SQLTemplates.class.getDeclaredField("quoteStr");
			quoteStrField.setAccessible(true);
		}catch(Exception e) {
			log.error("get field 'quoteStr' error, from {}",SQLTemplates.class);
		}
	}

	public Configuration get() {
		return configuration;
	}
	
	public ConfigurationEx allowTableDropAndCreate() {
		allowTableDropAndCreate = true;
		return this;
	}

	public ConfigurationEx(Configuration configuration) {
		this.configuration = configuration;
		initConfiguration(configuration);
		initTemplateEx();
	}

	protected void initConfiguration(Configuration c) {
	}

	private synchronized SQLTemplatesEx initTemplateEx() {
		if(this.template!=null) {
			return this.template;
		}
		SQLTemplates templates = configuration.getTemplates();
		if (templates instanceof SQLTemplatesEx) {
			return this.template = (SQLTemplatesEx) templates;
		}
		String clz=dialectMapping.get(templates.getClass().getName());
		if(StringUtils.isNotEmpty(clz)) {
			try {
				Class<?> c=Class.forName(clz);
				Constructor<?> constructor=c.getConstructor(SQLTemplates.class);
				SQLTemplatesEx templateExt=(SQLTemplatesEx)constructor.newInstance(templates);
				log.info("Using {} as the extension SQLTemplates of current database.", clz);
				this.template=templateExt;
				templateExt.init(templates);	
				return templateExt;
			}catch(Exception e) {
				log.error("Load SQLTemplateExt [{}] failure.",clz,e);
			}
		}
		// 如果当前方言（SQLTemplates）类没有实现SQLTemplatesExt，那么就用默认方言
		return this.template=new DefaultSQLTemplatesEx(templates);
	}


	public ConfigurationEx(SQLTemplates templates) {
		this(new Configuration(templates));
	}

	public String getQuoteString() {
		try {
			String s=(String) quoteStrField.get(configuration.getTemplates());
			return s;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw Exceptions.illegalState(e);
		}
	}
	

	/**
	 * @return 获得扩展方言对象
	 */
	public SQLTemplatesEx getTemplates() {
		if(this.template!=null) {
			return this.template;
		}
		return initTemplateEx();
	}

	public void addListener(SQLListener listener) {
		configuration.addListener(listener);
	}

	/**
	 * 为Snowflake id生成器进行初始化。
	 * 
	 * @param workerId
	 * @param datacenterId
	 */
	public void initSnowflake(int workerId, int datacenterId) {
		this.snowflakeWorker = new SnowflakeIdWorker(workerId, datacenterId);
	}

	public SnowflakeIdWorker getSnowflakeWorker() {
		return snowflakeWorker;
	}

	public void registerPrintSchemas(RelationalPath<?> path) {
		this.withSchemas.add(path);
	}

	/**
	 * 注册一个表映射实例。通过扫描该对象上的注解，来识别表定义中的信息。
	 * 
	 * @param path
	 */
	public void registerExType(RelationalPathEx<?> path) {
		if (registeredclasses.add(path)) {
			for (Path<?> p : path.getColumns()) {
				ColumnMapping c = path.getColumnMetadata(p);
				Type<?> customType = c.getCustomType();
				if (customType != null) {
					configuration.register(path.getTableName(), c.getColumn().getName(), customType);
					log.info("Column [{}.{}] is registered to:{}", path.getTableName(), c.getColumn().getName(), customType);
				}
			}
		}
	};


	/**
	 * 注册一个全局的自定义类型映射（Java到数据库）
	 * 
	 * @param type 映射类型
	 */
	public void register(Type<?> type) {
		configuration.register(type);
	}

	/**
	 * 为某个表的某个字段注册一个自定义类型映射。一般是用于Bean写入和查询。
	 * 
	 * @param table  表名
	 * @param column 列名
	 * @param type   映射类型
	 */
	public void register(String table, String column, Type<?> type) {
		configuration.register(table, column, type);
	}

	/**
	 * @see Configuration#registerType(String, Class)
	 * @param typeName
	 * @param clazz
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
		configuration.setExceptionTranslator(exceptionTranslator);
	}
	
	/**
	 * 设置表名大小写。如MySQL为小写，Oracle为大写
	 * @param letterCase 
	 * @return this
	 */
	public synchronized ConfigurationEx nameInCaseOf(LetterCase letterCase) {
		if(initLetterCase(letterCase)) {
			return this;	
		}else {
			throw new UnsupportedOperationException("The letter case was already set. case = "+ this.letterCase);
		}
	}
	
	
	private synchronized boolean initLetterCase(LetterCase letterCase) {
		if(this.letterCase==null) {
			this.letterCase=letterCase;
			configuration.setDynamicNameMapping(ChangeLetterCaseNameMapping2.valueOf(letterCase,Locale.getDefault()));
			return true;
		}
		if(this.letterCase==letterCase) {
			return true;
		}
		return false;
	}
	
	/**
	 * 设置一个慢SQL监测阈值。当SQL执行耗时大于这个数值时，日志打印警告。
	 * 
	 * @param slowSqlWarnMillis 慢SQL阈值，单位毫秒
	 * @return this
	 */
	public ConfigurationEx setSlowSqlWarnMillis(long slowSqlWarnMillis) {
		this.slowSqlWarnMillis = slowSqlWarnMillis;
		return this; 
	}

	public int getDefaultQueryTimeout() {
		return defaultQueryTimeout;
	}

	/**
	 * 设置通用的查询超时时间，对应JDBC的查询超时，查询超时后让数据库放弃任务，保护数据库不被慢查挂死。
	 * 
	 * @param defaultQueryTimeout 超时保护，单位秒。不建议设置5秒内的数值。
	 * @return this
	 */
	public ConfigurationEx setDefaultQueryTimeout(int defaultQueryTimeout) {
		this.defaultQueryTimeout = defaultQueryTimeout;
		return this; 
	}

	/**
	 * 进行包扫描，自动发现实体类和字段上的注解，从而无需对字段映射等进行单个的注册和配置。
	 * 扫描对象为元模型，即Q字母开头的这些类，并自动根据这些类去寻找到默认的Bean类。
	 * 
	 * 要求元模型类必须继承com/github/xuse/querydsl/sql/RelationalPathBaseEx。
	 * 
	 * @param pkgNames
	 * @return 扫描实体定义数量
	 */
	public int scanPackages(String... pkgNames) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		List<Resource> clss = new ClassScanner().scan(pkgNames);
		int n = 0;
		for (Resource cls : clss) {
			if (!cls.isReadable()) {
				continue;
			}
			if (cls.getFilename().startsWith("Q")) {
				try {
					if (processEnhance(cls, cl)) {
						n++;
					}
				} catch (Exception e) {
					log.error("Enhance error: {}", cls, e);
					continue;
				}
			}
		}
		return n;
	}


	public boolean isPrintSchema(Path<?> path) {
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
		if(letterCase==null && lc!=null) {
			//如果用户没有设置表名大小写转换，就使用方言自带的case。
			initLetterCase(lc);
		}
		//兼容处理，似乎没有兼容的必要。
//		String schema=table.getSchema();
//		String matchName = table.getTable();
//		if (matchName != null) {
//			// 兼容处理，带schema的情况
//			int n = matchName.indexOf('.');
//			if (n > -1) {
//				schema = matchName.substring(0, n);
//				matchName = matchName.substring(n + 1);
//				table=new SchemaAndTable(schema, matchName);
//			}
//		}
		//要处理为null的情况
		return configuration.getOverride(table);
	}
	
	public String getColumnOverride(SchemaAndTable table, String column) {
		LetterCase lc = getTemplates().getDefaultLetterCase();
		if(letterCase==null && lc!=null) {
			//如果用户没有设置表名大小写转换，就使用方言自带的case。
			initLetterCase(lc);
		}
		return configuration.getColumnOverride(table, column);
	}

	
	private boolean processEnhance(Resource res, ClassLoader cl) {
		byte[] data;
		try (InputStream in = res.getInputStream()) {
			data = IOUtils.toByteArray(in);
		} catch (IOException e) {
			throw Exceptions.illegalState("Load resource {} error", res, e);
		}
		ClassReader reader = new ClassReader(data);
		if ((reader.getAccess() & Opcodes.ACC_PUBLIC) == 0) {
			return false;// 非公有跳过
		}
		String superName = reader.getSuperName();
		// 如果父类在，直接加载即可
		if ("com/github/xuse/querydsl/sql/RelationalPathBaseEx".equals(superName)) {
			try {
				doRegiste(reader, cl);
				return true;
			} catch (Exception e) {
				log.error("registe for {} error.", res, e);
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private void doRegiste(ClassReader res, ClassLoader cl)
			throws IOException, ClassNotFoundException {
		String name = res.getClassName().replace('/', '.');
		Class<?> clz = cl.loadClass(name);
		for (Field field : clz.getDeclaredFields()) {
			if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType() == clz) {
				try {
					Object obj = field.get(null);
					log.info("Scan register entity class:{}", name);
					this.registerExType((RelationalPathEx) obj);
				}catch(IllegalArgumentException | IllegalAccessException e) {
					log.error("registe class {}",name,e);
					throw Exceptions.toRuntime(e);
				}
				break;
			}
		}
	}
	
	public boolean has(SpecialFeature feature) {
		return getTemplates().supports(feature);
	}
	
	public AdvancedMapper getInsertBindingMap(boolean batch) {
		return batch ? FOR_INSERT_BATCH : FOR_INSERT;
	}
}
