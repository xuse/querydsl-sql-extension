package com.github.xuse.querydsl.r2dbc.core.dml;

import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContext;
import com.querydsl.core.QueryMetadata;

public class Dummy {
	public static final R2Clause R2Clause=new R2Clause(){
		@Override
		public QueryMetadata getMetadata() {
			return null;
		}

		@Override
		public String notifyAction(R2BaseListener listener, R2ListenerContext context) {
			return "";
		}
	};
}
