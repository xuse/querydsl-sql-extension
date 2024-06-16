package com.github.xuse.querydsl.sql.ddl;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

public class DropIndexQuery extends AbstractDDLClause<DropIndexQuery>{

	private boolean ifExists = true;
	
	private MetadataQuerySupport metadata;
	
	private final Constraint index = new Constraint();
	
	public DropIndexQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, path);
	}

	@Override
	protected String generateSQL() {
		String ifExists = "";
		if(this.ifExists) {
			ifExists = configuration.getTemplates().getIfExists();
			//数据库不支持该语法。需要额外判断
			if(StringUtils.isEmpty(ifExists)) {
				SchemaAndTable actualTable = metadata.asInCurrentSchema(table.getSchemaAndTable());
				List<Constraint> index=metadata.getIndexes(actualTable, MetadataQuerySupport.INDEX_POILCY_ALL_INDEX);
				Constraint exist = null;
				for(Constraint i:index) {
					if(Objects.equals(i.getName(), this.index.getName())){
						exist = i;
						break;
					}
				}
				if(exist!=null) {
					return null;
				}
			}
		}
		SQLSerializerAlter serializer=new SQLSerializerAlter(configuration,false);
		serializer.serializePath(Expressions.path(Object.class, table, index.getName()),"DROP INDEX ",ifExists);
		return serializer.toString();
	}

	@Override
	protected boolean preExecute(MetadataQuerySupport metadata) {
		this.metadata=metadata;
		return true;
	}
	
	
	public DropIndexQuery name(String name) {
		index.setName(name);
		return this;
	};
}
