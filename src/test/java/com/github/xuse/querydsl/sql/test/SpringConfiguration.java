package com.github.xuse.querydsl.sql.test;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.SQLTemplates;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class SpringConfiguration {
	static String s1 = "r-o-o-t";
	static String s2 = "88-07-59-98";

	private RDBMS type = RDBMS.Derby;

	static {
		System.setProperty("mysql.user", s1.replace("-", ""));
		System.setProperty("mysql.password", s2.replace("-", ""));
	}

	private static enum RDBMS {
		MySQL, Derby
	}

	@Bean
	public DataSource mysqlDs() {
		DriverManagerDataSource ds = new DriverManagerDataSource();

		switch (type) {
		case MySQL:
			ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
			// &useInformationSchema=true
			ds.setUrl("jdbc:mysql://10.86.16.12:3306/test?useSSL=false");
			ds.setUsername(System.getProperty("mysql.user"));
			ds.setPassword(System.getProperty("mysql.password"));
			break;
		case Derby:
			ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
			ds.setUrl("jdbc:derby:db;create=true");
			break;
		}
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

	private ConfigurationEx querydslConfiguration() {
		SQLTemplates templates = null;
		switch (type) {
		case Derby:
			templates = DerbyTemplates.builder().build();
			break;
		case MySQL:
			templates = new MySQLWithJSONTemplates();
			break;
		default:
			throw Exceptions.unsupportedOperation("");
		}
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.setExceptionTranslator(null);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		return configuration;
	}

	@Bean
	public SQLQueryFactory factory(DataSource ds) {
		try {
			return SQLQueryFactory.createSpringQueryFactory(ds, querydslConfiguration());
		} catch (Exception e) {
			e.printStackTrace();
			throw Exceptions.toRuntime(e);
		}

	}
}
