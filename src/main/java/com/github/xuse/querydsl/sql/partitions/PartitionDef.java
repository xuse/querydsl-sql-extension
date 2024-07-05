package com.github.xuse.querydsl.sql.partitions;

import java.lang.annotation.Annotation;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PartitionDef implements com.github.xuse.querydsl.annotation.partition.Partition{
	private String name;
	
	private String value;

	@Override
	public Class<? extends Annotation> annotationType() {
		return com.github.xuse.querydsl.annotation.partition.Partition.class;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String value() {
		return value;
	}

}
