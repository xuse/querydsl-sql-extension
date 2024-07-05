package com.github.xuse.querydsl.sql.partitions;

import java.lang.annotation.Annotation;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.Period;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutoTimePartitionImpl implements com.github.xuse.querydsl.annotation.partition.AutoTimePartitions{
	private Period period;
	private int pastPeriods;
	private int futurePeriods;
	private boolean maxValue;
	
	@Override
	public Class<? extends Annotation> annotationType() {
		return AutoTimePartitions.class;
	}

	@Override
	public Period unit() {
		return period;
	}

	@Override
	public int periodsBegin() {
		return pastPeriods;
	}

	@Override
	public int periodsEnd() {
		return futurePeriods;
	}

	@Override
	public boolean createForMaxValue() {
		return maxValue;
	}

}
