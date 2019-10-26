package com.github.xuse.querydsl.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.annotation.CustomType;
import com.github.xuse.querydsl.annotation.Parameter;
import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
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
	
	private static final Logger log=LoggerFactory.getLogger(ConfigurationEx.class);
	
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
	 * 已完成类型注册的Class
	 */
	private final Set<IRelationPathEx> registeredclasses = new HashSet<>();

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

	public void registerExType(IRelationPathEx path) {
		if (registeredclasses.add(path)) {
			for (Path<?> p : path.getColumns()) {
				ColumnMapping c = path.getColumnMetadata(p);
				CustomType anno = c.getAnnotation(CustomType.class);
				if (anno == null) {
					continue;
				}
				Class<? extends Type> clz=anno.value();
				Type<?> t = null;
				try {
					t = createInstance(clz, anno.parameters(),p.getType());
				} catch (Exception e) {
					log.error("customType on {}.{} error",path.getTableName(),p.getMetadata().getName(),e);
				}
				if(t!=null) {
					configuration.register(path.getTableName(), c.get().getName(), t);
					log.info("Column [{}.{}] is registered to:{}",path.getTableName(), c.get().getName(),t);
				}
			}
		}
	};

	private Type<?> createInstance(Class<? extends Type> clz, Parameter[] parameters, Class<?> fieldType) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?>[] constructors=clz.getConstructors();
		for(Constructor<?> c:constructors) {
			if(c.getParameterCount()==1 && c.getParameterTypes()[0]==Class.class) {
				return init((Type<?>) c.newInstance(fieldType),parameters);
			}
		}
		for(Constructor<?> c:constructors) {
			if(c.getParameterCount()==0) {
				return init((Type<?>) c.newInstance(),parameters);
			}
		}
		return null;
	}

	private Type<?> init(Type<?> t, Parameter[] parameters) {
		return t;
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

	public void setSlowSqlWarnMillis(long slowSqlWarnMillis) {
		this.slowSqlWarnMillis = slowSqlWarnMillis;
	}
}
