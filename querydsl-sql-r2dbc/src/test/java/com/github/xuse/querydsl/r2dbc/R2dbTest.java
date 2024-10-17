package com.github.xuse.querydsl.r2dbc;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.r2dbc.core.R2dbFactory;
import com.github.xuse.querydsl.r2dbc.entity.Foo;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;

public class R2dbTest extends R2DbTestBase implements LambdaHelpers{
	
	LambdaTable<Foo> table=()->Foo.class;
	
	
	@Test
	public void testInit() {
		
	}
	
	@Test
	public void testSelect() {
		R2dbFactory factory=getR2Factory();
		List<Foo> list=factory.selectFrom(table).prepare(q->q.where(string(Foo::getName).eq("Zhangsan"))).fetch().buffer().blockFirst();
		System.out.println(list);
		
		//FIXME 目前连接池连接关闭动作没做
		//TODO Spring事务还没支持
		
	}
	

	
	@Test
	public void testInsert() {
		R2dbFactory factory=getR2Factory();
		SQLQueryFactory sqlFactory=getSqlFactory();
		
		
		Foo foo=new Foo();
		foo.setCode("A");
		foo.setContent("Test");
		foo.setCreated(Instant.now());
		foo.setId(123);
		foo.setName("Zhangsan");
		foo.setUpdated(new Date());
		foo.setVolume(100);
		
		SQLInsertClauseAlter insert= sqlFactory.insert(table).populate(foo);
		System.out.println(insert.getSQL().get(0).getSQL());
		
		
		Long v=factory.insert(table)
		.prepare(q->q.populate(foo)).execute().block();
		System.out.println(v);
	}

}
