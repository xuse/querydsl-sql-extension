package com.github.xuse.querydsl.config;

import com.github.xuse.querydsl.types.IntegerASVarcharType;
import com.github.xuse.querydsl.types.LongASDateTimeType;
import com.github.xuse.querydsl.types.LongASVarcharType;
import com.github.xuse.querydsl.types.StringAsBigIntType;
import com.github.xuse.querydsl.types.StringAsIntegerType;
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
	

	public Configuration get() {
		return configuration;
	}
	
	public ConfigurationEx(Configuration configuration) {
		this.configuration=configuration;
		extendTypes();
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

	/**
	 * 默认的数据映射扩充类型
	 */
	private void extendTypes() {
		this.register(new IntegerASVarcharType());
		this.register(new LongASDateTimeType(true));
		this.register(new LongASVarcharType());
		this.register(new StringAsBigIntType());
		this.register(new StringAsIntegerType());
	}
}
