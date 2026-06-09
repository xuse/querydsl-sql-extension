package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DbType} verifying enum lookup by name/alias
 * and JDBC URL database name extraction.
 *
 * Requirements: 8.8
 */
@DisplayName("DbType - database type enum lookup and URL parsing")
class DbTypeTest {

	@Nested
	@DisplayName("find - known product names")
	class FindKnownNames {

		@Test
		@DisplayName("'mysql' returns DbType.mysql")
		void testFindMysql() {
			assertSame(DbType.mysql, DbType.find("mysql"));
		}

		@Test
		@DisplayName("'postgresql' returns DbType.postgresql")
		void testFindPostgresql() {
			assertSame(DbType.postgresql, DbType.find("postgresql"));
		}

		@Test
		@DisplayName("'h2' returns DbType.h2")
		void testFindH2() {
			assertSame(DbType.h2, DbType.find("h2"));
		}

		@Test
		@DisplayName("'derby' returns DbType.derby")
		void testFindDerby() {
			assertSame(DbType.derby, DbType.find("derby"));
		}

		@Test
		@DisplayName("'oracle' returns DbType.oracle")
		void testFindOracle() {
			assertSame(DbType.oracle, DbType.find("oracle"));
		}

		@Test
		@DisplayName("'sqlite' returns DbType.sqlite")
		void testFindSqlite() {
			assertSame(DbType.sqlite, DbType.find("sqlite"));
		}
	}

	@Nested
	@DisplayName("find - alias lookup")
	class FindByAlias {

		@Test
		@DisplayName("'gbasedbt' resolves to DbType.gbase8s via alias")
		void testFindGbase8sAlias() {
			assertSame(DbType.gbase8s, DbType.find("gbasedbt"));
		}

		@Test
		@DisplayName("'kingbase8' resolves to DbType.kingbase via alias")
		void testFindKingbaseAlias() {
			assertSame(DbType.kingbase, DbType.find("kingbase8"));
		}

		@Test
		@DisplayName("'taos-rs' resolves to DbType.TDengine via alias")
		void testFindTDengineAlias() {
			assertSame(DbType.TDengine, DbType.find("taos-rs"));
		}
	}

	@Nested
	@DisplayName("find - unknown names return DbType.other")
	class FindUnknownNames {

		@Test
		@DisplayName("unknown product name returns DbType.other")
		void testFindUnknownReturnsOther() {
			assertSame(DbType.other, DbType.find("nonexistent_db"));
		}

		@Test
		@DisplayName("empty string returns DbType.other")
		void testFindEmptyStringReturnsOther() {
			assertSame(DbType.other, DbType.find(""));
		}

		@Test
		@DisplayName("random string returns DbType.other")
		void testFindRandomStringReturnsOther() {
			assertSame(DbType.other, DbType.find("some_unknown_database_xyz"));
		}
	}

	@Nested
	@DisplayName("extractDbNameFromURL - JDBC URL parsing")
	class ExtractDbNameFromURL {

		@Test
		@DisplayName("jdbc:mysql://host/db yields 'mysql'")
		void testExtractMysql() {
			assertEquals("mysql", DbType.extractDbNameFromURL("jdbc:mysql://host/db"));
		}

		@Test
		@DisplayName("jdbc:postgresql://host/db yields 'postgresql'")
		void testExtractPostgresql() {
			assertEquals("postgresql", DbType.extractDbNameFromURL("jdbc:postgresql://host/db"));
		}

		@Test
		@DisplayName("jdbc:h2:mem:test yields 'h2'")
		void testExtractH2() {
			assertEquals("h2", DbType.extractDbNameFromURL("jdbc:h2:mem:test"));
		}

		@Test
		@DisplayName("jdbc:oracle:thin:@host:1521:db yields 'oracle'")
		void testExtractOracle() {
			assertEquals("oracle", DbType.extractDbNameFromURL("jdbc:oracle:thin:@host:1521:db"));
		}

		@Test
		@DisplayName("jdbc:derby:memory:testdb;create=true yields 'derby'")
		void testExtractDerby() {
			assertEquals("derby", DbType.extractDbNameFromURL("jdbc:derby:memory:testdb;create=true"));
		}

		@Test
		@DisplayName("jdbc:sqlite:test.db yields 'sqlite'")
		void testExtractSqlite() {
			assertEquals("sqlite", DbType.extractDbNameFromURL("jdbc:sqlite:test.db"));
		}

		@Test
		@DisplayName("non-jdbc URL returns the input unchanged")
		void testNonJdbcUrlReturnsInput() {
			assertEquals("some:other:url", DbType.extractDbNameFromURL("some:other:url"));
		}
	}
}
