package com.github.xuse.querydsl.lambda;

/**
 * 用方法引用来代替字段模型，需要提示编译器将方法引用转换为LambdaColumn的函数接口。否则无法使用该函数接口提供的API。
 * 相当于需要提示编译器做这样的转换。
 * <p>
 * 使用时在业务类上实现本接口，即可使用转换提示功能。
 *
 */
public interface LambdaHelpers {
	/**
	 * 提示编译器将一个方法引用包装为字段模型。
	 * @param <B> type of the entity bean.
	 * @param <T> type of the column field.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Comparable<T>> LambdaColumn<B, T> $(LambdaColumn<B, T> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为字段模型。
	 * @param <B> type of the entity bean.
	 * @param <T> type of the column field.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Comparable<T>> LambdaColumn<B, T> column(LambdaColumn<B, T> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为String类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B> StringLambdaColumn<B> s(StringLambdaColumn<B> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为String类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B> StringLambdaColumn<B> string(StringLambdaColumn<B> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为Number类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Number & Comparable<T>> NumberLambdaColumn<B,T> n(NumberLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为Number类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Number & Comparable<T>> NumberLambdaColumn<B,T> num(NumberLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为Date类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Comparable<T>> DateLambdaColumn<B,T> date(DateLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为Time类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Comparable<T>> TimeLambdaColumn<B,T> time(TimeLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为DateTime类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Comparable<T>> DateTimeLambdaColumn<B,T> datetime(DateTimeLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * 提示编译器将一个方法引用包装为DateTime类型的字段模型。这是{@link #datetime(DateTimeLambdaColumn)}的别名。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Comparable<T>> DateTimeLambdaColumn<B,T> dt(DateTimeLambdaColumn<B,T> path) {
		return path;
	}
	
	
	
}
