package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>English:</h2>
 * Marks a class as a query condition form bean. A condition bean is a POJO whose fields
 * represent query parameters passed from the UI layer. Each field can be annotated with
 * {@link Condition}, {@link When}, or {@link Order} to define how it maps to a SQL predicate.
 *
 * <p>The framework automatically inspects the bean's fields at runtime:
 * <ul>
 *   <li>Fields with non-null values participate in query filtering.</li>
 *   <li>Fields with null values are skipped (no condition generated).</li>
 * </ul>
 *
 * <h3>Field annotations:</h3>
 * <ul>
 *   <li>{@link Condition} — direct mapping: field value + operator → SQL condition
 *       (e.g. {@code name LIKE '%value%'}).</li>
 *   <li>{@link When} — case-based mapping: different field values produce different SQL conditions
 *       (supports String, Boolean, Integer dispatching).</li>
 *   <li>{@link Order} — defines sort order for the query.</li>
 * </ul>
 * <p><b>Note:</b> {@code @Condition}, {@code @When}, and {@code @Order} are mutually exclusive
 * on a single field — only one may be present.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * @ConditionBean
 * public class UserQuery {
 *
 *     @Condition(Ops.STRING_CONTAINS)
 *     private String name;           // WHERE name LIKE '%value%'
 *
 *     @Condition(path = "age", value = Ops.GOE)
 *     private Integer minAge;        // WHERE age >= value
 *
 *     @When(path = "status", forBool = {
 *         @BoolCase(is = true, ops = Ops.EQ, value = "1"),
 *         @BoolCase(is = false, ops = Ops.EQ, value = "0")
 *     })
 *     private Boolean active;        // WHERE status = 1 or status = 0
 * }
 * }</pre>
 *
 * <h2>Chinese:</h2>
 * 将一个类标记为查询条件表单 Bean。条件 Bean 是一个 POJO，其字段代表从 UI 层传入的查询参数。
 * 每个字段可以使用 {@link Condition}、{@link When} 或 {@link Order} 注解来定义其到 SQL 条件的映射方式。
 *
 * <p>框架在运行时自动检查 Bean 的字段：
 * <ul>
 *   <li>值非 null 的字段参与查询过滤。</li>
 *   <li>值为 null 的字段被跳过（不生成条件）。</li>
 * </ul>
 *
 * <h3>字段注解：</h3>
 * <ul>
 *   <li>{@link Condition} — 直接映射：字段值 + 运算符 → SQL 条件。</li>
 *   <li>{@link When} — 基于 case 的映射：不同字段值产生不同的 SQL 条件。</li>
 *   <li>{@link Order} — 定义查询的排序方式。</li>
 * </ul>
 * <p><b>注意：</b>{@code @Condition}、{@code @When}、{@code @Order} 在同一字段上互斥，只能出现一个。
 *
 * @author Joey
 * @see Condition
 * @see When
 * @see Order
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ConditionBean {

	/**
	 * Specifies the field name that holds the LIMIT (page size) value for pagination.
	 * <p>指定持有分页 LIMIT（每页条数）值的字段名。
	 *
	 * @deprecated Please use method parameter to pass the limit value directly.
	 * @return the field name for limit
	 */
	String limitField() default "";

	/**
	 * Specifies the field name that holds the OFFSET (starting position) value for pagination.
	 * <p>指定持有分页 OFFSET（起始位置）值的字段名。
	 *
	 * @deprecated Please use method parameter to pass the offset value directly.
	 * @return the field name for offset
	 */
	String offsetField() default "";

	/**
	 * Specifies a boolean field name indicating whether the query should return the total
	 * record count (useful for paginated queries that need total pages).
	 * <p>指定一个布尔类型的字段名，表示本次查询是否需要返回记录总数（用于分页查询计算总页数）。
	 *
	 * @return the field name for the "require total count" flag
	 */
	String isRequireTotalField() default "";
}
