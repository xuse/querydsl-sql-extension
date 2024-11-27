package com.github.xuse.querydsl.r2dbc.core.dml;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.github.xuse.querydsl.repository.QueryBuilder;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.RelationalPath;

public class Delete<E> extends SQLDeleteClauseAlter implements R2Clause,SQLContainer{

	public Delete( ConfigurationEx configuration, RelationalPath<E> entity) {
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

	
	@SuppressWarnings("unchecked")
	public QueryBuilder<E,?,Delete<E>> where(){
		DefaultQueryMetadata meta=super.metadata;
		return new QueryBuilder<>((RelationalPath<E>)super.entity, meta, this);
	}
}

