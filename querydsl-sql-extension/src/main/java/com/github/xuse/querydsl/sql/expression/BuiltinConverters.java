package com.github.xuse.querydsl.sql.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.util.lang.Primitives;

/**
 * <h2>English:</h2>
 * Registry of built-in type converters for common type conversions between
 * database result types and DTO field types.
 *
 * <h2>Chinese:</h2>
 * 内置类型转换器注册表，用于数据库结果类型与 DTO 字段类型之间的常见类型转换。
 *
 * @author Joey
 */
final class BuiltinConverters {

	private static final Map<ConversionKey, Function<?, ?>> CONVERTERS = new HashMap<>();

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

		// Numeric widening
		register(Integer.class, long.class, n -> ((Integer) n).longValue());
		register(Integer.class, Long.class, n -> ((Integer) n).longValue());
		register(int.class, long.class, n -> ((Integer) n).longValue());
		register(int.class, Long.class, n -> ((Integer) n).longValue());
		register(Integer.class, double.class, n -> ((Integer) n).doubleValue());
		register(Integer.class, Double.class, n -> ((Integer) n).doubleValue());
		register(Long.class, double.class, n -> ((Long) n).doubleValue());
		register(Long.class, Double.class, n -> ((Long) n).doubleValue());
		register(Float.class, double.class, n -> ((Float) n).doubleValue());
		register(Float.class, Double.class, n -> ((Float) n).doubleValue());

		// Numeric narrowing (common cases)
		register(Long.class, int.class, n -> ((Long) n).intValue());
		register(Long.class, Integer.class, n -> ((Long) n).intValue());
		register(long.class, int.class, n -> ((Long) n).intValue());
		register(long.class, Integer.class, n -> ((Long) n).intValue());
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
		ConversionKey key = new ConversionKey(sourceType, targetType);
		Function<?, ?> converter = CONVERTERS.get(key);
		if (converter != null) {
			return converter;
		}
		// Try with boxed types
		Class<?> boxedSource = Primitives.toWrapperClass(sourceType);
		Class<?> boxedTarget = Primitives.toWrapperClass(targetType);
		if (boxedSource != sourceType || boxedTarget != targetType) {
			return CONVERTERS.get(new ConversionKey(boxedSource, boxedTarget));
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static void register(Class<?> source, Class<?> target, Function converter) {
		CONVERTERS.put(new ConversionKey(source, target), converter);
	}

	private static final class ConversionKey {
		private final Class<?> source;
		private final Class<?> target;
		private final int hash;

		ConversionKey(Class<?> source, Class<?> target) {
			this.source = source;
			this.target = target;
			this.hash = source.hashCode() * 31 + target.hashCode();
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ConversionKey) {
				ConversionKey other = (ConversionKey) obj;
				return this.source == other.source && this.target == other.target;
			}
			return false;
		}
	}

	private BuiltinConverters() {
	}
}
