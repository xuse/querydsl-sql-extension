package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.querydsl.sql.RelationalPath;

public class DropConstraintQuery extends AbstractDDLClause<DropConstraintQuery> {

	public DropConstraintQuery(MetadataQuerySupport connection, ConfigurationEx configuration,
			RelationalPath<?> path) {
		super(connection, configuration, path);
	}

	@Override
	protected String generateSQL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean preExecute(MetadataQuerySupport metadata) {
		return true;
	}

}
