package com.github.xuse.querydsl.lambda;

import com.github.xuse.querydsl.sql.RelationalPathEx;

/**
 * 用于帮助编码者获得字段引用对象。
 * <p>
 * 在构建查询时，经常需要引用数据表的字段，querydsl使用自动生成的Query Class解决了这个问题。例如官方示例
 * <pre>{@code List<String> lastNames = queryFactory.select(customer.lastName)
 * .from(customer).where(customer.firstName.eq("Bob")).fetch();
 * }</pre>
 * 其中的“customer.lastName”和"customer.firstName"就是表的字段引用。
 * <p>
 * 当我们使用Lambda表达式来代替queryclass时，也需要使用字段的函数，就写成这样了
 * <p><pre>{@code
 * List<String> lastNames = queryFactory.select(Customer::getLastName)
 *  .from(customer)
 *  .where(
 *     ((LambdaColumn<Customer,String>)Customer::getFirstName).eq("Bob"))
 *  .fetch();}</pre>
 * 需要通过一个强制类型转换来提示编译器将方法引用转换为LambdaColumn的函数接口，才能作为列对象使用。
 * 为了简化代码，可以使用本接口内的方法，简化为
 * <pre>{@code List<String> lastNames = queryFactory.select(Customer::getLastName)
 *  .from(customer)
 *  .where(
 *     string(Customer::getFirstName).eq("Bob"))
 *  .fetch();}</pre>
 * <p>使用时在业务类上实现本接口，即可使用转换提示功能。
 *
 */
public interface LambdaHelpers {
	/**
	 * @implNote
	 * 提示编译器将一个方法引用包装为字段模型。等效于 {@link #column(LambdaColumn)}
	 * @param <B> type of the entity bean.
	 * @param <T> type of the column field.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B, T extends Comparable<T>> LambdaColumn<B, T> $(LambdaColumn<B, T> path) {
		return path;
	}
	
	/**
	 * @implNote
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
	 * @implNote
	 * 提示编译器将一个方法引用包装为String类型的字段模型。等效于 {@link #string(StringLambdaColumn)}
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B> StringLambdaColumn<B> s(StringLambdaColumn<B> path) {
		return path;
	}
	
	/**
	 * @implNote
	 * 提示编译器将一个方法引用包装为String类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path 传入
	 * @return 字段模型
	 */
	default <B> StringLambdaColumn<B> string(StringLambdaColumn<B> path) {
		return path;
	}
	
	/**
	 * @implNote
	 * 提示编译器将一个方法引用包装为Number类型的字段模型。等效于{@link #num(NumberLambdaColumn)}
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
	
	/**
	 * @param <B> java type of the table.
	 * @param table lambdaTable
	 * @param variable table alias(variable)
	 * @return 获得一个指定了别名的表模型对象
	 */
	default <B> RelationalPathEx<B> forVariable(LambdaTable<B> table, String variable){
		return PathCache.getPath(table, variable);	
	}
	
}
