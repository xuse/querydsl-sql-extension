package com.github.xuse.querydsl.sql.support;

import java.util.Date;

import com.github.xuse.querydsl.util.Entry;

public interface DistributedLock {
	boolean tryLock();
	
	Entry<String,Date> lockedBy();
	
	 boolean unlock();
}
