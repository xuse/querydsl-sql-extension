package com.github.xuse.querydsl.spring;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.xuse.querydsl.spring.entity.QTestEntity;
import com.github.xuse.querydsl.spring.entity.TestEntity;
import com.github.xuse.querydsl.spring.enums.Status;
import com.github.xuse.querydsl.spring.repo.TestEntityRpository;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ContextConfiguration(classes = SpringConfiguration.class)
public class SpringProviderTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Resource
	private SQLQueryFactory factory;
	
	@Resource
	private TestEntityRpository repository;

	@PostConstruct
	public void initTables() throws SQLException {
	}
	
	@Test
	public void testAutoInit() {
	}
	
	
	@Test
	public void reCreateTables() {
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		meta.dropTable(()->TestEntity.class).ifExists(true).execute();
		meta.createTable(()->TestEntity.class).execute();
	}

	@Test
	public void testRepository() {
		TestEntity a = new TestEntity();
		a.setName("刘备");
		a.setStatus(Status.INIT);
		repository.insert(a);
		
		QTestEntity t=QTestEntity.testEntity;
		List<TestEntity> list=repository.query().where(t.name.startsWith("张")).fetch();
		for(TestEntity TestEntity:list) {
			System.out.println(TestEntity);
		}
		int count=repository.delete(7L);
		System.out.println(count);
	}
	
	@Test
	public void test1() {
		factory.getMetadataFactory().truncate(QTestEntity.testEntity).execute();
		System.out.println("================test1");
		TestEntity a = new TestEntity();
		a.setName("张三");
		a.setCreated(new Date(System.currentTimeMillis()));
		int id=factory.insert(QTestEntity.testEntity).populate(a).executeWithKey(Integer.class);
		a.setId(id);
		System.out.println(a.getId());

		TestEntity b = new TestEntity();
		b.setName("李四");
		b.setVersion(2);
		
		TestEntity c = new TestEntity();
		b.setName("王五");
		b.setVersion(1);
		factory.insert(QTestEntity.testEntity).populate(b).addBatch().populate(c).addBatch()
		.batchToBulk(true).execute();
		
		factory.getMetadataFactory().truncate(QTestEntity.testEntity).execute();
	}

	@Test
	@Transactional(propagation = Propagation.NEVER)
	public void testTableRefresh() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		
		Collection<Constraint> cs = metadata.getConstraints(QTestEntity.testEntity.getSchemaAndTable());
		for(Constraint c:cs) {
			log.info("constraint:{}",c);
		}
		Collection<Constraint> is = metadata.getIndices(QTestEntity.testEntity.getSchemaAndTable());
		for(Constraint c:is) {
			log.info("index:{}",c);
		}
		
//		QTestEntity t=QTestEntity.TestEntity;
//		SQLMetadataQueryFactory metaFactory = factory.getMetadataFactory();
//		metaFactory.truncate(t).execute();
//		
//		
//		System.out.println("======== testTableRefresh() Columns ==========");
//		Collection<ColumnDef> columns=metaFactory.getColumns(QTestEntity.TestEntity.getSchemaAndTable());
//		for(ColumnDef c:columns) {
//			System.out.println(ToStringBuilder.reflectionToString(c));
//		}
//		
//		System.out.println("======== testTableRefresh() Indexes ==========");
//		Collection<Constraint> constraints=metaFactory.getIndexes(QTestEntity.TestEntity.getSchemaAndTable());
//		for(Constraint c:constraints) {
//			System.out.println(c);
//		}
//		System.out.println("======== testTableRefresh() Constraints ==========");
//		constraints=metaFactory.getConstraints(QTestEntity.TestEntity.getSchemaAndTable());
//		for(Constraint c:constraints) {
//			System.out.println(c);
//		}
		
		
		metadata.refreshTable(QTestEntity.testEntity)
			.dropColumns(true)
			.dropConstraint(true)
			.dropIndexes(true)
			.simulate(false)
			.execute();
		
		
	}
}
