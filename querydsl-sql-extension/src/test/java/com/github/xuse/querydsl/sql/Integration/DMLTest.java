package com.github.xuse.querydsl.sql.Integration;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.AvsAuthParams;
import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.entity.CaAsset;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.entity.QAvsUserAuthority;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.repository.Selects;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dialect.DbType;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.JavaTimes;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Beans;
import com.querydsl.sql.Column;
import com.querydsl.sql.SQLExpressions;

import lombok.Data;

//@RunWith(Parameterized.class)
@SuppressWarnings("unused")
public class DMLTest extends AbstractTestBase implements LambdaHelpers {
//	@Parameterized.Parameters
//	public static List<Object> data() {
//		return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10 );
//	}
	
//	public DMLTest(int i) {
//	}

	@Test
	public void testTupleResult() {
		QAaa t1 = QAaa.aaa;
		Aaa a = generateEntity();
		factory.insert(t1).populate(a).execute();

		List<Tuple> maps = factory.select(t1.id, t1.name).from(t1).fetch();
		System.err.println(maps.get(0).get(0, Integer.class));
		System.err.println(maps.get(0).get(1, String.class));

		List<VO> vos = factory.select(ProjectionsAlter.bean(VO.class, t1.name, t1.id.count().as("cnt"))).from(t1)
				.groupBy(t1.name).fetch();
		for (VO vo : vos) {
			System.err.println(vo);
		}
	}

	private Aaa generateEntity() {
		Aaa a = new Aaa();
		a.setName(StringUtils.randomString());
		a.setGender(Gender.FEMALE);
		a.setTaskStatus(TaskStatus.INIT);
		a.setVersion(1);
		a.setDataDouble(2.4d);
		a.setDataInt(23);
		a.setDataFloat(0.2f);
		a.setDataShort((short) 1);
		a.setDataBigint(213L);
		a.setDataDecimal(new BigDecimal("1"));
		a.setDataBool(false);
		a.setDataDate(new Date());
		a.setDataTime(new Time(1000));
		a.setDateTimestamp(new Date());
		a.setDataBit(false);
		return a;
	}

	@Data
	public static class VO {
		private String name;

		@Column("cnt")
		private Integer count;
	}

	@Test
	public void reCreateTable() {
		SQLMetadataQueryFactory metadataFactory = factory.getMetadataFactory();
		metadataFactory.dropTable(QAaa.aaa).ifExists(true).execute();
		metadataFactory.dropTable(QAvsUserAuthority.avsUserAuthority).ifExists(true).execute();
		metadataFactory.dropTable(QCaAsset.caAsset).ifExists(true).execute();
		metadataFactory.dropTable(() -> Foo.class).ifExists(true).execute();

		metadataFactory.createTable(QAaa.aaa).execute();
		metadataFactory.createTable(QAvsUserAuthority.avsUserAuthority).execute();
		metadataFactory.createTable(QCaAsset.caAsset).execute();
		metadataFactory.createTable(() -> Foo.class).execute();
	}

	@Test
	public void testSelect() throws SQLException {
		QAaa t1 = QAaa.aaa;
		Aaa a = generateEntity();
		factory.insert(t1).populate(a).execute();
		List<Aaa> list = factory.selectFrom(t1).fetch();
		assertTrue(list.size() > 0);

		try (ResultSet rs = factory.selectFrom(t1).getResults()) {
			String str = SQLTypeUtils.toString(rs);
			System.err.println(str);
			
			char c=str.charAt(1);
			if(Character.isUpperCase(c)){
				assertTrue(str.startsWith("ID, NAME, CREATED"));	
			}else {
				assertTrue(str.startsWith("id, name, created"));
			}
			
			
		}
	}

