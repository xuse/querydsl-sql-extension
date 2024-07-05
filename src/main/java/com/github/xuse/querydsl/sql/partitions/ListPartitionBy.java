package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.List;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.types.DDLOps.PartitionDefineOps;
import com.querydsl.core.types.DDLOps.PartitionMethod;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.DDLExpressions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListPartitionBy implements PartitionAssigned{
	private boolean columns;
	private Expression<?> expr;
	private Partition[] partitions;


	public Expression<?> define(ConfigurationEx configurationEx) {
		PartitionMethod op=PartitionMethod.LIST;
		if(columns) {
			op=PartitionMethod.LIST_COLUMNS;
		}
		return DDLExpressions.simple(op, expr, partitions(configurationEx));
	}

	private Expression<?> partitions(ConfigurationEx configurationEx) {
		if(partitions==null) {
			return DDLExpressions.empty();
		}
		List<Expression<?>> partitions=new ArrayList<>(this.partitions.length);
		for(Partition p:this.partitions) {
			partitions.add(defineOne(p,configurationEx));
		}
		return DDLExpressions.wrapList(partitions);
	}

	@Override
	public Expression<?> defineOne(Partition p, ConfigurationEx configurationEx) {
		return DDLExpressions.simple(PartitionDefineOps.PARTITION_IN_LIST, DDLExpressions.text(p.name()),DDLExpressions.text(p.value()));
	}
}
