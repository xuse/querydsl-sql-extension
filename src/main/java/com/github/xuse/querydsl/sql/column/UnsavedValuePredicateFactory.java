package com.github.xuse.querydsl.sql.column;

import java.util.function.Predicate;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.Primitives;
import com.github.xuse.querydsl.util.StringUtils;

public final class UnsavedValuePredicateFactory {
	
	/**
	 * 根据类型和默认值来生成
	 * @param containerType
	 * @param value
	 * @return
	 */
	public static Predicate<Object> parseValue(Class<?> containerType, String value) {
		// int 226
		// short 215
		// long 221
		// boolean 222
		// float 219
		// double 228
		// char 201
		// byte 237
		Object condition = null;
		if (containerType.isPrimitive()) {
			String s = containerType.getName();
			switch (s.charAt(1) + s.charAt(2)) {
			case 226:
				condition = StringUtils.toInt(value, 0);
				break;
			case 215:
				condition = StringUtils.toInt(value, 0);
				break;
			case 221:
				condition = StringUtils.toLong(value, 0L);
				break;
			case 222:
				condition = StringUtils.toBoolean(value, false);
				break;
			case 219:
				condition = StringUtils.toFloat(value, 0f);
				break;
			case 228:
				condition = StringUtils.toDouble(value, 0d);
				break;
			case 201:
				if (value.length() == 0) {
					condition = (char) 0;
				} else {
					condition = value.charAt(0);
				}
				break;
			case 237:
				condition = (byte) StringUtils.toInt(value, 0);
				break;
			default:
			}
		} else if ("null".equalsIgnoreCase(value)) {
			return Null;
		} else if (UnsavedValue.MinusNumber.equals(value)) {
			return MinusNumber;
		} else if (UnsavedValue.ZeroAndMinus.equals(value)) {
			return ZeroAndMinus;
		} else if (UnsavedValue.NullOrEmpty.equals(value)) {
			return NullOrEmpty;
		} else if (String.class == containerType) {
			condition = value;
		} else {
			throw Exceptions.illegalArgument("Unsupport type [{}] for annotation @UnsavedValue.", containerType);
		}
		return condition == null ? Null : new ConstantFilter(condition);
	}

	private static class ConstantFilter implements Predicate<Object> {
		private Object object1;

		ConstantFilter(Object obj) {
			this.object1 = obj;
		}

		public boolean test(Object object2) {
			if (object1 == object2) {
				return true;
			}
			if ((object1 == null) || (object2 == null)) {
				return false;
			}
			return object1.equals(object2);
		}
	}

	private static class NonNullConstantFilter implements Predicate<Object> {
		private Object object1;

		NonNullConstantFilter(Object obj) {
			this.object1 = obj;
		}

		@Override
		public boolean test(Object t) {
			return object1.equals(t);
		}
	}

	private static final Predicate<Object> Null = new Predicate<Object>() {
		public boolean test(Object obj) {
			return obj == null;
		}
	};

	/**
	 * 如果字符串（或者对象转换为字符串后的长度为0，那么视为未设置值）
	 */
	public static final Predicate<Object> NullOrEmpty = new Predicate<Object>() {
		public boolean test(Object obj) {
			if (obj == null)
				return true;
			return String.valueOf(obj).length() == 0;
		}
	};

	/**
	 * 如果是负数，视为未设置值
	 */
	public static final Predicate<Object> MinusNumber = new Predicate<Object>() {
		public boolean test(Object obj) {
			if (obj == null)
				return true;
			if (obj instanceof Number) {
				return ((Number) obj).longValue() < 0;
			} else {
				return false;
			}
		}
	};

	/**
	 * 如果是零或者负数，视为未设置值
	 */
	public static final Predicate<Object> ZeroAndMinus = new Predicate<Object>() {
		public boolean test(Object obj) {
			if (obj == null)
				return true;
			if (obj instanceof Number) {
				return ((Number) obj).longValue() <= 0;
			} else {
				return false;
			}
		}
	};

	/**
	 * 根据类上的注解来解析生成
	 * @param type
	 * @param annotation
	 * @return
	 */
	public static Predicate<Object> create(Class<?> type, UnsavedValue annotation) {
		if (annotation != null) {
			return parseValue(type, annotation.value());
		}
		if (type.isPrimitive()) {
			return new NonNullConstantFilter(Primitives.defaultValueOfPrimitive(type));
		} else {
			return Null;
		}
	}
}
