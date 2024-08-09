package com.github.xuse.querydsl.entity.partition;

import java.sql.Date;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QPartitionFoo1b extends RelationalPathBaseEx<PartitionFoo1b> {
	private static final long serialVersionUID = -1972906214968601009L;

	public static final QPartitionFoo1b partitionFoo1b = new QPartitionFoo1b("pf1");
	
	public final NumberPath<Integer> id = createNumber("id", int.class);

	public final StringPath code = createString("code");
	
	public final StringPath name = createString("name");
	
	public final NumberPath<Integer> recordTime = createNumber("recordTime", int.class);
	
	public final DateTimePath<Date> created = createDateTime("created", Date.class);
	
	public final DateTimePath<Date> updated = createDateTime("updated", Date.class);
	
	public QPartitionFoo1b(String variable) {
		super(PartitionFoo1b.class, variable);
		scanClassMetadata();
	}
}
