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

	@JMXText(description = "Total completed task count. 累计完成任务数.")
	long getCompletedTaskCount();

	@JMXText(description = "Total rejected task count. 累计拒绝任务数.")
	long getRejectedTaskCount();

	@JMXText(description = "Average task execution time in milliseconds. 平均任务执行耗时(毫秒).")
	double getAverageExecutionTimeMs();

	@JMXText(description = "Maximum task execution time in milliseconds. 最大任务执行耗时(毫秒).")
	long getMaxExecutionTimeMs();

	@JMXText(description = "Minimum task execution time in milliseconds. 最小任务执行耗时(毫秒).")
	long getMinExecutionTimeMs();

	@JMXText(description = "Average task wait time in milliseconds (from enqueue to start execution). 平均任务等待耗时(毫秒).")
	double getAverageWaitTimeMs();

	@JMXText(description = "Maximum task wait time in milliseconds. 最大任务等待耗时(毫秒).")
	long getMaxWaitTimeMs();

	@JMXText(description = "Total times pool entered pressure state (queue reached pressureSize, expansion triggered). 累计进入压力状态次数.")
	long getPressureCount();

	@JMXText(description = "Total times pool queue became saturated (queue full, task force-added). 累计队列饱和次数.")
	long getSaturatedCount();

	void setMaximumSize(int size);
	
	void setCoreSize(int size);

	@JMXText(description = "Reset all metrics (completed count, rejected count, execution time). 重置所有统计指标.")
	void resetMetrics();
}