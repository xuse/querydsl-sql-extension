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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.query.PathBinder;
import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.entity.CaAsset;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.FooDTO;
import com.github.xuse.querydsl.entity.QAvsUserAuthority;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.entity.TableDataTypes;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.JavaTimes;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
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
		metadataFactory.dropTable(QAvsUserAuthority.avsUserAuthority).ifExists(true).execute();
		metadataFactory.dropTable(QCaAsset.caAsset).ifExists(true).execute();
		metadataFactory.dropTable(() -> Foo.class).ifExists(true).execute();
		metadataFactory.createTable(QTableDataTypes.aaa).execute();
		metadataFactory.createTable(QAvsUserAuthority.avsUserAuthority).execute();
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
		factory.update(t1).populate(a, AdvancedMapper.ofNullsBinding(0), false).where(Expressions.TRUE).execute();

		b = factory.selectFrom(t1).where(
				t1.taskStatus.in(Arrays.asList(TaskStatus.FAIL, TaskStatus.INIT)).and(t1.gender.eq(Gender.FEMALE)))
				.fetchFirst();

		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		Integer sid = factory.insert(t2).set(t2.authContent, "abcdefg").set(t2.devId, "123").set(t2.userId, "ddefe")
				.set(t2.createTime, LocalDateTime.now()).set(t2.updateTime, "01/12/2019 12:30:21")
				.set(t2.gender, Gender.MALE).executeWithKey(t2.id);
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();

		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchOne();
		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchFirst();
		factory.update(t1).set(t1.name, t1.name.concat("Abc123")).where(t1.id.eq(id)).execute();
		factory.update(t2).set(t2.createTime, DateTimeExpression.currentTimestamp(LocalDateTime.class))
				.where(t2.userId.eq("1")).execute();
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
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
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
		List<Integer> x = factory.insert(t1).writeNulls(false).populateBatch(Arrays.asList(a, b, c, d))
				.executeWithKeys(Integer.class);
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
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
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
		Integer sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();
		AvsUserAuthority d = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchOne();
		d = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchFirst();
	}

	/** Test selectFrom with orderBy. */
	@Test
	public void testFetchAll() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		List<AvsUserAuthority> list = factory.selectFrom(t2).orderBy(t2.authType.asc()).fetch();
	}

	/** Test insert with platform-specific behavior (MySQL insertIgnore). */
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
		Integer sid;
		if (factory.getMetadataFactory().getDatabaseProduct().startsWith("MySQL")) {
			sid = factory.asMySQL().insertIgnore(t2).populate(data).executeWithKey(t2.id);
		} else {
			sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		}
		CRUDRepository<AvsUserAuthority, Integer> repository = factory.asRepository(t2);
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
		
		private int volume;

		private int codeType;
		
		@PathBinder(fromDbRef = "fromSqlDate", toDbRef = "toSqlDate")
		private String inDay;
		
		static final Function<String,java.sql.Date> toSqlDate = s-> java.sql.Date.valueOf(s);
		static final Function<java.sql.Date,String> fromSqlDate = s->s.toString();
	}
	
	@Test
	public void testOperateWithDto2() {
		RelationalPathEx<Foo> qFoo = PathCache.getPath(() -> Foo.class, null);
		NumberLambdaColumn<Foo, Integer> _Id = Foo::getId;
		StringLambdaColumn<Foo> _Code = Foo::getCode;
		factory.getMetadataFactory().truncate(qFoo).execute();
		
		Foo1 f1=new Foo1();
		f1.setCode("TEST_CODE_1");
		f1.setName("Jan");
		f1.setGender(1);
		f1.setInDay("2026-02-01");
		f1.setVolume(100);
		//f1.setCodeType(0);
		
		Foo1 f2=new Foo1();
		f2.setCode("TEST_CODE_2");
		f2.setName("Feb");
		f2.setGender(0);
		f2.setInDay("2026-02-01");
		f2.setVolume(100);
		factory.insert(qFoo).populate(f1).execute();
		
		
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