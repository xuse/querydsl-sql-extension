package com.github.xuse.querydsl.r2dbc;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.Test;

import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.r2dbc.R2dbcFactory.R2Fetchable;
import com.github.xuse.querydsl.r2dbc.entity.Foo;
import com.github.xuse.querydsl.r2dbc.entity.QSchool;
import com.github.xuse.querydsl.r2dbc.entity.QUser;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.Tuple;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Test case of R2dbc
 * @author Joey
 *
 */

@SuppressWarnings("unused")
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
		{
			Flux<Foo> flux = factory.selectFrom(table).prepare(q -> q
					.where().eq(Foo::getName,"Zhangsan")).fetch();	
		}
		
		{
			R2Fetchable<Foo> fetch = factory.selectFrom(table).prepare(q -> q
					.where().eq(Foo::getName,"Zhangsan"));
			Mono<Long> count = fetch.fetchCount();
			Flux<Foo> flux = fetch.fetch();
			Stream<Foo> stream = flux.toStream();
			System.out.println("================");
			count.subscribe(System.out::println);
			stream.forEach(System.out::println);
		}
		{
			QUser user = QUser.user;
			QSchool school = QSchool.school;

			Flux<Tuple> flux = factory.select(user.id,user.name,school.id,school.name).prepare(q->
				q.from(user).leftJoin(school).on(user.uid.eq(school.code))
				.where(user.name.in("Jhon","Mark","Linda"))
			).fetch();
		}
		
		
		// 在toStream过程中，会消费掉一部分进行缓存。16~256
		
		insert(factory);
//		factory.select(null)
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

	private void update(R2dbcFactory factory) {
		//Update example
		Mono<Long> count = factory.update(table).prepare(update -> 
			update.set(Foo::getName,"new Name")
			.where().eq(Foo::getId, 100)
				.or(or->or.eq(Foo::getCode, "TEST"))
			.build()).execute();
		
		//Delete example
		Mono<Long> deleteCount = factory.delete(table).prepare(
				delete -> delete
				.where().eq(Foo::getName, "test")
					.between(Foo::getCreated, Instant.parse("2020-12-03T10:15:30.00Z. "), Instant.now())
					.build()).execute();
				
				
				
		
	}
	
}
