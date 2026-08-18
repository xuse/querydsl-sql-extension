package com.github.xuse.querydsl.sql.integration.db;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.entity.CaAsset;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.FooDTO;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.entity.TableDataTypes;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.lambda.DateLambdaColumn;
import com.github.xuse.querydsl.lambda.DateTimeLambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.expression.JavaTimes;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.SQLExpressions;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Column;

import lombok.Data;

/**
 * Core DML integration tests: insert, select, update, delete, batch operations, merge,
 * and custom type mapping.
 */
@SuppressWarnings("unused")
public class DMLTest extends AbstractTestBase implements LambdaHelpers {

	/** Test Tuple result with groupBy and projection to custom VO bean. */
	@Test
	public void testTupleResult() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		TableDataTypes a = generateEntity();
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

	private TableDataTypes generateEntity() {
		TableDataTypes a = new TableDataTypes();
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
		a.setStringArray(new String[] {"a","b","c"});
		return a;
	}

	@Data
	public static class VO {
		private String name;
		@Column("cnt")
		private Integer count;
	}

	/** Re-create all test tables from scratch. */
	@Test
	public void reCreateTable() {
		SQLMetadataQueryFactory metadataFactory = factory.getMetadataFactory();
		metadataFactory.dropTable(QTableDataTypes.aaa).ifExists(true).execute();
		metadataFactory.dropTable(() -> AvsUserAuthority.class).ifExists(true).execute();
		metadataFactory.dropTable(QCaAsset.caAsset).ifExists(true).execute();
		metadataFactory.dropTable(() -> Foo.class).ifExists(true).execute();
		metadataFactory.createTable(QTableDataTypes.aaa).execute();
		metadataFactory.createTable(() -> AvsUserAuthority.class).execute();
		metadataFactory.createTable(QCaAsset.caAsset).execute();
		metadataFactory.createTable(() -> Foo.class).execute();
	}

	/** Test basic selectFrom and raw ResultSet access. */
	@Test
	public void testSelect() throws SQLException {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		TableDataTypes a = generateEntity();
		factory.insert(t1).populate(a).execute();
		List<TableDataTypes> list = factory.selectFrom(t1).fetch();
		assertTrue(list.size() > 0);
		try (ResultSet rs = factory.selectFrom(t1).getResults()) {
			String str = SQLTypeUtils.toString(rs);
			System.err.println(str);
			char c = str.charAt(1);
			if (Character.isUpperCase(c)) {
				assertTrue(str.startsWith("ID, NAME, CREATED"));
			} else {
				assertTrue(str.startsWith("id, name, created"));
			}
		}
	}

