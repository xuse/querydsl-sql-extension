package com.github.xuse.querydsl.sql.column;

import java.util.Collections;
import java.util.function.Predicate;

import com.github.xuse.querydsl.annotation.GeneratedType;
import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.util.Primitives;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.types.Type;

public abstract class ColumnBuilderBase<T, Q extends ColumnBuilderBase<T, Q>> {

	private final PathMapping path;

	protected Q q;

	public ColumnBuilderBase(PathMapping p) {
		this.path = p;
	}

	/**
	 *  如果是数值类型，支持无符号，如果不是会抛出异常
	 *  @return the current object
	 */
	public Q unsigned() {
		path.setUnsigned(true);
		return q;
	}

	/**
	 * 设置默认值。仅限Java数值和SQL的数值表达方式一致的场合。、
	 * 如果Java中是枚举，数据库中是INT的映射，这个方法是无法使用的。请使用{@link #defaultExpression(String)}
	 * @param value value
	 * @return the current object
	 */
	public Q defaultValue(T value) {
		if (value instanceof String || value instanceof Number || value instanceof Boolean) {
			path.setDefaultExpression(ConstantImpl.create(value));
		} else {
			// if user has set CustomType for this java type, do not supported for now.
			throw new UnsupportedOperationException("");
		}
		return q;
	}

	/**
	 * 以String的方式设置列默认值。例如字段类型为Timestamp等时间时，可以用字符串描述时间。
	 * 注意这个方法不宜用于设置INT, FLOAT等类型的缺省值
	 * @param value value
	 * @return the current object
	 */
	public Q defaultValueInString(String value) {
		path.setDefaultExpression(ConstantImpl.create(value));
		return q;
	}

	/**
	 * 设置缺省值表达式。注意表达式要完整
	 * @param template template
	 * @return the current object
	 */
	public Q defaultExpression(String template) {
		Template tt = TemplateFactory.DEFAULT.create(template);
		path.setDefaultExpression(Expressions.simpleTemplate(path.getType(), tt, Collections.emptyList()));
		return q;
	}

	/**
	 * 设置缺省值
	 * @param expression expression
	 * @return the current object
	 */
	public Q defaultExpression(Expression<T> expression) {
		path.setDefaultExpression(expression);
		return q;
	}

	/**
	 * 定义其他字段修饰
	 * @param features features
	 * @return the current object
	 */
	public Q with(ColumnFeature... features) {
		path.setFeatures(features);
		return q;
	}

	/**
	 * 配置字段注释
	 * @param comment comment
	 * @return the current object
	 */
	public Q comment(String comment) {
		path.setComment(comment);
		return q;
	}

	/**
	 * 设置自定义的UnsavedValue判断器
	 * @param unsavedValue unsavedValue
	 * @return the current object
	 */
	public Q withUnsavePredicate(Predicate<T> unsavedValue) {
		path.setUnsavedValue(o->check(o,unsavedValue));
		return q;
	}
	
	/**
	 * 设置自定义的UnsavedValue判断器
	 * @param unsavedValue unsavedValue
	 * @return the current object
	 */
	public Q setUnsavePredicate(Predicate<Object> unsavedValue) {
		path.setUnsavedValue(unsavedValue);
		return q;
	}
	
	
	private boolean check(Object o,Predicate<T> unsavedValue) {
		if(o==null) {
			return true;
		}
		@SuppressWarnings("unchecked")
		Class<T> clz=(Class<T>) Primitives.toWrapperClass(path.getType());
		if(clz.isInstance(o)) {
			return unsavedValue.test(clz.cast(o));
		}
		return false;
	}

	/**
	 *  插入时默认的populate方法对该字段无效
	 *  @return the current object
	 */
	public Q withoutInsertPopulate() {
		path.setNotInsert(true);
		return q;
	}

	/**
	 *  更新时默认的populate方法对该字段无效
	 *  @return the current object
	 */
	public Q withoutUpdatePopulate() {
		path.setNotUpdate(true);
		return q;
	}

	/**
	 *  设置自定义的UnsavedValue判断器
	 *  @param expression 和 {@link UnsavedValue} 中 value的用法一样
	 *  @return the current object
	 */
	public Q withUnsavePredicate(String expression) {
		path.setUnsavedValue(UnsavedValuePredicateFactory.create(path.getType(), expression));
		return q;
	}

	/**
	 * 自定义映射类型
	 * @param type type
	 * @return the current object
	 */
	public Q withCustomType(Type<?> type) {
		path.setCustomType(type);
		return q;
	}

	/**
	 * 为当前列制定一个自动生成策略
	 * @param type type
	 * @param overwrite overwite
	 * @param writeback writeback
	 * @param params params
	 * @return the current object
	 */
	public Q withAutoGenerate(GeneratedType type, boolean overwrite, boolean writeback, String... params) {
		path.setGenerated(new AutoGeneratedImpl(type, overwrite, writeback, params));
		return q;
	}

	/**
	 * 为当前列制定一个自动生成策略
	 * @param type type
	 * @param params params
	 * @return the current object
	 */
	public Q withAutoGenerate(GeneratedType type, String... params) {
		path.setGenerated(new AutoGeneratedImpl(type, false, false, params));
		return q;
	}
}
