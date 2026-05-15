package com.github.xuse.querydsl.annotation.query;

/**
 * <h2>English:</h2>
 * Defines a mapping rule from a boolean field value to a database query condition.
 * Used inside {@link When#forBool()} on {@link ConditionBean} fields to specify how
 * a UI boolean value (true/false) should be translated into a SQL predicate.
 *
 * <p>When the boolean field matches the value specified by {@link #is()}, the corresponding
 * operator ({@link #ops()}) and operand ({@link #value()}) are applied to the target column,
 * or a raw SQL expression ({@link #expression()}) is used directly.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * @When(path = "status", forBool = {
 *     @BoolCase(is = true, ops = Ops.EQ, value = "1"),
 *     @BoolCase(is = false, ops = Ops.EQ, value = "0")
 * })
 * private Boolean active;
 * }</pre>
 * In this example, when {@code active} is {@code true}, the generated condition is
 * {@code status = 1}; when {@code false}, it becomes {@code status = 0}.
 *
 * <h2>Chinese:</h2>
 * 定义布尔字段值到数据库查询条件的映射规则。
 * 用于 {@link ConditionBean} 字段上的 {@link When#forBool()} 中，指定界面上的布尔值（true/false）
 * 如何转换为 SQL 查询条件。
 *
 * <p>当布尔字段的值与 {@link #is()} 指定的值匹配时，将使用对应的运算符（{@link #ops()}）
 * 和操作数（{@link #value()}）作用于目标列，或直接使用原始 SQL 表达式（{@link #expression()}）。
 *
 * @see When#forBool()
 * @see StringCase
 * @see IntCase
 * @see Ops
 */
public @interface BoolCase {

	/**
	 * The boolean value to match.
	 * <p>当条件 Bean 字段的布尔值等于此值时，本条规则生效。
	 *
	 * @return the boolean value that activates this case
	 */
	boolean is() default false;

	/**
	 * The operator to apply on the target column.
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
