package com.github.xuse.querydsl.lambda;

import com.github.xuse.querydsl.sql.RelationalPathEx;

/**
 * <h2>English:</h2>
 * Helps developers obtain column reference objects via Lambda expressions.
 * <p>
 * When building queries, you often need to reference table columns. QueryDSL solves this
 * with auto-generated Query Classes. When using Lambda expressions instead of Query Classes,
 * a type cast is needed to hint the compiler. Implement this interface in your business class
 * to use the helper methods that simplify the cast, e.g.:
 * <pre>{@code string(Customer::getFirstName).eq("Bob")}</pre>
 *
 * <h2>Chinese:</h2>
 * 用于帮助编码者通过Lambda表达式获得字段引用对象。
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
	 * Hint the compiler to wrap a method reference as a column model. Alias of {@link #column(LambdaColumn)}.
	 * <p>提示编译器将一个方法引用包装为字段模型。等效于 {@link #column(LambdaColumn)}
	 * @param <B> type of the entity bean.
	 * @param <T> type of the column field.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Comparable<?>> LambdaColumn<B, T> $(LambdaColumn<B, T> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a column model.
	 * <p>提示编译器将一个方法引用包装为字段模型。
	 * @param <B> type of the entity bean.
	 * @param <T> type of the column field.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Comparable<?>> LambdaColumn<B, T> column(LambdaColumn<B, T> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a String column model. Alias of {@link #string(StringLambdaColumn)}.
	 * <p>提示编译器将一个方法引用包装为String类型的字段模型。等效于 {@link #string(StringLambdaColumn)}
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B> StringLambdaColumn<B> s(StringLambdaColumn<B> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a String column model.
	 * <p>提示编译器将一个方法引用包装为String类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B> StringLambdaColumn<B> string(StringLambdaColumn<B> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a Number column model. Alias of {@link #num(NumberLambdaColumn)}.
	 * <p>提示编译器将一个方法引用包装为Number类型的字段模型。等效于{@link #num(NumberLambdaColumn)}
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Number & Comparable<T>> NumberLambdaColumn<B,T> n(NumberLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a Number column model.
	 * <p>提示编译器将一个方法引用包装为Number类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Number & Comparable<T>> NumberLambdaColumn<B,T> num(NumberLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a Date column model.
	 * <p>提示编译器将一个方法引用包装为Date类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Comparable<?>> DateLambdaColumn<B,T> date(DateLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a Time column model.
	 * <p>提示编译器将一个方法引用包装为Time类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Comparable<?>> TimeLambdaColumn<B,T> time(TimeLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a DateTime column model.
	 * <p>提示编译器将一个方法引用包装为DateTime类型的字段模型。
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Comparable<?>> DateTimeLambdaColumn<B,T> datetime(DateTimeLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * Hint the compiler to wrap a method reference as a DateTime column model. Alias of {@link #datetime(DateTimeLambdaColumn)}.
	 * <p>提示编译器将一个方法引用包装为DateTime类型的字段模型。这是{@link #datetime(DateTimeLambdaColumn)}的别名。
	 * @param <B> type of the entity bean.
	 * @param path the method reference / 方法引用
	 * @return column model / 字段模型
	 */
	default <B, T extends Comparable<?>> DateTimeLambdaColumn<B,T> dt(DateTimeLambdaColumn<B,T> path) {
		return path;
	}
	
	/**
	 * Get a table model object with the specified alias.
	 * <p>获得一个指定了别名的表模型对象
	 * @param <B> java type of the table.
	 * @param table lambdaTable
	 * @param variable table alias(variable)
	 * @return the table model with alias / 指定别名的表模型
	 */
	default <B> RelationalPathEx<B> forVariable(LambdaTable<B> table, String variable){
		return PathCache.getPath(table, variable);	
	}
	
}
