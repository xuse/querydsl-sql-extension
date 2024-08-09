package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于定制数据初始化的行为。这个注解是加在模型类上的（带Q的Class）
 *
 * @author Joey
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface InitializeData {

	/**
	 * @return
	 * 配置一个资源文件名，记录了所有的初始化记录值。
	 *  如果不指定，默认使用 classpath:/{classname}.csv
	 */
	String value() default "";

	/**
	 * @return
	 * 启用该表的自动初始化特性，可配置成false禁用
	 */
	boolean enable() default true;
	
	/**
	 * 
	 * @return 仅当为空数据表时才进行初始化
	 */
	boolean forEmptyTableOnly() default false;

	/**
	 * @return
	 * 如果数据不按主键合并，配置表的业务键字段名(请使用java字段名，而不是数据库列名)
	 * If data is not merged by the primary key, configure the business keys.（name of field, not the name of column in database）
	 * @implSpec
	 * <ul>
	 * <li>如果不配置，默认采用数据表主键进行合并</li>
	 * <li>如果不配置，数据表也没有主键，那么数据变为仅插入</li>
	 * </ul>
	 */
	String[] mergeKeys() default {};

	/**
	 * @return
	 * 自增键使用记录中的值，不使用数据库的自增编号。
	 * 注意，某些数据库可能不支持此特性。
	 * 此配置当前无效果。
	 */
	boolean manualSequence() default false;

	/**
	 * @return
	 * 数据文件的字符集，默认使用UTF-8
	 */
	String charset() default "UTF-8";

	/**
	 * @return
	 * 确认资源文件必需存在，如果资源不存在将抛出异常
	 */
	boolean ensureFileExists() default true;

	/**
	 * 在数据库中列有值的情况下，数据文件中的null值是否要更新到数据库中，
	 * @return if true, null values will be written to db.
	 */
	boolean updateNulls() default true;

	/**
	 * @return
	 * 支持指定一个SQL脚本文件，执行该SQL来更新数据库。 一旦使用该功能，value()的CSV文件功能将失效。
	 * 实验性功能。
	 */
	String sqlFile() default "";
}
