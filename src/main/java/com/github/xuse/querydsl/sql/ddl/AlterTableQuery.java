package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.RelationalPathBase;

public class AlterTableQuery extends AbstractDDLClause<AlterTableQuery> {

	public AlterTableQuery(Supplier<Connection> connection, ConfigurationEx configuration, RelationalPathBase<?> path) {
		super(connection, configuration, path);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
