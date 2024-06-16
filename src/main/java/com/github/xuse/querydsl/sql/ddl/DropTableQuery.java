package com.github.xuse.querydsl.sql.ddl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DropTableQuery extends AbstractDDLClause<DropTableQuery>{
	
	private boolean ifExists = true;

	public DropTableQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, path);
	}

	@Override
	protected String generateSQL() {
		SQLSerializerAlter serializer=new SQLSerializerAlter(configuration,true);
		String ifExists=null;
		if(this.ifExists) {
			ifExists = configuration.getTemplates().getIfExists();
			//数据库不支持该语法。需要额外判断
			if(StringUtils.isEmpty(ifExists)) {
				SchemaAndTable actualTable=connection.asInCurrentSchema(table.getSchemaAndTable());
				TableInfo table=connection.getTable(actualTable);
				if(table==null) {
					log.warn("table {} wasn't exists. will not do drop action.",actualTable);
					return null;
				}
			}
		}
		serializer.serializeAction(table,"DROP TABLE ",ifExists);
		return serializer.toString();
	}
	
	public DropTableQuery ifExists(boolean flag) {
		ifExists = flag;
		return this;
	}

	@Override
	protected boolean preExecute(MetadataQuerySupport metadata) {
		return true;
	}
	
	@Override
	protected void finished(List<String> sqls) {
		log.info("Drop table {} finished, {} sqls executed.",table.getSchemaAndTable(),sqls.size());
	}
}
