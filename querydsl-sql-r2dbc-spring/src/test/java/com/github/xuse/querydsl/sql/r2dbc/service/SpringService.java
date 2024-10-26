package com.github.xuse.querydsl.sql.r2dbc.service;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.r2dbc.R2dbcFactory;
import com.github.xuse.querydsl.r2dbc.R2dbcFactory.R2Fetchable;
import com.github.xuse.querydsl.sql.r2dbc.entity.Foo;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SpringService implements LambdaHelpers{
	@Autowired
	private R2dbcFactory factory;
	
	private final LambdaTable<Foo> table = ()-> Foo.class;
	
	@Transactional
	public void testSelect() {
		delete(factory);
		insert(factory);
		System.out.println("================");
		R2Fetchable<Foo> fetch = factory.selectFrom(table).prepare(q -> q.where(string(Foo::getName).eq("Zhangsan")));
		Mono<Long> count = fetch.fetchCount();
		Flux<Foo> flux = fetch.fetch();
		// 在toStream过程中，会消费掉一部分进行缓存。16~256
		Stream<Foo> stream = flux.toStream();
		System.out.println("================");
		count.subscribe(System.out::println);
		stream.forEach(System.out::println);
		insert(factory);
	}
	
	public Mono<Long> countRecord() {
		Mono<Long> count = factory.selectFrom(table).fetchCount();
		return count;
	}
	
	public long countRecordSync() {
		Mono<Long> count = factory.selectFrom(table).fetchCount();
		return count.block();
	}
	
	@Transactional
	public Flux<Long> testRollback(RoutingStrategy routing) {
		Foo foo = new Foo();
		foo.setCode("A" + StringUtils.randomString());
		foo.setContent("Test");
		foo.setCreated(Instant.now());
		foo.setName("Zhangsan");
		foo.setUpdated(new Date());
		foo.setVolume(100);
		Mono<Long> count1 = factory.insert(table).prepare(q -> q.populate(foo)).execute();
		Mono<Long> count2 = factory.delete(table).prepare(d -> d.withRouting(routing).where(column(Foo::getName).eq("zhangsan"))).execute();
		return Flux.merge(count1, count2);
	}

	public Flux<Long> testNoTx(RoutingStrategy routing) {
		Foo foo = new Foo();
		foo.setCode("A" + StringUtils.randomString());
		foo.setContent("Test");
		foo.setCreated(Instant.now());
		foo.setName("Zhangsan");
		foo.setUpdated(new Date());
		foo.setVolume(100);
		Mono<Long> count1 = factory.insert(table).prepare(q -> q.populate(foo)).execute();
		
		Mono<Long> count2 = countRecord().filter(e->e>2).flatMap(
				e->factory.delete(table).prepare(d->d.withRouting(routing).where(column(Foo::getName).eq("zhangsan"))).execute());
		return Flux.merge(count1, count2);
	}
	

	private void delete(R2dbcFactory factory) {
		Mono<Long> deleted = factory.delete(table).prepare(q -> q.limit(2)).execute();
		System.out.println("删除:" + deleted.block());
	}

	public void testInsert() {
		insert(factory);
	}

	private void insert(R2dbcFactory factory) {
		Foo foo = new Foo();
		foo.setCode("A" + StringUtils.randomString());
		foo.setContent("Test");
		foo.setCreated(Instant.now());
		foo.setName("Zhangsan");
		foo.setUpdated(new Date());
		foo.setVolume(100);

		Mono<Long> v = factory.insert(table).prepare(q -> q.populate(foo)).execute();
		System.out.println("插入：" + v.block());
	}
}
