package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.RelationalPathBase;

public class DropTableQuery extends AbstractDDLClause<DropTableQuery>{

	public DropTableQuery(Supplier<Connection> connection, ConfigurationEx configuration, RelationalPathBase<?> path) {
		super(connection, configuration, path);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
