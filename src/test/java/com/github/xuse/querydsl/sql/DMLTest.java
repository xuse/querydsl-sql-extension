package com.github.xuse.querydsl.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
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

import org.junit.Assume;
import org.junit.Test;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.ConditionBean;
import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.AvsAuthParams;
import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.entity.CaAsset;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.entity.QAvsUserAuthority;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.entity.StateMachine;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.repository.LambdaQueryWrapper;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.JavaTimes;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.github.xuse.querydsl.util.DateUtils;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Column;
import com.querydsl.sql.SQLExpressions;

import lombok.Data;

@SuppressWarnings("unused")
public class DMLTest extends AbstractTestBase implements LambdaHelpers{
	@Test
	public void testTupleResult() {
		QAaa t1 = QAaa.aaa;
		Aaa a = generateEntity();
		factory.insert(t1).populate(a).execute();
		
		List<Tuple> maps = factory.select(t1.id, t1.name).from(t1).fetch();
		System.err.println(maps.get(0).get(0, Integer.class));
		System.err.println(maps.get(0).get(1, String.class));

		List<VO> vos = factory.select(ProjectionsAlter.bean(VO.class, t1.name, t1.id.count().as("cnt")))
				.from(t1)
				.groupBy(t1.name).fetch();
		for (VO vo : vos) {
			System.err.println(vo);
		}
	}

	private Aaa generateEntity() {
		Aaa a=new Aaa();
		a.setName(StringUtils.randomString());
		a.setGender(Gender.FEMALE);
		a.setTaskStatus(TaskStatus.INIT);
		a.setVersion(1);
		a.setDataDouble(2.4d);
		a.setDataInt(23);
		a.setDataFloat(0.2f);
		a.setDataShort((short)1);
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
		SQLMetadataQueryFactory metadataFactory=factory.getMetadataFactory();
		metadataFactory.dropTable(QAaa.aaa).ifExists(true).execute();
		metadataFactory.dropTable(QAvsUserAuthority.avsUserAuthority).ifExists(true).execute();
		metadataFactory.dropTable(QCaAsset.caAsset).ifExists(true).execute();
		
		metadataFactory.createTable(QAaa.aaa).execute();
		metadataFactory.createTable(QAvsUserAuthority.avsUserAuthority).execute();
		metadataFactory.createTable(QCaAsset.caAsset).execute();
	}

