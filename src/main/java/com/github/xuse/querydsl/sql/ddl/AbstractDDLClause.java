package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;

import javax.inject.Provider;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPathBase;

public abstract class AbstractDDLClause<C extends DDLClause<C>> implements DDLClause<C>{

	public AbstractDDLClause(Provider<Connection> connection, Configuration configuration, RelationalPathBase<?> path) {
	}

}
