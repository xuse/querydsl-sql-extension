package com.querydsl.core.types;

import com.querydsl.core.types.DDLOps.IndexConstraintOps;

/**
 * The type of database constraints.
 * @author jiyi
 * 
 * The default options are from Oracle's document. As
 * C: Check constraint on a table
F: Constraint that involves a REF column
H: Hash expression
O: With read only, on a view
P: Primary key
R: Referential integrity (aka foreign key)
S: Supplemental logging
U: Unique key
V: With check option, on a view


All Constraint has these parameters.
{0}=TABLE Path
{1}=Constraint name
{2}=Definition expression
 *
 */
public enum ConstraintType implements Operator{
	/**
	 * SPATIAL索引
	 */
	SPATIAL("A", "SPATIAL", ConstraintClassify.INDEX_COLUMNS, IndexConstraintOps.CREATE_SPATIAL,IndexConstraintOps.ALTER_TABLE_DROP_KEY),

	/**
	 * Equivalent to 'FULLTEXT INDEX'
	 */
	FULLTEXT("T", "FULLTEXT", ConstraintClassify.INDEX_COLUMNS, IndexConstraintOps.CREATE_FULLTEXT,IndexConstraintOps.ALTER_TABLE_DROP_KEY), // full text全文索引
	
	/**
	 * Equivalent to 'INDEX'; Always use BTREE
	 */
	KEY("K", "KEY", ConstraintClassify.INDEX_COLUMNS, IndexConstraintOps.CREATE_INDEX,IndexConstraintOps.ALTER_TABLE_DROP_KEY), // 其实就是索引，MYSQL的默认行为
	
	/**
	 * Equivalent to 'INDEX USING HASH'
	 */
	HASH("H", "HASH INDEX", ConstraintClassify.INDEX_COLUMNS,IndexConstraintOps.CREATE_HASH,IndexConstraintOps.ALTER_TABLE_DROP_KEY), // Hash expression.
	
	/**
	 * Equivalent to 'BITMAP INDEX'
	 */
	BITMAP("B","BITMAP INDEX", ConstraintClassify.INDEX_COLUMNS,IndexConstraintOps.CREATE_BITMAP,null),
	
	/**
	 * Equivalent to 'UNIQUE INDEX'
	 * 本框架中UNIQUE按约束处理，其实现索引不视为用户管理的索引。
	 */
	UNIQUE("U", "UNIQUE", ConstraintClassify.COLUMNS,IndexConstraintOps.CREATE_UNIQUE,IndexConstraintOps.ALTER_TABLE_DROP_UNIQUE),  //Unique Key，唯一索引或唯一约束，取决于RDBMS的实现方式
	
	/**
	 * PRIMARY KEY
	 */
	PRIMARY_KEY("P", "PRIMARY KEY", ConstraintClassify.COLUMNS, null,IndexConstraintOps.ALTER_TABLE_DROP_PRIMARYKEY),  //Primary Key，主键
	
	/**
	 * Constraint CHECK (a rule on a column.) 
	 */
	CHECK("C", "CHECK", ConstraintClassify.CHECK, null,IndexConstraintOps.ALTER_TABLE_DROP_CONSTRAINT),  //Check on a table，数值检查，比如非0，或一些规则 
	
	//Below are not supported yet.
	
	FOREIGN_KEY("R", "FOREIGN KEY", ConstraintClassify.REF, null,IndexConstraintOps.ALTER_TABLE_DROP_CONSTRAINT),  //Referential a Foreign Key
	
	REF("F", "REF", ConstraintClassify.REF, null,null), // Constraint that involves a REF column
	
	READ_ONLY("O", "READ ONLY", ConstraintClassify.IGNORE, null,null),  //Read Only on a view
	
	SUPPLEMENTAL("S", "SUPPLEMENTAL", ConstraintClassify.IGNORE, null,null), // Supplemental logging
	
	VIEW_CHECK("V", "VIEW CHECK", ConstraintClassify.IGNORE, null,null),  //Check Option on a view
	;
	
	final String typeName;
	final String typeFullName;
	
	/**
	 * 约束在数据库操作有两种方式，一种是在create table / alter table的定义语句中写入，一种是作为独立的SQL语句进行操作。两种场景下语法有轻微区别。
	 * 因此ConstraintType作为操作符时，都是指作为建表或修改表进行声明的语法，而CREATE_OPS的才是独立语句进行操作时的语法。
	 */
	final IndexConstraintOps independentCreateOps;
	
	final IndexConstraintOps dropOpsInAlterTable;
	
	/**
	 * true表示定义为columnList
	 * false表示取columns的第一个元素作为表达式处理
	 */
	final ConstraintClassify classify;
	
	private ConstraintType(String typeName, String typeFullName, ConstraintClassify columnList, IndexConstraintOps createOps, IndexConstraintOps alterDropOps){
		this.typeName = typeName;
		this.typeFullName = typeFullName;
		this.classify = columnList;
		this.independentCreateOps = createOps;
		this.dropOpsInAlterTable = alterDropOps;
	}
	
	public static ConstraintType parseName(String name){
		
		for (ConstraintType a : ConstraintType.values()) {  
            if (a.typeName.equalsIgnoreCase(name)) {  
                return a;  
            }  
        }  
		return null;
	}
	
	public static ConstraintType parseFullName(String name){
		
		for (ConstraintType a : ConstraintType.values()) {  
            if (a.typeFullName.equalsIgnoreCase(name)) {  
                return a;  
            }  
        }  
		return null;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getTypeFullName() {
		return typeFullName;
	}

	public IndexConstraintOps getIndependentCreateOps() {
		return independentCreateOps;
	}
	
	/**
	 * returns the operator of drop constraint.
	 * @return null if current index can not modify in an alter table clause.
	 */
	public IndexConstraintOps getDropOpsInAlterTable() {
		return dropOpsInAlterTable;
	}

	@Override
	public Class<?> getType() {
		return Object.class;
	}
	
	/**
	 * @return true 框架不处理这些约束
	 */
	public boolean isIgnored() {
		return classify == ConstraintClassify.IGNORE || classify == ConstraintClassify.REF;
	}
	
	/**
	 * @return true一般意义上的索引类型
	 */
	public boolean isIndex() {
		return classify == ConstraintClassify.INDEX_COLUMNS;
	}
	
	/**
	 * @return true由若干列定义构成
	 */
	public boolean isColumnList() {
		return classify == ConstraintClassify.COLUMNS || classify==ConstraintClassify.INDEX_COLUMNS;
	}
	
	public boolean isCheckClause() {
		return classify == ConstraintClassify.CHECK;
	}
}
