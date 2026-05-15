package com.github.xuse.querydsl.annotation.dbdef;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>English:</h2> Annotation for table definition.
 * <h2>Chinese:</h2> 用于表的注解
 *
 * @author Joey
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface TableSpec {

	/**
	 * @return namespace
	 */
	String schema() default "";

	/**
	 * @return table name / 表名
	 */
	String name() default "";

	/**
	 * @return primary key fields, fill in Java field names (Path) / 主键字段，此处填写Java字段名（Path）
	 */
	String[] primaryKeys() default {};

	/**
	 * @return indexes and UNIQUE constraints / 索引和UNIQUE
	 */
	Key[] keys() default {};

	/**
	 * @return check constraints / 检查约束
	 */
	Check[] checks() default {};

	/**
	 *  @return character set and collation / 字符集
	 */
	String collate() default "";
	
	/**
	 * @return short alias / 短别名
	 */
	String alias() default "";
	
	/**
	 * @return auto-increment start value, 0 means unspecified (use default) / 自增值开始于，0表示不指定，采用默认。
	 */
	int autoIncrementStartAt() default 0;
}
