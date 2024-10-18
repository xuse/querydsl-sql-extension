package com.github.xuse.querydsl.sql.r2dbc.spring;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.github.xuse.querydsl.sql.r2dbc.service.SpringService;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.routing.TableRouting;

import reactor.core.publisher.Flux;

@ContextConfiguration(classes = SpringConfiguration.class)
public class SpringTransactionTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private SpringService service;
	
	@PostConstruct
	public void initTables() throws SQLException {
	}
	
	@Test
	public void testService() {
		long begin=service.countRecordSync();
		System.out.println("begin count:"+begin);
		{
			//传入一个TableRouting，使其SQL语句访问一张不存在的表，形成异常。
			Flux<Long> mono=service.testRollback(RoutingStrategy.DEFAULT);
			System.out.println("XXX:" + mono.buffer().blockFirst());			
		}
		//commited, 記録增加1
		assertTrue(++begin==service.countRecord().block());
		
		
		
		try {
			//传入一个TableRouting，使其SQL语句访问一张不存在的表，形成异常。
			Flux<Long> mono=service.testRollback(TableRouting.suffix("_FAKE"));
			System.out.println("XXX:" + mono.buffer().blockFirst());
		}catch(Exception e) {
			System.out.println("This exception will cause tx rollback:"+e.getClass().getName()+": "+e.getMessage());
		}
		assertTrue(begin==service.countRecord().block());
	}
}