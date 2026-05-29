package com.github.xuse.querydsl.sql.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.partition.HashType;
import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;

/**
 * Unit tests for {@link HashPartitionBy}.
 */
@DisplayName("HashPartitionBy Unit Tests")
class HashPartitionByTest {

	// ---- HASH type tests ----

	@Test
	@DisplayName("HASH type: getMethod() returns PartitionMethod.HASH")
	void testHashTypeReturnsHashMethod() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		HashPartitionBy partition = new HashPartitionBy(HashType.HASH, idPath, 4);

		assertEquals(PartitionMethod.HASH, partition.getMethod());
	}

	@Test
	@DisplayName("HASH type: count() returns specified bucket count")
	void testHashTypeCount() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		HashPartitionBy partition = new HashPartitionBy(HashType.HASH, idPath, 4);

		assertEquals(4, partition.count());
	}

	@Test
	@DisplayName("HASH type: getExpr() returns non-null expression matching input")
	void testHashTypeExpr() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		HashPartitionBy partition = new HashPartitionBy(HashType.HASH, idPath, 4);

		Expression<?> expr = partition.getExpr();
		assertNotNull(expr);
		assertEquals(idPath, expr);
	}

	@Test
	@DisplayName("HASH type: partitions() returns empty list")
	void testHashTypePartitionsEmpty() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		HashPartitionBy partition = new HashPartitionBy(HashType.HASH, idPath, 4);

		List<Partition> partitions = partition.partitions();
		assertNotNull(partitions);
		assertTrue(partitions.isEmpty());
	}

	// ---- LINEAR_HASH type tests ----

	@Test
	@DisplayName("LINEAR_HASH type: getMethod() returns PartitionMethod.LINEAR_HASH")
	void testLinearHashTypeReturnsLinearHashMethod() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "user_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.LINEAR_HASH, idPath, 8);

		assertEquals(PartitionMethod.LINEAR_HASH, partition.getMethod());
	}

	@Test
	@DisplayName("LINEAR_HASH type: count() returns specified bucket count")
	void testLinearHashTypeCount() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "user_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.LINEAR_HASH, idPath, 8);

		assertEquals(8, partition.count());
	}

	@Test
	@DisplayName("LINEAR_HASH type: getExpr() returns non-null expression")
	void testLinearHashTypeExpr() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "user_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.LINEAR_HASH, idPath, 8);

		Expression<?> expr = partition.getExpr();
		assertNotNull(expr);
		assertEquals(idPath, expr);
	}

	@Test
	@DisplayName("LINEAR_HASH type: partitions() returns empty list")
	void testLinearHashTypePartitionsEmpty() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "user_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.LINEAR_HASH, idPath, 8);

		List<Partition> partitions = partition.partitions();
		assertNotNull(partitions);
		assertTrue(partitions.isEmpty());
	}

	// ---- KEY type tests ----

	@Test
	@DisplayName("KEY type: getMethod() returns PartitionMethod.KEY")
	void testKeyTypeReturnsKeyMethod() {
		NumberPath<Long> idPath = Expressions.numberPath(Long.class, "order_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.KEY, idPath, 16);

		assertEquals(PartitionMethod.KEY, partition.getMethod());
	}

	@Test
	@DisplayName("KEY type: count() returns specified bucket count")
	void testKeyTypeCount() {
		NumberPath<Long> idPath = Expressions.numberPath(Long.class, "order_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.KEY, idPath, 16);

		assertEquals(16, partition.count());
	}

	@Test
	@DisplayName("KEY type: getExpr() returns non-null expression")
	void testKeyTypeExpr() {
		NumberPath<Long> idPath = Expressions.numberPath(Long.class, "order_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.KEY, idPath, 16);

		Expression<?> expr = partition.getExpr();
		assertNotNull(expr);
		assertEquals(idPath, expr);
	}

	@Test
	@DisplayName("KEY type: partitions() returns empty list")
	void testKeyTypePartitionsEmpty() {
		NumberPath<Long> idPath = Expressions.numberPath(Long.class, "order_id");
		HashPartitionBy partition = new HashPartitionBy(HashType.KEY, idPath, 16);

		List<Partition> partitions = partition.partitions();
		assertNotNull(partitions);
		assertTrue(partitions.isEmpty());
	}

	// ---- Factory method tests ----

	@Test
	@DisplayName("Partitions.byHash with Path: returns correct HashPartitionBy")
	void testFactoryMethodWithPath() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		PartitionBy partition = Partitions.byHash(HashType.HASH, idPath, 4);

		assertNotNull(partition);
		assertEquals(PartitionMethod.HASH, partition.getMethod());
		assertEquals(4, partition.count());
		assertNotNull(partition.getExpr());
	}

	@Test
	@DisplayName("Partitions.byHash with String expr: returns correct HashPartitionBy")
	void testFactoryMethodWithStringExpr() {
		PartitionBy partition = Partitions.byHash(HashType.KEY, "YEAR(created_at)", 12);

		assertNotNull(partition);
		assertEquals(PartitionMethod.KEY, partition.getMethod());
		assertEquals(12, partition.count());
		assertNotNull(partition.getExpr());
	}

	// ---- Distinct bucket count values ----

	@Test
	@DisplayName("Different bucket counts: verifies distinct values (4 and 8)")
	void testDistinctBucketCounts() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");

		HashPartitionBy partition4 = new HashPartitionBy(HashType.HASH, idPath, 4);
		HashPartitionBy partition8 = new HashPartitionBy(HashType.HASH, idPath, 8);

		assertEquals(4, partition4.count());
		assertEquals(8, partition8.count());
	}
}
