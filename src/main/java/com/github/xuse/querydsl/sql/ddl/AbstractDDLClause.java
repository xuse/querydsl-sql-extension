package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;

import javax.inject.Provider;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.RelationalPathBase;

public abstract class AbstractDDLClause<C extends DDLClause<C>> implements DDLClause<C>{

	public AbstractDDLClause(Provider<Connection> connection, ConfigurationEx configuration, RelationalPathBase<?> path) {
	}

}
