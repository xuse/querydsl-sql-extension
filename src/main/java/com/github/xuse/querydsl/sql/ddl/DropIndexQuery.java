package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;

import javax.inject.Provider;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPathBase;

public class DropIndexQuery extends AbstractDDLClause<DropIndexQuery>{

	public DropIndexQuery(Provider<Connection> connection, Configuration configuration, RelationalPathBase<?> path) {
		super(connection, configuration, path);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

}
