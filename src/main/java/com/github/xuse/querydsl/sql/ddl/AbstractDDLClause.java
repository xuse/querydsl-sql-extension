package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.RelationalPathBase;

public abstract class AbstractDDLClause<C extends DDLClause<C>> implements DDLClause<C>{

	public AbstractDDLClause(Supplier<Connection> connection, ConfigurationEx configuration, RelationalPathBase<?> path) {
	}

}
