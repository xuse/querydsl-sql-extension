package com.github.xuse.querydsl.r2dbc;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.Test;

import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.r2dbc.core.R2dbcFactory;
import com.github.xuse.querydsl.r2dbc.core.R2dbcFactory.R2Fetchable;
import com.github.xuse.querydsl.r2dbc.entity.Foo;
import com.github.xuse.querydsl.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Test case of R2dbc
 * @author Joey
 *
 */
public class R2dbcTest extends R2DbTestBase implements LambdaHelpers {

	LambdaTable<Foo> table = () -> Foo.class;

	@Test
	public void createTable() {
		getSqlFactory();
	}
	
	@Test
	public void testSelect() throws InterruptedException {
		R2dbcFactory factory = getR2Factory();

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

	private void delete(R2dbcFactory factory) {
		Mono<Long> deleted = factory.delete(table).prepare(q -> q.limit(2)).execute();
		System.out.println("删除:" + deleted.block());
	}

	@Test
	public void testInsert() {
		R2dbcFactory factory = getR2Factory();
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
