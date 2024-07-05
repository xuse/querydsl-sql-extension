package com.github.xuse.querydsl.sql;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.AvsAuthParams;
import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.entity.CaAsset;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.entity.QAvsUserAuthority;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Column;

import lombok.Data;

public class DMLTest extends AbstractTestBase{
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
	public void reCreateTable() throws SQLException {
		SQLMetadataQueryFactory metadataFactory=factory.getMetadataFactory();
		metadataFactory.dropTable(QAaa.aaa).ifExists(true).execute();
		metadataFactory.dropTable(QAvsUserAuthority.avsUserAuthority).ifExists(true).execute();
		metadataFactory.dropTable(QCaAsset.caAsset).ifExists(true).execute();
		
		metadataFactory.createTable(QAaa.aaa).execute();
		metadataFactory.createTable(QAvsUserAuthority.avsUserAuthority).execute();;
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
		int sid = factory.insert(t2).set(t2.authContent, "abcdefg").set(t2.devId, "123").set(t2.userId,"ddefe").set(t2.createTime, new Date())
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

		factory.update(t2).set(t2.createTime, DateTimeExpression.currentTimestamp(Timestamp.class))
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
		factory.update(t1).set(t1.created, Expressions.currentTimestamp()).set(t1.name, "李四").where(t1.id.eq(1))
				.execute();
	}

	@Test
	public void testUpdateAll() {
		QAaa t1 = QAaa.aaa;
		//清理
		factory.getMetadataFactory().truncate(t1);
		
		factory.insert(t1).populate(generateEntity()).addBatch()
		.populate(generateEntity()).addBatch().execute();
		
		long count=factory.update(t1)
			.set(t1.created, Expressions.currentTimestamp())
			.set(t1.dataBigint, 2774689L)
			.where(Expressions.TRUE)
			.execute();
		assertTrue(count == 2);
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
		a.setCreated(new Date());
		a.setName("张三");
		a.setTrantField("aaaa");

		Aaa b = new Aaa();
		b.setName("王五");
		b.setTaskStatus(TaskStatus.RUNNING);
		b.setCreated(new Date());
		b.setTrantField("bbbb");

		Aaa c = new Aaa();
		c.setName("sadfsfsdfs");
		c.setTaskStatus(TaskStatus.RUNNING);
		c.setCreated(new Date());
		c.setTrantField("cccc");
		
		Aaa d = new Aaa();
		d.setName("李四");
		d.setTaskStatus(TaskStatus.RUNNING);
		d.setTrantField("dsaasdsa");
		d.setVersion(123);

		List<Integer> x = factory.insert(t1).populateBatch(Arrays.asList(a, b, c, d)).executeWithKeys(Integer.class);
		System.err.println(x);

		// 虽然成功执行了，日志看上去像是batch，但实际上是分4个语句执行的，没起到batch效果。
//		long num=insert.execute();
//		System.err.println(num);

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
		List<String> entity = factory.select(ProjectionsAlter.map(t2.getProjection(), String.class, e -> e.getDevId())).from(t2)
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
		List<AvsUserAuthority> list = factory.selectFrom(t2).fetch();
		for (AvsUserAuthority a : list) {
			System.err.println(a);
		}
	}

	@Test
	@Ignore
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

		factory.insert(t2).populate(data).set(t2.id, 2).addFlag(Position.START_OVERRIDE, "insert ignore ").execute();
		int sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		System.err.println(sid);

	}

	@Test
	public void testMerge() {
		QAaa t1 = QAaa.aaa;
		Aaa a = new Aaa();
//		a.setId(1);
		a.setName("张222");
		a.setGender(Gender.FEMALE);
		a.setCreated(new Timestamp(System.currentTimeMillis()));
		a.setVersion(12);
		a.setDataDouble(2.3d);
		int id = factory.insert(t1).populate(a).executeWithKey(Integer.class);
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
		p.setAuthType(0);
		p.setLimit(100);
		p.setOffset(2);
		p.setDateGt(new Date());
		p.setDateLoe(new Date());
		p.setCreateTime(new Date[] { new Date(0), new Date() });
		List<AvsUserAuthority> values = factory.selectFrom(t).where(p, t).fetch();
		System.err.println(values);
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
		
		TableRouting routing=TableRouting.suffix( "2024Q2");
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

}
