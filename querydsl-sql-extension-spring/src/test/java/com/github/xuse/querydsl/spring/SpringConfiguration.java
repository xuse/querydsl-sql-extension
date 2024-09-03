package com.github.xuse.querydsl.spring;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.spring.enums.Gender;
import com.github.xuse.querydsl.spring.enums.Status;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.sql.spring.QueryDSLSqlExtension;
import com.github.xuse.querydsl.sql.support.UpdateDeleteProtectListener;
import com.github.xuse.querydsl.types.EnumByCodeType;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.sql.SQLTemplates;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan("com.github.xuse.querydsl.spring")
public class SpringConfiguration {
	@Bean
	public DataSource mysqlDs() {
		DriverManagerDataSource ds = SpringTestBase.effectiveDs;
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

	private ConfigurationEx querydslConfiguration(DataSource ds) {
		SQLTemplates templates = SQLQueryFactory.calcSQLTemplate(((DriverManagerDataSource)ds).getUrl());
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.setSlowSqlWarnMillis(800);
		configuration.addListener(new UpdateDeleteProtectListener());
		configuration.register(new EnumByCodeType<>(Gender.class));
		configuration.register(new EnumByCodeType<>(Status.class));
		configuration.getScanOptions()
			.allowDrops()
			.setCreateMissingTable(true)
			.setDataInitBehavior(DataInitBehavior.FOR_ALL_TABLE)
			.detectPermissions(true)
			.useDataInitTable(true);
		configuration.scanPackages("com.github.xuse.querydsl.spring.entity");
		return configuration;
	}

	@Bean
	public SQLQueryFactory factory(DataSource ds) {
		try {
			return QueryDSLSqlExtension.createSpringQueryFactory(ds, querydslConfiguration(SpringTestBase.getEffectiveDs()));
		} catch (Exception e) {
			e.printStackTrace();
			throw Exceptions.toRuntime(e);
		}
	}
}
