package com.github.xuse.querydsl.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.annotation.CustomType;
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

	@SuppressWarnings("rawtypes")
	public void registerExType(IRelationPathEx path) {
		if (registeredclasses.add(path)) {
			for (Path<?> p : path.getColumns()) {
				ColumnMapping c = path.getColumnMetadata(p);
				Type<?> customType=c.getCustomType();
				CustomType anno;
				if(customType==null && (anno = c.getAnnotation(CustomType.class))!=null) {
					Class<? extends Type> clz=anno.value();
					try {
						customType = createInstance(clz, anno.parameters(),p.getType());
					} catch (Exception e) {
						log.error("customType on {}.{} error",path.getTableName(),p.getMetadata().getName(),e);
					}
				}
				if(customType!=null) {
					configuration.register(path.getTableName(), c.get().getName(), customType);
					log.info("Column [{}.{}] is registered to:{}",path.getTableName(), c.get().getName(),customType);
				}
			}
		}
	};

	@SuppressWarnings("rawtypes")
	private Type<?> createInstance( Class<? extends Type> clz, String[] parameters, Class<?> fieldType) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?>[] constructors=clz.getConstructors();
		int size=parameters.length;
		for(Constructor<?> c:constructors) {
			if(size==0 && c.getParameterCount()==1 && c.getParameterTypes()[0]==Class.class) {
				return (Type<?>) c.newInstance(fieldType);
			}else if(c.getParameterCount()==size && isStringType(c.getParameterTypes())) {
				return (Type<?>) c.newInstance((Object[])parameters);
			}else if(c.getParameterCount()==size+1 && isStringType(ArrayUtils.subarray(c.getParameterTypes(), 1, c.getParameterCount()))) {
				return (Type<?>) c.newInstance(ArrayUtils.addAll(new Object[] {fieldType}, (Object[])parameters));
			}
		}
		for(Constructor<?> c:constructors) {
			if(c.getParameterCount()==0) {
				return (Type<?>) c.newInstance();
			}
		}
		throw new IllegalArgumentException("can not Instant type "+clz.getName()+".") ;
	}

	private boolean isStringType(Class<?>[] parameterTypes) {
		for(int i=0;i<parameterTypes.length;i++) {
			if(parameterTypes[i]!=String.class) {
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

	public void setSlowSqlWarnMillis(long slowSqlWarnMillis) {
		this.slowSqlWarnMillis = slowSqlWarnMillis;
	}
}
