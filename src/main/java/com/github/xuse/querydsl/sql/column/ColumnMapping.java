package com.github.xuse.querydsl.sql.column;

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
	boolean isGenerated();

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
	public Class<?> getType();
}
