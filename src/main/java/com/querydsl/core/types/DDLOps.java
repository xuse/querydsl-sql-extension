package com.querydsl.core.types;

import com.querydsl.sql.SQLTemplates;

/**
 * DDL Options
 * 
 * @author jiyi
 *
 */
public enum DDLOps implements Operator {
	/**
	 * ALTER TABLE {0:path} {1:actions}
	 */
	ALTER_TABLE,

	/**
	 * TRUNCATE TABLE {0}
	 */
	TRUNCATE_TABLE,

	/**
	 * DROP TABLE {0} [{CASCADE}]
	 */
	DROP_TABLE,

	/**
	 * {0}=Column_NAME(Path) {1}=DataType {2}=ColumnLevel Constraint
	 * 
	 * {0} {1} {2}
	 */
	COLUMN_SPEC,

	/**
	 * 专指COLUMN定义中的NULL。 如果NOT NULL, 不会由 OPS.NOT + OPS.NULL构成
	 */
	COLUMN_ALLOW_NULL,

	/**
	 * The definition of a data type. {0}=data type [UNSIGNED] {1}=NULL / NOT NULL
	 * {2}=DEFAULT exps
	 */
	DATA_TYPE,

	/**
	 * 数据类型中的无符号数值 {0} UNSIGNED
	 */
	UNSIGNED,

	/**
	 * default expression DEFAULT {0}
	 */
	DEFAULT,

	/**
	 * CHARSET={0} {1:charsetname}
	 * 
	 */
	CHARSET,

	/**
	 * COLLATE={0} {1:collation_name}
	 */
	COLLATE,

	/**
	 * {0} COMMENT {1}
	 */
	COMMENT,

	/**
	 * Two definitions separated by a space. {0} {1}
	 */
	DEF_LIST,

	/**
	 * AST: TABLE_DEFINITIONS( TABLE_DEFINITIONS()...))
	 * 
	 * Connect multiple table column definitions ({0},\n {1})
	 */
	TABLE_DEFINITIONS,;

	@Override
	public Class<?> getType() {
		return Object.class;
	}

	public enum Basic implements Operator {

		/**
		 * 无表查询。
		 * 这个本不属于DDL语法，但是不同数据库的无表查询差异较大，难以通过{@link SQLTemplates#getDummyTable()}完成。
		 * {@code select {0 - sql expression} from {1 - dummy table}}
		 * <p>
		 * for oracle, select {0} from {1}
		 * </p>
		 * <p>
		 * for derby and H2, values {0}
		 * </p>
		 */
		SELECT_VALUES,

		/**
		 * 对比两个时间是否一致，无视精度。
		 * 
		 * @implNote 在MYSQL 中 '12:00:00' != '12:00:00.000'
		 */
		TIME_EQ,
		;

		@Override
		public Class<?> getType() {
			return Object.class;
		}
	}

	public enum AlterTableOps implements Operator {
		/**
		 * ADD PARTITION {0:partition def}
		 */
		ADD_PARTITION,
		/**
		 * DROP PARTITION {0:names}
		 */
		DROP_PARTITION,

		/**
		 * DISCARD PARTITION {0:names}
		 */
		DISCARD_PARTITION,

		/**
		 * ADD COLUMN {0:COLUMN_SPEC}
		 */
		ADD_COLUMN,

		/**
		 * DROP COLUMN {0:path} [1:CASCADE | RESTRIC]
		 */
		DROP_COLUMN,

		/**
		 * CHANGE {0:old_path} {1:columnSpec}
		 * 
		 * [MySQL] CHANGE {0:path} {1:column name and definition}
		 */
		CHANGE_COLUMN,

		/**
		 * [Derby, once on action] ALTER COLUMN {0:path} {1:action} Alter column 是标准语法
		 * change [column]是Oracle扩展，MYSQL也兼容。 There should be another option to generate
		 * expressions in two formats for those various RDBMS.
		 */
		ALTER_COLUMN,

		/**
		 * RENAME COLUMN {0} TO {1}
		 */
		RENAME_COLUMN,

		/**
		 * RENAME KEY {0} TO {1}
		 */
		RENAME_KEY,

		/**
		 * 设置Table的注解
		 * 
		 * COMMENT = {0}
		 */
		COMMENT,;

		@Override
		public Class<?> getType() {
			return Object.class;
		}
	}
	public enum AlterColumnOps implements Operator{
		/**
		 * Derby: 
		 * SET DATA TYPE {0:data type}
		 * 
		 * PostgreSQL:
		 * TYPE {0:data type}
		 * 
		 * HSQLDB
		 * {0:data type}
		 */
		SET_DATATYPE,
		
		/**
		 * Derby
		 * SET NOT NULL
		 * 
		 * PostgreSQL
		 * NOT NULL
		 * 
		 * HSQLDB
		 * SET NOT NULL
		 */
		SET_NOTNULL,
		
		/**
		 * Derby
		 * DROP NOT NULL
		 * 
		 * PostgreSQL
		 * NULL
		 * 
		 * HSQLDB
		 * SET NULL 
		 */
		SET_NULL,
		
		/**
		 * Derby
		 * SET INCREMENT BY {0:number}
		 */
		SET_INCREMENT_BY,
		
		/**
		 * RESTART WITH {0:number}
		 */
		RESTART_WITH, 
		
		/**
		 * SET GENERATED {0:ALWAYS | BY DEFAULT}
		 */
		SET_GENERATED,

		/**
		 * SET DEFAULT {0:default expression}
		 */
		SET_DEFAULT,
		
		/**
		 * DROP DEFAULT
		 */
		DROP_DEFAULT,
		
		SET_COMMENT
		;
		
		@Override
		public Class<?> getType() {
			return Object.class;
		}
	}
	public enum IndexConstraintOps implements Operator{
		/**
		 * For independent SQL drop index...
		 * DROP INDEX {0}
		 */
		DOPR_INDEX,
		
		/**
		 * Index definition of creation.
		 */
		CREATE_INDEX,
		/**
		 * unique index definition of creation.
		 */
		CREATE_UNIQUE,
		
		/**
		 * hash index definition of creation
		 */
		CREATE_HASH,
		
		/**
		 * fulltext index definition of creation
		 */
		CREATE_FULLTEXT,
		
		/**
		 * SPATIAL index definition of creation
		 */
		CREATE_SPATIAL,
		
		/**
		 * BITMAP index definition of creation.
		 */
		CREATE_BITMAP,
		
		
		ALTER_TABLE_DROP_CONSTRAINT,	
		
		ALTER_TABLE_DROP_PRIMARYKEY,
		
		ALTER_TABLE_DROP_FOREIGNKEY,
		
		ALTER_TABLE_DROP_CHECK,
		
		ALTER_TABLE_DROP_UNIQUE,
		
		ALTER_TABLE_DROP_KEY,
		
		ALTER_TABLE_ADD,
		;

		@Override
		public Class<?> getType() {
			return Object.class;
		}

	}
}
