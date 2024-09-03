package com.github.xuse.querydsl.entity.partition;

import java.sql.Date;

import com.github.xuse.querydsl.annotation.partition.HashType;
import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.github.xuse.querydsl.sql.partitions.Partitions;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QPartitionFoo5 extends RelationalPathBaseEx<PartitionFoo5> {
	private static final long serialVersionUID = -1972906214968601009L;

	public static final QPartitionFoo5 partitionFoo5 = new QPartitionFoo5("pf5");
	
	public final NumberPath<Integer> id = createNumber("id", int.class);

	public final StringPath code = createString("code");
	
	public final StringPath name = createString("name");
	
	public final DateTimePath<Date> created = createDateTime("created", Date.class);
	
	public final DateTimePath<Date> updated = createDateTime("updated", Date.class);
	
	public QPartitionFoo5(String variable) {
		super(PartitionFoo5.class, variable);
		scanClassMetadata();
		
		//MySQL分区要求，主键中必须包含分区字段 
		createPrimaryKey(id,code);
		
		setPartitionBy(Partitions.byHash(HashType.KEY, code, 16));
		
//		setPartitionBy(Partitions.byListColumns(code)
//				.add("p1","'A','B','C'")
//				.add("p2", "'D','E','F'")
//				.add("p3", "'1','2','3','4'")
//				.build());
		
		
//		setPartitionBy(Partitions.byRangeColumns(created)
//				.autoParitionBy(Period.WEEK)
//				.autoRange(-1, 3)
//				.withMaxValuePartition().build());
	}
}
