package com.github.xuse.querydsl.sql;

import java.sql.Types;
import java.util.Collection;

import org.junit.Test;

import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.entity.QAvsUserAuthority;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
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
		metadata.refreshTable(QAaa.aaa)
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
		System.err.println("==========================");
		Collection<Constraint> cs=metadata.getConstraints(t.getSchemaAndTable());
		System.err.println(cs.size());
		for(Constraint c:cs) {
			log.info("constraint:{}",c);
		}
		Collection<Constraint> is = metadata.getIndices(QAaa.aaa.getSchemaAndTable());
		System.out.println(is.size());
		for(Constraint c:is) {
			log.info("index:{}",c);
		}
		metadata.refreshTable(t)
			.dropColumns(true)
			.dropConstraint(true)
			.dropIndexes(true)
			.execute();
	}
	
	
	
	@Test
	public void testTableAlter() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		metadata.refreshTable(QAaa.aaa)
			.removeConstraintOrIndex("unq_${table}_name_version")
			.addColumn(
					ColumnMetadata.named("new_column").ofType(Types.VARCHAR).withSize(64).notNull(), String.class).defaultValue("").build()
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
}
