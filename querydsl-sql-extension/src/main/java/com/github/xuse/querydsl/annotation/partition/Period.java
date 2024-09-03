package com.github.xuse.querydsl.annotation.partition;

/**
 * 当使用一个时间日期字段分区，描述每个分区包含的时间范围
 * <p>
 * When using a datetime field for partitioning, describe the time range contained in each partition.
 * @author Joey
 */
public enum Period {
	/**
	 * 按天分区<p>
	 * Partition by day. 
	 */
	DAY,
	/**
	 * 按周分区，周日是每周的第一天，即每个分区包含从周日到周六的数据。
	 * <p>
	 * Partition by week, with Sunday as the first day of each week, meaning each
	 * partition contains data from Sunday to Saturday.
	 */
	WEEK,
	/**
	 * 按月（自然月）分区
	 * <p> 
	 * Partition by month.
	 */
	MONTH,
	/**
	 * 按年分区
	 * <p> 
	 * Partition by year.
	 */
	YEAR
}
