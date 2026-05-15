package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * Declares the binding relationship between a DTO field and an entity column.
 * Used when the DTO field name differs from the entity field name, or when type
 * conversion is needed between the DTO and the database.
 * <p>
 * 声明 DTO 字段与实体列之间的绑定关系。
 * 用于 DTO 字段名与实体字段名不同，或需要在 DTO 与数据库之间进行类型转换的场景。
 *
 * <p>Example:</p>
 * <pre>
 * public class FooDTO {
 *     // Simple name remapping
 *     &#64;PathBinder("codeType")
 *     private int codeTypeX;
 *
 *     // Read-only field (not written back to DB on Insert/Update)
 *     &#64;PathBinder(value = "created", writable = false)
 *     private Date createdTime;
 *
 *     // With bidirectional type conversion
 *     &#64;PathBinder(value = "inDay", fromDb = SqlDateToString.class, toDb = StringToSqlDate.class)
 *     private String inDay;
 *
 *     // Using static field references for converters
 *     static final Function&lt;Object, String&gt; FROM_DB = v -&gt; String.valueOf(v);
 *     static final Function&lt;String, Object&gt; TO_DB = s -&gt; java.sql.Date.valueOf(s);
 *
 *     &#64;PathBinder(value = "inDay", fromDbRef = "FROM_DB", toDbRef = "TO_DB")
 *     private String inDayRef;
 *
 *     // Skip type-mismatch check (when a custom JDBC Type handles the binding)
 *     &#64;PathBinder(skipTypeCheck = true)
 *     private String someField;
 * }
 * </pre>
 *
 * @author Joey
 * @see com.github.xuse.querydsl.sql.expression.QBeanExWithConverter
 * @see com.github.xuse.querydsl.sql.expression.ConverterWrappedBean
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface PathBinder {

	/**
	 * The entity field name (Java field name, i.e. Path metadata name) to bind to.
	 * If empty, the DTO field's own name is used (i.e. same-name binding).
	 * <p>绑定的实体字段名。为空时使用 DTO 字段自身的名称（即同名绑定）。</p>
	 *
	 * @return entity field name, or empty string for same-name binding
	 */
	String value() default "";

	/**
	 * Whether this field should be written to the database on Insert/Update.
	 * Set to {@code false} for read-only fields that should not be persisted.
	 * <p>是否在 Insert/Update 时写入数据库。设为 {@code false} 表示只读字段，不参与写入。</p>
	 *
	 * @return {@code true} (default) if writable, {@code false} if read-only
	 */
	boolean writable() default true;

	/**
	 * Converter class for reading from database to this field (DB &rarr; DTO).
	 * Must implement {@code Function<DbColumnType, ThisFieldType>}.
	 * <p>从数据库读取到本字段的转换器类（DB &rarr; 本字段）。</p>
	 * <p>与 {@link #fromDbRef()} 互斥，两者同时指定时本属性优先。</p>
	 *
	 * @return converter class, or {@code Function.class} for no explicit converter
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Function> fromDb() default Function.class;

	/**
	 * Reference to a static {@link Function} field in the DTO class,
	 * used as the converter for reading from database (DB &rarr; DTO).
	 * <p>指向 DTO 类中的静态 Function 字段名，作为从数据库读取的转换器。</p>
	 * <p>与 {@link #fromDb()} 互斥，两者同时指定时 fromDb 优先。</p>
	 * <p>默认在当前 DTO 类中查找，可通过 {@link #converterSource()} 指定其他类。</p>
	 *
	 * @return static field name, or empty string for none
	 */
	String fromDbRef() default "";

	/**
	 * Converter class for writing this field to database (DTO &rarr; DB).
	 * Must implement {@code Function<ThisFieldType, DbColumnType>}.
	 * <p>将本字段写入数据库的转换器类（本字段 &rarr; DB）。</p>
	 * <p>与 {@link #toDbRef()} 互斥，两者同时指定时本属性优先。</p>
	 *
	 * @return converter class, or {@code Function.class} for no explicit converter
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Function> toDb() default Function.class;

	/**
	 * Reference to a static {@link Function} field in the DTO class,
	 * used as the converter for writing to database (DTO &rarr; DB).
	 * <p>指向 DTO 类中的静态 Function 字段名，作为写入数据库的转换器。</p>
	 * <p>与 {@link #toDb()} 互斥，两者同时指定时 toDb 优先。</p>
	 * <p>默认在当前 DTO 类中查找，可通过 {@link #converterSource()} 指定其他类。</p>
	 *
	 * @return static field name, or empty string for none
	 */
	String toDbRef() default "";

	/**
	 * The class in which to look up static converter fields referenced by
	 * {@link #fromDbRef()} and {@link #toDbRef()}.
	 * <p>
	 * Defaults to {@code void.class}, meaning the current DTO class is used.
	 * Specify another class to share converter definitions across multiple DTOs.
	 * </p>
	 * <p>
	 * 指定 {@link #fromDbRef()} 和 {@link #toDbRef()} 所引用的静态字段所在的类。
	 * 默认为 {@code void.class}，表示在当前 DTO 类中查找。
	 * 指定其他类可在多个 DTO 间共享转换器定义。
	 * </p>
	 *
	 * @return the class containing converter static fields, or {@code void.class} for current DTO
	 */
	Class<?> converterSource() default void.class;

	/**
	 * If {@code true}, suppress the type-mismatch check when no converter is found
	 * between the DTO field type and the entity column type.
	 * <p>
	 * Use this when a custom {@code Type<?>} is registered in the QueryDSL
	 * Configuration that handles the actual JDBC binding.
	 * </p>
	 * <p>
	 * 设为 {@code true} 时，类型不匹配且无转换器时不抛出异常。
	 * 适用于已注册自定义 Type 处理 JDBC 绑定的场景。
	 * </p>
	 *
	 * @return {@code false} (default) to enforce type-match check
	 */
	boolean skipTypeCheck() default false;
}
