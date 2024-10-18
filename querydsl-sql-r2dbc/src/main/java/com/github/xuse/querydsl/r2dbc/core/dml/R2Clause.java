package com.github.xuse.querydsl.r2dbc.core.dml;

import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.querydsl.core.QueryMetadata;

public interface R2Clause {
	QueryMetadata getMetadata();

	String notifyAction(R2BaseListener listener, R2ListenerContext context);
}
