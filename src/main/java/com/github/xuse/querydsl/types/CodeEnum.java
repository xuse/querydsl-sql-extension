package com.github.xuse.querydsl.types;

/**
 * 某些枚举在入库时使用Int，但不希望使用ordinal这样的顺序，而是自行返回的代码
 * @author jiyi
 *
 */
public interface CodeEnum<T> {
	/**
	 * 返回Code
	 * @return
	 */
	int getCode();
}
