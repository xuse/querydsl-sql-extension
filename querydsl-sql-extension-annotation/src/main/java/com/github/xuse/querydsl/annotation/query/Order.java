package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <h2>English:</h2>
 * Placed on a field of a {@link ConditionBean} class to define a sort (ORDER BY) clause.
 * The annotated field's value specifies which column(s) to sort by (as a comma-separated
 * string of column path names).
 *
 * <p>The sort direction (ASC/DESC) can be controlled in two ways:
 * <ul>
 *   <li><b>Via {@link #sortField()}</b> — references another field in the same bean whose value
 *       determines the direction. If that field is {@code Boolean}, {@code true} = ASC,
 *       {@code false} = DESC. If {@code String}, the value should be "asc" or "desc"
 *       (case-insensitive).</li>
 *   <li><b>Via {@link #orderExpr()}</b> — when {@code true}, the field value itself can contain
 *       inline direction expressions (e.g. {@code "name asc,age desc"}). Each segment is parsed
 *       as {@code "columnName direction"}.</li>
 * </ul>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * @ConditionBean
 * public class UserQuery {
 *
 *     // Simple: sort by the column named in 'sortBy', direction from 'sortDir'
 *     @Order(sortField = "sortDir")
 *     private String sortBy;       // e.g. "createTime"
 *     private String sortDir;      // e.g. "desc"
 *
 *     // Expression mode: field value contains column + direction pairs
 *     @Order(orderExpr = true)
 *     private String orderClause;  // e.g. "name asc,age desc"
 * }
 * }</pre>
 *
 * <h2>Chinese:</h2>
 * 放置在 {@link ConditionBean} 类的字段上，用于定义排序（ORDER BY）子句。
 * 被注解字段的值指定要排序的列（以逗号分隔的列路径名字符串）。
 *
 * <p>排序方向（ASC/DESC）可通过两种方式控制：
 * <ul>
 *   <li><b>通过 {@link #sortField()}</b> — 引用同一 Bean 中的另一个字段，其值决定排序方向。
 *       如果该字段为 {@code Boolean} 类型，{@code true} = ASC，{@code false} = DESC。
 *       如果为 {@code String} 类型，值应为 "asc" 或 "desc"（不区分大小写）。</li>
 *   <li><b>通过 {@link #orderExpr()}</b> — 为 {@code true} 时，字段值本身可包含内联方向表达式
 *       （如 {@code "name asc,age desc"}），每段解析为 {@code "列名 方向"}。</li>
 * </ul>
 *
 * <p><b>注意：</b>{@code @Order} 与 {@code @Condition}、{@code @When} 在同一字段上互斥。
 *
 * @see ConditionBean
 * @see Condition
 * @see When
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface Order {

	/**
	 * References another field name in the same {@link ConditionBean} that provides the
	 * sort direction. If the referenced field is {@code Boolean}, {@code true} means ASC,
	 * {@code false} means DESC. If {@code String}, the value should be "asc" or "desc"
	 * (case-insensitive); other values default to ASC.
	 * <p>配置一个字段名，用于描述本排序字段是 ASC 还是 DESC。
	 * 如果目标字段为 boolean 类型，则 true 表示 ASC，false 表示 DESC。
	 * 如果目标字段为 String 类型，则需要值为 asc/desc（无视大小写），其他值默认为 ASC。
	 *
	 * @return the field name that holds the sort direction, or empty to use default (ASC)
	 */
	String sortField() default "";

	/**
	 * When {@code true}, enables expression mode where the field value itself contains
	 * inline sort direction for each column (e.g. {@code "name asc,age desc"}).
	 * Each comma-separated segment is parsed as {@code "columnPath direction"}.
	 * <p>为 {@code true} 时启用表达式模式，字段值本身包含每列的排序方向
	 * （如 {@code "name asc,age desc"}），每段按 {@code "列路径 方向"} 解析。
	 *
	 * @return whether to parse inline order expressions from the field value
	 */
	boolean orderExpr() default false;
}
