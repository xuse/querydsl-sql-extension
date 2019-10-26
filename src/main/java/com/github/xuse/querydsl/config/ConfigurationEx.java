package com.github.xuse.querydsl.config;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.querydsl.core.types.Path;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.types.Type;

/**
 * 在QueryDSL原生基础上扩展的配置项目
 * @author jiyi
 *
 */
public class ConfigurationEx {
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
	private final Set<IRelationPathEx> registeredclasses=new HashSet<>(); 

	public Configuration get() {
		return configuration;
	}
	
	public ConfigurationEx(Configuration configuration) {
		this.configuration=configuration;
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

	public void checkRegister(IRelationPathEx path) {
		if(registeredclasses.add(path)) {
			for(Path<?> p:path.getColumns()) {
				Field field;
				Object o=p.getMetadata().getElement();
			}
		}
	};
	
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
