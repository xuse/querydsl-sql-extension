package com.github.xuse.querydsl.annotation.partition;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Example:
 * <p>
 * PARTITION BY HASH(YEAR(hired)) PARTITIONS 4;
 * PARTITION BY KEY(user_id) PARTITIONS 4;
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface HashPartition {

	/**
	 * @return <h2>English</h2>
	 * The expression.
	 * Note: the expression will be set into database directly.
	 * Note: The database expression configured here should use database column names, not Java field names. It is mutually exclusive with columns() and cannot be configured simultaneously.
	 * <h2>中文</h2>
	 * Note: 此处配置的数据库表达式，字段名要使用数据库列名，不是Java字段名。与columns() 二选一，不可同时配置。
	 */
	String expr() default "";

	/**
	 * <h2>English</h2>
	 * The expression.  (using columns)
	 * {@code partition by hash (column1[, column2]...),}
	 * Note:  The field names here are Java field names, not database column names. It is mutually exclusive with expr() and cannot be configured simultaneously.
	 * <h2>中文</h2>
	 * Note: 此处的字段名是Java字段名，不是数据库列名。与expr() 二选一，不可同时配置。
	 * @return String[]
	 */
	String[] columns() default {};

	/**
	 * @return Partition count.
	 */
	int count() default 1;

	/**
	 * @return <h2>English</h2>
	 * Which hash method to use.
	 * <h2>中文</h2>
	 * 使用哪种hash方式，
	 * <ul><li>hash仅可针对数字。</li>
	 * <li>linear hash是一致性哈希。
	 * Eg.
	 * <p>PARTITION BY LINEAR HASH(YEAR(hired)) PARTITIONS 4;
	 * </li>
	 * <li>key可针对字符串使用。
	 * Eg.
	 * <p>
	 * PARTITION BY LINEAR KEY(user_id) PARTITIONS 4;</li>
	 * </ul>
	 */
	HashType type() default HashType.HASH;
}
