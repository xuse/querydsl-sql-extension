package com.github.xuse.querydsl.config;

import com.github.xuse.querydsl.util.SnowflakeIdWorker;

public class SnowflakeIdManager {
	private volatile static SnowflakeIdWorker worker;
	
	public static SnowflakeIdWorker getInstance() {
		return worker;
	}

	public static void init(int workerId, int datacenterId) {
		if(worker!=null) {
			throw new IllegalStateException();
		}
		worker = new SnowflakeIdWorker(workerId, datacenterId);
	}
}
