package com.github.xuse.querydsl.sql.log;

public interface ContextKeyConstants {
	
	int MAX_BATCH_LOG = 5;
	
	/**
	 * 执行花费的毫秒
	 */
	String ELAPSED_TIME = "ELAPSED_NANO";
	/**
	 * 执行的结果计数
	 */
	String COUNT = "COUNT";
	/**
	 * 执行动作
	 */
	String ACTION = "ACTION";
	
	/**
	 * 是慢SQL
	 */
	String IMPORTANT = "SLOW";
	
	/**
	 * 达到请求的maxRows上限
	 */
	String EXCEED="EXCEED";
	
	
	String[] IMPORTANT_HINT = { "", "SlowSQL:", "DDL:", "SlowDDL:" };
	
	int SLOW = 1;
	
	int DDL = 2;
	
}
