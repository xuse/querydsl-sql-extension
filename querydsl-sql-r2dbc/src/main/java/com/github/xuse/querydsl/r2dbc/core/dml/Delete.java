package com.github.xuse.querydsl.r2dbc.core.dml;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.RelationalPath;

public class Delete extends SQLDeleteClauseAlter implements R2Clause{

	public Delete( ConfigurationEx configuration, RelationalPath<?> entity) {
		super(null, configuration, entity);
	}

	@Override
	public QueryMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String notifyAction(R2BaseListener listener, R2ListenerContext context) {
		listener.notifyDelete(entity, metadata);
		return "Delete";
	}


}
