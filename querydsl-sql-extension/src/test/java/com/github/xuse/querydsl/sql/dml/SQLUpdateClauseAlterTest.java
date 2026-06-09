package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
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
import com.github.xuse.querydsl.util.StringUtils;

/**
 * Tests for SQLUpdateClauseAlter covering:
 * - updateNulls(true) with null-valued fields: SET clause includes explicit NULL
 *
 * Requirements: 5.8
 */
@DisplayName("SQLUpdateClauseAlter Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLUpdateClauseAlterTest extends MockedTestBase {

	private static final QTableDataTypes T = QTableDataTypes.aaa;

	@BeforeAll
	static void setup() {
		doInit();
		SQLMetadataQueryFactory meta = factory.getMetadataFactory();
		meta.dropTable(T).ifExists(true).execute();
		meta.createTable(T).execute();
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
		// Set nullable fields with values for testing
		a.setDataText("some text content");
		a.setDataLongText("some long text");
		a.setGenderWithChar(Gender.MALE);
		return a;
	}

	/**
	 * Requirement 5.8: updateNulls(true) with null-valued fields causes the SET clause
	 * to include explicit NULL assignments for those fields.
	 *
	 * Verifies that after calling updateNulls(true).populate(bean) where the bean has
	 * null-valued nullable fields, those fields are actually set to NULL in the database.
	 */
	@Test
	@Order(1)
	@DisplayName("updateNulls(true): null-valued nullable field is explicitly set to NULL in DB")
	public void testUpdateNullsTrue_NullFieldsSetToNull() {
		// Insert a record with non-null dataText (a nullable field)
		TableDataTypes original = makeFullEntity("update-nulls-test1");
		original.setDataText("initial text");
		int id = factory.insert(T).populate(original).executeWithKey(Integer.class);
		assertTrue(id > 0, "Should get a valid ID");

		// Verify the record was inserted with non-null dataText
		TableDataTypes inserted = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(inserted);
		assertEquals("initial text", inserted.getDataText(), "DataText should be non-null after insert");

		// Now update with updateNulls(true) - set dataText to null
		// All NOT NULL fields must retain valid values
		TableDataTypes updateBean = makeFullEntity("update-nulls-test1");
		updateBean.setId(id);
		updateBean.setDataText(null); // This nullable field should be set to NULL in DB

		long updateCount = factory.update(T)
				.updateNulls(true)
				.populate(updateBean)
				.where(T.id.eq(id))
				.execute();
		assertTrue(updateCount >= 0, "Update should succeed");

		// Verify the dataText field is now NULL in the database
		TableDataTypes updated = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(updated);
		assertNull(updated.getDataText(), "DataText should be NULL after updateNulls(true) with null field");
	}

	/**
	 * Requirement 5.8: Verify that without updateNulls(true), null-valued fields
	 * are NOT updated (default behavior preserves existing values).
	 */
	@Test
	@Order(2)
	@DisplayName("Default (updateNulls=false): null-valued fields are NOT updated")
	public void testUpdateNullsFalse_NullFieldsPreserved() {
		// Insert a record with non-null dataText
		TableDataTypes original = makeFullEntity("keep-nulls-test1");
		original.setDataText("keep this text");
		int id = factory.insert(T).populate(original).executeWithKey(Integer.class);
		assertTrue(id > 0);

		// Verify the record was inserted
		TableDataTypes inserted = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(inserted);
		assertEquals("keep this text", inserted.getDataText());

		// Update without updateNulls - dataText is null in bean but should NOT be updated
		TableDataTypes updateBean = makeFullEntity("keep-nulls-test1-updated");
		updateBean.setId(id);
		updateBean.setDataText(null); // Should NOT be set to NULL in DB (default behavior)

		long updateCount = factory.update(T)
				.populate(updateBean) // default: updateNulls=false
				.where(T.id.eq(id))
				.execute();
		assertTrue(updateCount >= 0, "Update should succeed");

		// Verify the dataText field is still the original value
		TableDataTypes updated = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(updated);
		assertEquals("keep this text", updated.getDataText(),
				"DataText should be preserved when updateNulls is false and field is null");
		assertEquals("keep-nulls-test1-updated", updated.getName(), "Name should be updated");
	}

	/**
	 * Requirement 5.8: Verify updateNulls(true) with multiple null nullable fields.
	 */
	@Test
	@Order(3)
	@DisplayName("updateNulls(true): multiple null-valued nullable fields are all set to NULL")
	public void testUpdateNullsTrue_MultipleNullFields() {
		// Insert a record with non-null values for multiple nullable fields
		TableDataTypes original = makeFullEntity("multi-null-test1");
		original.setDataText("text value");
		original.setDataLongText("long text value");
		original.setGenderWithChar(Gender.MALE);
		int id = factory.insert(T).populate(original).executeWithKey(Integer.class);
		assertTrue(id > 0);

		// Verify the record was inserted
		TableDataTypes inserted = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(inserted);
		assertEquals("text value", inserted.getDataText());
		assertEquals("long text value", inserted.getDataLongText());

		// Update with multiple null nullable fields
		TableDataTypes updateBean = makeFullEntity("multi-null-test1");
		updateBean.setId(id);
		updateBean.setDataText(null);     // Should become NULL
		updateBean.setDataLongText(null); // Should become NULL
		updateBean.setGenderWithChar(null); // Should become NULL

		long updateCount = factory.update(T)
				.updateNulls(true)
				.populate(updateBean)
				.where(T.id.eq(id))
				.execute();
		assertTrue(updateCount >= 0, "Update should succeed");

		// Verify all targeted nullable fields are NULL
		TableDataTypes updated = factory.selectFrom(T).where(T.id.eq(id)).fetchFirst();
		assertNotNull(updated);
		assertNull(updated.getDataText(), "DataText should be NULL");
		assertNull(updated.getDataLongText(), "DataLongText should be NULL");
		assertNull(updated.getGenderWithChar(), "GenderWithChar should be NULL");
	}
}
