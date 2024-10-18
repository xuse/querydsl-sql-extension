package io.github.xuse.querydsl.r2dbc.spring;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.core.R2dbcFactory;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

public class QuerydslR2dbc {
	/**
	 * 创建SQLQueryFactory对象
	 * 
	 * @param datasource    DataSource
	 * @param configuration 配置
	 * @return com.github.xuse.querydsl.sql.SQLQueryFactory
	 */
	public static R2dbcFactory createSpringR2dbFactory(ConnectionFactory connectionPool,
			ConfigurationEx configuration) {
		SpringRectiveTransactionProvider txBinder = new SpringRectiveTransactionProvider(connectionPool);
		return new R2dbcFactory(txBinder, configuration, connection -> {
			if (connection instanceof UnmanagedR2Connection) {
				return (Mono<Void>) connection.close();
			}
			return Mono.empty();
		});
	}
}