	@Test
	public void testSelect() {
		QAaa t1 = QAaa.aaa;
		Aaa a = generateEntity();
		factory.insert(t1).populate(a).execute();
		
		
		
		List<Aaa> list = factory.selectFrom(t1).fetch();
		for (Aaa aa : list) {
			System.err.println(aa.getDataInt());
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
		factory.update(t1).set(t1.taskStatus, TaskStatus.FAIL)
		.set(t1.version, t1.version.add(Expressions.ONE))
				.where(t1.id.eq(id).and(t1.version.eq(b.getVersion()))).execute();
		System.err.println("===========更新t1 - populator===========");
		a.setGender(Gender.MALE);
		//TODO
		factory.update(t1).populate(a, AdvancedMapper.ofNullsBinding(0), false).where(Expressions.TRUE).execute();

		System.err.println("===========查询t1===========");
		b = factory.selectFrom(t1).where(
				t1.taskStatus.in(Arrays.asList(TaskStatus.FAIL, TaskStatus.INIT)).and(t1.gender.eq(Gender.FEMALE)))
				.fetchFirst();
		System.err.println(b);

		System.err.println("===========插入t2===========");
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		int sid = factory.insert(t2).set(t2.authContent, "abcdefg").set(t2.devId, "123").set(t2.userId,"ddefe").set(t2.createTime, LocalDateTime.now())
				.set(t2.updateTime, "01/12/2019 12:30:21").set(t2.gender, Gender.MALE).executeWithKey(t2.id);
		System.err.println(id);
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();
		System.err.println("Count:" + count);
		List<String> entity = factory.select(ProjectionsAlter.map(t2.getProjection(),String.class,e -> e.getDevId())).from(t2)
				.where(t2.id.eq(sid)).fetch();

		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchOne();
		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchFirst();
		System.err.println(entity);

		factory.update(t1).set(t1.name, t1.name.concat("Abc123")).where(t1.id.eq(id)).execute();

		factory.update(t2).set(t2.createTime, DateTimeExpression.currentTimestamp(LocalDateTime.class))
				.where(t2.userId.eq("1")).execute();
		// factory.select(Projections.tuple(exprs))
	}

	@Test
	public void test2() {

		QAaa t1 = QAaa.aaa;
//		Aaa a = new Aaa();
//		a.setName("张三");
//		a.setGender(Gender.FEMALE);
//		a.setTaskStatus(TaskStatus.INIT);
//		Integer id = factory.insert(t1).populate(a).executeWithKey(Integer.class);
//		
//		System.err.println("===========查询t1===========");
//		for(Aaa aaa:factory.selectFrom(t1).fetch()) {
//			System.err.println(aaa);	
//		}

		// 测试更新功能(7个字段)
		// [Aaa [created=2023-02-27 14:48:19.0, id=1, name=张三, gender=FEMALE,
		// taskStatus=INIT, version=0]]
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
		
		Integer id=factory.select(t1.id.max()).from(t1).fetchFirst();
		System.err.println(id);
		long count = factory.update(t1)
			.set(t1.created, JavaTimes.currentTimestamp())
			.set(t1.name, "李四")
			.where(t1.id.eq(id))
			.execute();
		assertTrue(count>0);
		
		Aaa a=new Aaa();
		a.setName("Wang Wu");
		a.setGender(Gender.MALE);
		a.setVersion(2);
		
		Aaa oldRecord = factory.selectFrom(t1).where(t1.id.eq(id)).fetchOne();
		factory.update(t1).populateWithCompare(a, oldRecord).where(t1.id.eq(id)).execute();
		
	}

	@Test
	public void testUpdateAll() {
		QAaa t1 = QAaa.aaa;
		//清理
		factory.getMetadataFactory().truncate(t1);
		
		factory.insert(t1).populate(generateEntity()).addBatch()
		.populate(generateEntity()).addBatch().execute();
		
		long count=factory.update(t1)
			.set(t1.created, JavaTimes.currentTimestamp())
			.set(t1.dataBigint, 2774689L)
			.where(Expressions.TRUE)
			.execute();
		assertEquals(2, count);
	}

	@Test
	public void testDeleteAll() {
		QAaa t1 = QAaa.aaa;
		factory.delete(t1).where(Expressions.TRUE).execute();
	}

	@Test
	public void testInertBatch() {
		QAaa t1 = QAaa.aaa;
		Aaa a = new Aaa();
		a.setTaskStatus(TaskStatus.RUNNING);
		a.setCreated(new Date().toInstant());
		a.setName("张三");
		a.setTrantField("aaaa");

		Aaa b = new Aaa();
		b.setName("王五");
		b.setTaskStatus(TaskStatus.RUNNING);
		b.setCreated(new Date().toInstant());
		b.setTrantField("bbbb");

		Aaa c = new Aaa();
		c.setName("sadfsfsdfs");
		c.setTaskStatus(TaskStatus.RUNNING);
		c.setCreated(new Date().toInstant());
		c.setTrantField("cccc");
		
		Aaa d = new Aaa();
		d.setName("李四");
		d.setTaskStatus(TaskStatus.RUNNING);
		d.setTrantField("dsaasdsa");
		d.setVersion(123);
		System.err.println("==== BATCH START ====");
		List<Integer> x = factory.insert(t1).populateBatch(Arrays.asList(a, b, c, d)).executeWithKeys(Integer.class);
		System.err.println(x);
	}

	@Test
	public void testUpdateBatch() {
		QAaa t1 = QAaa.aaa;
		long count = factory.update(t1).where(t1.name.eq("1")).set(t1.version, t1.version.add(1)).addBatch()
				.where(t1.name.eq("2")).set(t1.version, t1.version.add(2)).addBatch().execute();
		System.err.println(count);
	}

	@Test
	public void testDeleteBatch() throws SQLException {
		try(Connection conn=factory.getConnection()){
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
		int sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		System.err.println("update_time = " + data.getUpdateTime());

		// 查询数量
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();
		System.err.println("Count:" + count);

		// 查询数据，并支持流操作 (试验性功能，API还需要改进)
		List<String> entity = factory.select(ProjectionsAlter.map(t2.getProjection(), String.class, AvsUserAuthority::getDevId)).from(t2)
				.where(t2.id.eq(sid)).fetch();
		
		
		System.err.println(entity);

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
		int sid;
		System.err.println(factory.getMetadataFactory().getDatabaseProduct());
		System.err.println(factory.getMetadataFactory().getDatabaseVersion());
		
		if(factory.getMetadataFactory().getDatabaseProduct().startsWith("MySQL")) {
			sid = factory.asMySQL().insertIgnore(t2).populate(data).executeWithKey(t2.id);	
		}else {
			sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		}
		System.err.println(sid);
		
		CRUDRepository<AvsUserAuthority,Integer> repository=factory.asRepository(t2);
		AvsUserAuthority obj=repository.load(sid);
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
		a.setDataShort((short)-1);
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
		Pair<Integer,List<AvsUserAuthority>> result = factory.asRepository(t).findByCondition(p);
		System.err.println(result.getFirst());
		System.err.println(result.getSecond());
	}
	
	@Test
	public void testRouting() {
		QAaa t1=QAaa.aaa;
		QCaAsset t2=QCaAsset.caAsset;
		
		TableRouting routing=TableRouting.builder()
				.suffix(t1,"202406")
				.suffix(t2, "2024Q2")
				.build();
		boolean reCreateTable=true;
		if(reCreateTable){
			SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
			metadata.dropTable(t1).withRouting(routing).execute();
			metadata.createTable(t1).withRouting(routing).execute();
			metadata.dropTable(t2).withRouting(routing).execute();
			metadata.createTable(t2).withRouting(routing).execute();	
		}
		
		
		List<Tuple> tuples=factory.select(t1.name,t1.gender,t2.content,t2.code).from(t1)
				.withRouting(routing).innerJoin(t2).on(t1.id.eq(t2.id)).where(t1.name.eq("张三")).fetch();
		for(Tuple t:tuples) {
			System.out.println(t);
		}
	}
	
	@Test
	public void testRouting2() {
//		QAaa t1=QAaa.aaa;
		QCaAsset t2=QCaAsset.caAsset;
		
		TableRouting routing=TableRouting.suffix("2024Q2");
		boolean reCreateTable=true;
		if(reCreateTable){
			SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
			metadata.dropTable(t2).withRouting(routing).execute();
			metadata.createTable(t2).withRouting(routing).execute();	
		}
		
		
		List<Tuple> tuples=factory.select(t2.content,t2.code).from(t2).where(t2.name.eq("Test")).fetch();
		for(Tuple t:tuples) {
			System.out.println(t);
		}
	}
	
	@Test
	public void mysqlInsertOnDuplidateKey() {
		Assume.assumeTrue("Only for MYSQL",factory.getConfigurationEx().has(SpecialFeature.PARTITION_SUPPORT));
		QCaAsset t2 = QCaAsset.caAsset;
		CaAsset a=new CaAsset();
		a.setCode("a");
		a.setContent("test123");
		a.setGender(Gender.MALE);
		a.setMap(Collections.singletonMap("s", "b"));
		a.setName("LIU");
		//使用SQLExpressions.set是官方设计的，使用eq可以起到数值不变的效果，实际没啥必要。
		//SQLExpressions.set(path)，会变为 values(path)的效果，实际上引用了前文设置的新植，因此无需将数值再重复一遍。避免增加操作变量。
		factory.asMySQL().insertOnDuplicateKeyUpdate(t2,
				SQLExpressions.set(t2.name, t2.name),
				SQLExpressions.set(t2.ext, t2.ext),
				t2.gender.eq(Gender.FEMALE),
				t2.content.eq(t2.content)
		).populate(a).execute();
	}

	/*
	 * MyBatis-plus的用法打开了思路，可以使用实体类来表达一个表的元模型，
	 * 可以使用方法引用来表达一个元模型中的字段。
	 *  
	 *  即——
	 *  用Class来代替Query class。
	 *  用 Class::getSomeField()来代替中的静态字段模型。
	 *  这样依赖就可以省掉Query class，实现彻底的POJO Bean + Annotation进行数据表映射了。
	 *  虽然这种奇技_巧没啥意义，但还是很多人喜欢。
	 */
	@Test
	public void testPureBean() {
		LambdaTable<Foo> table = () -> Foo.class;
		LambdaColumn<Foo, String> p = Foo::getName;
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		metadata.createTable(table).ifExists().execute();
		List<Foo> list = factory.selectFrom(table).where(p.eq("1")).fetch();
	}
	
	/**
	 * 在支持了无需QueryClass的情况下，使用Repository界面，可以使用以下几种风格进行查询
	 * 五套风格，总有一套适合你。
	 */
	@Test
	public void testPureBean2() {
		/*
		 * 对于不喜欢SQLQueryFactory风格的用户。可以更换前端操作类。
		 * 当然更自由的方式是自己定义一个Repository，并通过SpringBean注入
		 * @Repository
		 * public class FooRepository extends AbstractCrudRepository<Foo,Long>{
		 * 
		 * }
		 */
		CRUDRepository<Foo, Integer> repo=factory.asRepository(()->Foo.class);

		//写法一，传统 repository风格，功能较弱，比如无法支持Between条件
		{
			Foo foo=new Foo();
			foo.setName("张三");
			foo.setCreated(new Date());
			repo.findByExample(foo);	
		}
		
		//写法二，MyBatis-Plus风格
		{
			LambdaQueryWrapper<Foo> wrapper=new LambdaQueryWrapper<>();
			wrapper
				.eq(Foo::getName, "张三")
				.between(Foo::getCreated, DateUtils.get(2023, 12, 1), new Date());
			
			Pair<Integer, List<Foo>> results=repo.findAndCount(wrapper);
		}
		
		
		//写法三，接近queryDSL原生风格，同时支持lambda
		{
			repo.query()
			.eq(Foo::getName, "张三")
			.between(Foo::getCreated, DateUtils.get(2023, 12, 1), new Date())
			.findAndCount();	
		}

		//写法四，QueryDSL风格
		{
			LambdaColumn<Foo, String> name = Foo::getName;
			LambdaColumn<Foo, Date> created = Foo::getCreated;
			List<Foo> list= repo.find(q->q.where(name.eq("张三").and(created.between(DateUtils.get(2023, 12, 1), new Date()))));	
		}
		
		//写法五，使用一个查询表单类
		{
			FooParams params=new FooParams();
			params.setName("张三");
			params.setCreated(new Date[] {DateUtils.get(2023, 12, 1), new Date()});
			repo.findByCondition(params);	
		}
		
	}
	
	/*
	 * 写法五中用到的查询表单类
	 */
	@Data
	@ConditionBean
	public static class FooParams{
		@Condition(Ops.STRING_CONTAINS)
		String name;
		
		@Condition(Ops.BETWEEN)
		Date[] created;
	}
	
	/**
	 * 打通普通bean,
	 * 1包扫描时支持普通的relationalPath注册。
	 * 2如果使用了querydsl原生的方式创建queryClz。那么这里会使用扫描到的entity path，从而不会对类进行反复解析。
	 */
	@Test
	public void testPureBean4() {
		/*
		 * 对于不喜欢SQLQueryFactory风格的用户。可以更换前端操作类。
		 * 当然更自由的方式是自己定义一个Repository，并通过SpringBean注入
		 * @Repository
		 * public class FooRepository extends AbstractCrudRepository<Foo,Long>{
		 * 
		 * }
		 */
		CRUDRepository<Aaa, Long> repo=factory.asRepository(()->Aaa.class);

		//写法一，传统 repository风格，功能较弱，比如无法支持Between条件
		{
			Aaa foo=new Aaa();
			foo.setName("张三");
			foo.setCreated(new Date().toInstant());
			repo.findByExample(foo);	
		}
		
		//写法二，MyBatis-Plus风格
		{
			LambdaQueryWrapper<Aaa> wrapper=new LambdaQueryWrapper<>();
			wrapper
				.eq(Aaa::getName, "张三")
				.between(Aaa::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
				.orderBy(
					$(Aaa::getCreated).asc(),
					$(Aaa::getId).desc()
				)
				.limit(10).offset(20);
			Pair<Integer, List<Aaa>> results=repo.findAndCount(wrapper);
		}
		
		
		//写法三，接近queryDSL原生风格，同时支持lambda
		{
			repo.query()
			.eq(Aaa::getName, "张三")
			.between(Aaa::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
			//.groupBy(Aaa::getGender,Aaa::getTaskStatus)
			.groupBy($(Aaa::getGender),s(Aaa::getName).upper())
			.findAndCount();	
		}

		//写法四，QueryDSL风格
		{
			LambdaColumn<Aaa, String> name = Aaa::getName;
			LambdaColumn<Aaa, Instant> created = Aaa::getCreated;
			List<Aaa> list= repo.find(q->q.where(name.eq("张三").and(created.between(DateUtils.getInstant(2023, 12, 1), Instant.now()))));	
		}
		
		//写法五，使用一个查询表单类
		{
			FooParams params=new FooParams();
			params.setName("张三");
			params.setCreated(new Date[] {DateUtils.get(2023, 12, 1), new Date()});
			repo.findByCondition(params);	
		}
	}
	
	//如果涉及较为复杂的函数和处理，就要一个接口进行辅助了
	@Test
	public void testPureBean5() {
		CRUDRepository<Aaa, Long> repo=factory.asRepository(()->Aaa.class);
		repo.query()
		.eq(Aaa::getName, "张三")
		.between(Aaa::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
		//.groupBy(Aaa::getGender,Aaa::getTaskStatus)
		.groupBy(column(Aaa::getGender),string(Aaa::getName).upper())
		.having(column(Aaa::getName).count().goe(15))
		.findAndCount();	
	}
	
	@Test
	public void testPureBean3() {
		SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
		metadata.dropTable(()->StateMachine.class).execute();
		metadata.createTable(()->StateMachine.class).execute();
		
		CRUDRepository<StateMachine, String> repo=factory.asRepository(()->StateMachine.class);
		StateMachine s=new StateMachine();
		s.setId(StringUtils.generateGuid());
		s.setEndParams("123");
		s.setGmtEnd(new Date());
		s.setGmtUpdated(new Date());
		s.setIsRunning(0);
		s.setMachineId("aa");
		s.setParentId("bb");
		repo.insert(s);
		
		List<StateMachine> list=repo.query().fetch();
		System.out.println(list);
		StateMachine indb=list.get(0);
		indb.setMachineId("456");
		repo.update(indb.getId(), indb);
		
		repo.delete(indb.getId());
	}
}
