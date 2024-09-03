package com.github.xuse.querydsl.annotation.dbdef;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Types;

/**
 * 位于字段上的注解，描述该字段对应的数据库列的特性。
 * <p>
 * An annotation on the field that describes the characteristics of the corresponding database column.
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface ColumnSpec {

	/**
	 *  @return 数据库列名称/ the name of column.
	 */
	String name() default "";

	/**
	 *  @return 数据库字段类型 / the jdbc type of column.
	 *  @see java.sql.Types
	 */
	int type() default Types.NULL;

	/**
	 *  @return 字段长度。当用在数值类型时，表示数字长度，当用在time和timestamp类型时，表示秒以下小数位数精度。<p>
	 *  Length of column. When used with numeric types, it indicates the length of the number; when used with `time` and `timestamp` types, it indicates the precision of fractional seconds.
	 */
	int size() default -1;

	/**
	 *  @return 小数位数 / Number of decimal places
	 */
	int digits() default -1;

	/**
	 *  @return 是否允许为null/ whether allowed to be null
	 */
	boolean nullable() default true;

	/**
	 * @return 是否为无符号数，如果列不是数值类型则配置无效。<p>Is it an unsigned number? If the column is not of a numeric type, this configuration is invalid.
	 */
	boolean unsigned() default false;

	/**
	 *  @return 缺省值表达式 <p> the default value of column.
	 */
	String defaultValue() default "";

	/**
	 * @return 是否自增，不同数据库对自增实现规格不同，一般只有1个列可以自增。<p> is the column auto increment.
	 */
	boolean autoIncrement() default false;

	/**
	 * <h2>Chinese:</h2>
	 * 如果设置为false，当使用{@code update(table).populate(bean)}自动从bean中提取字段进行在更新时，会忽略更新这个字段。
	 * 如果使用{@code update(table).set(path, value)}的方式显式指定，则不受影响。
	 * <h2>English:</h2> If set to false, when using
	 * {@code update(table).populate(bean)} to automatically extract fields from the
	 * bean for update, this field will be ignored. However, if you explicitly
	 * specify it using {@code update(table).set(path, value)}, it will not be
	 * affected.
	 * 
	 * @return 在update时是否参与自动字段提取。
	 *         <p>
	 *         Whether participate in automatic field extraction during update.
	 */
	boolean updatable() default true;

	/**
	 * <h2>Chinese:</h2>
	 * 如果设置为false，当使用{@code insert(table).populate(bean)}自动从bean中提取字段进行在更新时，会忽略更新这个字段。
	 * 如果使用{@code insert(table).set(path, value)}的方式显式指定，则不受影响。
	 * <h2>English:</h2> If set to false, when using
	 * {@code insert(table).populate(bean)} to automatically extract fields from the
	 * bean for insert, this field will be ignored. However, if you explicitly
	 * specify it using {@code insert(table).set(path, value)}, it will not be
	 * affected.
	 * 
	 * @return 在插入数据时是否参与自动字段提取。
	 *         <p>
	 *         Whether participate in automatic field extraction during insert.
	 */
	boolean insertable() default true;
}
