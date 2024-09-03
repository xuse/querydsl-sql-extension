package com.github.xuse.querydsl.sql.support;

public interface DistributedLockProvider {
	DistributedLock getLock(String lockName, int maxLockMinutes);
}
