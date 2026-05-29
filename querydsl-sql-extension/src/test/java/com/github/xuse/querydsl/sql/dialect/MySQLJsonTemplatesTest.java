package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.expression.JsonOps;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringOperation;
import com.querydsl.sql.SQLSerializerAlter;

/**
 * Unit tests for {@link MySQLWithJSONTemplates} verifying JSON function template
 * registration and SQL generation.
 *
 * Requirements: 8.1
 */
@DisplayName("MySQLWithJSONTemplates - JSON function templates")
class MySQLJsonTemplatesTest {

	private static MySQLWithJSONTemplates templates;
	private static ConfigurationEx configuration;

	@BeforeAll
	static void setUp() {
		templates = new MySQLWithJSONTemplates();
		configuration = new ConfigurationEx(templates);
	}

	// --- Template registration verification ---

	@Test
	@DisplayName("JSON_EXTRACT template is registered")
	void testJsonExtractTemplateRegistered() {
		Template template = templates.getTemplate(JsonOps.JSON_EXTRACT);
		assertNotNull(template, "JSON_EXTRACT template should be registered");
	}

	@Test
	@DisplayName("JSON_SET template is registered")
	void testJsonSetTemplateRegistered() {
		Template template = templates.getTemplate(JsonOps.JSON_SET);
		assertNotNull(template, "JSON_SET template should be registered");
	}

	@Test
	@DisplayName("JSON_CONTAINS template is registered")
	void testJsonContainsTemplateRegistered() {
		Template template = templates.getTemplate(JsonOps.JSON_CONTAINS);
		assertNotNull(template, "JSON_CONTAINS template should be registered");
	}

	// --- SQL string verification ---

	@Test
	@DisplayName("JSON_EXTRACT produces SQL containing 'JSON_EXTRACT'")
	void testJsonExtractSqlOutput() {
		StringExpression jsonDoc = Expressions.asString("doc");
		StringOperation expr = Expressions.stringOperation(JsonOps.JSON_EXTRACT,
				jsonDoc, Expressions.constant("$.name"));

		String sql = serializeExpression(expr);
		assertTrue(sql.contains("JSON_EXTRACT"),
				"SQL should contain JSON_EXTRACT function name, got: " + sql);
	}

	@Test
	@DisplayName("JSON_SET produces SQL containing 'JSON_SET'")
	void testJsonSetSqlOutput() {
		StringExpression jsonDoc = Expressions.asString("doc");
		StringOperation expr = Expressions.stringOperation(JsonOps.JSON_SET,
				jsonDoc, Expressions.constant("$.key"));

		String sql = serializeExpression(expr);
		assertTrue(sql.contains("JSON_SET"),
				"SQL should contain JSON_SET function name, got: " + sql);
	}

	@Test
	@DisplayName("JSON_CONTAINS produces SQL containing 'JSON_CONTAINS'")
	void testJsonContainsSqlOutput() {
		StringExpression jsonDoc = Expressions.asString("doc");
		BooleanOperation expr = Expressions.booleanOperation(JsonOps.JSON_CONTAINS,
				jsonDoc, Expressions.constant("1"));

		String sql = serializeExpression(expr);
		assertTrue(sql.contains("JSON_CONTAINS"),
				"SQL should contain JSON_CONTAINS function name, got: " + sql);
	}

	/**
	 * Serializes an expression to SQL using the MySQL JSON templates.
	 */
	private String serializeExpression(Expression<?> expr) {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, false);
		serializer.handle(expr);
		return serializer.toString();
	}
}
