package com.github.xuse.querydsl.sql.integration.mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.DateTimeLambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.repository.LambdaQueryWrapper;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.ComparableExpression;

/**
 * Tests for SelectBuilder: column projections, aggregations, toArray, toMap, toBean.
 */
class SelectBuilderTest extends MockedTestBase implements LambdaHelpers {

	static final LambdaTable<Foo> FOO = () -> Foo.class;
	static final StringLambdaColumn<Foo> NAME = Foo::getName;
	static final NumberLambdaColumn<Foo, Integer> ID = Foo::getId;
	static final NumberLambdaColumn<Foo, Integer> VOLUME = Foo::getVolume;
	static final LambdaColumn<Foo, Instant> CREATED = Foo::getCreated;
	static final DateTimeLambdaColumn<Foo, Date> UPDATED = Foo::getUpdated;

	@BeforeAll
	static void setup() {
		doInit();
	}

	/** Test select single column with aggregation. */
	@Test
	void testSelectSingleColumnMax() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Integer> results = repo.find(
				wrapper.selectSingleColumn(ID, id -> id.max()).groupBy(NAME));
		assertNotNull(results);
	}

	/** Test select to Object[] via toArray(). */
	@Test
	void testSelectToArray() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Object[]> list = repo.find(
				wrapper.select(q -> q.column(ID).to(e -> e.count()).as("cnt")
						.column(NAME).and().toArray())
						.groupBy(NAME));
		assertNotNull(list);
	}

	/** Test select to Map via toMap(). */
	@Test
	void testSelectToMap() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Map<String, ?>> list = repo.find(
				wrapper.select(q -> q.column(ID).to(e -> e.count()).as("cnt")
						.column(NAME).and().toMap())
						.groupBy(NAME));
		assertNotNull(list);
	}

	/** Test select to Tuple via toTuple(). */
	@Test
	void testSelectToTuple() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Tuple> list = repo.find(
				wrapper.select(q -> q.column(ID).and().column(NAME).and().toTuple()));
		assertNotNull(list);
	}

	/** Test select to custom bean via toBean(). */
	@Test
	void testSelectToBean() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Foo> list = repo.find(
				wrapper.select(q -> q.column(ID).and().column(NAME).and().toBean(Foo.class)));
		assertNotNull(list);
	}

	/** Test select string column with transformation. */
	@Test
	void testSelectStringColumn() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Object[]> list = repo.find(
				wrapper.select(q -> q.column(NAME).to(s -> s.upper()).as("upperName")
						.column(ID).and().toArray())
						.groupBy(NAME));
		assertNotNull(list);
	}

	/** Test select number column with aggregation. */
	@Test
	void testSelectNumberColumn() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Object[]> list = repo.find(
				wrapper.select(q -> q.num(VOLUME).to(v -> v.sum()).as("totalVolume")
						.column(NAME).and().toArray())
						.groupBy(NAME));
		assertNotNull(list);
	}

	/** Test select with comparable column transformation. */
	@Test
	void testSelectComparableMax() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Object[]> list = repo.find(
				wrapper.select(q -> q.column(CREATED).to(ComparableExpression::max).as("maxCreated")
						.column(NAME).and().toArray())
						.groupBy(NAME));
		assertNotNull(list);
	}

	/** Test select to List via toList(). */
	@Test
	void testSelectToList() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<List<?>> list = repo.find(
				wrapper.select(q -> q.column(ID).and().column(NAME).and().toList()));
		assertNotNull(list);
	}

	/** Test select datetime column with transformation. */
	@Test
	void testSelectDateTimeColumn() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		com.github.xuse.querydsl.lambda.DateTimeLambdaColumn<Foo, java.time.Instant> createdDt = Foo::getCreated;
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Object[]> list = repo.find(
				wrapper.select(q -> q.datetime(createdDt).to(e -> e.max()).as("maxDt")
						.column(NAME).and().toArray())
						.groupBy(NAME));
		assertNotNull(list);
	}

	/** Test select with all() columns from table. */
	@Test
	void testSelectAll() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Foo> list = repo.find(
				wrapper.select(q -> q.all(FOO).toBean(Foo.class)));
		assertNotNull(list);
	}

	/** Test select with columns() varargs. */
	@Test
	void testSelectColumns() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Object[]> list = repo.find(
				wrapper.select(q -> q.columns(ID, NAME).toArray()));
		assertNotNull(list);
	}

	/** Test select with custom expression via select(). */
	@Test
	void testSelectCustomExpr() {
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		LambdaQueryWrapper<Foo> wrapper = new LambdaQueryWrapper<>(Foo.class);
		List<Object[]> list = repo.find(
				wrapper.select(q -> q.select(ID.count()).as("cnt")
						.column(NAME).and().toArray())
						.groupBy(NAME));
		assertNotNull(list);
	}
}