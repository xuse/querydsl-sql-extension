package com.github.xuse.querydsl.sql.Integration;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.HashType;
import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.entity.partition.QPartitionFoo1;
import com.github.xuse.querydsl.entity.partition.QPartitionFoo1b;
import com.github.xuse.querydsl.entity.partition.QPartitionFoo3;
import com.github.xuse.querydsl.entity.partition.QPartitionFoo4;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTablePartitionOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.partitions.Partitions;
import com.github.xuse.querydsl.sql.partitions.RangePartitionBy;
import com.github.xuse.querydsl.util.DateFormats;
import com.querydsl.sql.SchemaAndTable;

public class TestPartitionsWithMock extends MockedTestBase{
	
	@Test
	public void test1() {
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		meta.dropTable(QTableDataTypes.aaa).ifExists(true).execute();
		meta.createTable(QTableDataTypes.aaa).ifExists().execute();
	}

	private void ensureDatabaseSupportsPartition() {
		Assumptions.assumeTrue(factory.getConfiguration().supports(PartitionDefineOps.PARTITION_BY),"Only for Database supports Partition");
	}
	
	@Test
	public void testAdjustPartitionsCount() {
		ensureDatabaseSupportsPartition();
		Assumptions.assumeTrue(factory.getConfiguration().supports(PartitionMethod.HASH),"Only for Database supports Partition");
		
		QPartitionFoo4 t4 = QPartitionFoo4.partitionFoo4;
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		List<PartitionInfo> list=metadata.getPartitions(t4.getSchemaAndTable());
		if(list.size()!=4) {
			metadata.dropTable(t4).execute();
			metadata.createTable(t4).execute();
			list=metadata.getPartitions(t4.getSchemaAndTable());
		}
		
		assertEquals(4, list.size());
		
		metadata.adjustPartitionSize(t4).toSize(3).execute();
		list=metadata.getPartitions(t4.getSchemaAndTable());
		
		assertEquals(3, list.size());
		
		metadata.adjustPartitionSize(t4).add(3).execute();
		list=metadata.getPartitions(t4.getSchemaAndTable());
		
		assertEquals(6, list.size());
		
		metadata.adjustPartitionSize(t4).coalesce(2).execute();
		list=metadata.getPartitions(t4.getSchemaAndTable());
		
		assertEquals(4, list.size());
	}
	
	
	@Test
	@Disabled
	public void createTables() {
		ensureDatabaseSupportsPartition();
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
//		metadata.createTable(QPartitionFoo1.partitionFoo1).reCreate().execute();
//		metadata.createTable(QPartitionFoo2.partitionFoo2).reCreate().execute();
		metadata.createTable(QPartitionFoo3.partitionFoo3).reCreate().execute();
//		metadata.createTable(QPartitionFoo4.partitionFoo4).reCreate().execute();
//		metadata.dropTable(QPartitionFoo5.partitionFoo5).execute();
//		metadata.createTable(QPartitionFoo5.partitionFoo5).reCreate().execute();
		List<PartitionInfo> partitions=metadata.getPartitions(QPartitionFoo3.partitionFoo3.getSchemaAndTable());
		System.out.println(partitions);
	}

	@Test
	public void testPartitionsAddSimple() {
		ensureDatabaseSupportsPartition();
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		QPartitionFoo1 t1=QPartitionFoo1.partitionFoo1;
		metadata.dropTable(t1).execute();
		
		boolean supports=factory.getConfiguration().supports(AlterTablePartitionOps.ADD_PARTITIONING);
		if(supports) {
			metadata.createTable(t1).partitions(false).execute();
			metadata.createPartitioning(t1).partitionBy(
					Partitions.byRangeColumns(t1.created)
					.add("p202401", "'2024-02-01'")
					.add("p202402", "'2024-03-01'")
					.build()).execute();
			metadata.addPartition(t1)
			.add("p202403","'2024-03-01'", "'2024-04-01'").execute();
			assertEquals(3,metadata.getPartitions(t1.getSchemaAndTable()).size());
		}else {
			metadata.createTable(t1).partitions(true).execute();
			metadata.addPartition(t1)
			.add("p202403","'2024-03-01'", "'2024-04-01'").execute();
			assertEquals(8,metadata.getPartitions(t1.getSchemaAndTable()).size());
		}
	}
	
	@Test
	public void testPartitionsAddSimple2() {
		ensureDatabaseSupportsPartition();
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		QPartitionFoo1b t1=QPartitionFoo1b.partitionFoo1b;
		metadata.dropTable(t1).execute();
		metadata.createTable(t1).partitions(true).execute();
		System.out.println(metadata.getPartitions(t1.getSchemaAndTable()));
	}
	
