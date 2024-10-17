package com.github.xuse.querydsl.r2dbc;

import java.time.Instant;
import java.util.Date;

import org.junit.Test;

import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.r2dbc.core.R2dbFactory;
import com.github.xuse.querydsl.r2dbc.entity.Foo;

public class R2dbTest extends R2DbTestBase{
	
	LambdaTable<Foo> table=()->Foo.class;
	
	@Test
	public void testInit2() {
		R2dbFactory factory=new R2dbFactory(getConnectionFactory(),getConfiguration());
		
	}
	
	@Test
	public void testInit() {
		R2dbcQueryFactory factory=new R2dbcQueryFactory(getConnectionFactory(),getConfiguration());
		
		Foo foo=new Foo();
		foo.setCode("A");
		foo.setContent("Test");
		foo.setCreated(Instant.now());
		foo.setId(123);
		foo.setName("Zhangsan");
		foo.setUpdated(new Date());
		foo.setVolume(100);
		
		
		
		factory.insert(table).populate(foo).execute();
	}

}
