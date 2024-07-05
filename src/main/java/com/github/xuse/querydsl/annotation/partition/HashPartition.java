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
 * </p>
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface HashPartition {
	/**
	 * The expression. 
	 */
	String expr();

	/**
	 * Partition count.
	 * @return
	 */
	int count() default 1;
	
	/**
	 * 使用哪种hash方式，
	 * <li>hash仅可针对数字。</li>
	 * <li>linear hash是一致性哈希。
	 * <p>
	 *   PARTITION BY LINEAR HASH(YEAR(hired)) PARTITIONS 4;
	 * </p></li>
	 * <li>key可针对字符串使用
	 * <p>
	 *   PARTITION BY LINEAR KEY(user_id) PARTITIONS 4;
	 * </p></li>
	 */
	HashType type() default HashType.HASH;
	;
}
