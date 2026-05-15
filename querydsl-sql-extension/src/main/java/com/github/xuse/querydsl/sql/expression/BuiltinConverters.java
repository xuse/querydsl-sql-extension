package com.github.xuse.querydsl.sql.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.annotation.query.PathBinder;
import com.github.xuse.querydsl.types.CodeEnum;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.TypeUtils;
import com.github.xuse.querydsl.util.Util;
import com.github.xuse.querydsl.util.lang.Annotations;
import com.github.xuse.querydsl.util.lang.Primitives;

/**
 * Registry of built-in type converters for common type conversions between
 * database result types and DTO field types.
 * <p>
 * 内置类型转换器注册表，用于数据库结果类型与 DTO 字段类型之间的常见类型转换。
 *
 * <h3>Maintenance Rules / 维护约束</h3>
 * <ol>
 *   <li><b>Symmetry（对称性）</b>: For every registered converter A &rarr; B, a corresponding
 *       B &rarr; A converter MUST also be registered. This ensures that a DTO field can be
 *       used for both read (DB &rarr; DTO) and write (DTO &rarr; DB) directions without
 *       requiring explicit {@code @PathBinder} converters.</li>
 *   <li><b>Precision loss is acceptable（精度丢失可接受）</b>: Narrowing conversions
 *       (e.g. {@code Double &rarr; int}) are allowed even though they may lose precision.
 *       The symmetry requirement takes precedence over losslessness.</li>
 *   <li><b>Primitive/Boxed coverage（基本类型/包装类型覆盖）</b>: Each numeric conversion
 *       should cover all 4 combinations: primitive &rarr; primitive, primitive &rarr; boxed,
 *       boxed &rarr; primitive, boxed &rarr; boxed.</li>
 *   <li><b>Enum converters（枚举转换器）</b>: Enum conversions are resolved dynamically
 *       in {@link #toEnumConverter} and {@link #fromEnumConverter}, inherently symmetric.</li>
 * </ol>
 *
 * @author Joey
 */
@SuppressWarnings("rawtypes")
public final class BuiltinConverters {

	private static final Map<ClassPairKey, Function<?, ?>> CONVERTERS = new HashMap<>();

