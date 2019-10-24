package com.github.xuse.querydsl.config;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.types.Type;

/**
 * 可以扩展的配置项目
 * @author jiyi
 *
 */
public class ConfigurationEx {
	private final Configuration configuration;
	
	/**
	 * 在使用com.github.xuse.querydsl.sql.log.QueryDSLDebugListener的场景下，如果SQL执行时间超过这个数值（毫秒）
	 * 就会记录为错误日志
	 */
	private long slowSqlWarnMillis = 10000;
	

	public Configuration get() {
		return configuration;
	}
	
	public ConfigurationEx(Configuration configuration) {
		this.configuration=configuration;
	}

	public ConfigurationEx(SQLTemplates templates) {
		this.configuration=new Configuration(templates);
	}

	public SQLTemplates getTemplates() {
		return configuration.getTemplates();
	}

	public void addListener(SQLListener listener) {
		configuration.addListener(listener);
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
