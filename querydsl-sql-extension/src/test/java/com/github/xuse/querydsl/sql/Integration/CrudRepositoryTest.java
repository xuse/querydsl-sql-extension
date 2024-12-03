package com.github.xuse.querydsl.sql.Integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.ConditionBean;
import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.FooWith2ColumnPK;
import com.github.xuse.querydsl.entity.FooWithoutPK;
import com.github.xuse.querydsl.entity.StateMachine;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.repository.LambdaQueryWrapper;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.util.DateUtils;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.ComparableExpression;

import lombok.Data;

public class CrudRepositoryTest extends AbstractTestBase  implements LambdaHelpers {
//	@BeforeAll
//	public static void initTable() {
//		SQLQueryFactory factory=getSqlFactory();
//		factory.getMetadataFactory().createTable(()->FooWithoutPK.class).ifExists().execute();
//		factory.getMetadataFactory().createTable(()->Foo.class).ifExists().execute();
//	}
	
	@Test
	public void testLoadBatch() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(() -> Foo.class);
		Foo foo=repo.load(1);
		List<Foo> foos=repo.loadBatch(Arrays.asList(1,2,3,4));
	}
	
	@Test
	public void testLoadBatchError() {
		CRUDRepository<FooWithoutPK, String> repo = factory.asRepository(() -> FooWithoutPK.class);
		int count = 0;
		try {
			FooWithoutPK foo=repo.load("name1");
		}catch(UnsupportedOperationException e) {
			count++;
		}
		try {
			List<FooWithoutPK> foos=repo.loadBatch(Arrays.asList("name1","name2"));
		}catch(UnsupportedOperationException e) {
			count++;
		}
		assertEquals(2, count);
	}
	
	@Test
	public void testLoadBatchError2() {
		CRUDRepository<FooWith2ColumnPK, String> repo = factory.asRepository(() -> FooWith2ColumnPK.class);
		int count = 0;
		try {
			FooWith2ColumnPK foo=repo.load("name1");
		}catch(IllegalArgumentException e) {
			count++;
		}
		try {
			List<FooWith2ColumnPK> foos=repo.loadBatch(Arrays.asList("name1","name2"));
		}catch(UnsupportedOperationException e) {
			count++;
		}
		assertEquals(2, count);
	}	

	/**
	 * 在支持了无需QueryClass的情况下，使用Repository界面，可以使用以下几种风格进行查询 五套风格，总有一套适合你。
	 */
	@Test
	public void testPureBean2() {
		/*
		 * 对于不喜欢SQLQueryFactory风格的用户。可以更换前端操作类。
		 * 当然更自由的方式是自己定义一个Repository，并通过SpringBean注入
		 * 
		 * @Repository public class FooRepository extends
		 * AbstractCrudRepository<Foo,Long>{
		 * 
		 * }
		 */
		CRUDRepository<Foo, Integer> repo = factory.asRepository(() -> Foo.class);

		// 写法一，传统 repository风格，功能较弱，比如无法支持Between条件
		{
			Foo foo = new Foo();
			foo.setName("张三");
			foo.setCreated(Instant.now());
			repo.findByExample(foo);
		}

		// 写法二，MyBatis-Plus风格
		{
			LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(Foo::getName, "张三").between(Foo::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now());

			Pair<Integer, List<Foo>> results = repo.findAndCount(wrapper);
		}

		// 写法三，接近queryDSL原生风格，同时支持lambda
		{
			repo.query().eq(Foo::getName, "张三")
					.between(Foo::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now()).findAndCount();
		}

		// 写法四，QueryDSL风格
		{
			LambdaColumn<Foo, String> name = Foo::getName;
			LambdaColumn<Foo, Instant> created = Foo::getCreated;
			List<Foo> list = repo.find(
					q -> q.where(name.eq("张三").and(created.between(DateUtils.getInstant(2023, 12, 1), Instant.now()))));
		}

		// 写法五，使用一个查询表单类
		{
			FooParams params = new FooParams();
			params.setName("张三");
			params.setCreated(new Date[] { DateUtils.get(2023, 12, 1), new Date() });
			repo.findByCondition(params);
		}
	}

	/**
	 * 1. use UpdateHandler to set column value via an expression.
	 */
	@Test
	public void testUpdateWithExpressions() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(() -> Foo.class);
		// 其他复杂表达式的更新
		repo.query().eq(Foo::getId, 1).update()
				// 相当于 set volume = volume + 100
				// .set(Foo::getVolume).add(100)

				// 相当于 set volume = id * 100
				// .set(Foo::getVolume).to(Foo::getId, id-> id.multiply(100))

				// 相当于 set volume = volume * id
				.set(Foo::getVolume).to(Foo::getId, (volume, id) -> volume.multiply(id.add(2))).set(Foo::getCode)
				.concat("_suffix").execute();
	}

	/**
	 * 
	 */
	@Test
	public void testSelectItems1() {
		boolean prepareData = false;
		CRUDRepository<Foo, Integer> repo = factory.asRepository(() -> Foo.class);
		if (prepareData) {
			Foo foo = new Foo();
			foo.setCode("123");
			foo.setContent("contnbt");
			foo.setName("ASSET1");
			foo.setCreated(Instant.now());
			foo.setUpdated(new Date());
			foo.setMap(Collections.singletonMap("K", "V"));
			foo.setVolume(100);
			repo.insert(foo);
		}
		{
			// 1. select one column from the table.
			LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>();
			List<Integer> results = repo.find(wrapper.selectSingleColumn(Foo::getId, id -> id.max())
					.like(Foo::getName, "%").between(Foo::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
					.groupBy(Foo::getName));
			System.out.println(results);
		}
		{
			// 2. select multiple columns to Object[]
			LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>();
			List<Object[]> list = repo.find(
					wrapper.select(q -> q.column(Foo::getCreated).to(e -> e.max()).as("maxCreated").column(Foo::getId)
							.to(ID -> ID.count()).as("IdCount").column(Foo::getUpdated).to(ComparableExpression::max)
							.as("maxUpdated").column(Foo::getName).and().toArray()).groupBy(Foo::getName));
			for (Object[] obj : list) {
				System.out.println(Arrays.toString(obj));
			}
		}
		{
			// 3. select multiple columns to Map
			LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>();
			List<Map<String, ?>> list = repo.find(wrapper
					.select(result -> result.column(Foo::getCreated).to(e -> e.max()).as("maxCreated")
							.column(Foo::getId).to(ID -> ID.count()).as("IdCount").column(Foo::getUpdated)
							.to(ComparableExpression::max).as("maxUpdated").column(Foo::getName).and().toMap())
					.groupBy(Foo::getName));
			for (Map<String, ?> obj : list) {
				System.out.println(obj);
			}
		}
	}

	@Test
	public void testSelectItems2() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(() -> Foo.class);

		List<Pair<Integer, String>> list = repo.query().eq(Foo::getName, "张三")
				.between(Foo::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now()).groupBy(Foo::getName)
				.having($(Foo::getId).count().goe(100)).selectPair(num(Foo::getId).max(), string(Foo::getName)).fetch();
	}

	/*
	 * 写法五中用到的查询表单类
	 */
	@Data
	@ConditionBean
	public static class FooParams {
		@Condition(Ops.STRING_CONTAINS)
		String name;

		@Condition(Ops.BETWEEN)
		Date[] created;
	}

	/**
	 * 打通普通bean, 1包扫描时支持普通的relationalPath注册。
	 * 2如果使用了querydsl原生的方式创建queryClz。那么这里会使用扫描到的entity path，从而不会对类进行反复解析。
	 */
	@Test
	public void testPureBean4() {
		/*
		 * 对于不喜欢SQLQueryFactory风格的用户。可以更换前端操作类。
		 * 当然更自由的方式是自己定义一个Repository，并通过SpringBean注入
		 * 
		 * @Repository public class FooRepository extends
		 * AbstractCrudRepository<Foo,Long>{
		 * 
		 * }
		 */
		CRUDRepository<Aaa, Long> repo = factory.asRepository(() -> Aaa.class);

		// 写法一，传统 repository风格，功能较弱，比如无法支持Between条件
		{
			Aaa foo = new Aaa();
			foo.setName("张三");
			foo.setCreated(new Date().toInstant());
			repo.findByExample(foo);
			repo.countByExample(foo);
		}

		// 写法二，MyBatis-Plus风格
		{
			LambdaQueryWrapper<Aaa> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(Aaa::getName, "张三").between(Aaa::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
					.orderBy($(Aaa::getCreated).asc(), $(Aaa::getId).desc()).limit(10).offset(20);
			Pair<Integer, List<Aaa>> results = repo.findAndCount(wrapper);
		}

		// 写法三，接近queryDSL原生风格，同时支持lambda
		{
			repo.query().eq(Aaa::getName, "张三")
					.between(Aaa::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
					// .groupBy(Aaa::getGender,Aaa::getTaskStatus)
					.groupBy($(Aaa::getGender), s(Aaa::getName).upper()).findAndCount();
		}

		// 写法四，QueryDSL风格
		{
			LambdaColumn<Aaa, String> name = Aaa::getName;
			LambdaColumn<Aaa, Instant> created = Aaa::getCreated;
			List<Aaa> list = repo.find(
					q -> q.where(name.eq("张三").and(created.between(DateUtils.getInstant(2023, 12, 1), Instant.now()))));
		}

		// 写法五，使用一个查询表单类
		{
			FooParams params = new FooParams();
			params.setName("张三");
			params.setCreated(new Date[] { DateUtils.get(2023, 12, 1), new Date() });
			repo.findByCondition(params);
		}
	}

	// 如果涉及较为复杂的函数和处理，就要一个接口进行辅助了
	@Test
	public void testPureBean5() {
		CRUDRepository<Aaa, Long> repo = factory.asRepository(() -> Aaa.class);
		repo.query().eq(Aaa::getName, "张三").between(Aaa::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
				// .groupBy(Aaa::getGender,Aaa::getTaskStatus)
				.groupBy(column(Aaa::getGender), string(Aaa::getName).upper())
				.having(column(Aaa::getName).count().goe(15)).findAndCount();
	}

	@Test
	public void testPureBean3() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		LambdaTable<StateMachine> table = () -> StateMachine.class;
		metadata.dropTable(table).execute();
		metadata.createTable(table).execute();

		CRUDRepository<StateMachine, String> repo = factory.asRepository(table);
		{
			StateMachine s = new StateMachine();
			s.setId(StringUtils.generateGuid());
			s.setEndParams("123");
			s.setGmtEnd(new Date());
			s.setGmtUpdated(new Date());
			s.setIsRunning(0);
			s.setMachineId("aa");
			s.setParentId("bb");
			String id = repo.insert(s);

			assertTrue(id == null || s.getId().equals(id));
		}
		{
			StateMachine s1 = new StateMachine();
			s1.setId(StringUtils.generateGuid());
			s1.setEndParams("123");
			s1.setGmtEnd(new Date());
			s1.setGmtUpdated(new Date());
			s1.setIsRunning(0);
			s1.setMachineId("aa");
			s1.setParentId("bb");

			StateMachine s2 = new StateMachine();
			s2.setId(StringUtils.generateGuid());
			s2.setEndParams("123");
			s2.setGmtEnd(new Date());
			s2.setGmtUpdated(new Date());
			s2.setIsRunning(0);
			s2.setMachineId("aa");
			s2.setParentId("bb");

			StateMachine s3 = new StateMachine();
			s3.setId(StringUtils.generateGuid());
			s3.setEndParams("123");
			s3.setGmtEnd(new Date());
			s3.setGmtUpdated(new Date());
			s3.setIsRunning(0);
			s3.setMachineId("aa");
			s3.setParentId("bb");
			String vs = factory.insert(table).populateBatch(Arrays.asList(s1, s2, s3)).executeWithKey(String.class);
			System.out.println(vs);
		}

		List<StateMachine> list = repo.query().fetch();
		System.out.println(list);
		StateMachine indb = list.get(0);
		indb.setMachineId("456");
		repo.update(indb.getId(), indb);

		repo.delete(indb.getId());
	}

	@Test
	public void testPureBeanBatch() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(() -> Foo.class);

		Foo foo = new Foo();
		foo.setCode("Test1");
		foo.setName("test1");
		Foo foo2 = new Foo();
		foo2.setCode("Test2");
		foo2.setName("test2");

		factory.getMetadataFactory().truncate(() -> Foo.class).execute();

		int count = repo.insertBatch(Arrays.asList(foo, foo2));
		System.err.println(count);

		for (Foo f : repo.query().fetch()) {
			System.out.println(f);
		}

	}
}