	static {
		// String -> numeric
		register(String.class, int.class, s -> Integer.parseInt((String) s));
		register(String.class, Integer.class, s -> Integer.valueOf((String) s));
		register(String.class, long.class, s -> Long.parseLong((String) s));
		register(String.class, Long.class, s -> Long.valueOf((String) s));
		register(String.class, double.class, s -> Double.parseDouble((String) s));
		register(String.class, Double.class, s -> Double.valueOf((String) s));
		register(String.class, float.class, s -> Float.parseFloat((String) s));
		register(String.class, Float.class, s -> Float.valueOf((String) s));
		register(String.class, short.class, s -> Short.parseShort((String) s));
		register(String.class, Short.class, s -> Short.valueOf((String) s));
		register(String.class, boolean.class, s -> Boolean.parseBoolean((String) s));
		register(String.class, Boolean.class, s -> Boolean.valueOf((String) s));

		// Numeric -> String
		register(Integer.class, String.class, n -> String.valueOf(n));
		register(int.class, String.class, n -> String.valueOf(n));
		register(Long.class, String.class, n -> String.valueOf(n));
		register(long.class, String.class, n -> String.valueOf(n));
		register(Double.class, String.class, n -> String.valueOf(n));
		register(double.class, String.class, n -> String.valueOf(n));
		register(Float.class, String.class, n -> String.valueOf(n));
		register(float.class, String.class, n -> String.valueOf(n));
		register(Short.class, String.class, n -> String.valueOf(n));
		register(short.class, String.class, n -> String.valueOf(n));
		register(Boolean.class, String.class, n -> String.valueOf(n));
		register(boolean.class, String.class, n -> String.valueOf(n));

		// Integer <-> Long
		register(Integer.class, long.class, n -> ((Integer) n).longValue());
		register(Integer.class, Long.class, n -> ((Integer) n).longValue());
		register(int.class, long.class, n -> ((Integer) n).longValue());
		register(int.class, Long.class, n -> ((Integer) n).longValue());
		register(Long.class, int.class, n -> ((Long) n).intValue());
		register(Long.class, Integer.class, n -> ((Long) n).intValue());
		register(long.class, int.class, n -> ((Long) n).intValue());
		register(long.class, Integer.class, n -> ((Long) n).intValue());

		// Integer <-> Double
		register(Integer.class, double.class, n -> ((Integer) n).doubleValue());
		register(Integer.class, Double.class, n -> ((Integer) n).doubleValue());
		register(int.class, double.class, n -> ((Integer) n).doubleValue());
		register(int.class, Double.class, n -> ((Integer) n).doubleValue());
		register(Double.class, int.class, n -> ((Double) n).intValue());
		register(Double.class, Integer.class, n -> ((Double) n).intValue());
		register(double.class, int.class, n -> ((Double) n).intValue());
		register(double.class, Integer.class, n -> ((Double) n).intValue());

		// Integer <-> Float
		register(Integer.class, float.class, n -> ((Integer) n).floatValue());
		register(Integer.class, Float.class, n -> ((Integer) n).floatValue());
		register(int.class, float.class, n -> ((Integer) n).floatValue());
		register(int.class, Float.class, n -> ((Integer) n).floatValue());
		register(Float.class, int.class, n -> ((Float) n).intValue());
		register(Float.class, Integer.class, n -> ((Float) n).intValue());
		register(float.class, int.class, n -> ((Float) n).intValue());
		register(float.class, Integer.class, n -> ((Float) n).intValue());

		// Integer <-> Short
		register(Integer.class, short.class, n -> ((Integer) n).shortValue());
		register(Integer.class, Short.class, n -> ((Integer) n).shortValue());
		register(int.class, short.class, n -> ((Integer) n).shortValue());
		register(int.class, Short.class, n -> ((Integer) n).shortValue());
		register(Short.class, int.class, n -> ((Short) n).intValue());
		register(Short.class, Integer.class, n -> ((Short) n).intValue());
		register(short.class, int.class, n -> ((Short) n).intValue());
		register(short.class, Integer.class, n -> ((Short) n).intValue());

		// Long <-> Double
		register(Long.class, double.class, n -> ((Long) n).doubleValue());
		register(Long.class, Double.class, n -> ((Long) n).doubleValue());
		register(long.class, double.class, n -> ((Long) n).doubleValue());
		register(long.class, Double.class, n -> ((Long) n).doubleValue());
		register(Double.class, long.class, n -> ((Double) n).longValue());
		register(Double.class, Long.class, n -> ((Double) n).longValue());
		register(double.class, long.class, n -> ((Double) n).longValue());
		register(double.class, Long.class, n -> ((Double) n).longValue());

		// Long <-> Float
		register(Long.class, float.class, n -> ((Long) n).floatValue());
		register(Long.class, Float.class, n -> ((Long) n).floatValue());
		register(long.class, float.class, n -> ((Long) n).floatValue());
		register(long.class, Float.class, n -> ((Long) n).floatValue());
		register(Float.class, long.class, n -> ((Float) n).longValue());
		register(Float.class, Long.class, n -> ((Float) n).longValue());
		register(float.class, long.class, n -> ((Float) n).longValue());
		register(float.class, Long.class, n -> ((Float) n).longValue());

		// Long <-> Short
		register(Long.class, short.class, n -> ((Long) n).shortValue());
		register(Long.class, Short.class, n -> ((Long) n).shortValue());
		register(long.class, short.class, n -> ((Long) n).shortValue());
		register(long.class, Short.class, n -> ((Long) n).shortValue());
		register(Short.class, long.class, n -> ((Short) n).longValue());
		register(Short.class, Long.class, n -> ((Short) n).longValue());
		register(short.class, long.class, n -> ((Short) n).longValue());
		register(short.class, Long.class, n -> ((Short) n).longValue());

		// Float <-> Double
		register(Float.class, double.class, n -> ((Float) n).doubleValue());
		register(Float.class, Double.class, n -> ((Float) n).doubleValue());
		register(float.class, double.class, n -> ((Float) n).doubleValue());
		register(float.class, Double.class, n -> ((Float) n).doubleValue());
		register(Double.class, float.class, n -> ((Double) n).floatValue());
		register(Double.class, Float.class, n -> ((Double) n).floatValue());
		register(double.class, float.class, n -> ((Double) n).floatValue());
		register(double.class, Float.class, n -> ((Double) n).floatValue());

		// Float <-> Short
		register(Float.class, short.class, n -> ((Float) n).shortValue());
		register(Float.class, Short.class, n -> ((Float) n).shortValue());
		register(float.class, short.class, n -> ((Float) n).shortValue());
		register(float.class, Short.class, n -> ((Float) n).shortValue());
		register(Short.class, float.class, n -> ((Short) n).floatValue());
		register(Short.class, Float.class, n -> ((Short) n).floatValue());
		register(short.class, float.class, n -> ((Short) n).floatValue());
		register(short.class, Float.class, n -> ((Short) n).floatValue());

		// Double <-> Short
		register(Double.class, short.class, n -> ((Double) n).shortValue());
		register(Double.class, Short.class, n -> ((Double) n).shortValue());
		register(double.class, short.class, n -> ((Double) n).shortValue());
		register(double.class, Short.class, n -> ((Double) n).shortValue());
		register(Short.class, double.class, n -> ((Short) n).doubleValue());
		register(Short.class, Double.class, n -> ((Short) n).doubleValue());
		register(short.class, double.class, n -> ((Short) n).doubleValue());
		register(short.class, Double.class, n -> ((Short) n).doubleValue());

		// String <-> java.sql.Date (ISO format: yyyy-MM-dd)
		register(String.class, java.sql.Date.class, s -> java.sql.Date.valueOf((String) s));
		register(java.sql.Date.class, String.class, d -> ((java.sql.Date) d).toString());

		// String <-> java.sql.Timestamp (ISO format: yyyy-MM-dd HH:mm:ss[.fffffffff])
		register(String.class, java.sql.Timestamp.class, s -> java.sql.Timestamp.valueOf((String) s));
		register(java.sql.Timestamp.class, String.class, t -> ((java.sql.Timestamp) t).toString());

		// String <-> java.sql.Time (ISO format: HH:mm:ss)
		register(String.class, java.sql.Time.class, s -> java.sql.Time.valueOf((String) s));
		register(java.sql.Time.class, String.class, t -> ((java.sql.Time) t).toString());

		// long <-> java.sql.Date (millis since epoch)
		register(Long.class, java.sql.Date.class, n -> new java.sql.Date(((Long) n).longValue()));
		register(long.class, java.sql.Date.class, n -> new java.sql.Date(((Long) n).longValue()));
		register(java.sql.Date.class, Long.class, d -> ((java.sql.Date) d).getTime());
		register(java.sql.Date.class, long.class, d -> ((java.sql.Date) d).getTime());

		// long <-> java.sql.Timestamp (millis since epoch)
		register(Long.class, java.sql.Timestamp.class, n -> new java.sql.Timestamp(((Long) n).longValue()));
		register(long.class, java.sql.Timestamp.class, n -> new java.sql.Timestamp(((Long) n).longValue()));
		register(java.sql.Timestamp.class, Long.class, t -> ((java.sql.Timestamp) t).getTime());
		register(java.sql.Timestamp.class, long.class, t -> ((java.sql.Timestamp) t).getTime());

		// long <-> java.util.Date (millis since epoch)
		register(Long.class, java.util.Date.class, n -> new java.util.Date(((Long) n).longValue()));
		register(long.class, java.util.Date.class, n -> new java.util.Date(((Long) n).longValue()));
		register(java.util.Date.class, Long.class, d -> ((java.util.Date) d).getTime());
		register(java.util.Date.class, long.class, d -> ((java.util.Date) d).getTime());

		// String <-> java.util.Date (ISO format via java.sql.Timestamp as intermediary)
		register(String.class, java.util.Date.class, s -> new java.util.Date(java.sql.Timestamp.valueOf((String) s).getTime()));
		register(java.util.Date.class, String.class, d -> new java.sql.Timestamp(((java.util.Date) d).getTime()).toString());
	}

