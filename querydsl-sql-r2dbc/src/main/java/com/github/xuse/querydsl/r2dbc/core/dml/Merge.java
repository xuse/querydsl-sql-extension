package com.github.xuse.querydsl.r2dbc.core.dml;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.github.xuse.querydsl.sql.dml.SQLMergeClauseAlter;
import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.RelationalPath;

public class Merge extends SQLMergeClauseAlter implements R2Clause{
	public Merge(ConfigurationEx configuration, RelationalPath<?> entity) {
		super(null, configuration, entity);
	}
	
	@Override
	public QueryMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String notifyAction(R2BaseListener listener, R2ListenerContext context) {
		listener.notifyMerge(entity, metadata, keys, columns, values, subQuery);
		return "Merge";
	}
}
