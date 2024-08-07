package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 扩展数据类型的注解，可用于支持新的数据映射方式
 *
 * @author Joey
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface CustomType {

	/**
	 * @return 自定义的java和数据库类型映射实现
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends com.querydsl.sql.types.Type> value();

	/**
	 * @return Any configuration parameters for the named type.
	 */
	String[] parameters() default {};
}
