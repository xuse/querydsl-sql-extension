package com.github.xuse.querydsl.sql;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.xuse.querydsl.sql.log.QueryDSLDebugListener;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.SQLTemplates;

@Configuration
public class SpringConfiguration {
	@Bean
	public DataSource ds() {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		ds.setUrl("jdbc:derby:db;create=true");
		return ds;
	}

	@Bean
	public PlatformTransactionManager tx(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

	private com.querydsl.sql.Configuration querydslConfiguration() {
		SQLTemplates templates = DerbyTemplates.builder().newLineToSingleSpace().build();
		com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
		configuration.addListener(new QueryDSLDebugListener());
		return configuration;
	}

	@Bean
	public SQLQueryFactory factory(DataSource ds) {
		return SQLQueryFactory.createSpringQueryFactory(ds, querydslConfiguration());
	}

//	@Bean
//	public SessionFactory sessionFactory(DataSource ds) {
//		// 自行控制事务(基于编程方式)
//		// 1、默认状态下，每个操作都获得连接，操作完成后关闭连接（需要有连接池）
//		// 2. 内置事务会去尝试改变autoCommit状态，并且调用commit rollback等方法。
//		// return new DbClient(ds);
//
//		// -----------------------------------------------------------------
//		// 日志中如何打印Session
//
//		// Spring进行事务控制的方案（基于Spring Datasource共享连接）
//		// 1、对连接不进行关闭、状态设置。
//		// 2、多个session公用事务由SpringProvider处理。
//
//		// 简单模式
//		// 由Provider提供一个固定的连接供操作。
//		// 整体关闭时，连接关闭，适用于一些单线程的小工具。
//		return new SessionFactoryBean().setPackagesToScan(new String[] { "com.github.xuse.querydsl.entity" }).setSpringDataSource(ds).setDebug(true).build();
//	}

}
