package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;

import javax.inject.Provider;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.RelationalPathBase;

public class DropIndexQuery extends AbstractDDLClause<DropIndexQuery>{

	public DropIndexQuery(Provider<Connection> connection, ConfigurationEx configuration, RelationalPathBase<?> path) {
		super(connection, configuration, path);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
