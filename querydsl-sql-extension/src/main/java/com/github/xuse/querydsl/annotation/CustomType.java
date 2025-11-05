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
	 * @implNote 自定义的Type类型可以继承 {@link com.querydsl.sql.types.AbstractType}。
	 * 可以通过构造器接收参数，构造器接收参数方法:
	 * <ol>
	 * <li>1. 空构造</li>
	 * <li>2. 传入字段类型(java.lang.Class)</li>
	 * <li>3. 传入字段泛型类型(java.lang.reflect.Type)</li>
	 * <li>3. 如果配置了{@linkplain #parameters()}项，则上述构造器需要增加接收String参数，String参数数量与配置保持一致</li>
	 * </ol>
	 * @return 自定义的java和数据库类型映射实现 / The mapping type of java and database
	 * @see com.querydsl.sql.types.Type
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends com.querydsl.sql.types.Type> value();

	/**
	 * @return 参数 / Any configuration parameters for the named type.
	 */
	String[] parameters() default {};
}
