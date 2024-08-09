package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>Chinese:</h2>
 * 在类上进行注解，用于描述数据库分区策略(PARTITION BY RANGE)。
 * 示例见下
 * <h2>English:</h2>
 * Annotate the class to describe the database partitioning strategy (PARTITION
 * BY RANGE). Example:
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
	 * @return
	 *         <h2>English:</h2>partition by range ($expression), Note: the
	 *         expression will be set into database directly. the name here must be
	 *         a column name, not the java field name.
	 *         <h2>Chinese:</h2> 按范围分区 ($expression)， 注意：该表达式将直接设置到数据库中。
	 *         此处的名称必须是列名称，而不是 Java 字段名称。
	 */
	String expr() default "";

	/**
	 * @return
	 *         <h2>Chinese:</h2> 按范围分区 (column1[, column2]...)， 路径是映射到数据库列的 Java
	 *         字段名称。 注意：如果设置了此属性，请使用 COLUMNS 关键字，例如：
	 *         <h2>English:</h2>partition by range (column1[, column2]...), the path
	 *         is the name of java field which mapping to a database column. Note:
	 *         if this attribute was set, the SQL will use COLUMNS keyword, Example:
	 *         <pre>
	 * PARTITION BY RANGE COLUMNS(column_list) (
	 *   PARTITION partition_name VALUES LESS THAN (value_list)[,
	 *   PARTITION partition_name VALUES LESS THAN (value_list)][,
	 *   ...]
	 *  )</pre>
	 */
	String[] columns() default {};

	/**
	 * <h2>Chinese:</h2>
	 * 具体包含的分区，每个分区用{@code @Partition}表示，包含partition_name和(partition_value属性。
	 * 后者是分区范围的表达式。实际效果见示例。
	 * <h2>English:</h2> The specific partitions included are represented by
	 * {@code @Partition}, each containing partition_name and partition_value
	 * attributes. The latter is an expression of the partition range. See the
	 * example for the actual effect.
	 * 
	 * @return PARTITION partition_name VALUES LESS THAN (partition_value)
	 */
	Partition[] value() default {};

	/**
	 * <h2>Chinese:</h2> 按时间组织的分区无法事先穷举，因此可以设置一个以当前时间为基准的配置自动生成策略。 计算出当前时间下，需要配置哪些分区。
	 * <h2>English:</h2> Time-organized partitions cannot be exhaustively listed in
	 * advance. Therefore, an automatic generation strategy based on the current
	 * time can be set. This strategy calculates which partitions need to be
	 * configured based on the current time.
	 * 
	 * @return An auto generate strategy according to current date time.
	 */
	AutoTimePartitions[] auto() default {};
}
