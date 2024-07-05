package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.HashType;
import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.annotation.partition.Period;
import com.github.xuse.querydsl.util.Assert;
import com.querydsl.core.types.DDLOps.PartitionMethod;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.DDLExpressions;
import com.querydsl.core.types.dsl.Expressions;

public class Partitions {

	/** 
	 * 创建分区策略 PARTITION BY LIST
	 * @param expr 分区表达式
	 * @return PartitionBuilder
	 */
	public static PartitionBuilder byList(String expr) {
		return new PartitionBuilder(PartitionMethod.LIST, DDLExpressions.text(expr));
	}
	
	/**
	 * 创建分区策略 PARTITION BY LIST
	 * @param expr 分区表达式
	 * @return PartitionBuilder
	 */
	public static PartitionBuilder byList(Expression<?> expr) {
		return new PartitionBuilder(PartitionMethod.LIST, expr);
	}

	/**
	 * 创建分区策略 PARTITION BY LIST COLUMN(...)
	 * @param expr 分区字段
	 * @return PartitionBuilder
	 */
	public static PartitionBuilder byListColumns(Path<?>... expr) {
		if(expr.length==0) {
			throw new IllegalArgumentException("Please input one path at least.");
		}
		Expression<?> expression;
		if(expr.length==1) {
			expression=expr[0];
		}else {
			expression=Expressions.list(expr);
		}
		return new PartitionBuilder(PartitionMethod.LIST_COLUMNS, expression);
	}

	/**
	 * 创建分区策略 PARTITION BY RANGE
	 * @param expr 分区表达式
	 * @return PartitionBuilder
	 */
	public static PartitionBuilder byRange(String expr) {
		return new PartitionBuilder(PartitionMethod.RANGE, DDLExpressions.text(expr));
	}

	/**
	 * 创建分区策略 PARTITION BY RANGE
	 * @param expr 分区表达式
	 * @return PartitionBuilder
	 */
	public static PartitionBuilder byRange(Expression<?> expr) {
		return new PartitionBuilder(PartitionMethod.RANGE, expr);
	}
	
	/**
	 * 创建分区策略 PARTITION BY RANGE COLUMN(...)
	 * @param expr 分区字段
	 * @return PartitionBuilder
	 */
	public static PartitionBuilder byRangeColumns(Path<?>... expr) {
		if(expr.length==0) {
			throw new IllegalArgumentException("Please input one path at least.");
		}
		Expression<?> expression;
		if(expr.length==1) {
			expression=expr[0];
		}else {
			expression=Expressions.list(expr);
		}
		return new PartitionBuilder(PartitionMethod.RANGE_COLUMNS, expression);
	}

	/**
	 * 创建分区策略 PARTITION BY HASH/KEY(...)
	 * @param expr 分区字段
	 * @return 分区策略
	 */
	public static PartitionBy byHash(HashType type, Path<?> path, int count) {
		PartitionBy p = new HashPartitionBy(type, path, count);
		return p;
	}
	
	/**
	 * 创建分区策略 PARTITION BY HASH/KEY(...)
	 * @param expr 分区字段
	 * @return 分区策略
	 */
	public static PartitionBy byHash(HashType type, String expr, int count) {
		PartitionBy p = new HashPartitionBy(type, DDLExpressions.text(expr), count);
		return p;
	}

	/**
	 * 分区策略构造器
	 */
	public static class PartitionBuilder {
		/**
		 * 分区类型
		 */
		final PartitionMethod method;
		/**
		 * 分区表达式
		 */
		final Expression<?> expr;
		
		/**
		 * 分区信息
		 */
		Map<String, String> partitions = new LinkedHashMap<>();
		
		private Period autoPartitionPeriod;
		private int autoPartitionBegin = 0;
		private int autoPartitionEnd = 10;
		private boolean withMaxValuePartition;

		PartitionBuilder(PartitionMethod method, Expression<?> expr) {
			Assert.notNull(method);
			this.method = method;
			this.expr = expr;
		}

		/**
		 * 添加一个分区定义
		 * @param name 名称
		 * @param expression 分区条件
		 * @return PartitionBuilder
		 */
		public PartitionBuilder add(String name, String expression) {
			partitions.put(name, expression);
			return this;
		}

		/**
		 * 按时间字段进行分区的策略
		 * @param period 确认是按天/周/月/年进行分区
		 * @return PartitionBuilder
		 */
		public PartitionBuilder autoParitionBy(Period period) {
			this.autoPartitionPeriod = period;
			return this;
		}

		/**
		 * 按时间字段进行分区，自动创建的时间范围。
		 * @param before 自动创建n时间单位之后的分区
		 * @param after 自动创建n时间单位之后的分区
		 * @return PartitionBuilder
		 */
		public PartitionBuilder autoRange(int before, int after) {
			this.autoPartitionBegin = before;
			this.autoPartitionEnd = after;
			return this;
		}

		/**
		 * 要求创建MAXVALUE的分区
		 * @return PartitionBuilder
		 */
		public PartitionBuilder withMaxValuePartition() {
			this.withMaxValuePartition = true;
			return this;
		}

		public PartitionBy build() {
			List<PartitionDef> partitions = new ArrayList<>();
			for (Map.Entry<String, String> entry : this.partitions.entrySet()) {
				partitions.add(new PartitionDef(entry.getKey(), entry.getValue()));
			}
			Partition[] pArray = partitions.toArray(new Partition[partitions.size()]);
			switch (method) {
			case LIST:
				return new ListPartitionBy(false, expr, pArray);
			case LIST_COLUMNS:
				return new ListPartitionBy(true, expr, pArray);
			case RANGE:
				return new RangePartitionBy(false, expr, pArray, auto());
			case RANGE_COLUMNS:
				return new RangePartitionBy(true, expr, pArray, auto());
			default:
				throw new IllegalStateException();
			}
		}

		private AutoTimePartitions[] auto() {
			if (autoPartitionPeriod == null) {
				return new AutoTimePartitions[0];
			}
			AutoTimePartitions a = new AutoTimePartitionImpl(autoPartitionPeriod, autoPartitionBegin,
					autoPartitionEnd, withMaxValuePartition);
			return new AutoTimePartitions[] { a };
		}
	}

}
