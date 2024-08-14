package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.ColumnFormat;
import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.DateUtils;
import com.querydsl.core.types.Expression;

import lombok.Getter;

@Getter
public class RangePartitionBy extends PartitionAssigned {
	private final Partition[] partitions;
	private AutoTimePartitions[] autoPartition;
	
	RangePartitionBy(boolean isColumn, Expression<?> expr, Partition[] partitions, AutoTimePartitions[] autoPartition) {
		super(isColumn,expr);
		this.partitions = partitions;
		setAutoPartition(autoPartition);
	}

	public RangePartitionBy(RelationalPathEx<?> table,String[] columns, String expr, Partition[] partitions, AutoTimePartitions[] autoPartition) {
		super(table, columns, expr);
		if (partitions == null) {
			partitions = new Partition[0];
		}
		this.partitions = partitions;
		setAutoPartition(autoPartition);
	}
	
	private void setAutoPartition(AutoTimePartitions[] autoPartition) {
		if (autoPartition == null) {
			autoPartition = new AutoTimePartitions[0];
		} else if (autoPartition.length > 1) {
			throw new IllegalArgumentException("Only one auto partition rule allowed.");
		}
		this.autoPartition = autoPartition;
	}

	public Partition[] value() {
		return partitions;
	}

	public AutoTimePartitions[] auto() {
		return autoPartition;
	}

	public Expression<?> define(ConfigurationEx config) {
		PartitionMethod op = getMethod();
		List<Expression<?>> partitions=partitions(config);
		return DDLExpressions.simple(op, getExpr(), DDLExpressions.wrapList(partitions));
	}

	public List<Expression<?>> partitions(ConfigurationEx config) {
		List<Expression<?>> partitions=new ArrayList<>(this.partitions.length);
		 List<Partition> partitionDefs;
		if (autoPartition != null && autoPartition.length > 0) {
			//配置了自动计算分区的，就不再使用手工配置了
			AutoTimePartitions auto=this.autoPartition[0];
			partitionDefs = generateAutoPartitions(auto);
		}else {
			if (this.partitions == null) {
				return Collections.emptyList();
			}
			partitionDefs=Arrays.asList(this.partitions);
		}
		for(Partition p: partitionDefs) {
			partitions.add(defineOnePartition(p, config));
		}
		return partitions;
	}

	public static List<Partition>  generateAutoPartitions(AutoTimePartitions auto) {
		List<Partition> partitions=new ArrayList<>();{
		Date date=new Date();
		ColumnFormat format=auto.columnFormat();
		switch(auto.unit()) {
		case DAY:
			for(Date d:DateUtils.dayIterator(DateUtils.adjustDate(date, 0, 0, auto.periodsBegin()), DateUtils.adjustDate(date, 0,0,auto.periodsEnd()))) {
				String name="p"+DateFormats.DATE_SHORT.format(d);
				Date begin=d;
				Date cutOffPoint = DateUtils.adjustDate(d, 0, 0, 1);
				partitions.add(new PartitionDef(name, format.generateExpression(begin), format.generateExpression(cutOffPoint)));
			}
			break;
		case MONTH:
			for(Date d:DateUtils.monthIterator(DateUtils.adjustDate(date, 0, auto.periodsBegin(), 0), DateUtils.adjustDate(date, 0,auto.periodsEnd(),0))) {
				String name="p"+DateFormats.YEAR_MONTH.format(d);
				Date begin = d;
				Date cutOffPoint = DateUtils.adjustDate(d, 0, 1, 0);
				partitions.add(new PartitionDef(name, format.generateExpression(begin), format.generateExpression(cutOffPoint)));
			}
			break;
		case WEEK:
			int weekDay=DateUtils.getWeekDay(date);
			date=DateUtils.adjustDate(date, 0,0,-weekDay);
			date=DateUtils.truncateToDay(date);//得到本周的开始时间。
			for (int i = auto.periodsBegin(); i <= auto.periodsEnd(); i++) {
				Date d=DateUtils.adjustDate(date, 0,0,i*7);
				int year=DateUtils.getYear(d);
				int weekNum=DateUtils.getWeekOfYear(d);
				String name = "p" + year + "w" + weekNum;
				Date begin = d; 
				Date cutOffPoint = DateUtils.adjust(d, TimeUnit.DAYS.toMillis(7));
				partitions.add(new PartitionDef(name, format.generateExpression(begin), format.generateExpression(cutOffPoint)));
			}
			break;
		case YEAR:
			int year=DateUtils.getYear(date);
			int max = year + auto.periodsEnd();
			for (int i = year + auto.periodsBegin(); i <= max; i++) {
				String name = "p" + i;
				Date begin = DateUtils.get(i, 1, 1);
				Date cutOffPoint = DateUtils.get(i + 1, 1, 1);
				partitions.add(new PartitionDef(name, format.generateExpression(begin), format.generateExpression(cutOffPoint)));
			}
			break;
		}
		if(auto.createForMaxValue()) {
			partitions.add(new PartitionDef("pmax","","MAXVALUE"));
		}
		return partitions;}
	}

	@Override
	public Expression<?> defineOnePartition(Partition p, ConfigurationEx configurationEx) {
		if(configurationEx.getTemplates().supports(PartitionDefineOps.PARTITION_FROM_TO)) {
			return DDLExpressions.simple(PartitionDefineOps.PARTITION_FROM_TO, DDLExpressions.text(p.name()), table, DDLExpressions.text(p.from()), DDLExpressions.text(p.value()));
		}else {
			return DDLExpressions.simple(PartitionDefineOps.PARTITION_LESS_THAN, DDLExpressions.text(p.name()), table, DDLExpressions.text(p.value()));	
		}
		
	}

	@Override
	public PartitionMethod getMethod() {
		return isColumns ? PartitionMethod.RANGE_COLUMNS : PartitionMethod.RANGE;
	}
}
