package com.github.xuse.querydsl.annotation.query;

/**
 * <h2>English:</h2>
 * Defines a mapping rule from an {@code Integer} field value to a database query condition.
 * Used inside {@link When#forInt()} on {@link ConditionBean} fields to specify how
 * a particular integer value should be translated into a SQL predicate.
 *
 * <p>When the integer field equals the value specified by {@link #is()}, the corresponding
 * operator ({@link #ops()}) and operand ({@link #value()}) are applied to the target column,
 * or a raw SQL expression ({@link #expression()}) is used directly.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * @When(path = "priority", forInt = {
 *     @IntCase(is = 1, ops = Ops.GOE, value = "10"),
 *     @IntCase(is = 2, ops = Ops.BETWEEN, value = "5,9"),
 *     @IntCase(is = 3, ops = Ops.LT, value = "5")
 * })
 * private Integer urgency;
 * }</pre>
 * In this example:
 * <ul>
 *   <li>When {@code urgency = 1} → {@code WHERE priority >= 10}</li>
 *   <li>When {@code urgency = 2} → {@code WHERE priority BETWEEN 5 AND 9}</li>
 *   <li>When {@code urgency = 3} → {@code WHERE priority < 5}</li>
 * </ul>
 *
 * <h2>Chinese:</h2>
 * 定义整数字段值到数据库查询条件的映射规则。
 * 用于 {@link ConditionBean} 字段上的 {@link When#forInt()} 中，指定特定整数值
 * 如何转换为 SQL 查询条件。
 *
 * <p>当整数字段的值等于 {@link #is()} 指定的值时，将使用对应的运算符（{@link #ops()}）
 * 和操作数（{@link #value()}）作用于目标列，或直接使用原始 SQL 表达式（{@link #expression()}）。
 *
 * @see When#forInt()
 * @see BoolCase
 * @see StringCase
 * @see Ops
 */
public @interface IntCase {

	/**
	 * The integer value to match against the field value.
	 * <p>要匹配的整数值。当条件 Bean 字段的值等于此值时，本条规则生效。
	 *
	 * @return the integer value that activates this case
	 */
	int is() default 0;

	/**
	 * The operator to apply on the target column.
	 * Ignored when {@link #expression()} is specified.
	 * <p>作用于目标列的运算符。当指定了 {@link #expression()} 时，此属性被忽略。
	 *
	 * @return the comparison operator
	 */
	Ops ops() default Ops.EQ;

	/**
	 * A value convertible to the target field type. Use comma to separate multiple values
	 * (e.g. for {@link Ops#IN} or {@link Ops#BETWEEN}).
	 * <p>可以转换为指定字段类型的数值。如为多值用逗号分隔。
	 *
	 * @return the operand value as a string
	 */
	String value() default "";

	/**
	 * For complex conditions, write the SQL expression directly. When using an expression,
	 * the {@link #ops()} is ignored — the full SQL predicate must be included in the expression.
	 * <p>复杂条件直接写SQL表达式，使用表达式时 {@link #ops()} 不生效，SQL需要全部写在表达式中。
	 *
	 * @return a raw SQL expression, or empty string if not used
	 */
	String expression() default "";
}
