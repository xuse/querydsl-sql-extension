package com.github.xuse.querydsl.r2dbc.clause;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public abstract class R2ClauseBase<T extends R2ClauseBase<T>> {
	protected final ConnectionFactory connection;
	protected final ConfigurationEx configuration;
	protected final Configuration   qConfiguration;
	protected final RelationalPath<?> entity;

	public R2ClauseBase(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		this.configuration=configuration;
		this.qConfiguration=configuration.get();
		this.connection=connection;
		this.entity=entity;
	}

}
