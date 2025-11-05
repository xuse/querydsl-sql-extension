package com.github.xuse.querydsl.config;

import com.github.xuse.querydsl.util.SnowflakeIdWorker;

public class SnowflakeIdManager {
	private volatile static SnowflakeIdWorker worker;

	public static SnowflakeIdWorker getInstance() {
		return worker;
	}

	/**
	 * Initialize a Snowflake ID Generator.
	 * @param workerId     worker ID (0~255)
	 * @param datacenterId data center ID (0~3)
	 */
	public static void init(int workerId, int datacenterId) {
		if (worker != null) {
			throw new IllegalStateException();
		}
		worker = new SnowflakeIdWorker(workerId, datacenterId);
	}
}
