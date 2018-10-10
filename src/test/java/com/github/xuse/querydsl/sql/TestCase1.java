package com.github.xuse.querydsl.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.QAaa;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;

public class TestCase1 {
	
	private DriverManagerDataSource ds=new DriverManagerDataSource();
	
	private SQLQueryFactoryAlter factory;
	{
		ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		ds.setUrl("jdbc:derby:db;create=true");
		factory= new SQLQueryFactoryAlter(querydslConfiguration(), ds);	
	}

	public com.querydsl.sql.Configuration querydslConfiguration() {
		SQLTemplates templates = MySQLTemplates.builder().build(); // change to your Templates
		com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
		configuration.addListener(new QueryDSLDebugListener());
		return configuration;
	}
	
	@Test
	public void test2() throws SQLException {
		Connection conn=ds.getConnection();
		try (Statement st=conn.createStatement()){
			st.executeUpdate("create table aaa(id int, name varchar(64), created timestamp)");
		}
		conn.close();
	}
	
	
	
	@Test
	public void test1() {
		QAaa t1=QAaa.aaa;
		Aaa a=new Aaa();
		a.setId(1);
		a.setName("张三");
		a.setCreated(new Timestamp(System.currentTimeMillis()));
		factory.insert(t1).populate(a).execute();
	}


}
