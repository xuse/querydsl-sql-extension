package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * *
 * <h3>Chinese</h3>
 * 用于定制数据初始化的行为。这个注解是加在模型类上的（带Q的Class）
 * <h3>English</h3>
 * This annotation is used to customize the initialization behavior. It is
 * applied to the model class (with Q class).
 *
 * @author Joey
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface InitializeData {

	/**
	 * 
	 * @return
	 *         <h3>Chinese</h3>
	 *         配置一个资源文件名，记录了所有的初始化记录值。 如果不指定，默认使用 classpath:/{classname}.csv
	 *         <h3>English</h3>
	 *         Configure a resource file name that records all initialization
	 *         records. If not specified, the default is
	 *         classpath:/{classname}.csv
	 * 
	 */
	String value() default "";

	/**
	 * @return 启用该表的自动初始化特性，可配置成false禁用 / Enable the
	 *         initialization feature of the table. It can be configured to
	 *         false to disable.
	 */
	boolean enable() default true;

	/**
	 * 
	 * @return 仅当为空数据表时才进行初始化 / Only initialize the table if it is
	 *         empty
	 */
	boolean forEmptyTableOnly() default false;

	/**
	 * @return 如果数据不按主键合并，配置表的业务键字段名(请使用java字段名，而不是数据库列名)/
	 *         If data is not merged by
	 *         the primary key, configure the business keys.（name of field, not the
	 *         name of column in database）
	 * @implSpec
	 *           Chinese
	 *           <ul>
	 *           <li>如果不配置，默认采用数据表主键进行合并</li>
	 *           <li>如果不配置，数据表也没有主键，那么数据变为仅插入</li>
	 *           </ul>
	 *           English
	 *           *
	 *           <ul>
	 *           <li>If not configured, the primary key is used for merging</li>
	 *           <li>If not configured, the data table has no primary key, then
	 *           data becomes only insert</li>
	 *           </ul>
	 */
	String[] mergeKeys() default {};

	/**
	 * <h2>English</h2>
	 * Whether the primary key field should be written to the database.
	 * <ul>
	 * <li>-1:Automatic</li>
	 * <li>0: Do not write</li>
	 * <li>1: Write</li>
	 * </ul>
	 * "Automatic" means that if the primary key column is singular and marked as an
	 * auto-increment column, it will not be written; or vice versa.
	 * <h2>Chinese</h2>
	 * 主键字段是否要写入数据库。
	 * <ul>
	 * <li>-1 自动</li>
	 * <li>0 不写入</li>
	 * <li>1 要写入</li>
	 * </ul>
	 * “自动”，当主键列仅有一个，且标记为自增列时不写入，其他情况会尝试写入。
	 * 
	 * @return 主键字段是否要写入数据库 / Whether the primary key field should be
	 *         written to the database
	 */
	int setPrimaryKeys() default -1;

	/**
	 * @return 数据文件的字符集，默认使用UTF-8 / The character set of the data file. Default
	 *         is UTF-8
	 */
	String charset() default "UTF-8";

	/**
	 * @return 确认资源文件必需存在，如果资源不存在将抛出异常 / Whether the resource file
	 *         must exist. If the resource does not exist, an exception will be
	 *         thrown.
	 */
	boolean ensureFileExists() default true;

	/**
	 * 在数据库中列有值的情况下，数据文件中的null值是否要更新到数据库中，
	 * 
	 * @return if true, null values will be written to db.
	 */
	boolean updateNulls() default true;

	/**
	 * @return 支持指定一个SQL脚本文件，执行该SQL来更新数据库。 一旦使用该功能，value()的CSV文件功能将失效。 实验性功能。/
	 *         Support
	 *         specifying a SQL script file to update the database. Once this
	 *         function is used, the CSV file function of value() will be
	 *         invalid. Experimental feature.
	 */
	String sqlFile() default "";
}
