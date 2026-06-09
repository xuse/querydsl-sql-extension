package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

/**
 * Tests for SQLMergeClauseAlter covering:
 * - Merge inserts new record when no matching key exists
 * - Merge updates existing record when matching key found
 *
 * Requirements: 5.10
 */
@DisplayName("SQLMergeClauseAlter Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLMergeClauseAlterTest extends MockedTestBase {

	private static final QTableDataTypes T = QTableDataTypes.aaa;

	@BeforeAll
	static void setup() {
		doInit();
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		meta.dropTable(T).ifExists(true).execute();
		meta.createTable(T).execute();
	}

	@BeforeEach
	void cleanTable() {
		factory.getMetadataFactory().truncate(T).execute();
	}

	/**
	 * Creates a fully populated entity with all NOT NULL fields set to valid values.
	 */
	private TableDataTypes makeFullEntity(String name) {
		TableDataTypes a = new TableDataTypes();
		a.setName(name);
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
		a.setDataTime(new Time(System.currentTimeMillis()));
		a.setDateTimestamp(new Date());
		return a;
	}

	/**
	 * Requirement 5.10: Merge inserts a new record when no matching key exists.
	 *
	 * When a merge operation is executed with keys, columns, and values set,
	 * and no existing record matches the key, a new record should be inserted.
	 */
	@Test
	@Order(1)
	@DisplayName("Merge inserts new record when no matching key exists")
	public void testMergeInsertsNewRecord() {
		// Verify table is empty
		long initialCount = factory.selectFrom(T).fetchCount();
		assertEquals(0, initialCount, "Table should be empty before merge");

		// Execute merge with a key value that does not exist in the table
		long result = factory.merge(T)
				.keys(T.id)
				.columns(T.id, T.name, T.gender, T.taskStatus, T.version,
						T.dataInt, T.dataFloat, T.dataDouble, T.dataShort,
						T.dataBigint, T.dataDecimal, T.dataBool,
						T.dataDate, T.dataTime, T.dateTimestamp)
				.values(9999, "merge-insert-test", Gender.MALE, TaskStatus.INIT, 1,
						10, 1.5f, 2.5d, (short) 2,
						100L, new BigDecimal("9.99"), true,
						new Date(), new Time(System.currentTimeMillis()), new Date())
				.execute();

		// The merge should have inserted a new record
		assertTrue(result >= 0, "Merge execute should return non-negative result");

		// Verify the record was inserted
		TableDataTypes inserted = factory.selectFrom(T).where(T.id.eq(9999)).fetchFirst();
		assertNotNull(inserted, "Record should exist after merge insert");
		assertEquals("merge-insert-test", inserted.getName());
		assertEquals(Gender.MALE, inserted.getGender());
		assertEquals(TaskStatus.INIT, inserted.getTaskStatus());
		assertEquals(10, inserted.getDataInt());
	}

	/**
	 * Requirement 5.10: Merge updates existing record when matching key found.
	 *
	 * When a merge operation is executed with keys, columns, and values set,
	 * and an existing record matches the key, the record should be updated.
	 */
	@Test
	@Order(2)
	@DisplayName("Merge updates existing record when matching key found")
	public void testMergeUpdatesExistingRecord() {
		// First, insert a record that we will later merge/update
		TableDataTypes original = makeFullEntity("merge-update-original");
		original.setDataInt(50);
		original.setDataText("original text");
		int id = factory.insert(T).populate(original).executeWithKey(Integer.class);
		assertTrue(id > 0, "Should get a valid ID from insert");

		// Verify the record was inserted with original values
		TableDataTypes beforeMerge = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(beforeMerge);
		assertEquals("merge-update-original", beforeMerge.getName());
		assertEquals(50, beforeMerge.getDataInt());

		// Execute merge with the same key - should update the existing record
		long result = factory.merge(T)
				.keys(T.id)
				.columns(T.id, T.name, T.gender, T.taskStatus, T.version,
						T.dataInt, T.dataFloat, T.dataDouble, T.dataShort,
						T.dataBigint, T.dataDecimal, T.dataBool,
						T.dataDate, T.dataTime, T.dateTimestamp)
				.values(id, "merge-update-modified", Gender.MALE, TaskStatus.INIT, 2,
						99, 3.5f, 4.5d, (short) 3,
						200L, new BigDecimal("19.99"), true,
						new Date(), new Time(System.currentTimeMillis()), new Date())
				.execute();

		assertTrue(result >= 0, "Merge execute should return non-negative result");

		// Verify the record was updated (not a new insert)
		long totalCount = factory.selectFrom(T).where(T.id.eq(id)).fetchCount();
		assertEquals(1, totalCount, "Should still have exactly one record with this ID");

		// Verify the values were updated
		TableDataTypes afterMerge = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(afterMerge);
		assertEquals("merge-update-modified", afterMerge.getName(), "Name should be updated");
		assertEquals(99, afterMerge.getDataInt(), "DataInt should be updated");
		assertEquals(2, afterMerge.getVersion(), "Version should be updated");
	}
}
