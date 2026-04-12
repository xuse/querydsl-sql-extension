package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Example:
 *
 * <pre>
 * PARTITION BY LIST(store_id) (
 *   PARTITION pNorth VALUES IN (3,5,6,9,17),
 *   PARTITION pEast VALUES IN (1,2,10,11,19,20),
 *   PARTITION pWest VALUES IN (4,12,13,14,18)
 * );
 * </pre>
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface ListPartition {

	/**
	 * @return partition by range (column1[, column2]...),
	 * the path is the name of java field which mapping to a database column.
	 * Note: if this attribute was set, using COLUMNS keyword, Example:
	 * <pre>
	 * PARTITION BY LIST COLUMNS(city) (
	 *   PARTITION pRegion_1 VALUES IN('Oskarshamn', 'Högsby', 'Mönsterås'),
	 *   PARTITION pRegion_2 VALUES IN('Vimmerby', 'Hultsfred', 'Västervik'),
	 *   PARTITION pRegion_3 VALUES IN('Nässjö', 'Eksjö', 'Vetlanda')
	 * );
	 * </pre>
	 */
	String[] columns() default {};

	/**
	 * @return the expression.
	 * Note: the expression will be set into database directly.
	 * the name here must be a column name, not the java field name.
	 */
	String expr() default "";

	/**
	 * @return Partitions.
	 * <h2>中文</h2>
	 * 分区配置
	 */
	Partition[] value() default {};
}
