package com.github.xuse.querydsl.sql.column;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.GeneratedType;
import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.types.Type;

/**
 * 描述一个数据库列 映射到java字段上的模型信息
 * 
 * @author Jiyi
 * 
 * @param <T> 该列在java中映射的数据类型
 * 
 * 
 * @Modify 2014-10-31 为了实现在重构中，内部对于Field对象的表示逐渐过渡为
 *         ColumnMapping对象，暂时先让ColumnMapping实现Field接口。
 */
public interface ColumnMapping {
	/**
	 * 得到默认未设置或修饰过的值
	 * 
	 * @return
	 */
	boolean isUnsavedValue(Object value);

	/**
	 * 是否为自动生成数值
	 * 
	 * @return
	 */
	AutoGenerated getGenerated();

	/**
	 * 该字段不参与插入
	 * 
	 * @return
	 */
	boolean isNotInsert();

	/**
	 * 该字段不参与更新
	 * 
	 * @return
	 */
	boolean isNotUpdate();

	/**
	 * java字段名
	 * 
	 * @return java字段名
	 */
	String fieldName();

	/**
	 * 返回该列在JDBC的数据库类型常量中定义的值。该值参见类{@link java.sql.Types}
	 * 
	 * @return JDBC数据类型
	 * @see java.sql.types
	 */
	int getSqlType();

	/**
	 * Is the column a promary key of table.
	 * 
	 * @return true is is promary key.
	 */
	boolean isPk();

	/**
	 * 获得Bean类型
	 * 
	 * @return
	 */
	Class<?> getType();
	
	
	/**
	 * 获得添加在字段上的注解
	 * @param <T>
	 * @param clz
	 * @return
	 */
	<T extends Annotation> T getAnnotation(Class<T> clz);
	
	
	/**
	 * 得到QueryDSL原生的ColumnMetadata
	 * @return
	 */
	ColumnMetadata get();
	
	
	/**
	 * 自定义映射类型
	 * @param type
	 * @return
	 */
	ColumnMapping withCustomType(Type<?> type);
	
	/**
	 * 返回自定义映射类型 
	 * @return
	 */
	Type<?> getCustomType();
	
	/**
	 * 设置自定义的UnsavedValue判断器
	 * @param unsavedValue
	 * @return this
	 */
	ColumnMapping withUnsavePredicate(Predicate<Object> unsavedValue);
	
	/**
	 * 设置自定义的UnsavedValue判断器
	 * @param expression 和 {@link UnsavedValue} 中 value的用法一样
	 * @return this
	 */
	ColumnMapping withUnsavePredicate(String expression);
	
	/**
	 * 为该列设置一个自动生成规则
	 * @param type
	 * @param overwite
	 * @param params
	 * @return this
	 */
	ColumnMapping withAutoGenerate(GeneratedType type,boolean overwite,String... params);
	
}
