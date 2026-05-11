package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.entity.TableDataTypes;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;
import com.querydsl.core.types.Path;

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
}
