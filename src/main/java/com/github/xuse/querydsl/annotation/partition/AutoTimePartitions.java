package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * 根据当前时间自动生成分区。
 * @implNote
 * 使用条件：对于基于年月日/年月/年等方式进行分区时，无法穷举到每个分区。
 * 此种情况下配置一个自动生成分区的规则即可。
 * 
 * 分区数量= (periodsEnd - periodsBegin) + createForMaxValue()? 2: 1
 *
 */
@Retention(RUNTIME)
public @interface AutoTimePartitions {
	
	/**
	 * 按日/周/月/年分区
	 * @return
	 */
	Period unit() default Period.MONTH;

	/**
	 * 追溯多少时间单位生成分区，如果要表达之前的时间请用负数。
	 */
	int periodsBegin() default 0;

	/**
	 * 提前多少个时间单位生成分区
	 */
	int periodsEnd() default 5;
	
	/**
	 * 创建最大值分区。
	 */
	boolean createForMaxValue() default false;
}
