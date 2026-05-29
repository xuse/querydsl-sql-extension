package com.github.xuse.querydsl.sql.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.partition.HashType;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Unit tests for {@link Partitions} factory methods.
 * Validates: Requirements 6.4, 6.5
 */
@DisplayName("Partitions Factory Method Tests")
class PartitionsTest {

	@Nested
	@DisplayName("byHash factory methods")
	class ByHashTests {

		@Test
		@DisplayName("byHash with Path returns HashPartitionBy instance")
		void testByHashWithPathReturnsHashPartitionBy() {
			NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
			PartitionBy result = Partitions.byHash(HashType.HASH, idPath, 4);

			assertNotNull(result);
			assertInstanceOf(HashPartitionBy.class, result);
		}

		@Test
		@DisplayName("byHash with String expr returns HashPartitionBy instance")
		void testByHashWithStringExprReturnsHashPartitionBy() {
			PartitionBy result = Partitions.byHash(HashType.KEY, "YEAR(created_at)", 8);

			assertNotNull(result);
			assertInstanceOf(HashPartitionBy.class, result);
		}

		@Test
		@DisplayName("byHash returns correct partition method for each HashType")
		void testByHashReturnsCorrectMethod() {
			NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");

			assertEquals(PartitionMethod.HASH, Partitions.byHash(HashType.HASH, idPath, 4).getMethod());
			assertEquals(PartitionMethod.LINEAR_HASH, Partitions.byHash(HashType.LINEAR_HASH, idPath, 4).getMethod());
			assertEquals(PartitionMethod.KEY, Partitions.byHash(HashType.KEY, idPath, 4).getMethod());
		}
	}

	@Nested
	@DisplayName("byList factory methods")
	class ByListTests {

		@Test
		@DisplayName("byList with String expr returns builder that builds ListPartitionBy")
		void testByListStringReturnsListPartitionBy() {
			PartitionBy result = Partitions.byList("status")
					.add("p1", "'A'")
					.build();

			assertNotNull(result);
			assertInstanceOf(ListPartitionBy.class, result);
			assertEquals(PartitionMethod.LIST, result.getMethod());
		}

		@Test
		@DisplayName("byList with Expression returns builder that builds ListPartitionBy")
		void testByListExpressionReturnsListPartitionBy() {
			StringPath statusPath = Expressions.stringPath("status");
			PartitionBy result = Partitions.byList(statusPath)
					.add("p1", "'active'")
					.build();

			assertNotNull(result);
			assertInstanceOf(ListPartitionBy.class, result);
			assertEquals(PartitionMethod.LIST, result.getMethod());
		}
	}

	@Nested
	@DisplayName("byListColumns factory methods")
	class ByListColumnsTests {

		@Test
		@DisplayName("byListColumns returns builder that builds ListPartitionBy with LIST_COLUMNS method")
		void testByListColumnsReturnsListPartitionBy() {
			StringPath regionPath = Expressions.stringPath("region");
			PartitionBy result = Partitions.byListColumns(regionPath)
					.add("p_east", "'east'")
					.add("p_west", "'west'")
					.build();

			assertNotNull(result);
			assertInstanceOf(ListPartitionBy.class, result);
			assertEquals(PartitionMethod.LIST_COLUMNS, result.getMethod());
		}

		@Test
		@DisplayName("byListColumns with empty path array throws IllegalArgumentException")
		void testByListColumnsEmptyPathThrows() {
			assertThrows(IllegalArgumentException.class, () -> Partitions.byListColumns());
		}
	}

	@Nested
	@DisplayName("byRange factory methods")
	class ByRangeTests {

		@Test
		@DisplayName("byRange with String expr returns builder that builds RangePartitionBy")
		void testByRangeStringReturnsRangePartitionBy() {
			PartitionBy result = Partitions.byRange("TO_DAYS(created)")
					.add("p1", "100")
					.build();

			assertNotNull(result);
			assertInstanceOf(RangePartitionBy.class, result);
			assertEquals(PartitionMethod.RANGE, result.getMethod());
		}

		@Test
		@DisplayName("byRange with Expression returns builder that builds RangePartitionBy")
		void testByRangeExpressionReturnsRangePartitionBy() {
			NumberPath<Integer> yearPath = Expressions.numberPath(Integer.class, "year_col");
			PartitionBy result = Partitions.byRange(yearPath)
					.add("p2020", "2020")
					.add("p2021", "2021")
					.build();

			assertNotNull(result);
			assertInstanceOf(RangePartitionBy.class, result);
			assertEquals(PartitionMethod.RANGE, result.getMethod());
		}
	}

	@Nested
	@DisplayName("byRangeColumns factory methods")
	class ByRangeColumnsTests {

		@Test
		@DisplayName("byRangeColumns returns builder that builds RangePartitionBy with RANGE_COLUMNS method")
		void testByRangeColumnsReturnsRangePartitionBy() {
			NumberPath<Integer> idPath = Expressions.numberPath(Integer.class, "id");
			PartitionBy result = Partitions.byRangeColumns(idPath)
					.add("p1", "100")
					.add("p2", "200")
					.build();

			assertNotNull(result);
			assertInstanceOf(RangePartitionBy.class, result);
			assertEquals(PartitionMethod.RANGE_COLUMNS, result.getMethod());
		}

		@Test
		@DisplayName("byRangeColumns with empty path array throws IllegalArgumentException")
		void testByRangeColumnsEmptyPathThrows() {
			assertThrows(IllegalArgumentException.class, () -> Partitions.byRangeColumns());
		}
	}
}
