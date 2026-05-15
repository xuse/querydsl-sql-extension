package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;

/**
 * <h2>English:</h2>
 * Automatically generate partitions based on the current time.
 * <p>
 * Use case: when partitioning by year/month/day, it is impossible to enumerate every partition.
 * In this case, configure an auto-generation rule.
 * <p>
 * Number of partitions = (periodsEnd - periodsBegin) + (createForMaxValue ? 2 : 1)
 *
 * <h2>Chinese:</h2>
 * 根据当前时间自动生成分区。
 * @implNote
 * 使用条件：对于基于年月日/年月/年等方式进行分区时，无法穷举到每个分区。
 * 此种情况下配置一个自动生成分区的规则即可。
 *
 * 分区数量= (periodsEnd - periodsBegin) + createForMaxValue()? 2: 1
 */
@Retention(RUNTIME)
public @interface AutoTimePartitions {

	/**
	 *  @return partition by day/week/month/year / 按日/周/月/年分区
	 */
	Period unit() default Period.MONTH;

	/**
	 * @return how many time units to look back for generating partitions (use negative for past) / 追溯多少时间单位生成分区，如果要表达之前的时间请用负数。
	 */
	int periodsBegin() default 0;

	/**
	 * @return how many time units ahead to generate partitions / 提前多少个时间单位生成分区
	 */
	int periodsEnd() default 5;

	/**
	 * @return whether to create a max-value partition / 创建最大值分区。
	 */
	boolean createForMaxValue() default false;

	/**
	 * @return the format of the database column used for partitioning / 用于分区的数据库列的格式
	 */
	ColumnFormat columnFormat() default ColumnFormat.TIMESTAMP;
}
