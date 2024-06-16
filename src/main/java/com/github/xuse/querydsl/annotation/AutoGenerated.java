package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 写入数据库前给一些字段自动赋值。当使用inseret或update的populate方法时会自动生效。
 * <p>
 * 这个规则是定义queryDSL框架在java处理逻辑中的数值生成，不代表基于数据库的AutoIncreament，Sequence，Random等生成逻辑。
 * </p>
 * @author jiyi
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
