package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.sql.Statement;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.Operator;
import com.querydsl.sql.SQLTemplates;

/**
 * DDL Options
 * 
 * @author Joey
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
	 * {0} 建表时前面一大串
	 * {1} 注释内容
	 */
	COMMENT_ON_COLUMN,

	/**
	 * 设置Table的注解
	 * 
	 * COMMENT = {0}
	 */
	COMMENT_ON_TABLE,

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
		 * ADD
		 */
		ALTER_TABLE_ADD,
		
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
		;

		@Override
		public Class<?> getType() {
			return Object.class;
		}
	}
	
	public enum AlterTableConstraintOps implements Operator{
		ALTER_TABLE_DROP_CONSTRAINT,	
		
		ALTER_TABLE_DROP_PRIMARYKEY,
		
		ALTER_TABLE_DROP_FOREIGNKEY,
		
		ALTER_TABLE_DROP_CHECK,
		
		ALTER_TABLE_DROP_UNIQUE,
		
		ALTER_TABLE_DROP_KEY,
		
		ALTER_TABLE_DROP_BITMAP,
		;
		
		@Override
		public Class<?> getType() {
			return Object.class;
		}
	}
	
	public enum AlterTablePartitionOps implements Operator{
		/**
		 * 增加一个分区
		 * ADD PARTITION {0:partition def}
		 */
		ADD_PARTITION,
		/**
		 * 删除分区，以及分区内的所有数据
		 * DROP PARTITION {0:names}
		 */
		DROP_PARTITION,
		
		/**
		 * 对于Hash类分区，收缩分区，会造成数据重新分布
		 * COALESCE PARTITION {0:count}
		 */
		COALESCE_PARTITION,
		
		/**
		 * ADD PARTITION PARTITIONS {0:count}
		 */
		ADD_PARTITION_COUNT,
		
		/**
		 * 去除表的分区设置，不影响数据
		 * REMOVE PARTITIONING
		 */
		REMOVE_PARTITIONING,
		TRUNCATE_PARTITION,
		EXCHANGE_PARTITION,
		
		/**
		 * REORGANIZE PARTITION {0} INTO {1}
		 */
		REORGANIZE_PARTITION,
		OPTMIZE_PARTITION,
		
		

		//以下是维护分区（包括迁移）等操作
		/**
		 * 这个操作是用于放弃分区文件，分区文件数据可以被IMPORT指令迁移到别的实例上，
		 * DISCARD PARTITION {0:names} TABLESPACE
		 */
		DISCARD_PARTITION,
		
		/**
		 * IMPORT PARTITION {0:names} TABLESPACE，用在迁移场景的
		 */
		IMPORT_PARTITION,
		/**
		 * 重建分区
		 */
		REBUILD_PARTITION,
		
		/**
		 * 修复分区
		 */
		REPAIR_PARTITION,
		
		/**
		 * 检查分区
		 */
		CHECK_PARTITION,
		
		/**
		 * 分析分区
		 */
		ANALYZE_PARTITION,
		;

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
	public enum DropStatement implements Statement{
		/**
		 * DROP TABLE {0} [{CASCADE}]
		 */
		DROP_TABLE,
		/**
		 * For independent SQL drop index...
		 * DROP INDEX {0}
		 */
		DROP_INDEX,
		
		DROP_DATABASE,
		DROP_FUNCTION,
		DROP_PROCEDURE,
		DROP_TABLESPACE,
		DROP_EVENT,
		DROP_TRIGGER,
		DROP_VIEW,
		;
		@Override
		public Class<?> getType() {
			return Void.class;
		}
	}
	public enum CreateStatement implements Statement{
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
	}

	public enum PartitionMethod implements Operator{
		/**
		 * HASH({0:expr}) PARTITIONS {1:count}
		 */
		HASH,
		
		/**
		 * LINEAR HASH({0}) PARTITIONS {1:count}
		 */
		LINEAR_HASH,
		
		/**
		 * 和hash的区别。hash的表达式必须为一个INT表达式，KEY可以是一个字符串表达式
		 * KEY({0:expr}) PARTITIONS {1:count}
		 */
		KEY,
		
		/**
		 * RANGE ({0:expr}) ({1:partitions})
		 */
		RANGE,
		
		/**
		 * RANGE COLUMNS({0:expr}) ({1:partitions})
		 */
		RANGE_COLUMNS,
		
		/**
		 * LIST ({0:expr}) ({1:partitions})
		 */
		LIST,
		
		/**
		 * LIST COLUMNS({0:expr}) ({1:partitions})
		 */
		LIST_COLUMNS,
		
		NOT_PARTITIONED
		;

		@Override
		public Class<?> getType() {
			return Object.class;
		}

		public static PartitionMethod parse(String text) {
			if(StringUtils.isEmpty(text)) {
				return NOT_PARTITIONED;
			}
			text = text.replace(' ', '_');
			return PartitionMethod.valueOf(text);
		}
	}
	
	public enum PartitionDefineOps implements Operator{
		/**
		 * PARTITION BY {0:DEFINE}
		 */
		PARTITION_BY,
		
		/**
		 * PARTITION {0:name} VALUES IN ({1:lists})
		 */
		PARTITION_IN_LIST,
		
		/**
		 * PARTITION {0:name} VALUE LESS THAN ({1:value})
		 */
		PARTITION_LESS_THAN
		
		;
		@Override
		public Class<?> getType() {
			return Object.class;
		}
		
	}
}
