package com.github.xuse.querydsl.sql.ddl;

import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

/**
 * CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
    [(create_definition,...)]
    [table_options]
    [partition_options]
    [IGNORE | REPLACE]
    [AS] query_expression

  CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name
    { LIKE old_tbl_name | (LIKE old_tbl_name) }
    
 *
 */
@Slf4j
public class CreateTableQuery extends AbstractDDLClause<CreateTableQuery> {
	
	private CheckExists check = CheckExists.ABORT;
	
	private boolean processPartition = true;
	
	public CreateTableQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, RelationalPathExImpl.toRelationPathEx(path));
	}

	@Override
	protected List<String> generateSQLs() {
		DDLMetadataBuilder builder=new DDLMetadataBuilder(configuration, table, routing);
		builder.serializeTableCreate(processPartition);
		return builder.getSqls();
//		//开始建
//		{
//			SQLSerializerAlter serializer=new SQLSerializerAlter(configuration,true);
//			serializer.setRouting(routing);
//			others = serializer.serializeTableCreate(table, processPartition);
//			String sql=serializer.toString();
//			sqls.add(sql);	
//		}
//		for(Constraint c:others.getConstraints()) {
//			if(configuration.getTemplates().supports(c.getConstraintType().getIndependentCreateOps())) {
//				SQLSerializerAlter serializer=new SQLSerializerAlter(configuration,true);
//				serializer.serialzeConstraintIndepentCreate(table,c);
//				serializer.setRouting(routing);
//				String sql=serializer.toString();
//				//log.info(sql);
//				sqls.add(sql);	
//			}else {
//				log.warn("[CREATION IGNORED] The constraint {} is not supported on current database.",c);
//			}
//		}
//		for(Expression<?> comm:others.getComments()) {
//			//TODO
//		}
	}

	@Override
	protected int finished(List<String> sqls) {
		SchemaAndTable actual=this.table.getSchemaAndTable();
		if(routing!=null) {
			actual=routing.getOverride(actual, configuration); 
		}
		log.info("Create table {} finished, {} sqls executed.", actual, sqls.size());
		return super.finished(sqls);
	}

	@Override
	protected String generateSQL() {
		//Never used.
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 如果表已存在，那么什么也不做。
	 * @return this
	 */
	public CreateTableQuery ifExists() {
		this.check=CheckExists.IGNORE;
		return this;
	}
	
	/**
	 * 如果发现表已存在就删除重建，必须在全局开启允许删除表后才能生效，建议仅在单元测试等情况下使用。
	 * @return this
	 */
	public CreateTableQuery reCreate() {
		this.check=CheckExists.DROP_CREATE;
		return this;
	}
	
	/**
	 * 对于设置了分区策略的表，创建时是否同时创建分区。
	 * 默认true
	 * @param flag 是否创建分区
	 * @return this
	 */
	public CreateTableQuery partitions(boolean flag) {
		this.processPartition = flag;
		return this;
	}

	@Override
	protected boolean preExecute(MetadataQuerySupport connection) {
		if(check!=CheckExists.NOT_CHECK) {
			SchemaAndTable actualTable=connection.asInCurrentSchema(table.getSchemaAndTable());
			if(routing!=null) {
				actualTable=routing.getOverride(actualTable, configuration);
			}
			TableInfo t = connection.getTable(connection.asInCurrentSchema(actualTable));
			if(t!=null) {
				switch(check) {
				case IGNORE:
					log.warn("The table {} exists. create query will be ignored.",actualTable);
					return false;
				case ABORT:
					throw Exceptions.illegalState("The table {} has already exist.",actualTable);
				case DROP_CREATE:
					if(configuration.isAllowTableDropAndCreate()) {
						DropTableQuery d=new DropTableQuery(connection, configuration, this.table);
						d.execute();
					}else {
						throw Exceptions.illegalState("The table {} has already exist, and drop table action was disabled.",actualTable);
					}
					return true;
				case MERGE:
						AlterTableQuery q=new AlterTableQuery(connection, configuration, this.table);
						q.execute();
						return true;
				case NOT_CHECK:
				default:
					throw Exceptions.unsupportedOperation("Unknown check action:{}",check);
				}
			}
		}
		return true;
	}
}
