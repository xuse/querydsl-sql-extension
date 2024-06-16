package com.github.xuse.querydsl.sql.ddl;

import java.util.ArrayList;
import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
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
    
 * @author jiyi
 *
 */
@Slf4j
public class CreateTableQuery extends AbstractDDLClause<CreateTableQuery> {
	
	private CheckExists check = CheckExists.ABORT;
	
	public CreateTableQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, path);
	}

	@Override
	protected List<String> generateSQLs() {
		List<String> sqls=new ArrayList<>();
		//开始建
		SQLSerializerAlter serializer=new SQLSerializerAlter(configuration,true);
		List<Constraint> others= serializer.serializeTableCreate(table);
		{
			String sql=serializer.toString();
			//log.info(sql);
			sqls.add(sql);	
		}
		for(Constraint c:others) {
			if(configuration.getTemplates().supports(c.getConstraintType().getIndependentCreateOps())) {
				SQLSerializerAlter serializer2=new SQLSerializerAlter(configuration,true);
				serializer2.serialzeConstraintIndepentCreate(table,c);
				String sql=serializer2.toString();
				//log.info(sql);
				sqls.add(sql);	
			}else {
				log.warn("[CREATION IGNORED] The constraint {} is not supported on curent database.",c);
			}
		}
		return sqls;
	}

	@Override
	protected void finished(List<String> sqls) {
		log.info("Create table {} finished, {} sqls executed.",table.getSchemaAndTable(),sqls.size());
	}

	@Override
	protected String generateSQL() {
		//Never used.
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean preExecute(MetadataQuerySupport connection) {
		if(check!=CheckExists.NOT_CHECK) {
			SchemaAndTable actualTable=connection.asInCurrentSchema(table.getSchemaAndTable());
			TableInfo t = connection.getTable(connection.asInCurrentSchema(actualTable));
			if(t!=null) {
				switch(check) {
				case IGNORE:
					log.warn("The table {} exists. create query will be ignored.");
					return false;
				case ABORT:
					throw Exceptions.illegalState("The table {} has already exist.",actualTable);
				case DROP_CREATE:
					if(configuration.isAllowTableDropAndCreate()) {
						DropTableQuery d=new DropTableQuery(connection, configuration, this.table);
						d.execute();
					}else {
						throw Exceptions.illegalState("The table {} has already exist, and drop table action was disabled.",table.getSchemaAndTable());
					}
				case MERGE:
					throw Exceptions.unsupportedOperation("Merge table feature was not implemented now.");
				case NOT_CHECK:
				default:
					throw Exceptions.unsupportedOperation("Unknown check action:{}",check);
				}
			}
		}
		return true;
	}
}
