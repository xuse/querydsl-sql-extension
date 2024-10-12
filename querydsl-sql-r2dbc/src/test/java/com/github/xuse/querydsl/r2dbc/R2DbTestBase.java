package com.github.xuse.querydsl.r2dbc;

import java.time.Duration;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.H2Templates;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;

public class R2DbTestBase {
	private static volatile ConnectionPool pool;
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
	
	public static ConfigurationEx getConfiguration() {
		return new ConfigurationEx(H2Templates.builder().build());
	}
}
