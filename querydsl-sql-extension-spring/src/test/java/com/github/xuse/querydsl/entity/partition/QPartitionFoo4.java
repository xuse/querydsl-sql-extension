package com.github.xuse.querydsl.entity.partition;

import java.sql.Date;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QPartitionFoo4 extends RelationalPathBaseEx<PartitionFoo4> {
	private static final long serialVersionUID = -1972906214968601009L;

	public static final QPartitionFoo4 partitionFoo4 = new QPartitionFoo4("pf1");
	
	public final NumberPath<Integer> id = createNumber("id", int.class);

	public final StringPath code = createString("code");
	
	public final StringPath name = createString("name");
	
	public final DateTimePath<Date> created = createDateTime("created", Date.class);
	
	public final DateTimePath<Date> updated = createDateTime("updated", Date.class);
	
	public QPartitionFoo4(String variable) {
		super(PartitionFoo4.class, variable);
		scanClassMetadata();
	}
}