	/**
	 * 测试：创建一个分区表，该分区带有maxValue分区。
	 * 新增一个数年前的分区和一个未来的分区，由于时间段与已有分区重叠，
	 * 必须两个REORGANIZE PARTITION语句，才能成功创建。
	 */
	@Test
	public void testPartitionsReorganiztion() {
		ensureDatabaseSupportsPartition();
		Assumptions.assumeTrue(factory.getConfiguration().supports(AlterTablePartitionOps.REORGANIZE_PARTITION),"Only for Database supports Partition REORGANIZE_PARTITION");
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		QPartitionFoo1 t1=QPartitionFoo1.partitionFoo1;
		metadata.dropTable(t1).execute();
		metadata.createTable(t1).execute();
		
		Date futurePartition=new Date(System.currentTimeMillis()+TimeUnit.DAYS.toMillis(20));
		String pName="p"+DateFormats.DATE_SHORT.format(futurePartition);
		String pValue="'"+DateFormats.DATE_CS.format(futurePartition)+"'";
		metadata.addPartition(t1)
			.add("p20200101", "'2021-01-01'")
			.add(pName, pValue)
			.execute();
		
		RangePartitionBy partitionBy=(RangePartitionBy)t1.getPartitionBy();
		AutoTimePartitions rule=partitionBy.getAutoPartition()[0];
		int expectSize = rule.periodsEnd() - rule.periodsBegin() + 1 + (rule.createForMaxValue() ? 1 : 0);
		List<PartitionInfo> list=metadata.getPartitions(t1.getSchemaAndTable());
		
		//检查分区数量
		assertEquals(expectSize + 2, list.size());
		
		System.out.println(list.size());
		for(PartitionInfo p:list) {
			System.out.println(p);
		}
		//删除多余的分区 (刚才创建的两个)
		metadata.dropPartition(t1).partitionsOutOfTimeRange().execute();
		
		//检查分区数量是否一致
		assertEquals(expectSize, metadata.getPartitions(t1.getSchemaAndTable()).size());
	}
	
	@Test
	public void terPartitionsReorganiztionList() {
		ensureDatabaseSupportsPartition();
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		QPartitionFoo3 t1 = QPartitionFoo3.partitionFoo3;
		metadata.dropTable(t1).execute();
		metadata.createTable(t1).execute();
		
		List<PartitionInfo> list = metadata.getPartitions(t1.getSchemaAndTable());
		System.out.println(list.size());
		for(PartitionInfo p:list) {
			System.out.println(p);
		}
		assertEquals(4, list.size());
		
		boolean supportsReorganize = factory.getConfiguration().supports(AlterTablePartitionOps.REORGANIZE_PARTITION);
		if(supportsReorganize) {
			//对于支持分区重组织的DB (MySQL)，给一个涉及分区重组的测试数据。
			metadata.addPartition(t1)
			.add("p5", "'3','4','g'")
			.execute();	
		}else {
			//对于不支持分区重组织的DB，给一个简单测试数据。
			metadata.addPartition(t1)
			.add("p5", "'g','h','i'")
			.execute();
		}
		metadata.getPartitions(t1.getSchemaAndTable());
		list = metadata.getPartitions(t1.getSchemaAndTable());
		System.out.println(list.size());
		for(PartitionInfo p:list) {
			System.out.println(p);
		}
		assertEquals(5, list.size());
	}
	

	/**
	 * 从表上删除所有分区设置（数据不变）。然后重新创建另外一套分区规则
	 */
	@Test
	public void testRemoveAndRebuild() {
		ensureDatabaseSupportsPartition();
		Assumptions.assumeTrue(factory.getConfiguration().supports(AlterTablePartitionOps.REMOVE_PARTITIONING),"Only for Database supports Partition");
		
		QPartitionFoo1 t1 = QPartitionFoo1.partitionFoo1;
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		
		metadata.createTable(t1).reCreate().execute();
		
		if(!metadata.getPartitions(t1.getSchemaAndTable()).isEmpty()) {
			metadata.removePartitioning(t1).execute();
			System.err.println("All partitions removed.");			
		}
		List<PartitionInfo> list=metadata.getPartitions(t1.getSchemaAndTable());
		
		assertEquals(0, list.size());
		
		System.out.println(list.size());
		for(PartitionInfo p:list) {
			System.out.println(p);
		}
		
		metadata.createPartitioning(t1)
			.partitionBy(Partitions.byHash(HashType.HASH, "TO_DAYS(created)", 4))
			.execute();
		
		list=metadata.getPartitions(t1.getSchemaAndTable());
		assertEquals(4, list.size());
	}
	
	/**
	 *  从表上删除所有分区设置（数据不变）。然后修改主键字段，并用新的字段作为分区条件设置分区。
	 */
	@Test
	public void testAlterTableAddPrimaryKeyColumn() {
		ensureDatabaseSupportsPartition();
		Assumptions.assumeTrue(factory.getConfiguration().supports(AlterTablePartitionOps.REMOVE_PARTITIONING),"Only for Database supports Partition");
		
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		QPartitionFoo1 t1 = QPartitionFoo1.partitionFoo1;
		
		metadata.removePartitioning(t1).ignore().execute();
		
		assertEquals(0, metadata.getPartitions(t1.getSchemaAndTable()).size());
		
		metadata.refreshTable(t1).changePrimaryKey(t1.id,t1.code).execute();
		Constraint c=metadata.getPrimaryKey(t1.getSchemaAndTable());
		
		assertEquals(Arrays.asList("id","created"), c.getColumnNames());
		
		metadata.createPartitioning(t1).partitionBy(
				Partitions.byListColumns(t1.code)
				.add("p1", "'a','b','c'")
				.add("p2", "'d','e','f'")
				.build()
		).execute();
		assertEquals(2, metadata.getPartitions(t1.getSchemaAndTable()).size());
	}
	
	
	@Test
	public void testFetchPartitions() {
		ensureDatabaseSupportsPartition();
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		List<PartitionInfo> partitions=metadata.getPartitions(new SchemaAndTable(null, "s3"));
		for(PartitionInfo p:partitions) {
			System.out.println(p);
		}
	}
}
