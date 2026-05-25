package com.github.xuse.querydsl.sql.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.SQLUpdateBatch;

/**
 * Unit tests for {@link UpdateDeleteProtectListener}.
 * <p>
 * Verifies that the listener correctly rejects operations that would affect all records,
 * including the self-referencing equality condition (e.g. {@code ID.eq(ID)}).
 */
class UpdateDeleteProtectListenerTest {

	private static final UpdateDeleteProtectListener listener = new UpdateDeleteProtectListener();
	private static final QCaAsset t = QCaAsset.caAsset;

	@BeforeAll
	static void init() {
		// Trigger class metadata scanning
		MockedTestBase.doInit();
	}

	// ==================== DELETE tests ====================

	@Test
	@DisplayName("delete: null WHERE should be rejected")
	void deleteWithNullWhere() {
		QueryMetadata md = new DefaultQueryMetadata();
		// no where clause set
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: self-referencing EQ (ID.eq(ID)) should be rejected")
	void deleteWithSelfEq() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(t.id));
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: valid WHERE (id.eq(1)) should pass")
	void deleteWithValidWhere() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(1));
		assertDoesNotThrow(() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: AND of multiple self-referencing EQs should be rejected")
	void deleteWithMultipleSelfEq() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(t.id).and(t.name.eq(t.name)));
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: OR of self-referencing EQ and valid condition should pass")
	void deleteWithSelfEqOrValidCondition() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(t.id).or(t.id.eq(1)));
		assertDoesNotThrow(() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: AND of self-referencing EQ and valid condition should pass")
	void deleteWithSelfEqAndValidCondition() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(t.id).and(t.id.gt(0)));
		assertDoesNotThrow(() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: NOT(self-referencing EQ) should be rejected (conservative)")
	void deleteWithNotSelfEq() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(t.id).not());
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyDelete(t, md));
	}

	// ==================== UPDATE tests ====================

	@Test
	@DisplayName("update: null WHERE should be rejected")
	void updateWithNullWhere() {
		QueryMetadata md = new DefaultQueryMetadata();
		Map<Path<?>, Expression<?>> updates = new HashMap<>();
		updates.put(t.name, Expressions.constant("test"));
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyUpdate(t, md, updates));
	}

	@Test
	@DisplayName("update: self-referencing EQ (ID.eq(ID)) should be rejected")
	void updateWithSelfEq() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(t.id));
		Map<Path<?>, Expression<?>> updates = new HashMap<>();
		updates.put(t.name, Expressions.constant("test"));
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyUpdate(t, md, updates));
	}

	@Test
	@DisplayName("update: valid WHERE (id.eq(1)) should pass")
	void updateWithValidWhere() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(1));
		Map<Path<?>, Expression<?>> updates = new HashMap<>();
		updates.put(t.name, Expressions.constant("test"));
		assertDoesNotThrow(() -> listener.notifyUpdate(t, md, updates));
	}

	@Test
	@DisplayName("update: AND of multiple self-referencing EQs should be rejected")
	void updateWithMultipleSelfEq() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.id.eq(t.id).and(t.name.eq(t.name)));
		Map<Path<?>, Expression<?>> updates = new HashMap<>();
		updates.put(t.name, Expressions.constant("test"));
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyUpdate(t, md, updates));
	}

	// ==================== Batch DELETE tests ====================

	@Test
	@DisplayName("batch delete: all batches with valid WHERE should pass")
	void batchDeleteAllValid() {
		QueryMetadata md1 = new DefaultQueryMetadata();
		md1.addWhere(t.id.eq(1));
		QueryMetadata md2 = new DefaultQueryMetadata();
		md2.addWhere(t.id.eq(2));
		assertDoesNotThrow(() -> listener.notifyDeletes(t, Arrays.asList(md1, md2)));
	}

	@Test
	@DisplayName("batch delete: one batch with self-referencing EQ should be rejected")
	void batchDeleteWithSelfEq() {
		QueryMetadata md1 = new DefaultQueryMetadata();
		md1.addWhere(t.id.eq(1));
		QueryMetadata md2 = new DefaultQueryMetadata();
		md2.addWhere(t.id.eq(t.id));
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyDeletes(t, Arrays.asList(md1, md2)));
	}

	@Test
	@DisplayName("batch delete: one batch with null WHERE should be rejected")
	void batchDeleteWithNullWhere() {
		QueryMetadata md1 = new DefaultQueryMetadata();
		md1.addWhere(t.id.eq(1));
		QueryMetadata md2 = new DefaultQueryMetadata();
		// no where
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyDeletes(t, Arrays.asList(md1, md2)));
	}

	// ==================== Batch UPDATE tests ====================

	@Test
	@DisplayName("batch update: all batches with valid WHERE should pass")
	void batchUpdateAllValid() {
		QueryMetadata md1 = new DefaultQueryMetadata();
		md1.addWhere(t.id.eq(1));
		QueryMetadata md2 = new DefaultQueryMetadata();
		md2.addWhere(t.id.eq(2));
		SQLUpdateBatch batch1 = new SQLUpdateBatch(md1, Collections.emptyMap());
		SQLUpdateBatch batch2 = new SQLUpdateBatch(md2, Collections.emptyMap());
		assertDoesNotThrow(() -> listener.notifyUpdates(t, Arrays.asList(batch1, batch2)));
	}

	@Test
	@DisplayName("batch update: one batch with self-referencing EQ should be rejected")
	void batchUpdateWithSelfEq() {
		QueryMetadata md1 = new DefaultQueryMetadata();
		md1.addWhere(t.id.eq(1));
		QueryMetadata md2 = new DefaultQueryMetadata();
		md2.addWhere(t.id.eq(t.id));
		SQLUpdateBatch batch1 = new SQLUpdateBatch(md1, Collections.emptyMap());
		SQLUpdateBatch batch2 = new SQLUpdateBatch(md2, Collections.emptyMap());
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyUpdates(t, Arrays.asList(batch1, batch2)));
	}

	// ==================== Edge cases ====================

	@Test
	@DisplayName("delete: different columns self-referencing (name.eq(name)) should be rejected")
	void deleteWithDifferentColumnSelfEq() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.name.eq(t.name));
		assertThrows(UnsupportedOperationException.class,
				() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: non-EQ operator (name.ne('x')) should pass")
	void deleteWithNonEqOperator() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.name.ne("x"));
		assertDoesNotThrow(() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: LIKE condition should pass")
	void deleteWithLikeCondition() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.name.like("%test%"));
		assertDoesNotThrow(() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: IN condition should pass")
	void deleteWithInCondition() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.name.in("a", "b", "c"));
		assertDoesNotThrow(() -> listener.notifyDelete(t, md));
	}

	@Test
	@DisplayName("delete: isNotNull condition should pass")
	void deleteWithIsNotNull() {
		QueryMetadata md = new DefaultQueryMetadata();
		md.addWhere(t.name.isNotNull());
		assertDoesNotThrow(() -> listener.notifyDelete(t, md));
	}
}
