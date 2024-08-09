package com.github.xuse.querydsl.sql.support;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.xuse.querydsl.init.DataInitLog;
import com.github.xuse.querydsl.init.QDataInitLog;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.ProcessUtil;
import com.querydsl.core.types.dsl.Expressions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbLockImpl implements DistributedLock{
	private static final QDataInitLog t = QDataInitLog.dataInitLog;
	
	protected final SQLQueryFactory factory;
	
	protected final String lockName;
	
	private final String agentName;
	
	protected int lockExpireMinutes;
	
	private transient volatile DataInitLog lockedByOther;
	
	public DbLockImpl(SQLQueryFactory factory, String lockName) {
		this(factory, lockName, 5);
	}

	
	DbLockImpl(SQLQueryFactory factory, String lockName, int lockExpireMinutes){
		this.factory=factory;
		this.lockName=lockName;
		this.lockExpireMinutes=lockExpireMinutes;
		this.agentName=getAgent();
	}
	
	private String getAgent() {
		return ProcessUtil.getHostname() + "(" + ProcessUtil.getLocalIp() + ") OS:"
				+ ProcessUtil.getOSName();
	}

	public boolean tryLock() {
		boolean result= tryLock0();
		if(result) {
			log.info("[LOCK Obtained] {}",lockName);
		}else {
			if(lockedByOther==null) {
				lockedByOther=factory.selectFrom(t).where(t.tableName.eq(lockName)).fetchFirst();
			}
			log.info("[LOCK Failure] {}, {}",lockName,lockedByOther);
		}
		return result;
	}
	
	private boolean tryLock0() {
		DataInitLog lock=factory.selectFrom(t).where(t.tableName.eq(lockName)).fetchFirst();
		if(lock==null) {
			return tryLockWithInsert();
		}else if(lock.getDisabled()==0) {
			return tryLockWithUpdate(0);			
		}else if(lock.getDisabled()==2){
			Date lockTime=lock.getLastInitTime();
			if(lockTime==null || (System.currentTimeMillis()-lockTime.getTime())>TimeUnit.MINUTES.toMillis(lockExpireMinutes)) {
				log.warn("[Lock Expired] Last lock {} Expired, locked by {}@{}.",lockName,lock.getLastInitUser(),lock.getLastInitTime());
				return tryLockWithUpdate(2); 
			}
			if(agentName.equals(lock.getLastInitUser())) {
				log.warn("[Lock Reentrant] Last lock {} Reentrant, locked by {}@{}.",lockName,lock.getLastInitUser(),lock.getLastInitTime());
				return tryLockWithUpdate(2);
			}
			this.lockedByOther = lock;
			return false;
		}else {
			return false;
		}
	}

	public boolean unlock() {
		long count=factory.update(t)
		.set(t.disabled, Expressions.ZERO)
		.set(t.lastInitUser, "")
		.set(t.lastInitTime, new Date())
		.set(t.lastInitResult,"Unlocked")
		.where(t.tableName.eq(lockName),t.disabled.eq(2)).execute();
		return count>0;
	}
	
	public Entry<String,Date> lockedBy() {
		DataInitLog lock = this.lockedByOther;
		Assert.notNull(lock);
		this.lockedByOther = null;
		return new Entry<>(lock.getLastInitUser(),lock.getLastInitTime());
	}

	private boolean tryLockWithUpdate(int disabled) {
		//0启用,1禁用,2处理中
		long count=factory.update(t)
				.set(t.disabled, Expressions.TWO)
				.set(t.lastInitUser, agentName)
				.set(t.lastInitTime, new Date())
				.set(t.lastInitResult,"Processing")
				.where(t.tableName.eq(lockName),t.disabled.eq(disabled)).execute();
		return count>0;
	}

	private boolean tryLockWithInsert() {
		try {
			long count = factory.insert(t)
					.set(t.tableName, lockName)
					.set(t.records, 0)
					.set(t.disabled, Expressions.TWO)
					.set(t.lastInitUser, agentName)
					.set(t.lastInitTime, new Date())
					.set(t.lastInitResult,"Processing")
					.execute();	
			return count > 0;
		}catch(Exception e) {
			return false;
		}
	}

	public int getLockExpireMinutes() {
		return lockExpireMinutes;
	}

	public void setLockExpireMinutes(int lockExpireMinutes) {
		this.lockExpireMinutes = lockExpireMinutes;
	}
}
