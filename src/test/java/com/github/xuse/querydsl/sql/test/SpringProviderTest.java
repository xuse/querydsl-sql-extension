package com.github.xuse.querydsl.sql.test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.test.beans.AaaRpository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ContextConfiguration(classes = SpringConfiguration.class)
public class SpringProviderTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Resource
	private SQLQueryFactory factory;
	
	@Resource
	private AaaRpository repository;

	@PostConstruct
	public void initTables() throws SQLException {
	}
	
	@Test
	public void testAutoInit() {
	}
	
	
	@Test
	public void reCreateTables() {
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		meta.dropTable(QAaa.aaa).ifExists(true).execute();
		meta.createTable(QAaa.aaa).execute();
	}

	@Test
	public void testRepository() {
		Aaa a = new Aaa();
		a.setName("刘备");
		a.setDataBool(false);
		a.setDataFloat(2f);
		a.setDataDouble(3d);
		a.setTaskStatus(TaskStatus.INIT);
		repository.insert(a);
		
		QAaa t=QAaa.aaa;
		List<Aaa> list=repository.query().where(t.name.startsWith("张")).fetch();
		for(Aaa aaa:list) {
			System.out.println(aaa);
		}
		int count=repository.delete(7L);
		System.out.println(count);
		
		
		
		
	}
	
	@Test
	public void test1() {
		factory.getMetadataFactory().truncate(QAaa.aaa).execute();
		System.out.println("================test1");
		Aaa a = new Aaa();
		a.setName("张三");
		a.setCreated(new Timestamp(System.currentTimeMillis()).toInstant());
		int id=factory.insert(QAaa.aaa).populate(a).executeWithKey(Integer.class);
		a.setId(id);
		System.out.println(a.getId());

		Aaa b = new Aaa();
		b.setName("李四");
		b.setVersion(2);
		
		Aaa c = new Aaa();
		b.setName("王五");
		b.setVersion(1);
		factory.insert(QAaa.aaa).populate(b).addBatch().populate(c).addBatch().execute();
		
		factory.getMetadataFactory().truncate(QAaa.aaa).execute();
	}

	/**
	 * DML的Spring连接管理下，会在事务内提供同一个连接。 正常的DML不关闭连接，而是在Listener（endContext）的时候去处理连接关闭。
	 * 这个地方会判断是不是Spring管理连接，如果是，那么Listener也不会去关闭连接。 如果UnmanagedConnection。
	 * 
	 */
	@Test
	@Transactional(propagation = Propagation.NEVER)
	public void testTableRefresh() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		
		Collection<Constraint> cs = metadata.getConstraints(QAaa.aaa.getSchemaAndTable());
		for(Constraint c:cs) {
			log.info("constraint:{}",c);
		}
		Collection<Constraint> is = metadata.getIndices(QAaa.aaa.getSchemaAndTable());
		for(Constraint c:is) {
			log.info("index:{}",c);
		}
		
//		QAaa t=QAaa.aaa;
//		SQLMetadataQueryFactory metaFactory = factory.getMetadataFactory();
//		metaFactory.truncate(t).execute();
//		
//		
//		System.out.println("======== testTableRefresh() Columns ==========");
//		Collection<ColumnDef> columns=metaFactory.getColumns(QAaa.aaa.getSchemaAndTable());
//		for(ColumnDef c:columns) {
//			System.out.println(ToStringBuilder.reflectionToString(c));
//		}
//		
//		System.out.println("======== testTableRefresh() Indexes ==========");
//		Collection<Constraint> constraints=metaFactory.getIndexes(QAaa.aaa.getSchemaAndTable());
//		for(Constraint c:constraints) {
//			System.out.println(c);
//		}
//		System.out.println("======== testTableRefresh() Constraints ==========");
//		constraints=metaFactory.getConstraints(QAaa.aaa.getSchemaAndTable());
//		for(Constraint c:constraints) {
//			System.out.println(c);
//		}
		
		
		metadata.refreshTable(QAaa.aaa)
			.dropColumns(true)
			.dropConstraint(true)
			.dropIndexes(true)
			.simulate(false)
			.execute();
		
		
	}
}
