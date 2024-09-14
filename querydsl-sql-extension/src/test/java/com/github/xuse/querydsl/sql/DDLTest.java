package com.github.xuse.querydsl.sql;

import static org.junit.Assert.assertEquals;

import java.sql.Types;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.entity.QAvsUserAuthority;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.sql.column.PathMapping;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dialect.DbType;
import com.querydsl.sql.ColumnMetadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DDLTest extends AbstractTestBase {
	private QAaa t1 = QAaa.aaa;
	
	@Test
	public void reCreateTables() {
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		meta.dropTable(QAaa.aaa).ifExists(true).execute();
		meta.createTable(QAaa.aaa).execute();
	}
	
	@Test
	public void testDropConstraints() {
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		//准备数据
		int count=meta.refreshTable(t1)
		.createIndex("idx_a1", t1.gender)
		.createIndex("idx_a2", t1.dataBigint,t1.taskStatus)
		.createIndex("idx_a3", t1.dataInt,t1.name)
		.execute();
		System.err.println(count);
		
		count=meta.dropConstraintOrIndex(t1).dropAll().execute();
		System.err.println(count);
	}
	
	@Test
	public void testTruncateTable() {
		QAaa t=QAaa.aaa;
		SQLMetadataQueryFactory metaFactory = factory.getMetadataFactory();
		metaFactory.truncate(t).execute();
	}
	
	@Test
	public void testDropCreate() {
		QCaAsset t=QCaAsset.caAsset;
		SQLMetadataQueryFactory metaFactory = factory.getMetadataFactory();
		metaFactory.dropTable(t).ifExists(true).execute();
		metaFactory.createTable(t).execute();
	}
	
	
	@Test
	public void testGetIndexConstraint() {
		SQLMetadataQueryFactory metaFactory = factory.getMetadataFactory();
		
		Collection<ColumnDef> columns=metaFactory.getColumns(QAaa.aaa.getSchemaAndTable());
		for(ColumnDef c:columns) {
			System.out.println(c);
		}
		
		Collection<Constraint> cs = metaFactory.getConstraints(QAaa.aaa.getSchemaAndTable());
		for(Constraint c:cs) {
			log.info("constraint:{}",c);
		}
		Collection<Constraint> is = metaFactory.getIndices(QAaa.aaa.getSchemaAndTable());
		for(Constraint c:is) {
			log.info("index:{}",c);
		}
	}
	
	
	@Test
	public void testTableRefresh() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		RelationalPathExImpl<Aaa> table=QAaa.aaa.clone();
		//修改列备注
		PathMapping pathex = (PathMapping) table.getColumnMetadata(table.getColumn("version"));
		pathex.setComment("新的版本列注解");
		metadata.refreshTable(table)
			.dropColumns(true)
			.dropConstraint(true)
			.dropIndexes(true)
			.simulate(false)
			.execute();
	}
	
	@Test
	public void testTableRefresh2() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		QAvsUserAuthority t=QAvsUserAuthority.avsUserAuthority;
		
		//读取查看现有约束
		Collection<Constraint> cs=metadata.getConstraints(t.getSchemaAndTable());
		assertEquals(1,cs.size());
		for(Constraint c:cs) {
			log.info("constraint:{}",c);
		}
		//读取查看现有索引
		Collection<Constraint> is = metadata.getIndices(QAaa.aaa.getSchemaAndTable());
		for(Constraint c:is) {
			log.info("index:{}",c);
		}
		assertEquals(1,cs.size());
		
		//更新表
		metadata.refreshTable(t)
			.removeColumn(t.gender)
			.removeColumn("authType")
			.dropColumns(true)
			.dropConstraint(true)
			.dropIndexes(true)
			.execute();
		assertEquals(9,metadata.getColumns(t.getSchemaAndTable()).size());
		
		//再次更新表，把字段加回去
		metadata.refreshTable(t).execute();
		assertEquals(11,metadata.getColumns(t.getSchemaAndTable()).size());
	}
	
	
	
	@Test
	public void testTableAlter() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
//		metadata.dropTable(QAaa.aaa).execute();
//		metadata.createTable(QAaa.aaa).execute();
		metadata.refreshTable(QAaa.aaa)
			.removeConstraintOrIndex("unq_${table}_name_version")
			.addColumn(
					ColumnMetadata.named("new_column").ofType(Types.VARCHAR).withSize(64).notNull(), String.class).defaultValue("").build()
			.dropColumns(true)
			.dropConstraint(true)
			.execute();
	}
	
	@Test
	public void testCreateIndex() {
		QCaAsset t=QCaAsset.caAsset;
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		metadata.refreshTable(QAaa.aaa)
			.createIndex("idx_foo_gender", t.gender)
			.execute();
	}
	
	@Test
	public void testFetchInfo() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		
		List<TableInfo> infos=metadata.getTables("test","public");
		System.err.println(infos.size());
		for(TableInfo t:infos) {
			System.err.println(t);
		}
	}
	
	@Test
	public void testGetIndex() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		metadata.dropTable(QAaa.aaa).execute();
		metadata.createTable(QAaa.aaa).execute();
		
		Collection<Constraint> list;
		for(Constraint index: list = metadata.getIndices(QAaa.aaa.getSchemaAndTable())) {
			System.err.println(index);
		};
		assertEquals(1, list.size());
		for(Constraint index:list = metadata.getConstraints(QAaa.aaa.getSchemaAndTable())) {
			System.out.println(index);
		};
		if(metadata.getDatabaseInfo().getDbType()==DbType.mysql) {
			//目前用MySQL 5.x测试，Check在加入表后实际无效。所以只有2
			assertEquals(2, list.size());	
		}else {
			assertEquals(4, list.size());
		}
	}
}