	/** Comprehensive insert-query-update cycle with multiple tables and custom types. */
	@Test
	public void testGroup1() {
		boolean flag = false;
		QTableDataTypes t1 = QTableDataTypes.aaa;
		factory.getMetadataFactory().truncate(t1).execute();
		TableDataTypes a = generateEntity();
		a.setName("张三");
		Integer id = factory.insert(t1).populate(a).executeWithKey(Integer.class);
		if (flag) { return; }

		TableDataTypes b = factory.selectFrom(t1).where(t1.id.eq(id)).fetchFirst();
		assertArrayEquals(b.getStringArray(), new String[] {"a","b","c"});

		factory.update(t1).set(t1.taskStatus, TaskStatus.FAIL).set(t1.version, t1.version.add(Expressions.ONE))
				.where(t1.id.eq(id).and(t1.version.eq(b.getVersion()))).execute();
		a.setGender(Gender.MALE);

		b = factory.selectFrom(t1).where(
				t1.taskStatus.in(Arrays.asList(TaskStatus.FAIL, TaskStatus.INIT)).and(t1.gender.eq(Gender.FEMALE)))
				.fetchFirst();

		RelationalPathEx<AvsUserAuthority> t2 = PathCache.getPath(() -> AvsUserAuthority.class, null);
		NumberLambdaColumn<AvsUserAuthority, Integer> _id2 = AvsUserAuthority::getId;
		StringLambdaColumn<AvsUserAuthority> _userId2 = AvsUserAuthority::getUserId;
		AvsUserAuthority authData = new AvsUserAuthority();
		authData.setAuthContent("abcdefg");
		authData.setDevId("123");
		authData.setUserId("ddefe");
		authData.setCreateTime(LocalDateTime.now());
		authData.setUpdateTime("01/12/2019 12:30:21");
		authData.setGender(Gender.MALE);
		Integer sid = factory.insert(t2).populate(authData).executeWithKey(Integer.class);
		long count = factory.selectFrom(t2).where(_id2.eq(sid)).fetchCount();

		factory.selectFrom(t2).where(_id2.eq(sid)).fetchOne();
		factory.selectFrom(t2).where(_id2.eq(sid)).fetchFirst();
		factory.update(t1).set(t1.name, t1.name.concat("Abc123")).where(t1.id.eq(id)).execute();
		DateTimeLambdaColumn<AvsUserAuthority, LocalDateTime> _createTime2 = AvsUserAuthority::getCreateTime;
		factory.update(t2).set(_createTime2, DateTimeExpression.currentTimestamp(LocalDateTime.class))
				.where(_userId2.eq("1")).execute();
	}

