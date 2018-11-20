package com.github.xuse.querydsl.sql;

import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.QAaa;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.SQLTemplates;

@ContextConfiguration(classes=SpringProviderTest.class)
//@Configuration
public class SpringProviderTest extends AbstractJUnit4SpringContextTests {

	@Bean
	public DataSource ds() {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		ds.setUrl("jdbc:derby:db;create=true");
		return ds;
	}

	
	@Bean
	public PlatformTransactionManager tx(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}
	
	private com.querydsl.sql.Configuration querydslConfiguration() {
		SQLTemplates templates = DerbyTemplates.builder().newLineToSingleSpace().build();
		com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
		configuration.addListener(new QueryDSLDebugListener());
		return configuration;
	}
	
	
	@Bean
	public SQLQueryFactory factory(DataSource ds) {
		return SQLQueryFactory.createSpringQueryFactory(ds, querydslConfiguration());
	}
	
	
	@PostConstruct
	public void initTables() throws SQLException {
		//createTableIfExist("create table AAA(id int, name varchar(64), created timestamp)","AAA");
	}
	
	
//	private void createTableIfExist(String sql, String name) {
//		if(!containesTable(name)) {
//			jdbcTemplate.execute(sql);
//		}
//	}
//
//
//	protected boolean containesTable(final String tableName) {
//		return jdbcTemplate.execute(new ConnectionCallback<Boolean>() {
//			@Override
//			public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
//				try(ResultSet result=con.getMetaData().getTables(null, null, tableName, new String[] {"TABLE"})){
//					return result.next();
//				}
//			}
//		});
//	}
	
	
	@Resource
	private SQLQueryFactory factory;
	
	@Test
	public void test1() {
		Aaa a=new Aaa();
		a.setCreated(new Timestamp(System.currentTimeMillis()));
		a.setName("张三");
		factory.insert(QAaa.aaa).populate(a).execute();
		
		a.setName("李四");
		factory.insert(QAaa.aaa)
		.populate(a).addBatch()
		.populate(a).addBatch()
		.execute();
		
		
		
		
	}
}
