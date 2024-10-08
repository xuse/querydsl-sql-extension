package com.github.xuse.querydsl.sql.column;

import java.util.function.Predicate;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.Primitives;
import com.github.xuse.querydsl.util.StringUtils;

public final class UnsavedValuePredicateFactory {

	/**
	 * 根据类型和默认值来生成
	 * @param containerType containerType
	 * @param value value
	 * @return Predicate
	 */
	public static Predicate<Object> parseValue(Class<?> containerType, String value) {
		switch(value) {
		case UnsavedValue.Null:
			return Null;
		case UnsavedValue.Zero:
			return ZeroNumber;
		case UnsavedValue.MinusNumber:
			return MinusNumber;
		case UnsavedValue.ZeroAndMinus:
			return ZeroAndMinus;
		case UnsavedValue.NullOrEmpty:
			if (containerType.isPrimitive()) {
				return Null;
			} else {
				return NullOrEmpty;
			}
		case UnsavedValue.Never:
			return e -> false;
		}
		/* 
		 * process default / 处理缺省策略
		 * <p>
		 * it is in order to let the compiler create a byte code as a 'tableswitch', not a 'lookupswitch'.
		 * since the table switch has best performance.
		 * <p>
		 * 使用基础类型简单代码进行switch。上述策略使得switch数值分布控制在27以内，目的是让编译器在编译时使用tableswitch指令而不是lookupswitch指令。
		 * 前者具有更好的性能。经过反复测试，当byte到short的分布缩小到19时，编译后为tableswitch。在确定是primitive 类型时，将short=120放入default分支，分布宽度仅有11。
		 */
		Object condition = null;
		if (containerType.isPrimitive()) {
			String s = containerType.getName();
			switch(s.length() + s.charAt(0)) {
				case 108://int
					condition = StringUtils.toInt(value, 0);
					break;
				case 112://long
					condition = StringUtils.toLong(value, 0L);
					break;
				case 105://boolean
					condition = StringUtils.toBoolean(value, false);
					break;
				case 107://float
					condition = StringUtils.toFloat(value, 0f);
					break;
				case 106://double
					condition = StringUtils.toDouble(value, 0d);
					break;
				case 103://char
					if (value.length() == 0) {
						condition = (char) 0;
					} else {
						condition = value.charAt(0);
					}
					break;
				case 102://byte
					condition = (byte) StringUtils.toInt(value, 0);
					break;
				default://short = 120
					condition = StringUtils.toInt(value, 0);
					break;
			}
		} else if (String.class == containerType) {
			condition = value;
		} else {
			throw Exceptions.illegalArgument("Unsupport type [{}] for annotation @UnsavedValue('{}')", containerType,value);
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

	/**
	 *   default policy: null is null.
	 */
	public static final Predicate<Object> Null = new Predicate<Object>() {

		public boolean test(Object obj) {
			return obj == null;
		}
	};

	/**
	 *  如果字符串（或者对象转换为字符串后的长度为0，那么视为未设置值）
	 */
	public static final Predicate<Object> NullOrEmpty = new Predicate<Object>() {
		public boolean test(Object obj) {
			if (obj == null)
				return true;
			if(obj instanceof CharSequence) {
				return ((CharSequence) obj).length() == 0;
			}
			return false;
		}
	};

	/**
	 *  如果是负数，视为未设置值
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
	 *  如果是零或者负数，视为未设置值
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
	 *  如果是零，视为未设置值
	 */
	public static final Predicate<Object> ZeroNumber = new Predicate<Object>() {

		public boolean test(Object obj) {
			if (obj == null)
				return true;
			if (obj instanceof Number) {
				return ((Number) obj).longValue() == 0;
			} else {
				return false;
			}
		}
	};

	/**
	 * 根据类上的注解来解析生成
	 * @param type type
	 * @param annotationValue annotationValue
	 * @return Predicate
	 */
	public static Predicate<Object> create(Class<?> type, String annotationValue) {
		if (annotationValue != null && annotationValue.length() > 0) {
			return parseValue(type, annotationValue);
		}
		if (type.isPrimitive()) {
			return new NonNullConstantFilter(Primitives.defaultValueOfPrimitive(type));
		} else {
			return Null;
		}
	}
}
