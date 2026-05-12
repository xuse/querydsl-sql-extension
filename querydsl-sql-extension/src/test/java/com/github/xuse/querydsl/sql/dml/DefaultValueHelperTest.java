package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.TemplateExpression;

/**
 * Unit tests for {@link DefaultValueHelper}.
 * <p>
 * Verifies that NOT NULL columns with simple default values are correctly
 * pre-computed as Java literal substitutions, and that the substitution
 * logic correctly replaces null values in bean value arrays.
 */
public class DefaultValueHelperTest extends AbstractTestBase {

	private static QTableDataTypes entity;

	@BeforeAll
	public static void init() {
		doInit();
		entity = QTableDataTypes.aaa;
	}

	@Test
	public void testComputeNullSubstitutions_returnsSubstitutionsForNotNullColumns() {
		List<Path<?>> columns = entity.getColumns();
		Object[] substitutions = DefaultValueHelper.computeNullSubstitutions(entity, columns);

		// Should not be null since QTableDataTypes has many NOT NULL columns with defaults
		assertNotNull(substitutions, "Substitutions should not be null for entity with NOT NULL default columns");

		// 'gender' and 'taskStatus' have CustomType (EnumByCodeType), so they are correctly skipped
		int genderIdx = columns.indexOf(entity.gender);
		if (genderIdx >= 0 && substitutions.length > genderIdx) {
			assertNull(substitutions[genderIdx], "gender has CustomType, should have no substitution");
		}

		int taskStatusIdx = columns.indexOf(entity.taskStatus);
		if (taskStatusIdx >= 0 && substitutions.length > taskStatusIdx) {
			assertNull(substitutions[taskStatusIdx], "taskStatus has CustomType, should have no substitution");
		}

		// Find the index of 'version' column (NOT NULL, defaultValue=1)
		int versionIdx = columns.indexOf(entity.version);
		if (versionIdx >= 0 && substitutions.length > versionIdx) {
			assertEquals(1, substitutions[versionIdx], "version default should be 1");
		}

		// Find the index of 'dataInt' column (NOT NULL, defaultValue=0)
		int dataIntIdx = columns.indexOf(entity.dataInt);
		if (dataIntIdx >= 0 && substitutions.length > dataIntIdx) {
			assertEquals(0, substitutions[dataIntIdx], "dataInt default should be 0");
		}

		// 'name' is nullable, should have no substitution
		int nameIdx = columns.indexOf(entity.name);
		if (nameIdx >= 0 && substitutions.length > nameIdx) {
			assertNull(substitutions[nameIdx], "nullable column 'name' should have no substitution");
		}

		// 'dateTimestamp' is NOT NULL but default is CURRENT_TIMESTAMP (complex expression)
		// Should NOT have a substitution since we can't parse SQL functions
		int tsIdx = columns.indexOf(entity.dateTimestamp);
		if (tsIdx >= 0 && substitutions.length > tsIdx) {
			assertNull(substitutions[tsIdx],
					"CURRENT_TIMESTAMP default should not be parsed to Java literal");
		}
	}

	@Test
	public void testComputeNullSubstitutions_stringDefaultValue() {
		// dataText has defaultValue=" " (a space), and it's nullable → no substitution
		List<Path<?>> columns = entity.getColumns();
		Object[] substitutions = DefaultValueHelper.computeNullSubstitutions(entity, columns);

		int dataTextIdx = columns.indexOf(entity.dataText);
		if (dataTextIdx >= 0 && substitutions != null && substitutions.length > dataTextIdx) {
			// dataText is nullable, so no substitution expected
			assertNull(substitutions[dataTextIdx], "nullable column should have no substitution");
		}
	}

