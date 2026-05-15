package com.github.xuse.querydsl.annotation.partition;

/**
 * <h2>English:</h2>
 * When using a datetime field for partitioning, describe the time range contained in each partition.
 * <h2>Chinese:</h2>
 * 当使用一个时间日期字段分区，描述每个分区包含的时间范围
 *
 * @author Joey
 */
public enum Period {
	/**
	 * Partition by day.
	 * <p>按天分区
	 */
	DAY,
	/**
	 * Partition by week, with Sunday as the first day of each week, meaning each
	 * partition contains data from Sunday to Saturday.
	 * <p>按周分区，周日是每周的第一天，即每个分区包含从周日到周六的数据。
	 */
	WEEK,
	/**
	 * Partition by month.
	 * <p>按月（自然月）分区
	 * <p> 
	 * Partition by month.
	 */
	MONTH,
	/**
	 * Partition by year.
	 * <p>按年分区
	 * <p> 
	 * Partition by year.
	 */
	YEAR
}
