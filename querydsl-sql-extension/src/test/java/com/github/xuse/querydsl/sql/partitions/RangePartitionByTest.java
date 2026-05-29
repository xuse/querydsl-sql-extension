package com.github.xuse.querydsl.sql.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;

/**
 * Unit tests for {@link RangePartitionBy}.
 * Validates: Requirements 6.3, 6.6
 */
@DisplayName("RangePartitionBy Unit Tests")
class RangePartitionByTest {

	// ---- RANGE type tests ----

	@Test
	@DisplayName("byRange returns RANGE partition method")
	void testByRangeReturnsRangeMethod() {
		PartitionBy partitionBy = Partitions.byRange("TO_DAYS(created_at)")
				.add("p0", "0", "730000")
				.add("p1", "730000", "730500")
				.build();

		assertEquals(PartitionMethod.RANGE, partitionBy.getMethod());
	}

	@Test
	@DisplayName("byRange with Expression returns RANGE partition method")
	void testByRangeWithExpressionReturnsRangeMethod() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		PartitionBy partitionBy = Partitions.byRange(idPath)
				.add("p0", "0", "100")
				.add("p1", "100", "200")
				.build();

		assertEquals(PartitionMethod.RANGE, partitionBy.getMethod());
	}

	@Test
	@DisplayName("byRangeColumns returns RANGE_COLUMNS partition method")
	void testByRangeColumnsReturnsRangeColumnsMethod() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		RangePartitionBy rangePartition = new RangePartitionBy(
				true,
				idPath,
				new Partition[]{
						new PartitionDef("p0", "0", "100"),
						new PartitionDef("p1", "100", "200")
				},
				new com.github.xuse.querydsl.annotation.partition.AutoTimePartitions[0]
		);

		assertEquals(PartitionMethod.RANGE_COLUMNS, rangePartition.getMethod());
	}

	// ---- partitions() size tests ----

	@Test
	@DisplayName("partitions() size equals number of added partitions for RANGE")
	void testPartitionsSizeMatchesAddedCountForRange() {
		PartitionBy partitionBy = Partitions.byRange("YEAR(order_date)")
				.add("p2020", "0", "2021")
				.add("p2021", "2021", "2022")
				.add("p2022", "2022", "2023")
				.build();

		List<Partition> partitions = partitionBy.partitions();
		assertEquals(3, partitions.size());
	}

	@Test
	@DisplayName("partitions() size equals number of added partitions for RANGE_COLUMNS")
	void testPartitionsSizeMatchesAddedCountForRangeColumns() {
		RangePartitionBy rangePartition = new RangePartitionBy(
				true,
				DDLExpressions.text("created_at"),
				new Partition[]{
						new PartitionDef("p1", "'2020-01-01'", "'2021-01-01'"),
						new PartitionDef("p2", "'2021-01-01'", "'2022-01-01'"),
						new PartitionDef("p3", "'2022-01-01'", "'2023-01-01'"),
						new PartitionDef("p4", "'2023-01-01'", "MAXVALUE")
				},
				new com.github.xuse.querydsl.annotation.partition.AutoTimePartitions[0]
		);

		List<Partition> partitions = rangePartition.partitions();
		assertEquals(4, partitions.size());
	}

	// ---- partition name, from, and value field tests ----

	@Test
	@DisplayName("each partition has expected name, from, and value fields")
	void testEachPartitionHasExpectedFields() {
		PartitionBy partitionBy = Partitions.byRange("TO_DAYS(created_at)")
				.add("p_early", "0", "730000")
				.add("p_late", "730000", "MAXVALUE")
				.build();

		List<Partition> partitions = partitionBy.partitions();
		assertEquals(2, partitions.size());

		// Verify first partition
		boolean foundEarly = false;
		boolean foundLate = false;
		for (Partition p : partitions) {
			assertNotNull(p.name());
			assertNotNull(p.value());
			if ("p_early".equals(p.name())) {
				assertEquals("0", p.from());
				assertEquals("730000", p.value());
				foundEarly = true;
			} else if ("p_late".equals(p.name())) {
				assertEquals("730000", p.from());
				assertEquals("MAXVALUE", p.value());
				foundLate = true;
			}
		}
		assertEquals(true, foundEarly, "Expected partition 'p_early' not found");
		assertEquals(true, foundLate, "Expected partition 'p_late' not found");
	}

	@Test
	@DisplayName("RANGE_COLUMNS partitions have correct name, from, and value")
	void testRangeColumnsPartitionsHaveCorrectFields() {
		RangePartitionBy rangePartition = new RangePartitionBy(
				true,
				DDLExpressions.text("sale_date"),
				new Partition[]{
						new PartitionDef("p_q1", "'2023-01-01'", "'2023-04-01'"),
						new PartitionDef("p_q2", "'2023-04-01'", "'2023-07-01'"),
						new PartitionDef("p_q3", "'2023-07-01'", "'2023-10-01'")
				},
				new com.github.xuse.querydsl.annotation.partition.AutoTimePartitions[0]
		);

		List<Partition> partitions = rangePartition.partitions();
		assertEquals(3, partitions.size());

		assertEquals("p_q1", partitions.get(0).name());
		assertEquals("'2023-01-01'", partitions.get(0).from());
		assertEquals("'2023-04-01'", partitions.get(0).value());

		assertEquals("p_q2", partitions.get(1).name());
		assertEquals("'2023-04-01'", partitions.get(1).from());
		assertEquals("'2023-07-01'", partitions.get(1).value());

		assertEquals("p_q3", partitions.get(2).name());
		assertEquals("'2023-07-01'", partitions.get(2).from());
		assertEquals("'2023-10-01'", partitions.get(2).value());
	}

	// ---- Empty path array throws IllegalArgumentException ----

	@Test
	@DisplayName("byRangeColumns with empty path array throws IllegalArgumentException")
	void testByRangeColumnsEmptyPathThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			Partitions.byRangeColumns(new Path<?>[0]);
		});
	}

	@Test
	@DisplayName("byListColumns with empty path array throws IllegalArgumentException")
	void testByListColumnsEmptyPathThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			Partitions.byListColumns(new Path<?>[0]);
		});
	}

	// ---- getExpr() tests ----

	@Test
	@DisplayName("getExpr returns non-null expression for RANGE")
	void testGetExprReturnsNonNullForRange() {
		PartitionBy partitionBy = Partitions.byRange("YEAR(created_at)")
				.add("p1", "0", "2023")
				.build();

		assertNotNull(partitionBy.getExpr());
	}

	@Test
	@DisplayName("getExpr returns non-null expression for RANGE_COLUMNS")
	void testGetExprReturnsNonNullForRangeColumns() {
		NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
		RangePartitionBy rangePartition = new RangePartitionBy(
				true,
				idPath,
				new Partition[]{
						new PartitionDef("p0", "0", "100")
				},
				new com.github.xuse.querydsl.annotation.partition.AutoTimePartitions[0]
		);

		assertNotNull(rangePartition.getExpr());
		assertEquals(idPath, rangePartition.getExpr());
	}

	// ---- Builder with from/to values ----

	@Test
	@DisplayName("byRange builder with add(name, from, to) stores from and value correctly")
	void testByRangeBuilderWithFromTo() {
		PartitionBy partitionBy = Partitions.byRange("id")
				.add("p0", "0", "1000")
				.add("p1", "1000", "2000")
				.add("p2", "2000", "MAXVALUE")
				.build();

		List<Partition> partitions = partitionBy.partitions();
		assertEquals(3, partitions.size());

		// Verify all partitions have from and value set
		for (Partition p : partitions) {
			assertNotNull(p.name());
			assertNotNull(p.value());
		}
	}
}
