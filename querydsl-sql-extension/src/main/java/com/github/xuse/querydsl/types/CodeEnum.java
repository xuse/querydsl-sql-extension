package com.github.xuse.querydsl.types;

/**
 * 某些枚举在入库时使用Int，但不希望使用ordinal这样的顺序，而是自行返回的代码
 * @param <T> type of target
 */
public interface CodeEnum<T extends Enum<T>> {

	/**
	 *  @return Code
	 */
	int getCode();
	
	
	
}
