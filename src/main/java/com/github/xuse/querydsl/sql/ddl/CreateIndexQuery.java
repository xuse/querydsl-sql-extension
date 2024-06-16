package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.querydsl.sql.RelationalPath;

public class CreateIndexQuery extends AbstractDDLClause<CreateIndexQuery>{
	private final Constraint constraint = new Constraint();
	
	public CreateIndexQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, path);
	}


	public CreateIndexQuery name(String name) {
		constraint.setName(name);
		return this;
	}
	
	
	@Override
	protected String generateSQL() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected boolean preExecute(MetadataQuerySupport c) {
		return true;
	}

}
