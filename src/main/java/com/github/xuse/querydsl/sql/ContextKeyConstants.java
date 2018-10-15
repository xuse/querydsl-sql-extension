package com.github.xuse.querydsl.sql;

public interface ContextKeyConstants {
	
	int MAX_BATCH_LOG=5;
	
	/**
	 * 执行花费的纳秒
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
}
