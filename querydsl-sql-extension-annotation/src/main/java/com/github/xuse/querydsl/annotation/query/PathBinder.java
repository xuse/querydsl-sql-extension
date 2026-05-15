package com.github.xuse.querydsl.annotation.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * <h2>English:</h2>
 * Declares the binding relationship between a DTO field and an entity Path (field).
 * Used when the DTO field name differs from the entity field name, or when type
 * conversion is needed between the DTO and the database.
 * <p>
 * Supports both read (Select: DB &rarr; DTO) and write (Insert/Update: DTO &rarr; DB) directions
 * with independent type converters.
 * </p>
 *
 * <h2>Chinese:</h2>
 * 声明 DTO 字段与实体 Path（字段）之间的绑定关系。
 * 用于 DTO 字段名与实体字段名不同，或需要在 DTO 与数据库之间进行类型转换的场景。
 * <p>
 * 支持读（Select: DB &rarr; DTO）和写（Insert/Update: DTO &rarr; DB）两个方向，
 * 可分别指定独立的类型转换器。
 * </p>
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
 *     &#64;PathBinder(value = "codeType", readConverter = IntToString.class, writeConverter = StringToInt.class)
 *     private String codeTypeStr;
 *
 *     // Using static field references for converters
 *     public static final Function&lt;Object, String&gt; READ = v -&gt; String.valueOf(v);
 *     public static final Function&lt;String, Object&gt; WRITE = s -&gt; Integer.parseInt(s);
 *
 *     &#64;PathBinder(value = "codeType", readConverterRef = "READ", writeConverterRef = "WRITE")
 *     private String codeTypeRef;
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
	 * <p>绑定的实体字段名（Java 字段名，即 Path 的 metadata name）。</p>
	 *
	 * @return entity field name
	 */
	String value();

	/**
	 * Whether this field should be written to the database on Insert/Update.
	 * Set to {@code false} for read-only fields that should not be persisted.
	 * <p>是否在 Insert/Update 时写入数据库。设为 {@code false} 表示只读字段，不参与写入。</p>
	 *
	 * @return {@code true} (default) if writable, {@code false} if read-only
	 */
	boolean writable() default true;

	/**
	 * Optional read converter class for Select scenarios (DB &rarr; DTO).
	 * Must be a concrete implementation of {@code java.util.function.Function<DbType, DtoFieldType>}.
	 * <p>可选的读取转换器类，用于 Select 场景（DB &rarr; DTO）。</p>
	 * <p>与 {@link #readConverterRef()} 互斥，两者同时指定时本属性优先。</p>
	 *
	 * @return read converter class, or {@code Function.class} for no explicit converter
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Function> readConverter() default Function.class;

	/**
	 * Optional reference to a static {@link Function} field in the DTO class,
	 * used as the read converter (DB &rarr; DTO) for Select scenarios.
	 * <p>可选，指向 DTO 类中的一个静态 {@link Function} 字段名，作为读取转换器。</p>
	 * <p>与 {@link #readConverter()} 互斥，两者同时指定时 readConverter 优先。</p>
	 *
	 * @return static field name in the DTO class, or empty string for none
	 */
	String readConverterRef() default "";

	/**
	 * Optional write converter class for Insert/Update scenarios (DTO &rarr; DB).
	 * Must be a concrete implementation of {@code java.util.function.Function<DtoFieldType, DbType>}.
	 * <p>可选的写入转换器类，用于 Insert/Update 场景（DTO &rarr; DB）。</p>
	 * <p>与 {@link #writeConverterRef()} 互斥，两者同时指定时本属性优先。</p>
	 *
	 * @return write converter class, or {@code Function.class} for no explicit converter
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Function> writeConverter() default Function.class;

	/**
	 * Optional reference to a static {@link Function} field in the DTO class,
	 * used as the write converter (DTO &rarr; DB) for Insert/Update scenarios.
	 * <p>可选，指向 DTO 类中的一个静态 {@link Function} 字段名，作为写入转换器。</p>
	 * <p>与 {@link #writeConverter()} 互斥，两者同时指定时 writeConverter 优先。</p>
	 *
	 * @return static field name in the DTO class, or empty string for none
	 */
	String writeConverterRef() default "";
}
