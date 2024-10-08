package com.github.xuse.querydsl.entity.partition;

import java.sql.Date;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QPartitionFoo2 extends RelationalPathBaseEx<PartitionFoo2> {
	private static final long serialVersionUID = -1972906214968601009L;

	public static final QPartitionFoo2 partitionFoo2 = new QPartitionFoo2("pf1");
	
	public final NumberPath<Integer> id = createNumber("id", int.class);

	public final StringPath code = createString("code");
	
	public final StringPath name = createString("name");
	
	public final DateTimePath<Date> created = createDateTime("created", Date.class);
	
	public final DateTimePath<Date> updated = createDateTime("updated", Date.class);
	
	public QPartitionFoo2(String variable) {
		super(PartitionFoo2.class, variable);
		scanClassMetadata();
	}
}
