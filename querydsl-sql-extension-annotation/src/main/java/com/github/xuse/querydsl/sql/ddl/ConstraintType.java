package com.github.xuse.querydsl.sql.ddl;

/**
 * The type of database constraints.
 * <p>
 * The default options are from Oracle's document. As
 * <ol>
 * <li>C: Check constraint on a table</li>
 * <li>F: Constraint that involves a REF column</li>
 * <li>H: Hash expression</li>
 * <li>O: With read only, on a view</li>
 * <li>P: Primary key</li>
 * <li>R: Referential integrity (aka foreign key)</li>
 * <li>S: Supplemental logging</li>
 * <li>U: Unique key</li>
 * <li>V: With check option, on a view</li>
 * </ol>
 * 
 * All Constraint has these parameters. {0}=TABLE Path {1}=Constraint name
 * {2}=Definition expression
 *
 */
public enum ConstraintType{
	/**
	 * SPATIAL索引
	 */
	SPATIAL(0),

	/**
	 * Equivalent to 'FULLTEXT INDEX'
	 */
	FULLTEXT(1), // full text全文索引
	
	/**
	 * Equivalent to 'INDEX'; Always use BTREE
	 */
	KEY(2), // 其实就是索引，MYSQL的默认行为
	
	/**
	 * Equivalent to 'INDEX USING HASH'
	 */
	HASH(3), // Hash expression.
	
	/**
	 * Equivalent to 'BITMAP INDEX' (Oracle supports index in this type.)
	 */
	BITMAP(4),
	
	/**
	 * Equivalent to 'UNIQUE INDEX'
	 * 本框架中UNIQUE按约束处理，其实现索引不视为用户管理的索引。
	 */
	UNIQUE(5),  //Unique Key，唯一索引或唯一约束，取决于RDBMS的实现方式
	
	/**
	 * PRIMARY KEY
	 */
	PRIMARY_KEY(6),  //Primary Key，主键
	
	/**
	 * Constraint CHECK (a rule on a column.) 
	 */
	CHECK(7),  //Check on a table，数值检查，比如非0，或一些规则 
	
	;

	public final int ord;

	ConstraintType(int order) {
		this.ord = order;
	}
}