	/**
	 * Find a built-in converter for the given source and target types.
	 *
	 * @param sourceType the type of the source value
	 * @param targetType the type of the target field
	 * @return a converter function, or {@code null} if no built-in converter exists
	 */
	@SuppressWarnings("rawtypes")
	static Function find(Class<?> sourceType, Class<?> targetType) {
		ClassPairKey key = new ClassPairKey(sourceType, targetType);
		Function<?, ?> converter = CONVERTERS.get(key);
		if (converter != null) {
			return converter;
		}
		// Try with boxed types
		Class<?> boxedSource = Primitives.toWrapperClass(sourceType);
		Class<?> boxedTarget = Primitives.toWrapperClass(targetType);
		if (boxedSource != sourceType || boxedTarget != targetType) {
			converter = CONVERTERS.get(new ClassPairKey(boxedSource, boxedTarget));
			if (converter != null) {
				return converter;
			}
		}
		
		boolean sourceIsEnum  = sourceType.isEnum();
		boolean targetIsEnum  = targetType.isEnum();
		if(sourceIsEnum && !targetIsEnum) {
			return fromEnumConverter(sourceType.asSubclass(Enum.class), targetType);
		}else if(targetIsEnum && !sourceIsEnum) {
			return toEnumConverter(sourceType, targetType.asSubclass(Enum.class));
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Function toEnumConverter(Class<?> sourceType, Class<? extends Enum> enumClass) {
		if (sourceType == String.class) {
			// String -> Enum by name
			return s -> s == null ? null : Enum.valueOf(enumClass, (String) s);
		}
		if (isIntType(sourceType)) {
			if (CodeEnum.class.isAssignableFrom(enumClass)) {
				// int -> CodeEnum by code
				Enum[] constants = enumClass.getEnumConstants();
				return n -> {
					if (n == null) return null;
					int code = ((Number) n).intValue();
					for (Enum e : constants) {
						if (((CodeEnum) e).getCode() == code) {
							return e;
						}
					}
					return null;
				};
			} else {
				// int -> Enum by ordinal
				Enum[] constants = enumClass.getEnumConstants();
				return n -> {
					if (n == null) return null;
					int ordinal = ((Number) n).intValue();
					return (ordinal >= 0 && ordinal < constants.length) ? constants[ordinal] : null;
				};
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Function fromEnumConverter(Class<? extends Enum> enumClass, Class<?> targetType) {
		if (targetType == String.class) {
			// Enum -> String by name
			return e -> e == null ? null : ((Enum) e).name();
		}
		if (isIntType(targetType)) {
			if (CodeEnum.class.isAssignableFrom(enumClass)) {
				// CodeEnum -> int by code
				return e -> e == null ? null : ((CodeEnum) e).getCode();
			} else {
				// Enum -> int by ordinal
				return e -> e == null ? null : ((Enum) e).ordinal();
			}
		}
		return null;
	}

	private static boolean isIntType(Class<?> type) {
		return type == int.class || type == Integer.class;
	}

	@SuppressWarnings("rawtypes")
	private static void register(Class<?> source, Class<?> target, Function converter) {
		CONVERTERS.put(new ClassPairKey(source, target), converter);
	}

	/**
	 * Register a custom global converter for DTO field mapping.
	 * The converter will be used when no explicit @PathBinder converter is specified
	 * and the source/target types are not directly assignable.
	 *
	 * @param sourceType source type
	 * @param targetType target type
	 * @param converter  conversion function
	 * @param <S>        source type
	 * @param <T>        target type
	 */
	public static <S, T> void registerGlobal(Class<S> sourceType, Class<T> targetType, Function<S, T> converter) {
		CONVERTERS.put(new ClassPairKey(sourceType, targetType), converter);
	}

	private BuiltinConverters() {
	}

	/**
	 * Resolve a converter for a field mapping. Checks explicit converter class/ref first,
	 * then falls back to type compatibility check and built-in converters.
	 *
	 * @param dtoType        the DTO class (for static field lookup fallback)
	 * @param field          the DTO field property
	 * @param converterClass explicit converter class from @PathBinder (or Function.class if none)
	 * @param converterRef   explicit converter ref from @PathBinder (or empty if none)
	 * @param converterSource the class to look up converterRef in (resolved by caller)
	 * @param sourceType     the source type of the value
	 * @param targetType     the target type to convert to
	 * @param skipTypeCheck  if true, return identity when no converter found instead of throwing
	 * @return resolved converter function, never null
	 */
	static Function resolveConverter(Class<?> dtoType, Property field,
			Class<? extends Function> converterClass, String converterRef, Class<?> converterSource,
			Class<?> sourceType, Class<?> targetType, boolean skipTypeCheck) {
		if (converterClass != Function.class) {
			return (Function) TypeUtils.newInstance(converterClass);
		}
		if (converterRef != null && !converterRef.isEmpty()) {
			return Util.getStaticFunctionField(converterSource, converterRef, field.getName());
		}
		if (sourceType == targetType || Primitives.isAssignableWithBoxing(sourceType, targetType)) {
			return Function.identity();
		}
		Function builtin = find(sourceType, targetType);
		if (builtin != null) {
			return builtin;
		}
		if (skipTypeCheck) {
			return Function.identity();
		}
		throw Exceptions.illegalState(
				"No converter found for {} -> {} on field [{}] of class [{}]. "
				+ "Please specify a converter via @PathBinder or register a built-in converter.",
				sourceType.getName(), targetType.getName(), field.getName(), dtoType.getName());
	}

	/**
	 * Resolve the class to look up converter ref fields.
	 * Order: field-level converterSource > global default > DTO class.
	 */
	static Class<?> resolveConverterSource(PathBinder pathBinder, Class<?> dtoType) {
		Class<?> fieldLevel = pathBinder.converterSource();
		if (fieldLevel != void.class) {
			return fieldLevel;
		}
		Class<?> global = com.github.xuse.querydsl.config.ConfigurationEx.globalConverterSource;
		if (global != null) {
			return global;
		}
		return dtoType;
	}
	
	public static final PathBinder DEFAULT_PATH_BINDER = Annotations.builder(PathBinder.class).build();
}
