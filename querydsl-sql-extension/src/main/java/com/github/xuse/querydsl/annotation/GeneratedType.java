package com.github.xuse.querydsl.annotation;

import com.github.xuse.querydsl.config.ConfigurationEx;

/**
 * <h3>Chinese</h3>
 * 写入数据库时给一些字段自动赋值。
 * <h3>English</h3>
 * Auto generate value when insert or update.
 * 
 * @author Joey
 *
 */
public enum GeneratedType {
	/**
	 * 数据插入时，生成数据库时间戳 / The timestamp when insert
	 */
	CREATED_TIMESTAMP,
	/**
	 * 数据插入或更新时，生成数据库时间戳 / The timestamp when insert or update
	 */
	UPDATED_TIMESTAMP,
	/**
	 * 数据插入时，生成36位GUID / The GUID when insert
	 */
	GUID36,
	/**
	 * 数据插入时，生成32位GUID /The GUID when insert
	 */
	GUID32,
	/**
	 * <h3>Chinese</h3>
	 * 基于SnowFlake算法生成唯一标识。
	 * 该算法需要为系统分配workerID，否则无法正常使用。
	 * 参见 {@link ConfigurationEx#initSnowflake(int, int)}
	 * 注意workerId必须确保分布式环境中的每台主机都不同。
	 * 
	 * <h3>English</h3>
	 * Generate unique id based on SnowFlake algorithm.
	 * This algorithm needs to be assigned a workerID for the system, otherwise it
	 * cannot be used.
	 * See {@link ConfigurationEx#initSnowflake(int, int)}
	 * Note that the workerId must ensure that each host in the distributed
	 * environment is different.
	 */
	SNOWFLAKE
}
