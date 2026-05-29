package com.github.xuse.querydsl.sql.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;

/**
 * Unit tests for {@link ListPartitionBy}.
 * Validates: Requirements 6.2
 */
@DisplayName("ListPartitionBy Unit Tests")
class ListPartitionByTest {

	@Test
	@DisplayName("byList returns LIST partition method")
	void testByListReturnsListMethod() {
		PartitionBy partitionBy = Partitions.byList("status")
				.add("p_active", "'A','B'")
				.add("p_inactive", "'C','D'")
				.build();

		assertEquals(PartitionMethod.LIST, partitionBy.getMethod());
	}

	@Test
	@DisplayName("byListColumns returns LIST_COLUMNS partition method")
	void testByListColumnsReturnsListColumnsMethod() {
		// Use DDLExpressions.text to simulate a column path for unit testing
		PartitionBy partitionBy = Partitions.byList("region")
				.build();
		// Build a LIST_COLUMNS variant using the isColumns=true constructor
		ListPartitionBy listPartition = new ListPartitionBy(
				true,
				com.github.xuse.querydsl.sql.ddl.DDLExpressions.text("region"),
				new Partition[]{
						new PartitionDef("p_east", null, "'east','northeast'"),
						new PartitionDef("p_west", null, "'west','northwest'")
				}
		);

		assertEquals(PartitionMethod.LIST_COLUMNS, listPartition.getMethod());
	}

	@Test
	@DisplayName("partitions() size equals number of added partitions")
	void testPartitionsSizeMatchesAddedCount() {
		PartitionBy partitionBy = Partitions.byList("category")
				.add("p1", "'electronics'")
				.add("p2", "'clothing'")
				.add("p3", "'food'")
				.build();

		List<Partition> partitions = partitionBy.partitions();
		assertEquals(3, partitions.size());
	}

	@Test
	@DisplayName("each partition has expected name and value")
	void testEachPartitionHasExpectedNameAndValue() {
		PartitionBy partitionBy = Partitions.byList("status")
				.add("p_active", "'1','2','3'")
				.add("p_closed", "'4','5'")
				.build();

		List<Partition> partitions = partitionBy.partitions();
		assertEquals(2, partitions.size());

		// Verify partition names and values exist
		boolean foundActive = false;
		boolean foundClosed = false;
		for (Partition p : partitions) {
			assertNotNull(p.name());
			assertNotNull(p.value());
			if ("p_active".equals(p.name())) {
				assertEquals("'1','2','3'", p.value());
				foundActive = true;
			} else if ("p_closed".equals(p.name())) {
				assertEquals("'4','5'", p.value());
				foundClosed = true;
			}
		}
		assertEquals(true, foundActive, "Expected partition 'p_active' not found");
		assertEquals(true, foundClosed, "Expected partition 'p_closed' not found");
	}

	@Test
	@DisplayName("byListColumns with multiple partitions returns correct size and values")
	void testByListColumnsMultiplePartitions() {
		ListPartitionBy listPartition = new ListPartitionBy(
				true,
				com.github.xuse.querydsl.sql.ddl.DDLExpressions.text("region"),
				new Partition[]{
						new PartitionDef("p_north", null, "'N'"),
						new PartitionDef("p_south", null, "'S'"),
						new PartitionDef("p_east", null, "'E'"),
						new PartitionDef("p_west", null, "'W'")
				}
		);

		assertEquals(PartitionMethod.LIST_COLUMNS, listPartition.getMethod());
		List<Partition> partitions = listPartition.partitions();
		assertEquals(4, partitions.size());

		assertEquals("p_north", partitions.get(0).name());
		assertEquals("'N'", partitions.get(0).value());
		assertEquals("p_south", partitions.get(1).name());
		assertEquals("'S'", partitions.get(1).value());
		assertEquals("p_east", partitions.get(2).name());
		assertEquals("'E'", partitions.get(2).value());
		assertEquals("p_west", partitions.get(3).name());
		assertEquals("'W'", partitions.get(3).value());
	}

	@Test
	@DisplayName("getExpr returns non-null expression")
	void testGetExprReturnsNonNull() {
		PartitionBy partitionBy = Partitions.byList("TO_DAYS(created)")
				.add("p1", "1")
				.build();

		assertNotNull(partitionBy.getExpr());
	}
}
