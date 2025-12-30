package com.github.xuse.querydsl.util;

import javax.management.MXBean;

import com.github.xuse.querydsl.jmx.JMXText;

@MXBean
public interface FrontPressurePoolMXBean {
	@JMXText(description = "core size of pool now. 线程池初始值/最小值.")
	int getCoreSize();
	
	@JMXText(description = "size of pool now. 线程池当前值.")
	int getCurrentSize();
	
	@JMXText(description = "Maximum size of pool have reached in history. 线程池到达过的最大值.")
	int getLargestSize();
	
	@JMXText(description = "Maximum size of pool initialized. 线程池初始时指定的最大值.")
	int getMaximumSize();
	
	int getActiveCount();
	
	int getQueueLength();
	
	int getQueueMaximumLength();
	
	int getQueuePressureLength();
	
	void setMaximumSize(int size);
	
	void setCoreSize(int size);
}