	/** Test update via populate(). */
	@Test
	public void test2() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		TableDataTypes old = factory.selectFrom(t1).where(t1.id.eq(1)).fetchOne();
		TableDataTypes b = new TableDataTypes();
		b.setName("李四");
		b.setGender(Gender.MALE);
		b.setTaskStatus(TaskStatus.RUNNING);
		b.setVersion(51);
		long count = factory.update(t1).populate(b).where(t1.id.eq(1)).execute();
	}

	/** Test selectFrom with fetchResults (returns total count). */
	@Test
	public void test3() {
		RelationalPathEx<AvsUserAuthority> t2 = PathCache.getPath(() -> AvsUserAuthority.class, null);
		List<AvsUserAuthority> eee = factory.selectFrom(t2).fetch();
		QueryResults<AvsUserAuthority> results = factory.selectFrom(t2).fetchResults();
	}

	/** Test update with set() and populateWithCompare(). */
	@Test
	public void testUpdateSQL() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		Integer id = factory.select(t1.id.max()).from(t1).fetchFirst();
		long count = factory.update(t1).set(t1.created, JavaTimes.currentTimestamp()).set(t1.name, "李四")
				.where(t1.id.eq(id)).execute();
		assertTrue(count > 0);
		TableDataTypes a = new TableDataTypes();
		a.setName("Wang Wu");
		a.setGender(Gender.MALE);
		a.setVersion(2);
		TableDataTypes oldRecord = factory.selectFrom(t1).where(t1.id.eq(id)).fetchOne();
		factory.update(t1).populateWithCompare(a, oldRecord).where(t1.id.eq(id)).execute();
	}

	/** Test update all rows matching a condition. */
	@Test
	public void testUpdateAll() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		factory.getMetadataFactory().truncate(t1).execute();
		factory.insert(t1).populate(generateEntity()).addBatch().populate(generateEntity()).addBatch().execute();
		long count = factory.update(t1).set(t1.created, JavaTimes.currentTimestamp()).set(t1.dataBigint, 2774689L)
				.where(Expressions.TRUE).execute();
		assertEquals(2, count);
	}

	/** Test delete all rows. */
	@Test
	public void testDeleteAll() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		factory.delete(t1).where(Expressions.TRUE).execute();
	}

	/** Test batch insert with populateBatch(). */
	@Test
	public void testInsertBatch() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		factory.getMetadataFactory().truncate(t1).execute();
		TableDataTypes a = new TableDataTypes();
		a.setName("张三"); a.setGender(Gender.FEMALE); a.setTaskStatus(TaskStatus.RUNNING);
		a.setCreated(new Date().toInstant()); a.setTrantField("aaaa");
		TableDataTypes b = new TableDataTypes();
		b.setName("王五"); b.setGender(Gender.FEMALE); b.setTaskStatus(TaskStatus.RUNNING);
		b.setCreated(new Date().toInstant()); b.setTrantField("bbbb");
		TableDataTypes c = new TableDataTypes();
		c.setName("sadfsfsdfs"); c.setGender(Gender.MALE); c.setTaskStatus(TaskStatus.RUNNING);
		c.setCreated(new Date().toInstant()); c.setTrantField("cccc");
		TableDataTypes d = new TableDataTypes();
		d.setName("李四"); d.setGender(Gender.MALE); d.setTaskStatus(TaskStatus.RUNNING);
		d.setTrantField("dsaasdsa"); d.setVersion(123);
		
		
		List<TableDataTypes> list = Arrays.asList(a, b, c, d);
		List<Integer> ids = factory.insert(t1).populateBatch(list).executeWithKeys(Integer.class);
		assertEquals(list.size(), ids.size());
		for (int i = 0; i < ids.size(); i++) {
			list.get(i).setId(ids.get(i));
		}
		System.out.println(ids);
	}

	/** Test batch update with addBatch(). */
	@Test
	public void testUpdateBatch() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		long count = factory.update(t1)
				.where(t1.name.eq("1")).set(t1.version, t1.version.add(1)).addBatch()
				.where(t1.name.eq("2")).set(t1.version, t1.version.add(2)).addBatch()
				.execute();
	}

	/** Test batch delete with addBatch(). */
	@Test
	public void testDeleteBatch() throws SQLException {
		try (Connection conn = factory.getConnection()) {
			System.err.println("得到连接成功");
		}
		QTableDataTypes t1 = QTableDataTypes.aaa;
		long count = factory.delete(t1)
				.where(t1.name.eq("1aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
				.addBatch().where(t1.name.eq("2")).addBatch().execute();
	}

	/** Test insert and query with @CustomType mapped fields (JSON, encrypted, etc.). */
	@Test
	public void testComplexType() {
		RelationalPathEx<AvsUserAuthority> t2 = PathCache.getPath(() -> AvsUserAuthority.class, null);
		NumberLambdaColumn<AvsUserAuthority, Integer> _id2 = AvsUserAuthority::getId;
		AvsUserAuthority data = new AvsUserAuthority();
		data.setUserId("user-daslfnskfn23");
		data.setDevId("C12345678");
		data.setAuthContent("abcdefg");
		data.setGender(Gender.MALE);
		data.setMap(new HashMap<>());
		data.getMap().put("attr1", "测试属性");
		data.getMap().put("attr2", "男");
		CaAsset sub = new CaAsset();
		sub.setCode("123"); sub.setGender(Gender.FEMALE); sub.setName("李四");
		data.setAsserts(sub);
		Integer sid = factory.insert(t2).populate(data).executeWithKey(Integer.class);
		long count = factory.selectFrom(t2).where(_id2.eq(sid)).fetchCount();
		AvsUserAuthority d = factory.selectFrom(t2).where(_id2.eq(sid)).fetchOne();
		d = factory.selectFrom(t2).where(_id2.eq(sid)).fetchFirst();
	}

	/** Test selectFrom with orderBy. */
	@Test
	public void testFetchAll() {
		RelationalPathEx<AvsUserAuthority> t2 = PathCache.getPath(() -> AvsUserAuthority.class, null);
		NumberLambdaColumn<AvsUserAuthority, Integer> _authType = AvsUserAuthority::getAuthType;
		List<AvsUserAuthority> list = factory.selectFrom(t2).orderBy(_authType.asc()).fetch();
	}

	/** Test insert with platform-specific behavior (MySQL insertIgnore). */
	@Test
	public void testInsert() {
		RelationalPathEx<AvsUserAuthority> t2 = PathCache.getPath(() -> AvsUserAuthority.class, null);
		NumberLambdaColumn<AvsUserAuthority, Integer> _id2 = AvsUserAuthority::getId;
		AvsUserAuthority data = new AvsUserAuthority();
		data.setUserId("user-daslfnskfn23");
		data.setDevId("C12345678");
		data.setAuthContent("abcdefg");
		data.setUpdateTime("01/12/2019 13:30:21");
		data.setGender(Gender.MALE);
		data.setMap(new HashMap<>());
		data.getMap().put("attr1", "测试属性");
		data.getMap().put("attr2", "女");
		Integer sid;
		if (factory.getMetadataFactory().getDatabaseProduct().startsWith("MySQL")) {
			System.out.println("=============insertIgnore================");
			sid = factory.asMySQL().insertIgnore(t2).populate(data).executeWithKey(Integer.class);
			
			System.out.println(sid);
			
			System.out.println("=============insertOnDuplicateKeyUpdate================");
			//再测试一下insertOnDuplicateKeyUpdate
			//.setValues(AvsUserAuthority::getAuthContent)
			NumberLambdaColumn<AvsUserAuthority, Integer> _authType = AvsUserAuthority::getAuthType;
			StringLambdaColumn<AvsUserAuthority> _authContent = AvsUserAuthority::getAuthContent;
			sid = factory.asMySQL().insertOnDuplicateKeyUpdate(t2, SQLExpressions.set(_authType, _authType.add(Expressions.ONE)),
					SQLExpressions.setValues(_authContent))
					.populate(data)
					.executeWithKey(Integer.class);
			System.out.println(sid);
		} else {
			sid = factory.insert(t2).populate(data).executeWithKey(Integer.class);
		}
		CRUDRepository<AvsUserAuthority, Integer> repository = factory.asRepository(() -> AvsUserAuthority.class);
		AvsUserAuthority obj = repository.load(sid);
	}

	/** Test merge (upsert) operation. */
	@Test
	public void testMerge() {
		QTableDataTypes t1 = QTableDataTypes.aaa;
		TableDataTypes a = new TableDataTypes();
		a.setName("张222"); a.setGender(Gender.FEMALE);
		a.setCreated(new Timestamp(System.currentTimeMillis()).toInstant());
		a.setVersion(12); a.setDataInt(222); a.setDataDouble(2.3d);
		a.setTaskStatus(TaskStatus.FAIL);
		a.setDataFloat(-1f); a.setDataShort((short) -1); a.setDataBigint(-1);
		a.setDataDecimal(BigDecimal.ONE);
		a.setDataDate(new Date());
		a.setDataTime(new java.sql.Time(System.currentTimeMillis()));
		a.setDateTimestamp(new Date());
		int id = factory.insert(t1).writeNulls(true).populate(a).executeWithKey(Integer.class);
		Integer count = factory.merge(t1).keys(t1.id).columns(t1.id, t1.created, t1.gender)
				.values(id, new Date(), Gender.MALE).executeWithKey(t1.id);
	}
	
	@Data
	 static class Foo1{
		private String code;
		
		private String name;
		
		private int gender;
		
		private int volume = -1;

		private int codeType;
		
		private String inDay;
	}

	/**
	 * DTO for batch update: code is WHERE condition, volume is SET field.
	 */
	@Data
	static class UpdateVolumeByCode {
		@Condition
		private String code;

		private int volume;
	}

	/**
	 * DTO for batch delete: code is WHERE condition.
	 */
	@Data
	static class DeleteByCode {
		@Condition
		private String code;
	}
	
	@Test
	public void testFoo() {
		LambdaTable<Foo> t = () -> Foo.class;
		DateLambdaColumn<Foo, LocalDate> _InDay2 = Foo::getInDay2;
		StringLambdaColumn<Foo> _Code = Foo::getCode;

		// === Setup: clean table ===
		factory.getMetadataFactory().truncate(PathCache.getPath(t, null)).execute();

		// === 1. INSERT: LocalDate field mapped to TIMESTAMP column ===
		LocalDate today = LocalDate.of(2026, 1, 15);
		{
			Foo foo = new Foo();
			foo.setCode("FOO_DATE_1");
			foo.setCodeType(1);
			foo.setName("DateTest");
			foo.setInDay2(today);
			factory.insert(t).populate(foo).execute();	
		}
//		{
//			Foo foo = new Foo();
//			foo.setCode("FOO_DATE_1");
//			foo.setCodeType(1);
//			foo.setName("DateTest");
//			foo.setInDay2(today);
//			long v= factory.asMySQL().insertIgnore(t).populate(foo).execute();
//			System.out.println("IGNOR:"+v);
//		}
		
		
		
		
		// === 2. SELECT: verify LocalDate read back correctly ===
		Foo fetched = factory.selectFrom(t).where(_Code.eq("FOO_DATE_1")).fetchOne();
		assertEquals(today, fetched.getInDay2());
		assertEquals("DateTest", fetched.getName());

		// === 3. WHERE eq with LocalDate: the key scenario ===
		// DB stores '2026-01-15 00:00:00' as TIMESTAMP, verify eq with LocalDate works
		List<Foo> matched = factory.selectFrom(t).where(_InDay2.eq(today)).fetch();
		assertEquals(1, matched.size());
		assertEquals("FOO_DATE_1", matched.get(0).getCode());

		// Verify non-matching date returns empty
		List<Foo> noMatch = factory.selectFrom(t).where(_InDay2.eq(LocalDate.of(2026, 1, 16))).fetch();
		assertTrue(noMatch.isEmpty());

		// === 4. UPDATE: change the date field ===
		LocalDate newDate = LocalDate.of(2026, 3, 20);
		long updateCount = factory.update(t)
				.set(_InDay2, newDate)
				.where(_Code.eq("FOO_DATE_1"))
				.execute();
		assertEquals(1, updateCount);

		// Verify updated value and eq still works
		Foo afterUpdate = factory.selectFrom(t).where(_InDay2.eq(newDate)).fetchOne();
		assertEquals(newDate, afterUpdate.getInDay2());
		assertEquals("FOO_DATE_1", afterUpdate.getCode());

		// Old date should no longer match
		assertTrue(factory.selectFrom(t).where(_InDay2.eq(today)).fetch().isEmpty());

		// === 5. INSERT more records for range query ===
		Foo foo2 = new Foo();
		foo2.setCode("FOO_DATE_2");
		foo2.setCodeType(2);
		foo2.setName("DateTest2");
		foo2.setInDay2(LocalDate.of(2026, 3, 25));
		factory.insert(t).populate(foo2).execute();

		// === 6. DELETE by date condition ===
		long deleteCount = factory.delete(t).where(_InDay2.eq(newDate)).execute();
		assertEquals(1, deleteCount);

		// Only FOO_DATE_2 remains
		List<Foo> remaining = factory.selectFrom(t).fetch();
		assertEquals(1, remaining.size());
		assertEquals("FOO_DATE_2", remaining.get(0).getCode());

		// === 7. TIMESTAMP(3) millisecond precision: inDay3 field ===
		// Column is datetime(3)/timestamp(3), but Java type is still LocalDate.
		// Verify that eq comparison works even when DB column has sub-second precision.
		factory.getMetadataFactory().truncate(PathCache.getPath(t, null)).execute();

		DateLambdaColumn<Foo, LocalDate> _InDay3 = Foo::getInDay3;
		LocalDate day = LocalDate.of(2026, 6, 10);
		Foo foo3 = new Foo();
		foo3.setCode("FOO_MS_1");
		foo3.setCodeType(1);
		foo3.setName("MillisTest");
		foo3.setInDay3(day);
		factory.insert(t).populate(foo3).execute();

		// Read back: should still be the same LocalDate
		Foo fetchedMs = factory.selectFrom(t).where(_Code.eq("FOO_MS_1")).fetchOne();
		assertEquals(day, fetchedMs.getInDay3());

		// eq with LocalDate on TIMESTAMP(3) column: '2026-06-10 00:00:00.000' == '2026-06-10'
		List<Foo> msMatched = factory.selectFrom(t).where(_InDay3.eq(day)).fetch();
		assertEquals(1, msMatched.size());
		assertEquals("FOO_MS_1", msMatched.get(0).getCode());

		// Different date should not match
		assertTrue(factory.selectFrom(t).where(_InDay3.eq(LocalDate.of(2026, 6, 11))).fetch().isEmpty());

		// Update and verify eq on TIMESTAMP(3)
		LocalDate newDay = LocalDate.of(2026, 12, 31);
		factory.update(t).set(_InDay3, newDay).where(_Code.eq("FOO_MS_1")).execute();
		Foo updatedMs = factory.selectFrom(t).where(_InDay3.eq(newDay)).fetchOne();
		assertEquals(newDay, updatedMs.getInDay3());
	}
	
	
	
	@Test
	public void testOperateWithDto2() {
		RelationalPathEx<Foo> qFoo = PathCache.getPath(() -> Foo.class, null);
		StringLambdaColumn<Foo> _Code = Foo::getCode;
		factory.getMetadataFactory().truncate(qFoo).execute();

		// === 1. INSERT single DTO (no id, business key = code) ===
		Foo1 f1=new Foo1();
		f1.setCode("TEST_CODE_1");
		f1.setName("Jan");
		f1.setGender(1);
		f1.setInDay("2026-02-01");
		f1.setVolume(100);
		f1.setCodeType(3);
		factory.insert(qFoo).populate(f1).execute();

		// Verify insert via entity query
		Foo inserted = factory.selectFrom(qFoo).where(_Code.eq("TEST_CODE_1")).fetchOne();
		assertEquals("Jan", inserted.getName());
		assertEquals(Gender.FEMALE, inserted.getGender()); // code=1 -> FEMALE
		assertEquals(100, inserted.getVolume());

		// === 2. SELECT as DTO (read path: Gender->int, sql.Date->String) ===
		List<Foo1> dtoList = factory.select(ProjectionsAlter.bean(Foo1.class, qFoo))
				.from(qFoo).where(_Code.eq("TEST_CODE_1")).fetch();
		assertEquals(1, dtoList.size());
		Foo1 readBack = dtoList.get(0);
		assertEquals("TEST_CODE_1", readBack.getCode());
		assertEquals("Jan", readBack.getName());
		assertEquals(1, readBack.getGender());
		assertEquals(100, readBack.getVolume());
		assertEquals("2026-02-01", readBack.getInDay());

		// === 3. UPDATE via DTO (use code as where condition) ===
		System.out.println("3. UPDATE via DTO (use code as where condition)");
		Foo1 updateDto=new Foo1();
		updateDto.setName("January");
		updateDto.setGender(0);
		updateDto.setInDay("2026-03-15");
		updateDto.setCodeType(5);
		
		//updateDto.setVolume(200);
		long updateCount = factory.update(qFoo)
				.populate(updateDto)
				.where(_Code.eq("TEST_CODE_1"))
				.execute();
		assertEquals(1, updateCount);

		// Verify update
		Foo updated = factory.selectFrom(qFoo).where(_Code.eq("TEST_CODE_1")).fetchOne();
		assertEquals("January", updated.getName());
		assertEquals(Gender.MALE, updated.getGender()); // code=0 -> MALE
		assertEquals(100, updated.getVolume());

		// === 4. BATCH INSERT via DTO collection ===
		System.out.println("4. BATCH INSERT via DTO collection");
		Foo1 f2=new Foo1();
		f2.setCode("TEST_CODE_2");
		f2.setName("Feb");
		f2.setGender(0);
		f2.setInDay("2026-04-01");
		f2.setVolume(50);
		f2.setCodeType(1);

		Foo1 f3=new Foo1();
		f3.setCode("TEST_CODE_3");
		f3.setName("Mar");
		f3.setGender(1);
		f3.setInDay("2026-05-01");
		f3.setVolume(75);
		f3.setCodeType(2);

		factory.insert(qFoo).populateBatch(Arrays.asList(f2, f3)).execute();

		// Verify batch insert
		long totalCount = factory.selectFrom(qFoo).fetchCount();
		assertEquals(3, totalCount);

		// === 5. SELECT all as DTO, verify batch results ===
		System.out.println("5. SELECT all as DTO, verify batch results");
		List<Foo1> allDtos = factory.select(ProjectionsAlter.bean(Foo1.class, qFoo))
				.from(qFoo).orderBy(_Code.asc()).fetch();
		assertEquals(3, allDtos.size());
		assertEquals("January", allDtos.get(0).getName());  // TEST_CODE_1 (updated)
		assertEquals("Feb", allDtos.get(1).getName());       // TEST_CODE_2
		assertEquals("Mar", allDtos.get(2).getName());       // TEST_CODE_3

		// === 6. BATCH UPDATE via applyBatch (update volume by code) ===
		System.out.println("6. BATCH UPDATE via applyBatch (update volume by code)");
		UpdateVolumeByCode u1 = new UpdateVolumeByCode();
		u1.setCode("TEST_CODE_1");
		u1.setVolume(999);

		UpdateVolumeByCode u2 = new UpdateVolumeByCode();
		u2.setCode("TEST_CODE_3");
		u2.setVolume(888);

		long batchUpdateCount = factory.update(qFoo)
				.applyBatch(Arrays.asList(u1, u2)).execute();
		assertTrue(batchUpdateCount >= 2);

		// Verify batch update results
		List<Foo1> afterUpdate = factory.select(ProjectionsAlter.bean(Foo1.class, qFoo))
				.from(qFoo).orderBy(_Code.asc()).fetch();
		assertEquals(999, afterUpdate.get(0).getVolume()); // TEST_CODE_1
		assertEquals(888, afterUpdate.get(2).getVolume()); // TEST_CODE_3

		// === 7. BATCH DELETE via applyBatch (delete by code) ===
		System.out.println("7. BATCH DELETE via applyBatch (delete by code)");
		DeleteByCode d1 = new DeleteByCode();
		d1.setCode("TEST_CODE_2");

		DeleteByCode d2 = new DeleteByCode();
		d2.setCode("TEST_CODE_3");

		long batchDeleteCount = factory.delete(qFoo)
				.applyBatch(Arrays.asList(d1, d2)).execute();
		assertTrue(batchDeleteCount >= 2);

		// Verify: only TEST_CODE_1 remains
		long remaining = factory.selectFrom(qFoo).fetchCount();
		assertEquals(1, remaining);
		Foo1 last = factory.select(ProjectionsAlter.bean(Foo1.class, qFoo))
				.from(qFoo).fetchFirst();
		assertEquals("TEST_CODE_1", last.getCode());
		assertEquals(999, last.getVolume());
	}
	
	@Test
	public void testOperateWithDto() {
		RelationalPathEx<Foo> qFoo = PathCache.getPath(() -> Foo.class, null);

		
		
		NumberLambdaColumn<Foo, Integer> _Id = Foo::getId;
		StringLambdaColumn<Foo> _Code = Foo::getCode;
		
		// === Setup: ensure table exists and is clean ===
		factory.getMetadataFactory().truncate(qFoo).execute();

		// === 1. INSERT via DTO (auto-detected @PathBinder wrapping) ===
		FooDTO insertDto = new FooDTO();
		insertDto.setCode("DTO-001");
		insertDto.setName("Test Insert");
		insertDto.setGender(Gender.MALE);
		insertDto.setVolume(100);
		insertDto.setCodeTypeX("42");
		insertDto.setVersion(1);

		Integer id = factory.insert(qFoo).populate(insertDto).executeWithKey(Integer.class);
		assertTrue(id != null && id > 0, "Insert should return generated key");

		// === 2. SELECT: verify inserted data via entity ===
		Foo inserted = factory.selectFrom(qFoo).where(
				_Id.eq(id)).fetchOne();
		assertEquals("DTO-001", inserted.getCode());
		assertEquals("Test Insert", inserted.getName());
		assertEquals(Gender.MALE, inserted.getGender());
		assertEquals(100, inserted.getVolume());
		// codeTypeX("42") -> codeType column (String to int depends on DB implicit conversion)

		// === 3. SELECT as DTO (QBeanExWithConverter read path) ===
		List<FooDTO> dtoList = factory.select(ProjectionsAlter.bean(FooDTO.class, qFoo))
				.from(qFoo).fetch();
		assertEquals(1, dtoList.size());
		FooDTO readBack = dtoList.get(0);
		assertEquals("DTO-001", readBack.getCode());
		assertEquals("Test Insert", readBack.getName());
		assertEquals(Gender.MALE, readBack.getGender());
		assertEquals(id, readBack.getId());

		// === 4. UPDATE via DTO ===
		FooDTO updateDto = new FooDTO();
		updateDto.setId(id);
		updateDto.setCode("DTO-001");
		updateDto.setName("Updated Name");
		updateDto.setGender(Gender.FEMALE);
		updateDto.setVolume(200);
		updateDto.setVersion(1);
		updateDto.setCodeTypeX("99");

		long updateCount = factory.update(qFoo).populate(updateDto, true).execute();
		assertEquals(1, updateCount);

		// Verify update result
		Foo updated = factory.selectFrom(qFoo).where(
				_Id.eq(id)).fetchOne();
		assertEquals("Updated Name", updated.getName());
		assertEquals(Gender.FEMALE, updated.getGender());
		assertEquals(200, updated.getVolume());

		// === 5. BATCH INSERT via DTO collection ===
		FooDTO batch1 = new FooDTO();
		batch1.setCode("DTO-B01");
		batch1.setName("Batch One");
		batch1.setGender(Gender.MALE);
		batch1.setVolume(10);
		batch1.setVersion(1);
		batch1.setCodeTypeX("1");

		FooDTO batch2 = new FooDTO();
		batch2.setCode("DTO-B02");
		batch2.setName("Batch Two");
		batch2.setGender(Gender.FEMALE);
		batch2.setVolume(20);
		batch2.setVersion(1);
		batch2.setCodeTypeX("2");

		List<Integer> batchIds = factory.insert(qFoo)
				.populateBatch(Arrays.asList(batch1, batch2))
				.executeWithKeys(Integer.class);
		assertEquals(2, batchIds.size());

		// Verify batch insert
		long totalCount = factory.selectFrom(qFoo).fetchCount();
		assertEquals(3, totalCount); // 1 from single insert + 2 from batch

		// === 6. SELECT all as DTO, verify batch results ===
		List<FooDTO> allDtos = factory.select(ProjectionsAlter.bean(FooDTO.class, qFoo))
				.from(qFoo).orderBy(_Id.asc()).fetch();
		assertEquals(3, allDtos.size());
		assertEquals("Updated Name", allDtos.get(0).getName());
		assertEquals("Batch One", allDtos.get(1).getName());
		assertEquals("Batch Two", allDtos.get(2).getName());

		// === 7. DELETE and verify ===
		long deleted = factory.delete(qFoo).where(
				_Code.eq("DTO-B01")).execute();
		assertEquals(1, deleted);

		long remaining = factory.selectFrom(qFoo).fetchCount();
		assertEquals(2, remaining);
	}
}