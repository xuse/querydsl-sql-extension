package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.DateUtils;
import com.querydsl.core.types.DDLOps.PartitionDefineOps;
import com.querydsl.core.types.DDLOps.PartitionMethod;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.DDLExpressions;

import lombok.Data;

@Data
public class RangePartitionBy implements PartitionAssigned {
	private boolean columns;
	private Expression<?> expr;
	
	private Partition[] partitions;
	private AutoTimePartitions[] autoPartition;

	public RangePartitionBy(boolean columns, Expression<?> expr, Partition[] partitions, AutoTimePartitions[] autoPartition) {
		this.columns = columns;
		this.expr = expr;
		if (partitions == null) {
			partitions = new Partition[0];
		}
		this.partitions = partitions;
		if (autoPartition == null) {
			autoPartition = new AutoTimePartitions[0];
		} else if (autoPartition.length > 1) {
			throw new IllegalArgumentException("Only one auto partition rule allowed.");
		}
		this.autoPartition = autoPartition;
	}
	

	public boolean columns() {
		return columns;
	}

	public Expression<?> expr() {
		return expr;
	}

	public Partition[] value() {
		return partitions;
	}

	public AutoTimePartitions[] auto() {
		return autoPartition;
	}

	public Expression<?> define(ConfigurationEx config) {
		PartitionMethod op=PartitionMethod.RANGE;
		if(columns) {
			op=PartitionMethod.RANGE_COLUMNS;
		}
		return DDLExpressions.simple(op, expr, partitions(config));
	}

	private Expression<?> partitions(ConfigurationEx config) {
		List<Expression<?>> partitions=new ArrayList<>(this.partitions.length);
		 List<Partition> partitionDefs;
		if (autoPartition != null && autoPartition.length > 0) {
			//配置了自动计算分区的，就不再使用手工配置了
			AutoTimePartitions auto=this.autoPartition[0];
			partitionDefs = generateAutoPartitions(auto);
		}else {
			if (this.partitions == null) {
				return DDLExpressions.empty();
			}
			partitionDefs=Arrays.asList(this.partitions);
		}
		for(Partition p: partitionDefs) {
			partitions.add(defineOne(p, config));
		}
		return DDLExpressions.wrapList(partitions);
	}

	public static List<Partition>  generateAutoPartitions(AutoTimePartitions auto) {
		List<Partition> partitions=new ArrayList<>();{
		Date date=new Date();
		switch(auto.unit()) {
		case DAY:
			for(Date d:DateUtils.dayIterator(DateUtils.adjustDate(date, 0, 0, auto.periodsBegin()), DateUtils.adjustDate(date, 0,0,auto.periodsEnd()))) {
				String name="p"+DateFormats.DATE_SHORT.format(d);
				String exp = "'" + DateFormats.DATE_CS.format(DateUtils.adjustDate(d, 0, 0, 1)) + "'";
				partitions.add(new PartitionDef(name,exp));
			}
			break;
		case MONTH:
			for(Date d:DateUtils.monthIterator(DateUtils.adjustDate(date, 0, auto.periodsBegin(), 0), DateUtils.adjustDate(date, 0,auto.periodsEnd(),0))) {
				String name="p"+DateFormats.YAER_MONTH.format(d);
				String exp = "'" + DateFormats.DATE_CS.format(DateUtils.adjustDate(d, 0, 1, 0)) + "'";
				partitions.add(new PartitionDef(name,exp));
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
				String exp = "'" + DateFormats.DATE_CS.format(DateUtils.adjust(d, TimeUnit.DAYS.toMillis(7))) + "'";
				partitions.add(new PartitionDef(name,exp));
			}
			break;
		case YEAR:
			int year=DateUtils.getYear(date);
			int max = year + auto.periodsEnd();
			for (int i = year + auto.periodsBegin(); i <= max; i++) {
				String name = "p" + i;
				String exp = "'" + (i + 1) + "-01-01'";
				partitions.add(new PartitionDef(name,exp));
			}
			break;
		}
		if(auto.createForMaxValue()) {
			partitions.add(new PartitionDef("pmax","MAXVALUE"));
		}
		return partitions;}
	}


	@Override
	public Expression<?> defineOne(Partition p, ConfigurationEx configurationEx) {
		return DDLExpressions.simple(PartitionDefineOps.PARTITION_LESS_THAN, DDLExpressions.text(p.name()),DDLExpressions.text(p.value()));
	}
}
