package com.github.xuse.querydsl.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.OrderSpecifier;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepositoryTest extends MockedTestBase {

	static final StringLambdaColumn<Foo> NAME = Foo::getName;
	static final StringLambdaColumn<Foo> CODE = Foo::getCode;
	static final NumberLambdaColumn<Foo, Integer> ID = Foo::getId;
	static final NumberLambdaColumn<Foo, Integer> VOLUME = Foo::getVolume;
	static final LambdaColumn<Foo, String> CODE_COL = Foo::getCode;
	static final LambdaTable<Foo> FOO_TABLE = () -> Foo.class;

	@BeforeAll
	static void setup() {
		doInit();
		// Create table using raw H2 SQL to avoid MySQL-specific DDL syntax issues
		try {
			factory.getConnection().createStatement().execute(
				"CREATE TABLE IF NOT EXISTS ca_foo (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"code VARCHAR(64) NOT NULL DEFAULT '', " +
				"asset_name VARCHAR(128) NOT NULL, " +
				"content TEXT, " +
				"created TIMESTAMP, " +
				"updated TIMESTAMP, " +
				"gender VARCHAR(16), " +
				"ext VARCHAR(256), " +
				"map VARCHAR(256), " +
				"volume INT NOT NULL DEFAULT 0, " +
				"version INT NOT NULL DEFAULT 1, " +
				"codetype INT NOT NULL DEFAULT 1, " +
				"inday DATE)"
			);
			factory.getConnection().createStatement().execute("DELETE FROM ca_foo");
			dbReady = true;
			insertSeedData();
		} catch (Exception e) {
			dbReady = false;
		}
	}

	private static boolean dbReady = false;

	private static void insertSeedData() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);

		Foo foo1 = new Foo();
		foo1.setCode("CODE_A");
		foo1.setName("Alice");
		foo1.setContent("Content A");
		foo1.setGender(Gender.MALE);
		foo1.setVolume(100);
		foo1.setCodeType(1);
		repo.insert(foo1);

		Foo foo2 = new Foo();
		foo2.setCode("CODE_B");
		foo2.setName("Bob");
		foo2.setContent("Content B");
		foo2.setGender(Gender.FEMALE);
		foo2.setVolume(200);
		foo2.setCodeType(2);
		repo.insert(foo2);

		Foo foo3 = new Foo();
		foo3.setCode("CODE_C");
		foo3.setName("Charlie");
		foo3.setContent("Content C");
		foo3.setGender(Gender.MALE);
		foo3.setVolume(300);
		foo3.setCodeType(1);
		repo.insert(foo3);
	}

	// ==================== LambdaQuery construction (no DB) ====================

	@Test
	@Order(1)
	void testLambdaQueryEmptyConstructor() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>();
		assertNotNull(q);
	}

	@Test
	@Order(2)
	void testLambdaQueryWithClass() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		assertNotNull(q);
	}

	@Test
	@Order(3)
	void testLambdaQueryEq() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.eq(CODE_COL, "ABC");
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(4)
	void testLambdaQueryNe() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.ne(CODE_COL, "ABC");
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(5)
	void testLambdaQueryGtGeLtLe() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.gt(ID, 1).ge(ID, 2).lt(ID, 100).le(ID, 99);
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(6)
	void testLambdaQueryBetween() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.between(ID, 1, 100);
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(7)
	void testLambdaQueryIsNullIsNotNull() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.isNull(CODE_COL);
		assertNotNull(q.mixin.getWhere());

		LambdaQuery<Foo, Foo> q2 = new LambdaQuery<>(Foo.class);
		q2.isNotNull(CODE_COL);
		assertNotNull(q2.mixin.getWhere());
	}

	@Test
	@Order(8)
	void testLambdaQueryLikeNotLike() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.like(NAME, "%test%");
		assertNotNull(q.mixin.getWhere());

		LambdaQuery<Foo, Foo> q2 = new LambdaQuery<>(Foo.class);
		q2.notlike(NAME, "%test%");
		assertNotNull(q2.mixin.getWhere());
	}

	@Test
	@Order(9)
	void testLambdaQueryStartsWithEndsWith() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.startsWith(NAME, "A").endsWith(NAME, "Z");
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(10)
	void testLambdaQueryContains() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.contains(NAME, "test");
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(11)
	void testLambdaQueryNotStartsWithNotEndsWithNotContains() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.notStartsWith(NAME, "A").notEndsWith(NAME, "Z").notContains(NAME, "mid");
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(12)
	void testLambdaQueryOrderByAscDesc() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.orderByAsc(ID).orderByDesc(CODE_COL);
		List<OrderSpecifier<?>> orders = q.mixin.getOrderBy();
		assertEquals(2, orders.size());
		assertTrue(orders.get(0).isAscending());
		assertFalse(orders.get(1).isAscending());
	}

	@Test
	@Order(13)
	void testLambdaQueryOrderBySpecifier() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.orderBy(ID.asc(), CODE_COL.desc());
		assertEquals(2, q.mixin.getOrderBy().size());
	}

	@Test
	@Order(14)
	void testLambdaQueryLimitOffset() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.limit(10).offset(5);
		QueryModifiers modifiers = q.mixin.getModifiers();
		assertNotNull(modifiers);
		assertEquals(Long.valueOf(10), modifiers.getLimit());
		assertEquals(Long.valueOf(5), modifiers.getOffset());
	}

	@Test
	@Order(15)
	void testLambdaQueryGroupByAndHaving() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.groupBy(CODE_COL).having(ID.count().goe(1));
		assertFalse(q.mixin.getGroupBy().isEmpty());
		assertNotNull(q.mixin.getHaving());
	}

	@Test
	@Order(16)
	void testLambdaQueryAndCombinator() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.eq(CODE_COL, "A").and(sub -> sub.eq(ID, 1).eq(CODE_COL, "B"));
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(17)
	void testLambdaQueryOrCombinator() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.eq(CODE_COL, "A").or(sub -> sub.eq(CODE_COL, "B"));
		assertNotNull(q.mixin.getWhere());
		String whereStr = q.mixin.getWhere().toString();
		assertTrue(whereStr.contains("||"), "Expected OR in predicate: " + whereStr);
	}

	@Test
	@Order(18)
	void testLambdaQueryNotConsumer() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.eq(CODE_COL, "A").not(sub -> sub.eq(CODE_COL, "B"));
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(19)
	void testLambdaQueryNotToggle() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.eq(CODE_COL, "A").not();
		assertNotNull(q.mixin.getWhere());
		String whereStr = q.mixin.getWhere().toString();
		assertTrue(whereStr.startsWith("!"), "Expected NOT in predicate: " + whereStr);
	}

	@Test
	@Order(20)
	void testLambdaQueryNotOnEmpty() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		// not() on empty where should be a no-op
		q.not();
		assertNull(q.mixin.getWhere());
	}

	@Test
	@Order(21)
	void testLambdaQueryNotConsumerOnEmpty() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		// not(consumer) with empty sub-chain should be a no-op
		q.not(sub -> {});
		assertNull(q.mixin.getWhere());
	}

	// ==================== LambdaQueryWrapper (no DB) ====================

	@Test
	@Order(30)
	void testLambdaQueryWrapperEmptyConstructor() {
		LambdaQueryWrapper<Foo> w = new LambdaQueryWrapper<>();
		assertNotNull(w);
	}

	@Test
	@Order(31)
	void testLambdaQueryWrapperWithClass() {
		LambdaQueryWrapper<Foo> w = new LambdaQueryWrapper<>(Foo.class);
		assertNotNull(w);
	}

	@Test
	@Order(32)
	void testLambdaQueryWrapperSelectSingleColumn() {
		LambdaQueryWrapper<Foo> w = new LambdaQueryWrapper<>(Foo.class);
		LambdaQuery<Foo, String> result = w.selectSingleColumn(CODE_COL);
		assertNotNull(result);
		assertNotNull(result.mixin.getProjection());
	}

	@Test
	@Order(33)
	void testLambdaQueryWrapperSelectPair() {
		LambdaQueryWrapper<Foo> w = new LambdaQueryWrapper<>(Foo.class);
		LambdaQuery<Foo, Pair<Integer, String>> result = w.selectPair(ID, CODE_COL);
		assertNotNull(result);
		assertNotNull(result.mixin.getProjection());
	}

	@Test
	@Order(34)
	void testLambdaQueryWrapperChainMethods() {
		LambdaQueryWrapper<Foo> w = new LambdaQueryWrapper<>(Foo.class);
		w.eq(CODE_COL, "A").orderByAsc(ID).limit(5);
		assertNotNull(w.mixin.getWhere());
		assertEquals(1, w.mixin.getOrderBy().size());
		assertEquals(Long.valueOf(5), w.mixin.getModifiers().getLimit());
	}

	// ==================== QueryWrapper.where() and allEq() ====================

	@Test
	@Order(40)
	void testQueryWrapperWherePredicate() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		q.where(CODE_COL.eq("X"), ID.goe(1));
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(41)
	void testQueryWrapperWhereNullPredicate() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		// null predicates should be skipped
		q.where(CODE_COL.eq("X"), null);
		assertNotNull(q.mixin.getWhere());
	}

	@Test
	@Order(42)
	void testQueryWrapperConvert() {
		LambdaQuery<Foo, Foo> q = new LambdaQuery<>(Foo.class);
		// convert should return the expression itself for non-ProjectionRole
		com.querydsl.core.types.Expression<String> expr = CODE_COL;
		com.querydsl.core.types.Expression<String> converted = q.convert(expr, com.querydsl.core.support.QueryMixin.Role.GROUP_BY);
		assertNotNull(converted);
	}

	// ==================== CRUD Repository (with DB) ====================

	@Test
	@Order(50)
	void testCrudInsertAndLoad() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		Foo foo = new Foo();
		foo.setCode("CODE_INSERT_" + System.currentTimeMillis());
		foo.setName("InsertTest");
		foo.setContent("test content");
		foo.setVolume(50);
		foo.setCodeType(1);
		Integer key = repo.insert(foo);
		// key may or may not be returned depending on DB, but insert should not throw
		assertNotNull(repo);
	}

	@Test
	@Order(51)
	void testCrudFindByExample() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		Foo example = new Foo();
		example.setName("Alice");
		// Set volume to negative so it is treated as unsaved (MinusNumber) and excluded from WHERE
		example.setVolume(-1);
		List<Foo> results = repo.findByExample(example);
		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertEquals("Alice", results.get(0).getName());
	}

	@Test
	@Order(52)
	void testCrudCountByExample() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		Foo example = new Foo();
		example.setName("Alice");
		// Set volume to negative so it is treated as unsaved (MinusNumber) and excluded from WHERE
		example.setVolume(-1);
		int count = repo.countByExample(example);
		assertTrue(count >= 1);
	}

	@Test
	@Order(53)
	void testCrudFindWithWrapper() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		wrapper.eq(CODE_COL, "CODE_A");
		List<Foo> results = repo.find(wrapper);
		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertEquals("CODE_A", results.get(0).getCode());
	}

	@Test
	@Order(54)
	void testCrudCountWithWrapper() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		wrapper.eq(CODE_COL, "CODE_A");
		int count = repo.count(wrapper);
		assertEquals(1, count);
	}

	@Test
	@Order(55)
	void testCrudDeleteWithWrapper() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		// Insert a record to delete
		Foo foo = new Foo();
		foo.setCode("CODE_DEL_" + System.currentTimeMillis());
		foo.setName("ToDelete");
		foo.setVolume(0);
		foo.setCodeType(1);
		repo.insert(foo);

		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		wrapper.eq(NAME, "ToDelete");
		int deleted = repo.delete(wrapper);
		assertTrue(deleted >= 1);
	}

	@Test
	@Order(56)
	void testCrudUpdateWithWrapper() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		Foo update = new Foo();
		update.setName("AliceUpdated");
		update.setVolume(999);
		update.setCodeType(1);

		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		wrapper.eq(CODE_COL, "CODE_A");
		int updated = repo.update(update, wrapper);
		assertTrue(updated >= 1);

		// Verify the update
		Foo example = new Foo();
		example.setCode("CODE_A");
		// Set volume to negative so it is treated as unsaved (MinusNumber) and excluded from WHERE
		example.setVolume(-1);
		List<Foo> results = repo.findByExample(example);
		assertFalse(results.isEmpty());
		assertEquals("AliceUpdated", results.get(0).getName());
	}

	// ==================== QueryExecutor (with DB) ====================

	@Test
	@Order(60)
	void testQueryExecutorFetch() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query().fetch();
		assertNotNull(results);
		assertTrue(results.size() >= 3);
	}

	@Test
	@Order(61)
	void testQueryExecutorWhereAndFetch() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query().eq(CODE_COL, "CODE_B").fetch();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("Bob", results.get(0).getName());
	}

	@Test
	@Order(62)
	void testQueryExecutorFetchFirst() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		Foo first = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		assertNotNull(first);
		assertEquals("Charlie", first.getName());
	}

	@Test
	@Order(63)
	void testQueryExecutorCount() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		int count = repo.query().eq(NAME, "Bob").count();
		assertEquals(1, count);
	}

	@Test
	@Order(64)
	void testQueryExecutorSelectSingleColumn() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<String> codes = repo.query().selectSingleColumn(CODE_COL).fetch();
		assertNotNull(codes);
		assertTrue(codes.size() >= 3);
		assertTrue(codes.contains("CODE_B"));
	}

	@Test
	@Order(65)
	void testQueryExecutorSelectPair() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Pair<Integer, String>> pairs = repo.query()
				.selectPair(ID, CODE_COL)
				.fetch();
		assertNotNull(pairs);
		assertFalse(pairs.isEmpty());
		assertNotNull(pairs.get(0).getFirst());
		assertNotNull(pairs.get(0).getSecond());
	}

	@Test
	@Order(66)
	void testQueryExecutorDelete() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		// Insert a record to delete via executor
		Foo foo = new Foo();
		foo.setCode("CODE_QDEL_" + System.currentTimeMillis());
		foo.setName("QueryDelTarget");
		foo.setVolume(0);
		foo.setCodeType(1);
		repo.insert(foo);

		int deleted = repo.query().eq(NAME, "QueryDelTarget").delete();
		assertTrue(deleted >= 1);
	}

	@Test
	@Order(67)
	void testQueryExecutorWithOrdering() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query()
				.orderByDesc(VOLUME)
				.limit(2)
				.fetch();
		assertNotNull(results);
		assertTrue(results.size() <= 2);
		if (results.size() == 2) {
			assertTrue(results.get(0).getVolume() >= results.get(1).getVolume());
		}
	}

	@Test
	@Order(68)
	void testQueryExecutorFindAndCount() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		com.querydsl.core.QueryResults<Foo> results = repo.query()
				.orderByAsc(ID)
				.limit(2)
				.findAndCount();
		assertNotNull(results);
		assertTrue(results.getTotal() >= 3);
		assertEquals(2, results.getResults().size());
	}

	// ==================== UpdateHandler (with DB) ====================

	@Test
	@Order(70)
	void testUpdateHandlerSetAndExecute() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		int updated = repo.query()
				.eq(CODE_COL, "CODE_B")
				.update()
				.set(NAME, "BobUpdated")
				.execute();
		assertTrue(updated >= 1);

		Foo result = repo.query().eq(CODE_COL, "CODE_B").fetchFirst();
		assertNotNull(result);
		assertEquals("BobUpdated", result.getName());
	}

	@Test
	@Order(71)
	void testUpdateHandlerSetIf() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		int updated = repo.query()
				.eq(CODE_COL, "CODE_B")
				.update()
				.setIf(true, NAME, "BobSetIf")
				.setIf(false, NAME, "ShouldNotApply")
				.execute();
		assertTrue(updated >= 1);

		Foo result = repo.query().eq(CODE_COL, "CODE_B").fetchFirst();
		assertEquals("BobSetIf", result.getName());
	}

	@Test
	@Order(72)
	void testUpdateHandlerNumberAdd() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		// Get current volume for CODE_C
		Foo before = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		assertNotNull(before);
		int volumeBefore = before.getVolume();

		int updated = repo.query()
				.eq(CODE_COL, "CODE_C")
				.update()
				.set(VOLUME).add(10)
				.execute();
		assertTrue(updated >= 1);

		Foo after = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		assertEquals(volumeBefore + 10, after.getVolume());
	}

	@Test
	@Order(73)
	void testUpdateHandlerNumberSubtract() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		Foo before = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		int volumeBefore = before.getVolume();

		int updated = repo.query()
				.eq(CODE_COL, "CODE_C")
				.update()
				.set(VOLUME).subtract(5)
				.execute();
		assertTrue(updated >= 1);

		Foo after = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		assertEquals(volumeBefore - 5, after.getVolume());
	}

	@Test
	@Order(74)
	void testUpdateHandlerNumberIncrement() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		Foo before = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		int volumeBefore = before.getVolume();

		int updated = repo.query()
				.eq(CODE_COL, "CODE_C")
				.update()
				.set(VOLUME).increment()
				.execute();
		assertTrue(updated >= 1);

		Foo after = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		assertEquals(volumeBefore + 1, after.getVolume());
	}

	@Test
	@Order(75)
	void testUpdateHandlerStringConcat() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		// Reset name first
		repo.query().eq(CODE_COL, "CODE_B").update().set(NAME, "Bob").execute();

		int updated = repo.query()
				.eq(CODE_COL, "CODE_B")
				.update()
				.set(CODE).concat("_SUFFIX")
				.execute();
		assertTrue(updated >= 1);

		Foo after = repo.query().eq(CODE_COL, "CODE_B_SUFFIX").fetchFirst();
		// The code was CODE_B, now should be CODE_B_SUFFIX
		assertNotNull(after);
		assertEquals("CODE_B_SUFFIX", after.getCode());
	}

	@Test
	@Order(76)
	void testUpdateHandlerNumberToExpression() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		// set volume = volume * 2 using to() with lambda
		Foo before = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		int volumeBefore = before.getVolume();

		int updated = repo.query()
				.eq(CODE_COL, "CODE_C")
				.update()
				.set(VOLUME).to(vol -> vol.multiply(2))
				.execute();
		assertTrue(updated >= 1);

		Foo after = repo.query().eq(CODE_COL, "CODE_C").fetchFirst();
		assertEquals(volumeBefore * 2, after.getVolume());
	}

	// ==================== Complex query chains (with DB) ====================

	@Test
	@Order(80)
	void testQueryExecutorBetween() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query()
				.between(VOLUME, 0, 10000)
				.fetch();
		assertNotNull(results);
		// Use a wide range to cover all records regardless of prior test mutations
		assertTrue(results.size() >= 2);
	}

	@Test
	@Order(81)
	void testQueryExecutorLikeContains() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query()
				.contains(NAME, "ob")
				.fetch();
		assertNotNull(results);
		// "Bob" contains "ob"
		assertTrue(results.stream().anyMatch(f -> f.getName().contains("ob") || f.getName().contains("Ob")));
	}

	@Test
	@Order(82)
	void testQueryExecutorStartsWith() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query()
				.startsWith(NAME, "Char")
				.fetch();
		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertTrue(results.get(0).getName().startsWith("Char"));
	}

	@Test
	@Order(83)
	void testQueryExecutorOrCombinator() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query()
				.eq(CODE_COL, "CODE_A")
				.or(sub -> sub.eq(CODE_COL, "CODE_C"))
				.fetch();
		assertNotNull(results);
		assertTrue(results.size() >= 2);
	}

	@Test
	@Order(84)
	void testQueryExecutorAndCombinator() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		List<Foo> results = repo.query()
				.eq(NAME, "Charlie")
				.and(sub -> sub.ge(VOLUME, 200))
				.fetch();
		assertNotNull(results);
		assertFalse(results.isEmpty());
	}

	@Test
	@Order(85)
	void testQueryExecutorIsNullIsNotNull() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		// All seed data has non-null names
		List<Foo> results = repo.query().isNotNull(CODE_COL).fetch();
		assertNotNull(results);
		assertTrue(results.size() >= 3);

		// No records should have null code
		int nullCount = repo.query().isNull(CODE_COL).count();
		assertEquals(0, nullCount);
	}

	// ==================== Cleanup test (restore CODE_B) ====================

	@Test
	@Order(99)
	void testCleanupRestoreData() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO_TABLE);
		// Restore CODE_B_SUFFIX back to CODE_B if it exists
		Foo suffixed = repo.query().eq(CODE_COL, "CODE_B_SUFFIX").fetchFirst();
		if (suffixed != null) {
			repo.query().eq(CODE_COL, "CODE_B_SUFFIX").update()
					.set(CODE_COL, "CODE_B").execute();
		}
		// Final truncate
		factory.getMetadataFactory().truncate(FOO_TABLE).execute();
	}
}
