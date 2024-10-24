package com.github.xuse.querydsl.r2dbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.r2dbc.R2dbcFactory.R2Fetchable;
import com.github.xuse.querydsl.r2dbc.entity.QSchool;
import com.github.xuse.querydsl.r2dbc.entity.QUser;
import com.github.xuse.querydsl.r2dbc.entity.School;
import com.github.xuse.querydsl.r2dbc.entity.User;
import com.github.xuse.querydsl.util.DateFormats;
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

	LambdaTable<School> table = () -> School.class;

	@BeforeClass
	public static void createTable() {
		getSqlFactory();
	}
	
	@Test
	public void testSelect() throws InterruptedException {
		R2dbcFactory factory = getR2Factory();
		delete(factory,2);
		insert(factory);
		insertUser(factory);
		Flux<Object> global=Flux.just();
		System.out.println("================");
		{
			Flux<School> flux = factory.selectFrom(table).prepare(q -> q
					.where().eq(School::getName,"Zhangsan")).fetch();
			global=global.mergeWith(flux);
		}
		System.out.println("================");
		{
			R2Fetchable<School> fetch = factory.selectFrom(table).prepare(q -> q
					.where().eq(School::getName,"Zhangsan"));
			Mono<Long> count = fetch.fetchCount();
			Flux<School> schools = fetch.fetch();
			count.subscribe(System.out::println);
			schools.subscribe(System.out::println);
			global=global.mergeWith(schools);
			global=global.mergeWith(count);
		}
		System.out.println("================");
		{
			QUser user = QUser.user;
			QSchool school = QSchool.school;

			Flux<Tuple> flux = factory.select(user.id,user.name,school.id,school.name).prepare(q->
				q.from(user).leftJoin(school).on(user.uid.eq(school.code))
				.where(user.name.in("Jhon","Mark","Linda"))
			).fetch();
			global=global.mergeWith(flux);
		}
		// 在toStream过程中，会消费掉一部分进行缓存。16~256
		insert(factory);
		
		global.buffer().blockFirst();
	}

	private void delete(R2dbcFactory factory, int limit) {
		Mono<Long> deleted = factory.delete(table).prepare(q -> q.limit(limit)).execute();
		System.out.println("删除:" + deleted.block());
	}

	@Test
	public void testInsert() {
		R2dbcFactory factory = getR2Factory();
		insertUser(factory);
	}

	@Test
	public void updateAndDelete() {
		R2dbcFactory factory = getR2Factory();
		//Update example
		Mono<Long> count = factory.update(table).prepare(update -> 
			update.set(School::getName,"new Name")
			.where().eq(School::getId, 100)
				.or(or->or.eq(School::getCode, "TEST"))
			.build()).execute();
		
		//Delete example
		Mono<Long> deleteCount = factory.delete(table).prepare(
				delete -> delete
				.where().eq(School::getName, "Zhangsan")
					.between(School::getCreated, DateFormats.DATE_CS.parse("2020-12-03 10:15:30"), new Date())
					.build()).execute();
		
		//Sync execute
		List<Long> result=Flux.merge(count, deleteCount).buffer().blockFirst();
		System.out.println(result);
		assertEquals(Long.valueOf(0L),result.get(0));
		assertTrue(result.get(1)>-1);
	}
	
	
	private void insertUser(R2dbcFactory factory) {
		User foo = new User();
		foo.setEmail("test@host.com");;
		foo.setName("Mark");;
		foo.setUid(StringUtils.randomString());
		foo.setCreated(new Date());
		foo.setModified(new Date());
		Mono<Long> result=factory.insert(QUser.user).prepare(q->q.populate(foo)).execute();
		System.out.println("插入：" + result.block());
	}
	
	
	private void insert(R2dbcFactory factory) {
		School foo = new School();
		foo.setCode("A" + StringUtils.randomString());
		foo.setContent("Test");
		foo.setCreated(new Date());
		foo.setName("Zhangsan");
		foo.setUpdated(new Date());
		foo.setVolume(100);

		Mono<Long> result = factory.insert(table).prepare(q -> q.populate(foo)).execute();
		
		System.out.println("插入：" + result.block());
	}
}
