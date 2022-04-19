package com.github.xuse.querydsl.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.github.xuse.querydsl.annotation.CustomType;
import com.github.xuse.querydsl.asm.ClassReader;
import com.github.xuse.querydsl.asm.Opcodes;
import com.github.xuse.querydsl.config.util.ClassScanner;
import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.SnowflakeIdWorker;
import com.querydsl.core.types.Path;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.types.Type;

/**
 * 在QueryDSL原生基础上扩展的配置项目
 * 
 * @author jiyi
 *
 */
public class ConfigurationEx {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationEx.class);

	/**
	 * 配置
	 */
	private final Configuration configuration;

	/**
	 * 在使用com.github.xuse.querydsl.sql.log.QueryDSLDebugListener的场景下，如果SQL执行时间超过这个数值（毫秒）
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
	private final Set<IRelationPathEx<?>> registeredclasses = new HashSet<>();
	
	
	private SnowflakeIdWorker snowflakeWorker;

	public Configuration get() {
		return configuration;
	}

	public ConfigurationEx(Configuration configuration) {
		this.configuration = configuration;
	}

	public ConfigurationEx(SQLTemplates templates) {
		this(new Configuration(templates));
	}

	public SQLTemplates getTemplates() {
		return configuration.getTemplates();
	}

	public void addListener(SQLListener listener) {
		configuration.addListener(listener);
	}
	
	/**
	 * 为Snowflake id生成器进行初始化。
	 * @param workerId
	 * @param datacenterId
	 */
	public void initSnowflake(int workerId, int datacenterId) {
		this.snowflakeWorker = new SnowflakeIdWorker(workerId, datacenterId);
	}

	public SnowflakeIdWorker getSnowflakeWorker() {
		return snowflakeWorker;
	}

	/**
	 * 注册一个表映射实例。通过扫描该对象上的注解，来识别表定义中的信息。
	 * 
	 * @param path
	 */
	@SuppressWarnings("rawtypes")
	public void registerExType(IRelationPathEx<?> path) {
		if (registeredclasses.add(path)) {
			for (Path<?> p : path.getColumns()) {
				ColumnMapping c = path.getColumnMetadata(p);
				Type<?> customType = c.getCustomType();
				CustomType anno;
				if (customType == null && (anno = c.getAnnotation(CustomType.class)) != null) {
					Class<? extends Type> clz = anno.value();
					try {
						customType = createInstance(clz, anno.parameters(), p.getType());
					} catch (Exception e) {
						log.error("customType on {}.{} error", path.getTableName(), p.getMetadata().getName(), e);
					}
				}
				if (customType != null) {
					configuration.register(path.getTableName(), c.get().getName(), customType);
					log.info("Column [{}.{}] is registered to:{}", path.getTableName(), c.get().getName(), customType);
				}
			}
		}
	};

	@SuppressWarnings("rawtypes")
	private Type<?> createInstance(Class<? extends Type> clz, String[] parameters, Class<?> fieldType)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?>[] constructors = clz.getConstructors();
		int size = parameters.length;
		for (Constructor<?> c : constructors) {
			if (size == 0 && c.getParameterCount() == 1 && c.getParameterTypes()[0] == Class.class) {
				return (Type<?>) c.newInstance(fieldType);
			} else if (c.getParameterCount() == size && isStringType(c.getParameterTypes())) {
				return (Type<?>) c.newInstance((Object[]) parameters);
			} else if (c.getParameterCount() == size + 1
					&& isStringType(ArrayUtils.subarray(c.getParameterTypes(), 1, c.getParameterCount()))) {
				return (Type<?>) c.newInstance(ArrayUtils.addAll(new Object[] { fieldType }, (Object[]) parameters));
			}
		}
//		for(Constructor<?> c:constructors) {
//			if(c.getParameterCount()==0) {
//				return (Type<?>) c.newInstance();
//			}
//		}
		throw new IllegalArgumentException("can not Instant type " + clz.getName() + ".");
	}

	private boolean isStringType(Class<?>[] parameterTypes) {
		for (int i = 0; i < parameterTypes.length; i++) {
			if (parameterTypes[i] != String.class) {
				return false;
			}
		}
		return true;
	}

	public void register(Type<?> type) {
		configuration.register(type);
	}

	public void registerType(String typeName, Class<?> clazz) {
		configuration.registerType(typeName, clazz);
	}

	public void register(String table, String column, Type<?> type) {
		configuration.register(table, column, type);
	}

	public long getSlowSqlWarnMillis() {
		return slowSqlWarnMillis;
	}

	/**
	 * 设置一个慢SQL监测阈值。当SQL执行耗时大于这个数值时，日志打印警告。
	 * 
	 * @param slowSqlWarnMillis 慢SQL阈值，单位毫秒
	 */
	public void setSlowSqlWarnMillis(long slowSqlWarnMillis) {
		this.slowSqlWarnMillis = slowSqlWarnMillis;
	}

	public int getDefaultQueryTimeout() {
		return defaultQueryTimeout;
	}

	/**
	 * 设置通用的查询超时时间，对应JDBC的查询超时，查询超时后让数据库放弃任务，保护数据库不被慢查挂死。
	 * 
	 * @param defaultQueryTimeout 超时保护，单位秒。不建议设置5秒内的数值。
	 */
	public void setDefaultQueryTimeout(int defaultQueryTimeout) {
		this.defaultQueryTimeout = defaultQueryTimeout;
	}

	public void scanPackages(String... pkgNames) {
		ClassLoader cl=Thread.currentThread().getContextClassLoader();
		List<Resource> clss = new ClassScanner().scan(pkgNames);
		int n = 0;
		for (Resource cls : clss) {
			if (!cls.isReadable()) {
				continue;
			}
			if (cls.getFilename().startsWith("Q")) {
				try {
					if (processEnhance(cls,cl)) {
						n++;
					}
				} catch (Exception e) {
					log.error("Enhance error: {}", cls, e);
					continue;
				}
			}
		}

	}

	private boolean processEnhance(Resource res,ClassLoader cl) {
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
		if ("com/github/xuse/querydsl/sql/RelationalPathBaseEx".equals(superName)
				) {
			try {
				doRegiste(reader,cl);
			} catch (Exception e) {
				log.error("registe for {} error.",res,e);
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private void doRegiste(ClassReader res, ClassLoader cl) throws IOException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		String name = res.getClassName().replace('/', '.');
		Class<?> clz=cl.loadClass(name);
		for(Field field:clz.getDeclaredFields()) {
			if((field.getModifiers() & Modifier.STATIC)>0 && field.getType()==clz) {
				Object obj=field.get(null);
				log.info("Scan register entity class:{}",name);
				this.registerExType((IRelationPathEx)obj);
				break;
			}
		}
	}
}
