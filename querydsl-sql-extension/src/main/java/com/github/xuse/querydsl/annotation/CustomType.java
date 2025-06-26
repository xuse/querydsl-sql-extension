package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * *
 * <h3>Chinese</h3>
 * 扩展数据类型的注解，可用于支持新的数据映射方式
 * <h3>English</h3>
 * Extend data type annotation, can be used to support new data mapping
 *
 * @author Joey
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface CustomType {

	/**
	 * @return 自定义的java和数据库类型映射实现 / The mapping type of java and database
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends com.querydsl.sql.types.Type> value();

	/**
	 * @return 参数 / Any configuration parameters for the named type.
	 */
	String[] parameters() default {};
}
