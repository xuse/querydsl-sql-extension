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
	
	@JMXText(description = "Active size of pool. 线程池当前活动线程数.")
	int getActiveCount();
	
	@JMXText(description = "Queue size of pool. 线程池任务当前排队长度")
	int getQueueLength();
	
	@JMXText(description = "Queue size of pool. 线程池任务最大长度")
	int getQueueMaximumLength();
	
	@JMXText(description = "Queue size of pool. 线程池队列压力位")
	int getQueuePressureLength();
	
	void setMaximumSize(int size);
	
	void setCoreSize(int size);
}