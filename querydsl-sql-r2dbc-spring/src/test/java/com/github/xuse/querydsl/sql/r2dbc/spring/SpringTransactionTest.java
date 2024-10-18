package com.github.xuse.querydsl.sql.r2dbc.spring;

import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.github.xuse.querydsl.sql.r2dbc.service.SpringService;

import reactor.core.publisher.Mono;

@ContextConfiguration(classes = SpringConfiguration.class)
public class SpringTransactionTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private SpringService service;
	
	@PostConstruct
	public void initTables() throws SQLException {
	}
	
	@Test
	public void testService() {
		System.out.println("数量:"+service.countRecord().block());
		try {
			Mono<Long> mono=service.testRollback(this::run);
			System.out.println("插入"+mono.block());
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("数量:"+service.countRecord().block());
	}
	
	private void run() {
		throw new RuntimeException();
	}
	
}
