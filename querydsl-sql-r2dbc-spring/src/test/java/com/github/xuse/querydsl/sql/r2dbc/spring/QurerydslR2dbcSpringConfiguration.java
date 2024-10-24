package com.github.xuse.querydsl.sql.r2dbc.spring;

import java.time.Duration;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.R2dbcFactory;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import io.github.xuse.querydsl.r2dbc.spring.QuerydslR2dbc;

import com.querydsl.sql.*;
import io.r2dbc.h2.*;
import io.r2dbc.pool.*;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class QurerydslR2dbcSpringConfiguration {
	@Bean
	public ConnectionFactory connectionPool() {
		H2ConnectionFactory datasource = new H2ConnectionFactory(H2ConnectionConfiguration.builder().file("~/h2db").build());
		ConnectionPool pool = new ConnectionPool(ConnectionPoolConfiguration.builder(datasource)
				.minIdle(0)
				.maxSize(10)
				.maxIdleTime(Duration.ofMillis(1000)).build());
		return pool;
	}
	
	@Bean
	public R2dbcFactory r2dbcFactory(ConnectionFactory connectionPool) {
		SQLTemplates templates =H2Templates.builder().newLineToSingleSpace().build();
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.scanPackages("com.github.xuse.querydsl.sql.r2dbc.entity");
		return QuerydslR2dbc.createSpringR2dbFactory(connectionPool, configuration);
	}

	@Bean
	public R2dbcTransactionManager tx(ConnectionFactory ds) {
		return new R2dbcTransactionManager(ds);
	}
}
