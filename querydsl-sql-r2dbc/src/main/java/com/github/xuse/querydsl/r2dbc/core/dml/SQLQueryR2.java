package com.github.xuse.querydsl.r2dbc.core.dml;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.github.xuse.querydsl.repository.QueryBuilder;
import com.github.xuse.querydsl.sql.SQLQueryAlter;

import java.sql.Connection;
import java.util.function.Supplier;

public class SQLQueryR2<T> extends SQLQueryAlter<T> implements R2Clause{

	public SQLQueryR2(ConfigurationEx configuration) {
		super((Supplier<Connection>) null, configuration);
	}

	@Override
	public String notifyAction(R2BaseListener listener, R2ListenerContext context) {
		listener.notifyQuery(getMetadata());
		return null;
	}
	
	public QueryBuilder<T,?,SQLQueryR2<T>> where(){
		return new QueryBuilder<>(null, getMetadata(), this);
	}
	
	public <E> QueryBuilder<E,?,SQLQueryR2<T>> where(Class<E> clz){
		return new QueryBuilder<>(null, getMetadata(), this);
	}
}
