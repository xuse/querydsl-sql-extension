package com.github.xuse.querydsl.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(classes = SpringConfiguration.class)
public class SpringProviderTest extends AbstractTransactionalJUnit4SpringContextTests {


	@PostConstruct
	public void initTables() throws SQLException {
		//geequery.getDefaultDatabaseMetadata().dropTable(Aaa.class);
//		geequery.getDefaultDatabaseMetadata().createTable(Aaa.class);
	}


	protected boolean containesTable(final String tableName) {
		return jdbcTemplate.execute(new ConnectionCallback<Boolean>() {
			@Override
			public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
				try (ResultSet result = con.getMetaData().getTables(null, null, tableName, new String[] { "TABLE" })) {
					return result.next();
				}
			}
		});
	}

	@Resource
	private SQLQueryFactory factory;
//	@Resource
//	private SessionFactory geequery;

//	@Test
//	public void test1() {
//		System.out.println("================test1");
//		Aaa a = new Aaa();
//		a.setCreated(new Timestamp(System.currentTimeMillis()));
//		a.setName("张22222");
//		geequery.getSession().insert(a);
//		System.out.println(a.getId());
//		//factory.insert(QAaa.aaa).populate(a).execute();
//
//		Aaa b = new Aaa();
//		b.setName("李四333");
//		factory.insert(QAaa.aaa).populate(b).addBatch().populate(b).addBatch().execute();
//	}

//	@Test
//	public void test2() {
//		System.out.println("================test2");
//		//关于实体的同步问题（两套元模型，后续再逐渐合并）
//		//正向生成
//		//使用JPA注解处理器生成
//		//
//		//逆向生成
//	    //要编写一个序列化器，在生成QueryDSL类的同时，生成元模型和注解。
//		//或者改为GeQuery逆向出Bean,Query基于注解生成 Q类。
//		
//		//要点，要向QueryDSL牺牲，int不能用于判断，要用Integer才能实现插入的dynamic判定
//		//因此在执行插入等简单操作时，GQ优于QueryDSL
//		Aaa a = new Aaa();
//		a.setCreated(new Timestamp(System.currentTimeMillis()));
//		a.setName("张三");
//		factory.insert(QAaa.aaa).populate(a).execute();
//
//		a.setName("李四");
//		factory.insert(QAaa.aaa).populate(a).addBatch().populate(a).addBatch().execute();
//	}
}
