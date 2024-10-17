package com.github.xuse.querydsl.r2dbc;

import java.time.Duration;

import javax.sql.DataSource;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.core.R2dbFactory;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLTemplates;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;

public class R2DbTestBase {
	private static volatile ConnectionPool pool;
	
	private static DataSource ds;
	
	private static R2dbFactory r2factory;
	
	private static SQLQueryFactory factory;
	
	
	public static R2dbFactory getR2Factory() {
		if(r2factory!=null) {
			return r2factory;
		}
		return r2factory = new R2dbFactory(getConnectionFactory(), getConfiguration());
	}
	
	//
	public static ConnectionPool getConnectionFactory() {
		if(pool!=null) {
			return pool;
		}
		synchronized (R2DbTestBase.class) {
			if (pool == null) {
				H2ConnectionConfiguration config = H2ConnectionConfiguration.builder().file("~/h2db").build();
				H2ConnectionFactory datasource = new H2ConnectionFactory(config);
				pool = new ConnectionPool(ConnectionPoolConfiguration.builder(datasource)
						.maxIdleTime(Duration.ofMillis(1000)).maxSize(20).build());
			}
		}
		return pool; 
	}
	
	public static SQLQueryFactory getSqlFactory() {
		if(factory!=null) {
			return factory;
		}
		SimpleDataSource ds=new SimpleDataSource("jdbc:h2:~/h2db","","");
		ds.setDriverClassName("org.h2.Driver");
		R2DbTestBase.ds=ds;
		return factory = new SQLQueryFactory(querydslConfiguration(SQLQueryFactory.calcSQLTemplate(ds.getUrl())), ds);
	}
	
	public static ConfigurationEx querydslConfiguration(SQLTemplates templates) {
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.setSlowSqlWarnMillis(4000);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.allowTableDropAndCreate();
		// 如果使用了自定义映射，需要提前注册，或者扫描指定包
		configuration.getScanOptions()
			.setAlterExistTable(false);
//			.allowDrops()
//			.setDataInitBehavior(DataInitBehavior.NONE);
		configuration.scanPackages("com.github.xuse.querydsl.r2dbc.entity");
		return configuration;
	}
	public static ConfigurationEx getConfiguration() {
		return new ConfigurationEx(H2Templates.builder().build());
	}
}