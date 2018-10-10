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
	/**
	 * SQL参数名
	 */
	String PARAMS_PATH = "PARAMS_PATH";
	/**
	 * SQL参数
	 */
	String SIGLE_PARAMS = "SIGLE_PARAMS";
	/**
	 * 批量参数
	 */
	String BATCH_PARAMS = "BATCH_PARAMS";
	/**
	 * 批量总数
	 */
	String BATCH_SIZE = "BATCH_SIZE";
}
