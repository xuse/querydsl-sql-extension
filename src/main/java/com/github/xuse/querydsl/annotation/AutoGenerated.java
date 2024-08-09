package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>Chinese:</h2>
 * 写入数据库前给一些字段自动赋值。当使用insert或update的populate方法时会自动生效。
 * 这个生成规则在java侧计算处理，不依赖数据库的AutoIncrement，Sequence，Random等特性。
 * <h2>English:</h2>
 * Automatically assign values to some fields before writing to the database.
 * This will take effect automatically when using the populate method of insert
 * or update. This generation rule is calculated and processed on the Java side
 * and does not rely on the database features like AutoIncrement, Sequence, or
 * Random.
 * 
 * @author Joey
 *
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface AutoGenerated {
	/**
	 * @return 自动生成类型
	 */
	GeneratedType value();
	
	/**
	 * @return 如果传入的数据不为空，是否用生成的值覆盖 
	 */
	boolean overwrite() default false;
	
	/**
	 * @return 生成值是否回写
	 */
	boolean writeback() default false;
	
	/**
	 * @return 参数
	 */
	String[] params() default {};
}
