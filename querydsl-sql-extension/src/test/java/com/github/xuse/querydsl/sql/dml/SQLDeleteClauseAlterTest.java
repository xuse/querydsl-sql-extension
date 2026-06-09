package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;

/**
 * Tests for SQLDeleteClauseAlter covering:
 * - Delete without WHERE clause with UpdateDeleteProtectListener: UnsupportedOperationException thrown
 *
 * Note: The AbstractTestBase already registers UpdateDeleteProtectListener in the configuration,
 * so the protection is active for all tests extending it.
 *
 * Requirements: 5.9
 */
@DisplayName("SQLDeleteClauseAlter Tests")
public class SQLDeleteClauseAlterTest extends AbstractTestBase {

	private static final LambdaTable<Foo> FOO = () -> Foo.class;
	private static final StringLambdaColumn<Foo> CODE = Foo::getCode;

	@BeforeEach
	public void truncate() {
		factory.getMetadataFactory().truncate(FOO).execute();
	}

	/**
	 * Requirement 5.9: Delete without WHERE clause with UpdateDeleteProtectListener
	 * should throw UnsupportedOperationException.
	 *
	 * The UpdateDeleteProtectListener is registered in AbstractTestBase's configuration.
	 * When a delete is executed without any WHERE clause, the listener intercepts and
	 * throws UnsupportedOperationException to prevent accidental full-table deletion.
	 */
	@Test
	@DisplayName("Delete without WHERE clause: UnsupportedOperationException thrown")
	public void testDeleteWithoutWhere_ThrowsUnsupportedOperationException() {
		// Insert a record so the table is not empty
		Foo record = new Foo();
		record.setCode("DEL_PROTECT_01");
		record.setName("Protected Record");
		record.setVolume(10);

		long insertCount = factory.insert(FOO)
				.populateBatch(Collections.singletonList(record))
				.execute();
		assertEquals(1, insertCount, "Should insert 1 record");

		// Attempt to delete without WHERE clause - should be rejected
		UnsupportedOperationException ex = assertThrows(
				UnsupportedOperationException.class,
				() -> factory.delete(FOO).execute(),
				"Delete without WHERE clause should throw UnsupportedOperationException"
		);

		// Verify the exception message indicates the deletion was rejected
		assertNotNull(ex.getMessage());
		assertTrue(ex.getMessage().contains("rejected") || ex.getMessage().contains("prevent"),
				"Exception message should indicate the deletion was rejected, got: " + ex.getMessage());

		// Verify the record still exists (deletion was prevented)
		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		assertEquals(1, repo.query().count(), "Record should still exist after rejected delete");
	}

	/**
	 * Requirement 5.9: Delete WITH a valid WHERE clause should succeed normally.
	 * This confirms the protection only blocks unconditional deletes.
	 */
	@Test
	@DisplayName("Delete with valid WHERE clause: succeeds normally")
	public void testDeleteWithWhere_Succeeds() {
		// Insert records
		Foo record1 = new Foo();
		record1.setCode("DEL_OK_01");
		record1.setName("Delete Me");
		record1.setVolume(10);

		Foo record2 = new Foo();
		record2.setCode("DEL_OK_02");
		record2.setName("Keep Me");
		record2.setVolume(20);

		factory.insert(FOO).populateBatch(Arrays.asList(record1, record2)).execute();

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		assertEquals(2, repo.query().count(), "Should have 2 records");

		// Delete with WHERE clause using lambda column - should succeed
		int deleted = repo.deleteBy(CODE.eq("DEL_OK_01"));
		assertTrue(deleted >= 1, "Should delete at least 1 record");

		// Verify only the targeted record was deleted
		assertEquals(1, repo.query().count(), "Should have 1 record remaining");
	}

	/**
	 * Requirement 5.9: Verify that the protection works even on an empty table.
	 * A delete without WHERE on an empty table should still be rejected.
	 */
	@Test
	@DisplayName("Delete without WHERE on empty table: still throws UnsupportedOperationException")
	public void testDeleteWithoutWhere_EmptyTable_StillThrows() {
		// Table is already empty after truncate in @BeforeEach

		// Attempt to delete without WHERE clause - should still be rejected
		assertThrows(
				UnsupportedOperationException.class,
				() -> factory.delete(FOO).execute(),
				"Delete without WHERE clause should throw even on empty table"
		);
	}
}