	@Test
	public void testGroup1() {
		boolean flag = false;
		QAaa t1 = QAaa.aaa;
		factory.getMetadataFactory().truncate(t1).execute();
		Aaa a = generateEntity();
		a.setName("张三");
		Integer id = factory.insert(t1).populate(a).executeWithKey(Integer.class);
		if (flag) {
			return;
		}
		System.err.println("===========查询t1===========");

		Aaa b = factory.selectFrom(t1).where(t1.id.eq(id)).fetchFirst();
		System.err.println(b);

		System.err.println("===========更新t1===========");
		factory.update(t1).set(t1.taskStatus, TaskStatus.FAIL).set(t1.version, t1.version.add(Expressions.ONE))
				.where(t1.id.eq(id).and(t1.version.eq(b.getVersion()))).execute();
		System.err.println("===========更新t1 - populator===========");
		a.setGender(Gender.MALE);
		// TODO
		factory.update(t1).populate(a, AdvancedMapper.ofNullsBinding(0), false).where(Expressions.TRUE).execute();

		System.err.println("===========查询t1===========");
		b = factory.selectFrom(t1).where(
				t1.taskStatus.in(Arrays.asList(TaskStatus.FAIL, TaskStatus.INIT)).and(t1.gender.eq(Gender.FEMALE)))
				.fetchFirst();
		System.err.println(b);

		System.err.println("===========插入t2===========");
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		Integer sid = factory.insert(t2).set(t2.authContent, "abcdefg").set(t2.devId, "123").set(t2.userId, "ddefe")
				.set(t2.createTime, LocalDateTime.now()).set(t2.updateTime, "01/12/2019 12:30:21")
				.set(t2.gender, Gender.MALE).executeWithKey(t2.id);
		System.err.println(id);
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();
		System.err.println("Count:" + count);

		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchOne();
		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchFirst();

		factory.update(t1).set(t1.name, t1.name.concat("Abc123")).where(t1.id.eq(id)).execute();

		factory.update(t2).set(t2.createTime, DateTimeExpression.currentTimestamp(LocalDateTime.class))
				.where(t2.userId.eq("1")).execute();
		// factory.select(Projections.tuple(exprs))
	}

	@Test
	public void test2() {
		boolean prepareData = false;
		QAaa t1 = QAaa.aaa;

		if (prepareData) {
			Aaa a = new Aaa();
			a.setName("张三");
			a.setGender(Gender.FEMALE);
			a.setTaskStatus(TaskStatus.INIT);
			Integer id = factory.insert(t1).populate(a).executeWithKey(Integer.class);

			System.err.println("===========查询t1===========");
			for (Aaa aaa : factory.selectFrom(t1).fetch()) {
				System.err.println(aaa);
			}
			// assertEquals("[Aaa [created=2023-02-27 14:48:19.0, id=1, name=张三,
			// gender=FEMALE,taskStatus=INIT, version=0]]",aaa.toString())
		}
		Aaa old = factory.selectFrom(t1).where(t1.id.eq(1)).fetchOne();

		Aaa b = new Aaa();
		b.setName("李四");
		b.setGender(Gender.MALE);
		b.setTaskStatus(TaskStatus.RUNNING);
		b.setVersion(51);
		long count = factory.update(t1).populate(b).where(t1.id.eq(1)).execute();
		System.err.println("更新" + count);

	}

	@Test
	public void test3() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		List<AvsUserAuthority> eee = factory.selectFrom(t2).fetch();
		System.err.println(eee);

