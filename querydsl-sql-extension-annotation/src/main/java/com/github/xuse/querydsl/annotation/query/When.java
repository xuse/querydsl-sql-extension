package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>English:</h2>
 * Experimental feature. Placed on fields of a {@link ConditionBean} class to define value-based
 * condition mapping — translating a UI field value into a database query predicate.
 *
 * <p>Unlike {@link Condition} which directly maps a field to a column with a fixed operator,
 * {@code @When} supports <b>case-based dispatching</b>: different field values produce different
 * SQL conditions (potentially with different operators, operands, or even raw SQL expressions).
 *
 * <h3>Supported field types:</h3>
 * <ul>
 *   <li>{@code String} — use {@link #value()} ({@link StringCase})</li>
 *   <li>{@code Boolean} — use {@link #forBool()} ({@link BoolCase})</li>
 *   <li>{@code Integer} — use {@link #forInt()} ({@link IntCase})</li>
 * </ul>
 * <p><b>Note:</b> Primitive types ({@code boolean}, {@code int}) are NOT supported.
 * The field must be a wrapper type so that {@code null} can represent "no condition".
 *
 * <h3>Processing logic:</h3>
 * <ol>
 *   <li>If the field value is {@code null}, no condition is generated (the field is skipped).</li>
 *   <li>The field value is matched against the defined cases (e.g. {@code BoolCase.is()}).</li>
 *   <li>If a match is found, the corresponding operator and operand (or expression) are applied
 *       to the column specified by {@link #path()}.</li>
 *   <li>If no match is found, behavior depends on {@link #ignoreIfNoMatchCase()}.</li>
 * </ol>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * @ConditionBean
 * public class UserQuery {
 *
 *     // When active=true → WHERE status = 'ACTIVE'
 *     // When active=false → WHERE status = 'DISABLED'
 *     @When(path = "status", forBool = {
 *         @BoolCase(is = true, ops = Ops.EQ, value = "ACTIVE"),
 *         @BoolCase(is = false, ops = Ops.EQ, value = "DISABLED")
 *     })
 *     private Boolean active;
 *
 *     // When type="vip" → WHERE level >= 5
 *     // When type="normal" → WHERE level < 5 (via expression)
 *     @When(path = "level", value = {
 *         @StringCase(is = "vip", ops = Ops.GOE, value = "5"),
 *         @StringCase(is = "normal", expression = "level < 5")
 *     })
 *     private String type;
 * }
 * }</pre>
 *
 * <h2>Chinese:</h2>
 * 试验性功能。放置在 {@link ConditionBean} 类的字段上，用于定义基于值的条件映射——
 * 将界面字段的值转换为数据库查询条件。
 *
 * <p>与 {@link Condition} 直接将字段映射到固定运算符的列不同，{@code @When} 支持
 * <b>基于 case 的分发</b>：不同的字段值可以产生不同的 SQL 条件（可以使用不同的运算符、
 * 操作数，甚至原始 SQL 表达式）。
 *
 * <h3>支持的字段类型：</h3>
 * <ul>
 *   <li>{@code String} — 使用 {@link #value()} ({@link StringCase})</li>
 *   <li>{@code Boolean} — 使用 {@link #forBool()} ({@link BoolCase})</li>
 *   <li>{@code Integer} — 使用 {@link #forInt()} ({@link IntCase})</li>
 * </ul>
 * <p><b>注意：</b>不支持基本类型（{@code boolean}、{@code int}），字段必须使用包装类型，
 * 以便 {@code null} 表示"无条件"。
 *
 * <h3>处理逻辑：</h3>
 * <ol>
 *   <li>如果字段值为 {@code null}，则不生成条件（跳过该字段）。</li>
 *   <li>将字段值与定义的 case 进行匹配（如 {@code BoolCase.is()}）。</li>
 *   <li>如果匹配成功，将对应的运算符和操作数（或表达式）应用到 {@link #path()} 指定的列上。</li>
 *   <li>如果没有匹配的 case，行为取决于 {@link #ignoreIfNoMatchCase()}。</li>
 * </ol>
 *
 * @see ConditionBean
 * @see Condition
 * @see BoolCase
 * @see StringCase
 * @see IntCase
 * @see Ops
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface When {

	/**
	 * The target column path (Q-class field name) to apply the condition on.
	 * If empty, defaults to the annotated field's name.
	 * <p>目标列的路径（Q类字段名）。为空时默认使用当前字段名。
	 *
	 * @return the column path name
	 */
	String path() default "";

	/**
	 * Case rules for {@code String} type fields. Each {@link StringCase} defines a mapping
	 * from a specific string value to a SQL condition.
	 * <p>用于 {@code String} 类型字段的 case 规则。每个 {@link StringCase} 定义一个
	 * 从特定字符串值到 SQL 条件的映射。
	 *
	 * @return the string case mappings
	 */
	StringCase[] value() default {};

	/**
	 * Case rules for {@code Boolean} type fields. Each {@link BoolCase} defines a mapping
	 * from a boolean value ({@code true}/{@code false}) to a SQL condition.
	 * <p>用于 {@code Boolean} 类型字段的 case 规则。每个 {@link BoolCase} 定义一个
	 * 从布尔值到 SQL 条件的映射。
	 *
	 * @return the boolean case mappings
	 */
	BoolCase[] forBool() default {};

	/**
	 * Case rules for {@code Integer} type fields. Each {@link IntCase} defines a mapping
	 * from a specific integer value to a SQL condition.
	 * <p>用于 {@code Integer} 类型字段的 case 规则。每个 {@link IntCase} 定义一个
	 * 从特定整数值到 SQL 条件的映射。
	 *
	 * @return the integer case mappings
	 */
	IntCase[] forInt() default {};

	/**
	 * Whether to silently ignore when no case matches the field value.
	 * <ul>
	 *   <li>{@code true} (default) — if no case matches, no condition is generated (field is skipped).</li>
	 *   <li>{@code false} — if no case matches, an {@link IllegalArgumentException} is thrown.</li>
	 * </ul>
	 * <p>当字段值没有匹配的 case 时是否静默忽略。
	 * <ul>
	 *   <li>{@code true}（默认）— 无匹配时不生成条件，跳过该字段。</li>
	 *   <li>{@code false} — 无匹配时抛出 {@link IllegalArgumentException}。</li>
	 * </ul>
	 *
	 * @return whether to ignore unmatched values
	 */
	boolean ignoreIfNoMatchCase() default true;
}
