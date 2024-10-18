package com.github.xuse.querydsl.r2dbc.core.dml;

import java.sql.Connection;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.github.xuse.querydsl.sql.SQLQueryAlter;

public class SQLQueryR2<T> extends SQLQueryAlter<T> implements R2Clause{

	public SQLQueryR2(ConfigurationEx configuration) {
		super((Supplier<Connection>) null, configuration);
	}

	@Override
	public String notifyAction(R2BaseListener listener, R2ListenerContext context) {
		listener.notifyQuery(getMetadata());
		return null;
	}
}
