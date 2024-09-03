package com.github.xuse.querydsl.sql.spring;

import javax.sql.DataSource;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;

public class QueryDSLSqlExtension {
	/**
	 * 创建SQLQueryFactory对象
	 * @param datasource DataSource
	 * @param configuration 配置
	 * @return com.github.xuse.querydsl.sql.SQLQueryFactory
	 */
	public static SQLQueryFactory createSpringQueryFactory(DataSource datasource, ConfigurationEx configuration) {
		configuration.addListener(UnmanagedConnectionCloseListener.DEFAULT);
		configuration.setExceptionTranslator(new SpringExceptionTranslator());
		return new SQLQueryFactory(configuration, new SpringProvider(datasource));
	}
}
