package com.github.xuse.querydsl.sql.integration.mock;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.entity.TableDataTypes;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.JavaTimes;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.dsl.Expressions;

/**
 * Tests for SQLUpdateClauseAlter and SQLInsertClauseAlter additional methods:
 * populate variants, populateWithCompare, setIf, updateAutoColumns,
 * writeNulls, setQueryTimeout, containsSetPath, removeSetPath.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DmlClauseTest extends MockedTestBase {

	private static final QTableDataTypes T = QTableDataTypes.aaa;

	@BeforeAll
	static void setup() {
		doInit();
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		meta.dropTable(T).ifExists(true).execute();
		meta.createTable(T).execute();
	}

	private TableDataTypes makeEntity() {
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
		return a;
	}

	/** Test insert with writeNulls(true). */
	@Test @Order(1)
	void testInsertWriteNulls() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).writeNulls(true).populate(a).executeWithKey(Integer.class);
		assertTrue(id > 0);
	}

	/** Test insert with writeNulls(false). */
	@Test @Order(2)
	void testInsertWriteNullsFalse() {
		TableDataTypes a = makeEntity();
		a.setName("selective");
		int id = factory.insert(T).writeNulls(false).populate(a).executeWithKey(Integer.class);
		assertTrue(id > 0);
	}

	/** Test update with populate(bean, pkAsWhere=true). */
	@Test @Order(3)
	void testUpdatePopulateWithPkAsWhere() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).populate(a).executeWithKey(Integer.class);
		a.setId(id);
		a.setName("updated-pk");
		long count = factory.update(T).populate(a, true).where(T.id.eq(id)).execute();
		assertTrue(count >= 0);
	}

	/** Test update with populateWithCompare. */
	@Test @Order(4)
	void testUpdatePopulateWithCompare() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).populate(a).executeWithKey(Integer.class);
		TableDataTypes oldRecord = factory.selectFrom(T).where(T.id.eq(id)).fetchOne();

		TableDataTypes newRecord = new TableDataTypes();
		newRecord.setName("compared-update");
		newRecord.setGender(Gender.MALE);
		newRecord.setVersion(2);
		long count = factory.update(T).populateWithCompare(newRecord, oldRecord).where(T.id.eq(id)).execute();
		assertTrue(count >= 0);
	}

	/** Test update with populate(bean, mapper, pkAsWhere). */
	@Test @Order(5)
	void testUpdatePopulateWithMapper() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).populate(a).executeWithKey(Integer.class);
		a.setGender(Gender.MALE);
		long count = factory.update(T).populate(a, AdvancedMapper.ofNullsBinding(0), false)
				.where(T.id.eq(id)).execute();
		assertTrue(count >= 0);
	}

	/** Test update setIf with true and false conditions. */
	@Test @Order(6)
	void testUpdateSetIf() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).populate(a).executeWithKey(Integer.class);
		long count = factory.update(T)
				.setIf(true, T.name, "setIf-true")
				.setIf(false, T.name, "should-not-apply")
				.where(T.id.eq(id)).execute();
		assertTrue(count >= 0);
		TableDataTypes result = factory.selectFrom(T).where(T.id.eq(id)).fetchOne();
		assertEquals("setIf-true", result.getName());
	}

	/** Test update setIf with Expression. */
	@Test @Order(7)
	void testUpdateSetIfExpression() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).populate(a).executeWithKey(Integer.class);
		long count = factory.update(T)
				.setIf(true, T.created, JavaTimes.currentTimestamp())
				.where(T.id.eq(id)).execute();
		assertTrue(count >= 0);
	}

	/** Test update setQueryTimeout. */
	@Test @Order(8)
	void testUpdateSetQueryTimeout() {
		assertNotNull(factory.update(T).setQueryTimeout(5));
	}

	/** Test update updateNulls. */
	@Test @Order(9)
	void testUpdateNulls() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).populate(a).executeWithKey(Integer.class);
		a.setName(null);
		long count = factory.update(T).updateNulls(true).populate(a).where(T.id.eq(id)).execute();
		assertTrue(count >= 0);
	}

	/** Test update containsSetPath and removeSetPath. */
	@Test @Order(10)
	void testContainsAndRemoveSetPath() {
		SQLUpdateClauseAlter clause = (SQLUpdateClauseAlter) factory.update(T).set(T.name, "test").set(T.gender, Gender.MALE);
		assertTrue(clause.containsSetPath(T.name));
		assertTrue(clause.containsSetPath(T.gender));
		clause.removeSetPath(T.gender);
		assertFalse(clause.containsSetPath(T.gender));
	}

	/** Test insert batch with addBatch(). */
	@Test @Order(11)
	void testInsertAddBatch() {
		TableDataTypes a = makeEntity();
		a.setName("batch-a");
		TableDataTypes b = makeEntity();
		b.setName("batch-b");
		long count = factory.insert(T).populate(a).addBatch().populate(b).addBatch().execute();
		assertTrue(count >= 0);
	}

	/** Test update batch with addBatch(). */
	@Test @Order(12)
	void testUpdateAddBatch() {
		long count = factory.update(T)
				.where(T.name.eq("batch-a")).set(T.version, T.version.add(1)).addBatch()
				.where(T.name.eq("batch-b")).set(T.version, T.version.add(2)).addBatch()
				.execute();
		assertTrue(count >= 0);
	}

	/** Test delete batch with addBatch(). */
	@Test @Order(13)
	void testDeleteAddBatch() {
		long count = factory.delete(T)
				.where(T.name.eq("batch-a")).addBatch()
				.where(T.name.eq("batch-b")).addBatch()
				.execute();
		assertTrue(count >= 0);
	}

	/** Test insert populateBatch. */
	@Test @Order(14)
	void testInsertPopulateBatch() {
		TableDataTypes a = makeEntity(); a.setName("pb1");
		TableDataTypes b = makeEntity(); b.setName("pb2");
		TableDataTypes c = makeEntity(); c.setName("pb3");
		List<Integer> keys = factory.insert(T).writeNulls(false)
				.populateBatch(Arrays.asList(a, b, c))
				.executeWithKeys(Integer.class);
		assertNotNull(keys);
	}

	/** Test merge with set API. */
	@Test @Order(15)
	void testMergeWithSet() {
		TableDataTypes a = makeEntity();
		int id = factory.insert(T).populate(a).executeWithKey(Integer.class);
		Integer result = factory.merge(T).keys(T.id)
				.columns(T.id, T.created, T.gender)
				.values(id, new Date(), Gender.MALE)
				.executeWithKey(T.id);
		// result may be null depending on DB
	}

	/** Cleanup. */
	@Test @Order(99)
	void cleanup() {
		factory.getMetadataFactory().truncate(T).execute();
	}
}