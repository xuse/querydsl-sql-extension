package com.github.xuse.querydsl.annotation;

import com.github.xuse.querydsl.config.ConfigurationEx;

/**
 * 写入数据库时给一些字段自动赋值。
 * @author jiyi
 *
 */
public enum GeneratedType {
	/**
	 * 数据插入时，生成数据库时间戳
	 */
	CREATED_TIMESTAMP,
	/**
	 * 数据插入或更新时，生成数据库时间戳
	 */
	UPDATED_TIMESTAMP,
	/**
	 * 数据插入时，生成36位GUID
	 */
	GUID36,
	/**
	 * 数据插入时，生成32位GUID
	 */
	GUID32,
	/**
	 * 基于SnowFlake算法生成唯一标识。
	 * 该算法需要为系统分配workerID，否则无法正常使用。<p/>
	 * 参见  {@link ConfigurationEx#initSnowflake(int, int)}
	 * 注意workerId必须确保分布式环境中的每台主机都不同。
	 */
	SNOWFLAKE
}