	@Test
	public void testParseDefaultToJavaValue_enumByName() {
		// Test parsing enum from string (VARCHAR column)
		TemplateExpression<String> expr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "'MALE'");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.VARCHAR, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertEquals(com.github.xuse.querydsl.enums.Gender.MALE, result, 
				"Should parse 'MALE' to Gender.MALE enum");
	}

	@Test
	public void testParseDefaultToJavaValue_enumByOrdinal() {
		// Test parsing enum from number (INTEGER column) using ordinal
		TemplateExpression<Integer> expr = 
				com.querydsl.core.types.dsl.Expressions.template(Integer.class, "1");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.INTEGER, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertEquals(com.github.xuse.querydsl.enums.Gender.FEMALE, result, 
				"Should parse 1 to Gender.FEMALE (ordinal 1)");
	}

	@Test
	public void testParseDefaultToJavaValue_enumByCode() {
		// Test parsing enum from number (INTEGER column) using CodeEnum.getCode()
		// Since Gender implements CodeEnum with getCode() returning ordinal(), 
		// this should work the same as ordinal
		TemplateExpression<Integer> expr = 
				com.querydsl.core.types.dsl.Expressions.template(Integer.class, "2");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.INTEGER, 
				com.github.xuse.querydsl.enums.TaskStatus.class, 
				"taskStatus");
		
		assertEquals(com.github.xuse.querydsl.enums.TaskStatus.FAIL, result, 
				"Should parse 2 to TaskStatus.FAIL (code 2)");
	}

	@Test
	public void testParseDefaultToJavaValue_enumInvalidName() {
		// Test parsing invalid enum name
		TemplateExpression<String> expr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "'INVALID'");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.VARCHAR, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertNull(result, "Should return null for invalid enum name");
	}

	@Test
	public void testParseDefaultToJavaValue_enumOutOfRangeOrdinal() {
		// Test parsing out-of-range ordinal
		TemplateExpression<Integer> expr = 
				com.querydsl.core.types.dsl.Expressions.template(Integer.class, "99");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.INTEGER, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertNull(result, "Should return null for out-of-range ordinal");
	}

	@Test
	public void testParseDefaultToJavaValue_enumWithCLOB() {
		// Test parsing enum from CLOB column (should use name parser)
		TemplateExpression<String> expr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "'FEMALE'");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.CLOB, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertEquals(com.github.xuse.querydsl.enums.Gender.FEMALE, result, 
				"Should parse 'FEMALE' from CLOB column");
	}

	@Test
	public void testParseDefaultToJavaValue_enumWithNCLOB() {
		// Test parsing enum from NCLOB column (should use name parser)
		TemplateExpression<String> expr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "'RUNNING'");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.NCLOB, 
				com.github.xuse.querydsl.enums.TaskStatus.class, 
				"taskStatus");
		
		assertEquals(com.github.xuse.querydsl.enums.TaskStatus.RUNNING, result, 
				"Should parse 'RUNNING' from NCLOB column");
	}

	@Test
	public void testParseDefaultToJavaValue_enumWithCHAR() {
		// Test parsing enum from CHAR column
		TemplateExpression<String> expr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "'MALE'");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.CHAR, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertEquals(com.github.xuse.querydsl.enums.Gender.MALE, result, 
				"Should parse 'MALE' from CHAR column");
	}

	@Test
	public void testParseDefaultToJavaValue_enumWithNVARCHAR() {
		// Test parsing enum from NVARCHAR column
		TemplateExpression<String> expr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "'SUCCESS'");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.NVARCHAR, 
				com.github.xuse.querydsl.enums.TaskStatus.class, 
				"taskStatus");
		
		assertEquals(com.github.xuse.querydsl.enums.TaskStatus.SUCCESS, result, 
				"Should parse 'SUCCESS' from NVARCHAR column");
	}

	@Test
	public void testParseDefaultToJavaValue_enumWithTINYINT() {
		// Test parsing enum from TINYINT column
		TemplateExpression<Integer> expr = 
				com.querydsl.core.types.dsl.Expressions.template(Integer.class, "0");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.TINYINT, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertEquals(com.github.xuse.querydsl.enums.Gender.MALE, result, 
				"Should parse 0 to Gender.MALE from TINYINT column");
	}

	@Test
	public void testParseDefaultToJavaValue_enumWithBIGINT() {
		// Test parsing enum from BIGINT column
		TemplateExpression<Long> expr = 
				com.querydsl.core.types.dsl.Expressions.template(Long.class, "3");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.BIGINT, 
				com.github.xuse.querydsl.enums.TaskStatus.class, 
				"taskStatus");
		
		assertEquals(com.github.xuse.querydsl.enums.TaskStatus.SUCCESS, result, 
				"Should parse 3 to TaskStatus.SUCCESS from BIGINT column");
	}

	@Test
	public void testParseDefaultToJavaValue_enumZeroValue() {
		// Test parsing zero value (first enum constant)
		TemplateExpression<Integer> expr = 
				com.querydsl.core.types.dsl.Expressions.template(Integer.class, "0");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.INTEGER, 
				com.github.xuse.querydsl.enums.TaskStatus.class, 
				"taskStatus");
		
		assertEquals(com.github.xuse.querydsl.enums.TaskStatus.INIT, result, 
				"Should parse 0 to TaskStatus.INIT (first constant)");
	}

	@Test
	public void testParseDefaultToJavaValue_enumEmptyString() {
		// Test parsing empty string
		TemplateExpression<String> expr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "''");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.VARCHAR, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertNull(result, "Should return null for empty string");
	}

	@Test
	public void testParseDefaultToJavaValue_enumNegativeOrdinal() {
		// Test parsing negative ordinal
		TemplateExpression<Integer> expr = 
				com.querydsl.core.types.dsl.Expressions.template(Integer.class, "-1");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				expr, 
				java.sql.Types.INTEGER, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertNull(result, "Should return null for negative ordinal");
	}

	@Test
	public void testParseDefaultToJavaValue_standardTypes() {
		// Test parsing standard types still work correctly
		
		// String
		TemplateExpression<String> strExpr = 
				com.querydsl.core.types.dsl.Expressions.template(String.class, "'test'");
		Object strResult = DefaultValueHelper.parseDefaultToJavaValue(
				strExpr, java.sql.Types.VARCHAR, String.class, "name");
		assertEquals("test", strResult, "Should parse string correctly");
		
		// Integer
		TemplateExpression<Integer> intExpr = 
				com.querydsl.core.types.dsl.Expressions.template(Integer.class, "42");
		Object intResult = DefaultValueHelper.parseDefaultToJavaValue(
				intExpr, java.sql.Types.INTEGER, Integer.class, "count");
		assertEquals(42, intResult, "Should parse integer correctly");
		
		// Boolean
		TemplateExpression<Boolean> boolExpr = 
				com.querydsl.core.types.dsl.Expressions.template(Boolean.class, "true");
		Object boolResult = DefaultValueHelper.parseDefaultToJavaValue(
				boolExpr, java.sql.Types.BOOLEAN, Boolean.class, "flag");
		assertEquals(true, boolResult, "Should parse boolean correctly");
	}

	@Test
	public void testParseDefaultToJavaValue_constantEnum() {
		// Test parsing enum from Constant expression (not TemplateExpression)
		com.querydsl.core.types.Constant<Integer> constExpr = 
				com.querydsl.core.types.ConstantImpl.create(1);
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				constExpr, 
				java.sql.Types.INTEGER, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertEquals(com.github.xuse.querydsl.enums.Gender.FEMALE, result, 
				"Should parse constant 1 to Gender.FEMALE");
	}

	@Test
	public void testParseDefaultToJavaValue_constantString() {
		// Test parsing string constant
		com.querydsl.core.types.Constant<String> constExpr = 
				com.querydsl.core.types.ConstantImpl.create("MALE");
		
		Object result = DefaultValueHelper.parseDefaultToJavaValue(
				constExpr, 
				java.sql.Types.VARCHAR, 
				com.github.xuse.querydsl.enums.Gender.class, 
				"gender");
		
		assertEquals(com.github.xuse.querydsl.enums.Gender.MALE, result, 
				"Should parse constant 'MALE' to Gender.MALE");
	}
}
