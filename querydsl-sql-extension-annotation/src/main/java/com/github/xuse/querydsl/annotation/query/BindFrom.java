package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * <h2>English:</h2>
 * Specifies the source field name for binding when the DTO field name differs from the
 * entity/table field name. Used in projection scenarios where query results are mapped
 * to a custom DTO class.
 * <p>
 * Optionally, a type converter ({@link Function} implementation) can be specified to
 * perform simple type conversions (e.g., String to int) during the binding process.
 * </p>
 *
 * <h2>Chinese:</h2>
 * 当 DTO 字段名与实体/表字段名不同时，指定绑定的源字段名。
 * 用于查询结果映射到自定义 DTO 类的投影场景。
 * <p>
 * 可选地，可以指定一个类型转换器（{@link Function} 实现类），
 * 在绑定过程中执行简单的类型转换（如 String 转 int）。
 * </p>
 *
 * <p>Example:</p>
 * <pre>
 * public class FooDTO {
 *     // Maps to the "codeType" field in the source table
 *     &#64;BindFrom("codeType")
 *     private int codeTypeX;
 *
 *     // Maps to "name" with a custom converter
 *     &#64;BindFrom(value = "count", converter = StringToInt.class)
 *     private int totalCount;
 * }
 * </pre>
 *
 * @author Joey
 * @see com.github.xuse.querydsl.sql.expression.QBeanExWithConverter
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface BindFrom {

	/**
	 * The source field name (Java field name in the entity/table model) to bind from.
	 * <p>数据源中的 Java 字段名。</p>
	 *
	 * @return source field name
	 */
	String value();

	/**
	 * Optional type converter class. Must be a concrete implementation of
	 * {@code java.util.function.Function<SourceType, TargetType>}.
	 * <p>
	 * When not specified (default {@code void.class}), the framework will attempt
	 * automatic type conversion for common types (String↔int, String↔long, etc.).
	 * </p>
	 * <p>可选的类型转换器类。未指定时框架会尝试常见类型的自动转换。</p>
	 * <p>与 {@link #converterRef()} 互斥，两者同时指定时 converter 优先。</p>
	 *
	 * @return converter class, or {@code Function.class} for no explicit converter
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Function> converter() default Function.class;

	/**
	 * Optional reference to a static {@link Function} field in the DTO class,
	 * used as the type converter. This allows defining converters as lambda expressions
	 * directly in the DTO class without creating a separate class.
	 * <p>
	 * Example:
	 * <pre>
	 * public class FooDTO {
	 *     public static final Function&lt;Object, Integer&gt; NULL_TO_ZERO = v -&gt; v == null ? 0 : ((Number) v).intValue();
	 *
	 *     &#64;BindFrom(value = "codeType", converterRef = "NULL_TO_ZERO")
	 *     private int codeTypeX;
	 * }
	 * </pre>
	 * </p>
	 * <p>可选，指向 DTO 类中的一个静态 {@link Function} 字段名，作为类型转换器。
	 * 允许用 lambda 表达式定义转换器，无需创建额外的类。</p>
	 * <p>与 {@link #converter()} 互斥，两者同时指定时 converter 优先。</p>
	 *
	 * @return static field name in the DTO class, or empty string for none
	 */
	String converterRef() default "";
}
