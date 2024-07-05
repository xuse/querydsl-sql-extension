package com.github.xuse.querydsl.sql.partitions;

import com.github.xuse.querydsl.annotation.partition.HashType;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.DDLExpressions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HashPartitionBy implements PartitionBy{
	private HashType type;
	
	private Expression<?> expr;
	
	private int count;

	public Expression<?> expr() {
		return expr;
	}

	public int count() {
		return count;
	}

	public Expression<?> define(ConfigurationEx configurationEx) {
		return DDLExpressions.simple(type.getMethod(), expr, DDLExpressions.text(String.valueOf(count)));
	}

	public HashType type() {
		return type;
	}
}
