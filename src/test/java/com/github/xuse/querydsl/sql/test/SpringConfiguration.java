package com.github.xuse.querydsl.sql.test;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.sql.AbstractTestBase;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.sql.support.UpdateDeleteProtectListener;
import com.github.xuse.querydsl.types.EnumByCodeType;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.sql.SQLTemplates;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan("com.github.xuse.querydsl.sql.test.beans")
public class SpringConfiguration {
	@Bean
	public DataSource mysqlDs() {
		DriverManagerDataSource ds = AbstractTestBase.getEffectiveDs();
		return wrapAsPool(ds);
	}

	private DataSource wrapAsPool(DataSource ds) {
		HikariDataSource pool = new HikariDataSource();
		pool.setDataSource(ds);
		return pool;
	}

	@Bean
	public PlatformTransactionManager tx(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

	private ConfigurationEx querydslConfiguration(DriverManagerDataSource ds) {
		SQLTemplates templates = SQLQueryFactory.calcSQLTemplate(ds.getUrl());
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.setSlowSqlWarnMillis(800);
		configuration.addListener(new UpdateDeleteProtectListener());
		configuration.register(new EnumByCodeType<>(Gender.class));
		configuration.register(new EnumByCodeType<>(TaskStatus.class));
		configuration.getScanOptions()
			.allowDrops()
			.setCreateMissingTable(true)
			.setDataInitBehavior(DataInitBehavior.FOR_ALL_TABLE)
			.detectPermissions(true)
			.useDataInitTable(true);
		configuration.scanPackages("com.github.xuse.querydsl.entity");
		return configuration;
	}

	@Bean
	public SQLQueryFactory factory(DataSource ds) {
		try {
			return SQLQueryFactory.createSpringQueryFactory(ds, querydslConfiguration(AbstractTestBase.getEffectiveDs()));
		} catch (Exception e) {
			e.printStackTrace();
			throw Exceptions.toRuntime(e);
		}
	}
}
