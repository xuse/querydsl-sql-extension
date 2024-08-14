package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.querydsl.core.types.Expression;

public class ListPartitionBy extends PartitionAssigned{
	
	ListPartitionBy(boolean isColumns,Expression<?> expr,Partition[] partitions) {
		super(isColumns, expr);
		this.partitions=partitions;
	}
	
	public ListPartitionBy(RelationalPathEx<?> table, String[] columns, String expr,Partition[] partitions) {
		super(table, columns, expr);
		this.partitions=partitions;
	}

	private final Partition[] partitions;


	public Expression<?> define(ConfigurationEx configurationEx) {
		Expression<?> expr = super.getExpr();
		PartitionMethod op = getMethod();
		List<Expression<?>>  partitions = partitions(configurationEx);
		return DDLExpressions.simple(op, expr, DDLExpressions.wrapList(partitions));
	}

	public List<Expression<?>> partitions(ConfigurationEx configurationEx) {
		if(partitions==null) {
			return Collections.emptyList();
		}
		List<Expression<?>> partitions=new ArrayList<>(this.partitions.length);
		for(Partition p:this.partitions) {
			partitions.add(defineOnePartition(p,configurationEx));
		}
		return partitions;
	}

	@Override
	public Expression<?> defineOnePartition(Partition p, ConfigurationEx configurationEx) {
		return DDLExpressions.simple(PartitionDefineOps.PARTITION_IN_LIST, DDLExpressions.text(p.name()), table, DDLExpressions.text(p.value()));
	}

	@Override
	public PartitionMethod getMethod() {
		return isColumns ? PartitionMethod.LIST_COLUMNS : PartitionMethod.LIST;
	}
}
