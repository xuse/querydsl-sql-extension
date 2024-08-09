package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;

/**
 * <h2>English</h2> UnsavedValue is used to define the range of invalid values
 * in business logic.
 * <p>
 * What is an invalid value? <blockquote> For example, for the `user.age` field,
 * a valid value can be considered as 0-200 years (as legally, a newborn is
 * considered 0 years old). Similarly, for the `user.name` field, a valid value can be
 * considered a non-empty string, while null and empty strings are invalid
 * values. For most fields, null is invalid, and other values can be considered
 * valid. </blockquote>
 * <p>
 * What happens after the system determines a value is invalid? <blockquote>
 * <ol>
 * <li>1. During Insert: The `populate(bean)` method will not actively insert
 * the column into the database (it will use the database's default value).</li>
 * <li>2. During Update: The `populate(bean)` method will automatically
 * determine that the field does not need to be updated.</li>
 * <li>3. When applied in Where clause: Manually injected query conditions using
 * API are not affected, but automatically calculated query conditions based on
 * `ConditionBean` or `findByExample` will ignore these values.</li>
 * </ol>
 * </blockquote>
 * <p>
 * Under general circumstances, invalid values do not participate in database
 * operations. But what if sometimes it's necessary to write them into the
 * database? 
 * <blockquote> Most APIs in QueryDSL explicitly operate
 * set/values/where, allowing you to explicitly operate the Query object to do
 * anything you want, including updating primary key fields.
 * <p>
 * If you insist on using the automatically calculated field features, you can
 * use {@link SQLUpdateClauseAlter#updateNulls(boolean)} and
 * {@link SQLInsertClauseAlter#writeNulls(boolean)} to request null values to be
 * updated into the database. In this case, only {@code null} value will be written to these columns. 
 * This is because the feature is designed to distinguish between valid and invalid 
 * business data and is not intended to satisfy other special purposes. </blockquote>
 * <p>
 * How to specify negative numbers and zero as invalid values for an int type
 * field? <blockquote> Use `@UnsavedValue(UnsavedValue.ZeroAndMinus)`, see other
 * usage in the documentation for this class.</blockquote>
 * <p>
 * How to support more complex invalid value judgment conditions? <blockquote>
 * Refer to
 * {@link com.github.xuse.querydsl.sql.column.ColumnBuilderBase#withUnsavePredicate(Predicate)}
 * <p>
 * Example:{@code
 * 	 columnBuilder.withUnsavePredicate(i-> i<0 || i>200);
 * } </blockquote>
 * <p>
 * Which values are considered invalid by default? <blockquote> All instances of
 * Object are considered invalid when they are null. When the data type is a
 * primitive type such as int, long, short, char, etc., it always has a value.
 * The system considers (int) 0, (byte) 0, (char) 0, (boolean) false as invalid
 * values. If defining a primitive type field, it is recommended to set this
 * property to avoid business logic ambiguity. </blockquote>
 * <h2>Chinese</h2>
 * 什么是无效数值？ <blockquote>
 * 比如某{@code user.age}字段，可以认为有效值是0~200岁（法律上认为新生婴儿是0岁）。对于这个字段负数就可以认为是无效值。
 * 又比如某{@code user.name}字段，可以认为有效值是有长度的字符串，null和空字符串都是无效值。
 * 对于大部分字段而言，null是无效值，其他数值都可以认为是有效值。 </blockquote>
 * <p>
 * 系统判定数值无效后会做什么？ <blockquote>
 * <ol>
 * <li>Insert时：populate(bean)方法不会主动向数据库插入该列(使用数据库的默认值)</li>
 * <li>update时：populate(bean)方法自动判定认为这个字段不需要更新。</li>
 * <li>应用于where时：使用API显式注入的查询条件不受影响，但基于ConditionBean或者findByExample等自动计算查询条件时会忽略这些值。</li>
 * </ol>
 * </blockquote>
 * <p>
 * 一般情况无效值无需参与数据库操作，但某些情况下需要将其写入数据库呢？ <blockquote>
 * QueryDSL的API大多是显式操作set/values/where，显式操作Query对象可以做任何你想做的事，包括更新主键字段。
 * <p>
 * 如果坚持要用自动计算字段的功能，可以使用
 * {@link SQLUpdateClauseAlter#updateNulls(boolean)}和{@link SQLInsertClauseAlter#writeNulls(boolean)}要求将null值更新到数据中。
 * 即便强行将无效值写入数据库，也只会写入null值。因为无效就意味着在业务上无意义，等同与null.
 * </blockquote>
 * <p>
 * 对于int类型的字段，如何指定负数和零表示无效值？ <blockquote>
 * {@code @UnsavedValue(UnsavedValue.ZeroAndMinus)}，其他用法参见本类下文。 </blockquote>
 * <p>
 * 怎么支持更复杂的无效值判断条件？ <blockquote>
 * 参见。{@link com.github.xuse.querydsl.sql.column.ColumnBuilderBase#withUnsavePredicate(Predicate)}
 * <p>
 * 示例 {@code
 * 	 columnBuilder.withUnsavePredicate(i-> i<0 || i>200);
 * } </blockquote>
 * <p>
 * 默认情况下，系统认为哪些数值是无效值？ <blockquote> 所有Object的实例，为null时即为无效值。 当数据类型为int, long,
 * short, char等基础类型时，其总是有值。系统认为 (int)0, (byte)0,(char)0,(boolean)false这些都是无效值。
 * 如果定义Primitive类型的子类，建议设置此属性以免业务理解发生歧义。 </blockquote>
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface UnsavedValue {
	String value() default Null;

	/**
	 * Only null values are considered invalid. `value == null`. For data of
	 * primitive types, boxing will inevitably result in non-null values. This
	 * configuration means that primitive fields will never be treated as null
	 * values.
	 * <p>
	 * 仅有null值被视作无效值。 value==null。对于基元类型(Primitive)的数据，装箱以后必然不为null。
	 * 此配置意味着primitive字段永远不会被当作null值处理。
	 */
	String Null = "#null";

	/**
	 * Empty strings are treated as null, applicable to strings.
	 * <p>
	 * 空字符串当作null，适用于字符串。
	 * @implNote If this configuration is applied to primitive types, primitive
	 *           types will always be considered valid values. /
	 *           如果将这个配置应用于基元类型(primitive)，则基元类型总被视为有效值。
	 */
	String NullOrEmpty = "#null+";

	/**
	 * Negative numbers are considered invalid values, while zero and positive numbers are considered valid values.
	 * <p>
	 * 负数视作无效值，零和正数视为有效值。
	 */
	String MinusNumber = "#<0";

	/**
	 * Zero and negative numbers are considered invalid values, positive numbers are considered valid values.
	 * <p>
	 * 零和负数视作无效值，仅有正数视为有效值。
	 * 
	 */
	String ZeroAndMinus = "#<=0";

	/**
	 * Zero is considered an invalid value, whereas both positive and negative numbers are considered valid values.
	 * <p>
	 * 零视作无效值，正数和负数则都视为有效值。
	 */
	String Zero = "#0";

	/**
	 * There are no invalid values; all values, including null, need to be written into the database.
	 * <p>
	 * 不存在无效值，所有数值都需要写入数据库，包括null。
	 */
	String Never = "#never";

	/**
	 * This is the default behavior when the @UnsavedValue annotation is not added.
	 * The default strategy is:
	 * <ul>
	 * <li>For Object types, {@code value==null} is considered an invalid
	 * value.</li>
	 * <li>For primitive types, such as {@code (int)value == 0} or
	 * {@code (boolean)value == false}, it is considered an invalid value.</li>
	 * </ul>
	 * <p>
	 * 这是不添加@UnsavedValue注解时的默认行为 默认策略: 
	 * <ul>
	 * <li>对于Object类型，{@code value==null}为无效值。</li>
	 * <li>对于基元类型(primitive)，例如
	 * {@code  (int)value == 0}或{@code (boolean)value == false)}为无效值。</li>
	 * </ul>
	 */
	String Default = "#default";
}
