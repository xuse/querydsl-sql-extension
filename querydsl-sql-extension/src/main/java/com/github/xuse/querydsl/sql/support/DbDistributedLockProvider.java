package com.github.xuse.querydsl.sql.support;

import com.github.xuse.querydsl.init.DDLLockUtils;
import com.github.xuse.querydsl.init.DataInitLog;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.util.Exceptions;

public class DbDistributedLockProvider implements DistributedLockProvider{
	final SQLQueryFactory factory;
	
	DbDistributedLockProvider(SQLQueryFactory factory) {
		this.factory=factory;
		DataInitLog global=DDLLockUtils.initInitLogTable(factory);
		if(global==null) {
			throw Exceptions.illegalState("Unable to init db lock table.");
		}
	}
	
	@Override
	public DistributedLock getLock(String lockName, int maxLockMinutes) {
		return new DbLockImpl(factory,lockName,maxLockMinutes);
	}

	
	public static DistributedLockProvider create(SQLQueryFactory factory) {
		return new DbDistributedLockProvider(factory);
	}
}
