package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.SQLTemplates;

/**
 * Unit tests for {@link SQLTemplatesEx} interface default methods.
 * Tests via concrete implementations (MySQLWithJSONTemplates, H2TemplatesEx, DefaultSQLTemplatesEx).
 */
class SQLTemplatesExTest {

	private final SQLTemplatesEx mysql = new MySQLWithJSONTemplates();
	private final SQLTemplatesEx h2 = new H2TemplatesEx(com.querydsl.sql.H2Templates.DEFAULT);

	// --- getIfExists ---
	@Test
	void testGetIfExists_mysql() {
		// MySQL implementation may return "IF EXISTS " 
		String ifExists = mysql.getIfExists();
		assertNotNull(ifExists, "MySQL should support IF EXISTS");
	}

	// --- getColumnDataType ---
	@Test
	void testGetColumnDataType_mysql() {
		ColumnDef def = mysql.getColumnDataType(java.sql.Types.VARCHAR, 255, 0);
		assertNotNull(def);
		assertNotNull(def.getDataType());
	}

	@Test
	void testGetColumnDataType_h2() {
		ColumnDef def = h2.getColumnDataType(java.sql.Types.INTEGER, 0, 0);
		assertNotNull(def);
	}

	// --- supportCreateInTableDefinition ---
	@Test
	void testSupportCreateInTableDefinition_default() {
		// Default returns true for all types
		assertTrue(mysql.supportCreateInTableDefinition(
				com.github.xuse.querydsl.sql.ddl.ConstraintTypeDef.KEY));
	}

	// --- getSchemaAccessor ---
	@Test
	void testGetSchemaAccessor() {
		assertNotNull(mysql.getSchemaAccessor());
		assertNotNull(h2.getSchemaAccessor());
	}

	// --- getDefaultLetterCase ---
	@Test
	void testGetDefaultLetterCase_mysql() {
		// MySQL defaults to lowercase
		assertNotNull(mysql.getDefaultLetterCase());
	}

	@Test
	void testGetDefaultLetterCase_default() {
		// DefaultSQLTemplatesEx may return a letter case based on the underlying templates
		DefaultSQLTemplatesEx defaultEx = new DefaultSQLTemplatesEx(SQLTemplates.DEFAULT);
		// Just verify it doesn't throw
		defaultEx.getDefaultLetterCase();
	}

	// --- getPrivilegeDetector ---
	@Test
	void testGetPrivilegeDetector() {
		assertNotNull(mysql.getPrivilegeDetector());
	}

	// --- initDefaultDDLTemplate ---
	@Test
	void testInitDefaultDDLTemplate() {
		// Verify that init populates templates without error
		SQLTemplates templates = SQLTemplates.DEFAULT;
		SQLTemplatesEx.initDefaultDDLTemplate(templates);
		// No exception means success
	}

	// --- notSupports / supports ---
	@Test
	void testSupportsOperator() {
		// MySQL supports most operators
		assertFalse(mysql.notSupports(com.github.xuse.querydsl.sql.ddl.DDLOps.DEFAULT));
		assertTrue(mysql.supports(com.github.xuse.querydsl.sql.ddl.DDLOps.DEFAULT));
	}

	// --- getSchemaPolicy ---
	@Test
	void testGetSchemaPolicy() {
		assertNotNull(mysql.getSchemaPolicy());
	}

	// --- getOriginal ---
	@Test
	void testGetOriginal() {
		SQLTemplates original = mysql.getOriginal();
		assertNotNull(original);
	}

	// --- getColumnSizeParser ---
	@Test
	void testGetColumnSizeParser() {
		SizeParser parser = mysql.getColumnSizeParser(java.sql.Types.TIMESTAMP);
		assertNotNull(parser);
	}

	// --- getDummyTable ---
	@Test
	void testGetDummyTable() {
		// MySQL has DUAL as dummy table
		String dummy = mysql.getDummyTable();
		// May be null or "dual" depending on implementation
		// Just verify no exception
	}

	// --- getAutoIncrement ---
	@Test
	void testGetAutoIncrement() {
		String autoInc = mysql.getAutoIncrement();
		assertNotNull(autoInc);
		assertTrue(autoInc.toLowerCase().contains("auto_increment"),
				"MySQL should have AUTO_INCREMENT: " + autoInc);
	}

	// --- isWrapSelectParameters ---
	@Test
	void testIsWrapSelectParameters() {
		// Just verify no exception
		mysql.isWrapSelectParameters();
	}

	// --- getCreateTable ---
	@Test
	void testGetCreateTable() {
		String createTable = mysql.getCreateTable();
		assertNotNull(createTable);
	}

	// --- isBatchToBulkSupported ---
	@Test
	void testIsBatchToBulkSupported() {
		assertTrue(mysql.isBatchToBulkSupported(), "MySQL should support batch to bulk");
	}

	// --- isBatchToBulkInDefault ---
	@Test
	void testIsBatchToBulkInDefault() {
		assertTrue(mysql.isBatchToBulkInDefault(), "MySQL should default to batch-to-bulk");
	}

	// --- translateDefault ---
	@Test
	void testTranslateDefault() {
		String result = mysql.translateDefault("'hello'", java.sql.Types.VARCHAR, 255, 0);
		assertNotNull(result);
		// The result depends on the dialect's serialization logic
		assertTrue(result.contains("hello"), "Should contain the value: " + result);
	}

	// --- getDefaultExpr ---
	@Test
	void testGetDefaultExpr() {
		assertNotNull(mysql.getDefaultExpr());
	}

	// --- add (helper method) ---
	@Test
	void testAddOperator() {
		// Verify the add helper works without exception
		SQLTemplates templates = mysql.getOriginal();
		mysql.add(templates, com.github.xuse.querydsl.sql.ddl.DDLOps.DEFAULT, "DEFAULT {0}");
	}
}
