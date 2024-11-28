package com.github.xuse.querydsl.sql;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.SimpleOperation;
import com.querydsl.core.types.dsl.StringOperation;

public class JsonExpressionTest {

	@Test
	public void testJsonArray() {
		StringOperation operation = JsonExpressions.jsonArray(1, "two", 3.0);
		assertNotNull(operation);
	}

	@Test
	public void testJsonObject() {
		StringOperation operation = JsonExpressions.jsonObject("key1", 1, "key2", "two");
		assertNotNull(operation);
	}

	@Test
	public void testJsonQuote() {
		StringOperation operation = JsonExpressions.jsonQuote("text");
		assertNotNull(operation);
	}

	@Test
	public void testJsonContainsWithoutPath() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		BooleanOperation operation = JsonExpressions.jsonContains(jsonDoc, "value");
		assertNotNull(operation);
	}

	@Test
	public void testJsonContainsWithPath() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		BooleanOperation operation = JsonExpressions.jsonContains(jsonDoc, "value", "$.key");
		assertNotNull(operation);
	}

	@Test
	public void testJsonContainsPath() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		BooleanOperation operation = JsonExpressions.jsonContainsPath(jsonDoc, true, "$.key");
		assertNotNull(operation);
	}

	@Test
	public void testJsonExtract() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonExtract(jsonDoc, true, "$.key");
		assertNotNull(operation);
	}

	@Test
	public void testJsonKeysWithoutPath() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonKeys(jsonDoc);
		assertNotNull(operation);
	}

	@Test
	public void testJsonKeysWithPath() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonKeys(jsonDoc, "$.key");
		assertNotNull(operation);
	}

	@Test
	public void testJsonSearch() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonSearch(jsonDoc, true, "value");
		assertNotNull(operation);
	}

	@Test
	public void testJsonSearchWithPath() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonSearch(jsonDoc, false, "value", '\\', "$.key");
		assertNotNull(operation);
	}

	@Test
	public void testJsonValue() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonValue(jsonDoc, "$.key");
		assertNotNull(operation);
	}

	@Test
	public void testJsonArrayAppendWithPathValues() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": []}");
		StringOperation operation = JsonExpressions.jsonArrayAppend(jsonDoc, "$.key", "1", "$.key", "2");
		assertNotNull(operation);
	}

	@Test
	public void testJsonArrayAppendWithList() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": []}");
		List<Pair<String, String>> pathAndValues = Arrays.asList(Pair.of("$.key", "1"), Pair.of("$.key", "2"));
		StringOperation operation = JsonExpressions.jsonArrayAppend(jsonDoc, pathAndValues);
		assertNotNull(operation);
	}

	@Test
	public void testJsonSetWithPathValues() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonSet(jsonDoc, "$.key", "new_value");
		assertNotNull(operation);
	}

	@Test
	public void testJsonSetWithList() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		List<Pair<String, String>> pathAndValues = Arrays.asList(Pair.of("$.key", "new_value"));
		SimpleOperation<?> operation = JsonExpressions.jsonSet(jsonDoc, pathAndValues);
		assertNotNull(operation);
	}

	@Test
	public void testJsonType() {
		Expression<String> jsonDoc = ConstantImpl.create("{\"key\": \"value\"}");
		StringOperation operation = JsonExpressions.jsonType(jsonDoc);
		assertNotNull(operation);
	}

}