		QueryResults<AvsUserAuthority> results = factory.selectFrom(t2).fetchResults();
		System.err.println(results.getLimit() + "," + results.getOffset() + "," + results.getTotal());
		System.err.println(results.getResults());
	}

	@Test
	public void testUpdateSQL() {
		QAaa t1 = QAaa.aaa;

		Integer id = factory.select(t1.id.max()).from(t1).fetchFirst();
		System.err.println(id);
		long count = factory.update(t1).set(t1.created, JavaTimes.currentTimestamp()).set(t1.name, "李四")
				.where(t1.id.eq(id)).execute();
		assertTrue(count > 0);

		Aaa a = new Aaa();
		a.setName("Wang Wu");
		a.setGender(Gender.MALE);
		a.setVersion(2);

		Aaa oldRecord = factory.selectFrom(t1).where(t1.id.eq(id)).fetchOne();
		factory.update(t1).populateWithCompare(a, oldRecord).where(t1.id.eq(id)).execute();

	}

	@Test
	public void testUpdateAll() {
		QAaa t1 = QAaa.aaa;
		// 清理
		factory.getMetadataFactory().truncate(t1);

		factory.insert(t1).populate(generateEntity()).addBatch().populate(generateEntity()).addBatch().execute();

		long count = factory.update(t1).set(t1.created, JavaTimes.currentTimestamp()).set(t1.dataBigint, 2774689L)
				.where(Expressions.TRUE).execute();
		assertEquals(2, count);
	}

	@Test
	public void testDeleteAll() {
		QAaa t1 = QAaa.aaa;
		factory.delete(t1).where(Expressions.TRUE).execute();
	}

	@Test
	public void testInertBatch1() {
		QAaa t1 = QAaa.aaa;
		factory.getMetadataFactory().truncate(t1).execute();
		Aaa a = new Aaa();
		a.setName("张三");
		a.setGender(Gender.FEMALE);
		a.setTaskStatus(TaskStatus.RUNNING);
		a.setCreated(new Date().toInstant());
		a.setTrantField("aaaa");

		Aaa b = new Aaa();
		b.setName("王五");
		b.setGender(Gender.FEMALE);
		b.setTaskStatus(TaskStatus.RUNNING);
		b.setCreated(new Date().toInstant());
		b.setTrantField("bbbb");

		Aaa c = new Aaa();
		c.setName("sadfsfsdfs");
		c.setGender(Gender.MALE);
		c.setTaskStatus(TaskStatus.RUNNING);
		c.setCreated(new Date().toInstant());
		c.setTrantField("cccc");

		Aaa d = new Aaa();
		d.setName("李四");
		d.setGender(Gender.MALE);
		d.setTaskStatus(TaskStatus.RUNNING);
		d.setTrantField("dsaasdsa");
		d.setVersion(123);
		System.err.println("==== BATCH START ====");
		List<Integer> x = factory.insert(t1).writeNulls(false).populateBatch(Arrays.asList(a, b, c, d))
				.executeWithKeys(Integer.class);
		System.err.println(x);
	}

	@Test
	public void testUpdateBatch() {
		QAaa t1 = QAaa.aaa;
		long count = factory.update(t1)
				.where(t1.name.eq("1")).set(t1.version, t1.version.add(1)).addBatch()
				.where(t1.name.eq("2")).set(t1.version, t1.version.add(2)).addBatch()
				.execute();
		System.err.println(count);
	}

	@Test
	public void testDeleteBatch() throws SQLException {
		try (Connection conn = factory.getConnection()) {
			System.err.println("得到连接成功 ");
		}
		QAaa t1 = QAaa.aaa;
		long count = factory.delete(t1)
				.where(t1.name.eq("1aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
				.addBatch().where(t1.name.eq("2")).addBatch().execute();
		System.err.println(count);
	}

	@Test
	public void testComplexType() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;

		AvsUserAuthority data = new AvsUserAuthority();
		data.setUserId("user-daslfnskfn23");
		data.setDevId("C12345678");
		data.setAuthContent("abcdefg");
		// data.setUpdateTime("01/12/2019 13:30:21");
		data.setGender(Gender.MALE);
		data.setMap(new HashMap<>());
		data.getMap().put("attr1", "测试属性");
		data.getMap().put("attr2", "男");

		CaAsset sub = new CaAsset();
		sub.setCode("123");
		sub.setGender(Gender.FEMALE);
		sub.setName("李四");
		data.setAsserts(sub);

		// 插入
		Integer sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		System.err.println("update_time = " + data.getUpdateTime());

		// 查询数量
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();
		System.err.println("Count:" + count);

		// 查询数据，获取一条
		{
			System.err.println("========= fetch one========");
			AvsUserAuthority d = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchOne();
			System.err.println("update_time = " + d.getUpdateTime());
		}

		// 查询数据，获取第一个
		{
			System.err.println("========= fetch first========");
			AvsUserAuthority d = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchFirst();
			System.err.println(d);
		}

		// 更新数据
//		factory.update(t2).set(t2.createTime, DateTimeExpression.currentTimestamp(Timestamp.class))
//				.set(t2.map, Collections.singletonMap("TEST", "UPDATE")).where(t2.userId.eq(data.getUserId()))
//				.execute();
	}

	@Test
	public void testFetchAll() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		List<AvsUserAuthority> list = factory.selectFrom(t2).orderBy(t2.authType.asc()).fetch();
		for (AvsUserAuthority a : list) {
			System.err.println(a);
		}
	}

	@Test
	public void testInsert() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;

		AvsUserAuthority data = new AvsUserAuthority();
		data.setUserId("user-daslfnskfn23");
		data.setDevId("C12345678");
		data.setAuthContent("abcdefg");
		data.setUpdateTime("01/12/2019 13:30:21");
		data.setGender(Gender.MALE);
		data.setMap(new HashMap<>());
		data.getMap().put("attr1", "测试属性");
		data.getMap().put("attr2", "女");
		// 插入
		Integer sid;
		System.err.println(factory.getMetadataFactory().getDatabaseProduct());

		if (factory.getMetadataFactory().getDatabaseProduct().startsWith("MySQL")) {
			sid = factory.asMySQL().insertIgnore(t2).populate(data).executeWithKey(t2.id);
		} else {
			sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		}
		System.err.println(sid);
		CRUDRepository<AvsUserAuthority, Integer> repository = factory.asRepository(t2);
		AvsUserAuthority obj = repository.load(sid);
		System.out.println(obj.getCreateTime());
	}

	@Test
	public void testMerge() {
		QAaa t1 = QAaa.aaa;
		Aaa a = new Aaa();
//		a.setId(1);
		a.setName("张222");
		a.setGender(Gender.FEMALE);
		a.setCreated(new Timestamp(System.currentTimeMillis()).toInstant());
		a.setVersion(12);
		a.setDataInt(222);
		a.setDataDouble(2.3d);
		a.setTaskStatus(TaskStatus.FAIL);
		a.setDataFloat(-1f);
		a.setDataShort((short) -1);
		a.setDataBigint(-1);
		a.setDataDecimal(BigDecimal.ONE);
		a.setDataDate(new Date());
		a.setDataTime(new java.sql.Time(System.currentTimeMillis()));
		a.setDateTimestamp(new Date());
		int id = factory.insert(t1).writeNulls(true).populate(a).executeWithKey(Integer.class);
		System.err.println("id=" + id);

		Integer count = factory.merge(t1).keys(t1.id).columns(t1.id, t1.created, t1.gender)
				.values(id, new Date(), Gender.MALE).executeWithKey(t1.id);

		System.err.println("返回:" + count);

	}

	@Test
	public void testConditionBean() {
		QAvsUserAuthority t = QAvsUserAuthority.avsUserAuthority;
		AvsAuthParams p = new AvsAuthParams();
		p.setAuthContent("123");
//		p.setAuthType(0);
		p.setLimit(100);
		p.setOffset(2);
//		p.setDateGt(new Date());
//		p.setDateLoe(new Date());
//		p.setCreateTime(new Date[] { new Date(0), new Date() });
		p.setOrder("authType");
		p.setOrderAsc(true);
		p.setFetchTotal(false);
		Pair<Integer, List<AvsUserAuthority>> result = factory.asRepository(t).findByCondition(p);
		System.err.println(result.getFirst());
		System.err.println(result.getSecond());
	}

	@Test
	public void testRouting() {
		QAaa t1 = QAaa.aaa;
		QCaAsset t2 = QCaAsset.caAsset;

		TableRouting routing = TableRouting.builder().suffix(t1, "202406").suffix(t2, "2024Q2").build();
		boolean reCreateTable = true;
		if (reCreateTable) {
			SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
			metadata.dropTable(t1).withRouting(routing).execute();
			metadata.createTable(t1).withRouting(routing).execute();
			metadata.dropTable(t2).withRouting(routing).execute();
			metadata.createTable(t2).withRouting(routing).execute();
		}

		List<Tuple> tuples = factory.select(t1.name, t1.gender, t2.content, t2.code).from(t1).withRouting(routing)
				.innerJoin(t2).on(t1.id.eq(t2.id)).where(t1.name.eq("张三")).fetch();
		for (Tuple t : tuples) {
			System.out.println(t);
		}
	}

	@Test
	public void testRouting2() {
//		QAaa t1=QAaa.aaa;
		QCaAsset t2 = QCaAsset.caAsset;

		TableRouting routing = TableRouting.suffix("2024Q2");
		boolean reCreateTable = true;
		if (reCreateTable) {
			SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
			metadata.dropTable(t2).withRouting(routing).execute();
			metadata.createTable(t2).withRouting(routing).execute();
		}

		List<Tuple> tuples = factory.select(t2.content, t2.code).from(t2).where(t2.name.eq("Test")).fetch();
		for (Tuple t : tuples) {
			System.out.println(t);
		}
	}

	@Test
	public void mysqlInsertOnDuplidateKey() {
		Assumptions.assumeTrue(factory.getMetadataFactory().getDatabaseInfo().getDbType() == DbType.mysql,"Only for MYSQL");
		QCaAsset t2 = QCaAsset.caAsset;
		CaAsset a = new CaAsset();
		a.setCode("a");
		a.setContent("test123");
		a.setGender(Gender.MALE);
		a.setMap(Collections.singletonMap("s", "b"));
		a.setName("LIU");
		// 使用SQLExpressions.set是官方设计的，使用eq可以起到数值不变的效果，实际没啥必要。
		// SQLExpressions.set(path)，会变为
		// values(path)的效果，实际上引用了前文设置的新植，因此无需将数值再重复一遍。避免增加操作变量。
		factory.asMySQL()
				.insertOnDuplicateKeyUpdate(t2, SQLExpressions.set(t2.name, t2.name),
						SQLExpressions.set(t2.ext, t2.ext), t2.gender.eq(Gender.FEMALE), t2.content.eq(t2.content))
				.populate(a).execute();
	}

	/*
	 * MyBatis-plus的用法打开了思路，可以使用实体类来表达一个表的元模型， 可以使用方法引用来表达一个元模型中的字段。
	 * 
	 * 即—— 用Class来代替Query class。 用 Class::getSomeField()来代替中的静态字段模型。 这样依赖就可以省掉Query
	 * class，实现彻底的POJO Bean + Annotation进行数据表映射了。 虽然这种奇技_巧没啥意义，但还是很多人喜欢。
	 */
	@Test
	public void testPureBean() {
		LambdaTable<Foo> table = () -> Foo.class;
		LambdaColumn<Foo, String> p = Foo::getName;
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		metadata.createTable(table).ifExists().execute();
		List<Foo> list = factory.selectFrom(table).where(p.eq("1")).fetch();
	}

	/**
	 * 1. Select the table data to another bean. 2. Use LambdaHelpers to hint a
	 * lambda to a column model.
	 * 
	 */
	@Test
	public void testSelectAs() {
		SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
		LambdaTable<Foo> foo = () -> Foo.class;
		{
			List<Aaa> aaas = factory.select(Selects.bean(Aaa.class, foo)).from(foo).fetch();
			System.out.println(aaas);
		}
		{
			List<Aaa> aaas = factory.select(Selects.bean(Aaa.class, $(Foo::getName), $(Foo::getId),
					$(Foo::getVolume).as("version"), $(Foo::getCreated))).from(foo).fetch();
			System.out.println(aaas);
		}
	}

	/**
	 * 在没有Query class情况下的自表关联查询
	 */
	@Test
	public void testTableSelfJoin() {
		factory.getMetadataFactory().truncate(() -> Foo.class).execute();
		{
			// 准备数据
			Foo foo = new Foo();
			foo.setName("张三");
			foo.setCode("Zhangsan");
			foo.setCreated(Instant.now());
			Integer id = factory.insert(() -> Foo.class).populate(foo).executeWithKey(Integer.class);

			Foo foo2 = new Foo();
			foo2.setName("张三的儿子");
			foo2.setCode("Lisi");
			foo2.setCreated(Instant.now());
			// 这个字段当作ParentId使用
			foo2.setVolume(id);
			factory.insert(() -> Foo.class).populate(foo2).executeWithKey(Integer.class);
		}

		// 自表关联查询
		RelationalPathExImpl<Foo> t1 = ((LambdaTable<Foo>) () -> Foo.class).forVariable("t1");
		RelationalPathExImpl<Foo> parent = t1.forVariable("parent");

		List<Beans> list = factory.select(Selects.beans(t1, parent)).from(t1).innerJoin(parent)
				.on(parent.get(Foo::getId).eq(t1.get(Foo::getVolume))).where(parent.get(Foo::getName).eq("张三")).fetch();

		assertEquals(1, list.size());
		for (Beans beans : list) {
			Foo childFoo = beans.get(t1);
			Foo parentFoo = beans.get(parent);
			System.out.println(childFoo);
			System.out.println(parentFoo);
			assertEquals("张三的儿子", childFoo.getName());
			assertEquals("张三", parentFoo.getName());

		}
	}

}
