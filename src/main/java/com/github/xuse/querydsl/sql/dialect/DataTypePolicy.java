package com.github.xuse.querydsl.sql.dialect;

import lombok.Getter;
import lombok.Setter;

/**
 * 在方言中使用。用于返回JDBC数据类型在当前数据库上的实现策略。
 * <p>
 * @implNote 
 * 重要字段 {@code #template} 决定了在数据库语句中的数据类型定义模板。比如 Oracle的字符串为 VARCHAR2($l).
 * <p>
 * 其次，JDBC数据类型在各种数据库上具体实现的规格有很大差异，有些被转换为别的数据类型，有些长度定义上发生变化。
 * 通过sqlType、size,digits来记录原始JDBC类型在当前数据库上被实现为什么类型。
 * </p>
 * <p>
 * 当系统对比JDBC模型与数据库表结构时，会通过上述映射来判断两个模型是否一致。因此需要正确的设置当前数据库映射后的模型类型，否则框架会判断认为数据库与JDBC模型不一致。
 * </p>
 * @author jiyi
 *
 */
@Getter
@Setter
public class DataTypePolicy {
	public static final int UNCHANGED = -2;
    public static final int UNDEFINED = -1;
    
    /**
     * 
     *  $l 列长度
     *  $p 列数值位数 NUMBER($p)
     *  $s 小数位数   NUMBER($s)
     */
	private String template;
	/**
	 * SQL type as the current database implements.
	 * <H3>for Example.</H3>
	 * <p>
	 *  
	 * A Boolean path is defined as sql type = java.sql.Types.Boolean, 
	 * 
	 * MYSQL using a data type BIT to store the boolean value.
	 * It will mapping to java.sql.Types.BIT.
	 *  
	 * Oracle using a NUMBER(1) to store the boolean value.
	 * It will mapping to java.sql.Types.DECIMAL. 
	 * </p>
	 * 
	 * here is the new jdbc type as java.sql.Types.BIT/java.sql.Types.DECIMAL against original type.
	 */
	private int sqlType;
	
	/**
	 * Here is the length type. eg. Oracle using NUMBER(1) to store boolean.
	 * in this case, size will be 1.
	 * <p>
	 * size = -2 means not change.
	 * 
	 */
	private int size = UNCHANGED;;
	
	/**
	 * digits = -2 means not change.
	 */
	private int digits = UNCHANGED;
	
	/**
	 * the database supports alias definition for this data type.
	 */
	private String[] alias;
	

	/**
	 * @param type java.sql.Types
	 * @param template 
	 */
	public DataTypePolicy(int type, String template) {
		this.sqlType = type;
		this.template = template;
	}

	public void setAlias(String... alias) {
		this.alias = alias;
	}
	
	/**
	 * Adjust the jdbc data type on current database.
	 * @param jdbcType
	 * @return this
	 */
	public DataTypePolicy type(int jdbcType) {
		if(jdbcType!=0) {
			this.sqlType=jdbcType;
		}
		return this;
	}
	
	/**
	 * Adjust the column data size on current database.
	 * @param size
	 * @return this
	 */
	public DataTypePolicy size(int size) {
		this.size=size;
		return this;
	}
	
	/**
	 * Adjust the column data digits on current database.
	 * @param digits
	 * @return this
	 */
	public DataTypePolicy digits(int digits) {
		this.digits=digits;
		return this;
	}
}
