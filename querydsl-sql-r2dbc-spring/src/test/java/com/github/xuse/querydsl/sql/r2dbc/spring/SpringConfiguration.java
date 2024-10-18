package com.github.xuse.querydsl.sql.r2dbc.spring;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.core.R2dbcFactory;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLTemplates;

import io.github.xuse.querydsl.r2dbc.spring.QuerydslR2dbc;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan("com.github.xuse.querydsl.sql.r2dbc.service")
public class SpringConfiguration {
	/**
	 * @return 连接池
	 */
	@Bean
	public ConnectionFactory connectionPool() {
		H2ConnectionConfiguration config = H2ConnectionConfiguration.builder().file("~/h2db").build();
		H2ConnectionFactory datasource = new H2ConnectionFactory(config);
		ConnectionPool pool = new ConnectionPool(ConnectionPoolConfiguration.builder(datasource).minIdle(0).maxSize(2)
				.maxIdleTime(Duration.ofMillis(1000)).build());
		return pool;
	}
	
	/**
	 * 支持Spring事务的r2dbc factory;
	 * @param connectionPool
	 * @return
	 */
	@Bean
	public R2dbcFactory r2dbcFactory(ConnectionFactory connectionPool) {
		return QuerydslR2dbc.createSpringR2dbFactory(connectionPool, querydslConfiguration());
	}

	/**
	 * @param ds
	 * @return 事务管理器
	 */
	@Bean
	public R2dbcTransactionManager tx(ConnectionFactory ds) {
		return new R2dbcTransactionManager(ds);
	}

	private ConfigurationEx querydslConfiguration() {
		SQLTemplates templates =H2Templates.builder().newLineToSingleSpace().build();
		ConfigurationEx configuration = new ConfigurationEx(templates);
		
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.setSlowSqlWarnMillis(800);
		//configuration.addListener(new UpdateDeleteProtectListener());
//		configuration.getScanOptions().allowDrops().setCreateMissingTable(true)
//				.setDataInitBehavior(DataInitBehavior.FOR_ALL_TABLE).detectPermissions(true).useDataInitTable(true);
		configuration.scanPackages("com.github.xuse.querydsl.sql.r2dbc.entity");
		return configuration;
	}
}
