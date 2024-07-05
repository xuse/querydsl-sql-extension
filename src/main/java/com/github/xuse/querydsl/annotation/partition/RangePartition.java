package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Example:
 * <pre>
 * PARTITION BY RANGE (store_id) (
 *   PARTITION p0 VALUES LESS THAN (6),
 *   PARTITION p1 VALUES LESS THAN (11),
 *   PARTITION p2 VALUES LESS THAN (16)
 * );
 * </pre>
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface RangePartition {
	/**
	 * 
	 * using COLUMNS keyword, Example:
	 * <pre>
	 * PARTITION BY RANGE COLUMNS(column_list) (
	 *  PARTITION partition_name VALUES LESS THAN (value_list)[,
	 *  PARTITION partition_name VALUES LESS THAN (value_list)][,
	 *  ...] 
	 *  )
	 * </pre>
	 */
	boolean columns() default true;

	/**
	 * partition by range ($expr), or a column list.
	 */
	String expr();

	/**
	 * PARTITION partition_name VALUES LESS THAN (partition_value)
	 */
	Partition[] value() default {};

	/**
	 * Auto generate according to current date time.
	 */
	AutoTimePartitions[] auto() default {};
}
