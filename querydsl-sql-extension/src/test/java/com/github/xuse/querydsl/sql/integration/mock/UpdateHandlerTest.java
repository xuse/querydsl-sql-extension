package com.github.xuse.querydsl.sql.integration.mock;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.repository.CRUDRepository;

/**
 * Tests for UpdateHandler: set, setIf, add, subtract, increment, concat, to() expression.
 * Also covers additional CRUDRepository and QueryExecutor methods.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UpdateHandlerTest extends MockedTestBase {

	static final LambdaTable<Foo> FOO = () -> Foo.class;
	static final StringLambdaColumn<Foo> NAME = Foo::getName;
	static final StringLambdaColumn<Foo> CODE = Foo::getCode;
	static final NumberLambdaColumn<Foo, Integer> ID = Foo::getId;
	static final NumberLambdaColumn<Foo, Integer> VOLUME = Foo::getVolume;

	@BeforeAll
	static void setup() {
		doInit();
		try {
			factory.getConnection().createStatement().execute(
				"CREATE TABLE IF NOT EXISTS ca_foo (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"code VARCHAR(64) NOT NULL DEFAULT '', " +
				"asset_name VARCHAR(128) NOT NULL DEFAULT '', " +
				"content TEXT, " +
				"created TIMESTAMP, " +
				"updated TIMESTAMP, " +
				"gender VARCHAR(16), " +
				"ext VARCHAR(256), " +
				"map VARCHAR(256), " +
				"volume INT NOT NULL DEFAULT 0, " +
				"version INT NOT NULL DEFAULT 1, " +
				"codetype INT NOT NULL DEFAULT 1, " +
				"inday DATE)");
			factory.getConnection().createStatement().execute("DELETE FROM ca_foo");
			// Seed data
			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			Foo foo = new Foo();
			foo.setCode("UH_A"); foo.setName("Alpha"); foo.setVolume(100); foo.setCodeType(1);
			repo.insert(foo);
			Foo foo2 = new Foo();
			foo2.setCode("UH_B"); foo2.setName("Beta"); foo2.setVolume(200); foo2.setCodeType(2);
			repo.insert(foo2);
		} catch (Exception e) {
			// ignore if table already exists
		}
	}

	/** Test UpdateHandler.set(column, value). */
	@Test
	@Order(1)
	void testUpdateSet() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		int updated = repo.query().eq(CODE, "UH_A").update().set(NAME, "AlphaUpdated").execute();
		assertTrue(updated >= 1);
		Foo result = repo.query().eq(CODE, "UH_A").fetchFirst();
		assertEquals("AlphaUpdated", result.getName());
	}

	/** Test UpdateHandler.setIf(condition, column, value). */
	@Test
	@Order(2)
	void testUpdateSetIf() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		int updated = repo.query().eq(CODE, "UH_A").update()
				.setIf(true, NAME, "AlphaSetIf")
				.setIf(false, NAME, "ShouldNotApply")
				.execute();
		assertTrue(updated >= 1);
		Foo result = repo.query().eq(CODE, "UH_A").fetchFirst();
		assertEquals("AlphaSetIf", result.getName());
	}

	/** Test UpdateHandler.set(numberColumn).add(n). */
	@Test
	@Order(3)
	void testUpdateNumberAdd() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo before = repo.query().eq(CODE, "UH_B").fetchFirst();
		int volBefore = before.getVolume();
		repo.query().eq(CODE, "UH_B").update().set(VOLUME).add(50).execute();
		Foo after = repo.query().eq(CODE, "UH_B").fetchFirst();
		assertEquals(volBefore + 50, after.getVolume());
	}

	/** Test UpdateHandler.set(numberColumn).subtract(n). */
	@Test
	@Order(4)
	void testUpdateNumberSubtract() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo before = repo.query().eq(CODE, "UH_B").fetchFirst();
		int volBefore = before.getVolume();
		repo.query().eq(CODE, "UH_B").update().set(VOLUME).subtract(10).execute();
		Foo after = repo.query().eq(CODE, "UH_B").fetchFirst();
		assertEquals(volBefore - 10, after.getVolume());
	}

	/** Test UpdateHandler.set(numberColumn).increment(). */
	@Test
	@Order(5)
	void testUpdateNumberIncrement() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo before = repo.query().eq(CODE, "UH_B").fetchFirst();
		int volBefore = before.getVolume();
		repo.query().eq(CODE, "UH_B").update().set(VOLUME).increment().execute();
		Foo after = repo.query().eq(CODE, "UH_B").fetchFirst();
		assertEquals(volBefore + 1, after.getVolume());
	}

	/** Test UpdateHandler.set(stringColumn).concat(suffix). */
	@Test
	@Order(6)
	void testUpdateStringConcat() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		repo.query().eq(CODE, "UH_A").update().set(NAME, "Base").execute();
		repo.query().eq(CODE, "UH_A").update().set(NAME).concat("_EXT").execute();
		Foo after = repo.query().eq(CODE, "UH_A").fetchFirst();
		assertEquals("Base_EXT", after.getName());
	}

	/** Test UpdateHandler.set(numberColumn).to(expr). */
	@Test
	@Order(7)
	void testUpdateNumberToExpression() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo before = repo.query().eq(CODE, "UH_B").fetchFirst();
		int volBefore = before.getVolume();
		repo.query().eq(CODE, "UH_B").update().set(VOLUME).to(v -> v.multiply(2)).execute();
		Foo after = repo.query().eq(CODE, "UH_B").fetchFirst();
		assertEquals(volBefore * 2, after.getVolume());
	}

	/** Test QueryExecutor.findAndCount(). */
	@Test
	@Order(8)
	void testFindAndCount() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		com.querydsl.core.QueryResults<Foo> results = repo.query().orderByAsc(ID).limit(10).findAndCount();
		assertNotNull(results);
		assertTrue(results.getTotal() >= 2);
	}

	/** Test CRUDRepository.findByExample(). */
	@Test
	@Order(9)
	void testFindByExample() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo example = new Foo();
		example.setVolume(-1); // unsaved
		List<Foo> all = repo.findByExample(example);
		assertTrue(all.size() >= 2);
	}

	/** Cleanup. */
	@Test
	@Order(99)
	void cleanup() {
		factory.getMetadataFactory().truncate(FOO).execute();
	}
}