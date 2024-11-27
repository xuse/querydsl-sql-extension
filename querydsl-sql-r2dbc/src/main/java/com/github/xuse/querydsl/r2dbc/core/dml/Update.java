package com.github.xuse.querydsl.r2dbc.core.dml;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.github.xuse.querydsl.repository.QueryBuilder;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPath;

public class Update<E> extends SQLUpdateClauseAlter implements R2Clause,SQLContainer{
	public Update(ConfigurationEx configuration, RelationalPath<E> entity) {
		super(null, configuration, entity);
	}

	@Override
	public QueryMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String notifyAction(R2BaseListener listener, R2ListenerContext context) {
		listener.notifyUpdate(entity, metadata, updates);
		return "Update";
	}

	@SuppressWarnings("unchecked")
	public QueryBuilder<E,?,Update<E>> where(){
		DefaultQueryMetadata meta=(DefaultQueryMetadata) super.metadata;
		return new QueryBuilder<>((RelationalPath<E>)super.entity, meta, this);
	}

	public <T extends Comparable<T>> Update<E> set(LambdaColumn<E,T> path, T value) {
		super.set(path, value);
		return this;
	}
	
	public <T extends Comparable<T>> Update<E> set(LambdaColumn<E,T> path, Expression<T> value) {
		super.set(path, value);
		return this;
	}
	
	public <T extends Comparable<T>> Update<E> setIf(boolean doSet, LambdaColumn<E,T> path, T value) {
		setIf(doSet, (Path<T>)path, value);
		return this;
	}
	
	public <T extends Comparable<T>> Update<E> setIf(boolean doSet, LambdaColumn<E, T> path, Expression<? extends T> expression) {
		setIf(doSet, (Path<T>)path, expression);
		return this;
	}
}
