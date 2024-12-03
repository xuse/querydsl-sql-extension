package com.github.xuse.querydsl.sql;

import java.util.Arrays;
import java.util.List;

import com.github.xuse.querydsl.sql.expression.JsonOps;
import com.github.xuse.querydsl.util.Primitives;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberOperation;
import com.querydsl.core.types.dsl.SimpleOperation;
import com.querydsl.core.types.dsl.StringOperation;

/**
 * 扩展QueryDSL，支持JSON数据结构的函数
 *
 * @author Joey
 */
public class JsonExpressions {

	private JsonExpressions() {
	}
	
	/**
	 *  JSON_ARRAY([val[, val] ...]) Evaluates a (possibly empty) list of values and
	 *  returns a JSON array containing those values.
	 *
	 *  @param elements Elements of JSON array.
	 *  @return StringOperation
	 */
	public static StringOperation jsonArray(Object... elements) {
		List<?> list = Arrays.asList(elements);
		return Expressions.stringOperation(JsonOps.JSON_ARRAY, ConstantImpl.create(list));
	}

	/**
	 * JSON_OBJECT([key, val[, key, val] ...])
	 *
	 * Evaluates a (possibly empty) list of key-value pairs and returns a JSON
	 * object containing those pairs. An error occurs if any key name is NULL or the
	 * number of arguments is odd.
	 *
	 * @param elements elements
	 * @return StringOperation
	 */
	public static StringOperation jsonObject(Object... elements) {
		List<?> list = Arrays.asList(elements);
		return Expressions.stringOperation(JsonOps.JSON_OBJECT, ConstantImpl.create(list));
	}

	/**
	 * JSON_QUOTE(string)
	 *
	 * Quotes a string as a JSON value by wrapping it with double quote characters
	 * and escaping interior quote and other characters, then returning the result
	 * as a utf8mb4 string. Returns NULL if the argument is NULL.
	 *
	 * @param text text
	 * @return StringOperation
	 */
	public static StringOperation jsonQuote(String text) {
		return Expressions.stringOperation(JsonOps.JSON_QUOTE, ConstantImpl.create(text));
	}

	/**
	 * @see #jsonContains(Expression, String, String)
	 * @param jsonDoc jsonDoc
	 * @param text text
	 * @return BooleanOperation
	 */
	public static BooleanOperation jsonContains(Expression<String> jsonDoc, String text) {
		return jsonContains(jsonDoc, text, null);
	}

	/**
	 * JSON_CONTAINS(target, candidate[, path])
	 *
	 * Indicates by returning 1 or 0 whether a given candidate JSON document is
	 * contained within a target JSON document, or—if a path argument was
	 * supplied—whether the candidate is found at a specific path within the target.
	 * Returns NULL if any argument is NULL, or if the path argument does not
	 * identify a section of the target document. An error occurs if target or
	 * candidate is not a valid JSON document, or if the path argument is not a
	 * valid path expression or contains a * or ** wildcard.
	 *
	 * @param jsonDoc jsonDoc
	 * @param text text
	 * @param path path
	 * @return StringOperation
	 */
	public static BooleanOperation jsonContains(Expression<String> jsonDoc, String text, String path) {
		if (StringUtils.isEmpty(path)) {
			return Expressions.booleanOperation(JsonOps.JSON_CONTAINS, jsonDoc, ConstantImpl.create(text));
		} else {
			return Expressions.booleanOperation(JsonOps.JSON_CONTAINS_UNDER_PATH, jsonDoc, ConstantImpl.create(text), ConstantImpl.create(path));
		}
	}

	/**
	 * JSON_CONTAINS_PATH(json_doc, one_or_all, path[, path] ...)
	 *
	 * Returns 0 or 1 to indicate whether a JSON document contains data at a given
	 * path or paths. Returns NULL if any argument is NULL. An error occurs if the
	 * json_doc argument is not a valid JSON document, any path argument is not a
	 * valid path expression, or one_or_all is not 'one' or 'all'.
	 *
	 * @param jsonDoc jsonDoc
	 * @param trueAsAll trueAsAll
	 * @param paths paths
	 * @return StringOperation
	 */
	public static BooleanOperation jsonContainsPath(Expression<String> jsonDoc, boolean trueAsAll, String... paths) {
		Expression<String> oneAll = Expressions.asString(trueAsAll ? "all" : "one");
		return Expressions.booleanOperation(JsonOps.JSON_CONTAINS_PATH, jsonDoc, oneAll, ConstantImpl.create(paths));
	}

	/**
	 * JSON_EXTRACT(json_doc, path[, path] ...)
	 *
	 * Returns data from a JSON document, selected from the parts of the document
	 * matched by the path arguments. Returns NULL if any argument is NULL or no
	 * paths locate a value in the document. An error occurs if the json_doc
	 * argument is not a valid JSON document or any path argument is not a valid
	 * path expression.
	 *
	 * @param jsonDoc jsonDoc
	 * @param trueAsAll trueAsAll
	 * @param paths paths
	 * @return StringOperation
	 */
	public static StringOperation jsonExtract(Expression<String> jsonDoc, boolean trueAsAll, String... paths) {
		Expression<String> oneAll = Expressions.asString(trueAsAll ? "all" : "one");
		return Expressions.stringOperation(JsonOps.JSON_EXTRACT, jsonDoc, oneAll, ConstantImpl.create(paths));
	}

	/**
	 * JSON_KEYS(json_doc[, path])
	 *
	 * @see #jsonKeys(Expression, String)
	 * @param jsonDoc jsonDoc
	 * @return StringOperation
	 */
	public static StringOperation jsonKeys(Expression<String> jsonDoc) {
		return jsonKeys(jsonDoc, null);
	}

	/**
	 * JSON_KEYS(json_doc[, path])
	 *
	 * Returns the keys from the top-level value of a JSON object as a JSON array,
	 * or, if a path argument is given, the top-level keys from the selected path.
	 * Returns NULL if any argument is NULL, the json_doc argument is not an object,
	 * or path, if given, does not locate an object. An error occurs if the json_doc
	 * argument is not a valid JSON document or the path argument is not a valid
	 * path expression or contains a * or ** wildcard.
	 *
	 * The result array is empty if the selected object is empty. If the top-level
	 * value has nested subobjects, the return value does not include keys from
	 * those subobjects.
	 *
	 * @param jsonDoc jsonDoc
	 * @param path path
	 * @return StringOperation
	 */
	public static StringOperation jsonKeys(Expression<String> jsonDoc, String path) {
		if (path == null) {
			path = "$";
		}
		return Expressions.stringOperation(JsonOps.JSON_KEYS, jsonDoc, ConstantImpl.create(path));
	}

	/**
	 * JSON_SEARCH(json_doc, one_or_all, search_str[, escape_char[, path] ...])
	 *
	 * Returns the path to the given string within a JSON document. Returns NULL if
	 * any of the json_doc, search_str, or path arguments are NULL; no path exists
	 * within the document; or search_str is not found. An error occurs if the
	 * json_doc argument is not a valid JSON document, any path argument is not a
	 * valid path expression, one_or_all is not 'one' or 'all', or escape_char is
	 * not a constant expression.
	 *
	 * @param jsonDoc jsonDoc
	 * @param trueAsAll trueAsAll
	 * @param text text
	 * @return vStringOperation
	 */
	public static StringOperation jsonSearch(Expression<String> jsonDoc, boolean trueAsAll, String text) {
		return jsonSearch(jsonDoc, trueAsAll, text, '\\');
	}

	/**
	 * JSON_SEARCH(json_doc, one_or_all, search_str[, escape_char[, path] ...])
	 *
	 * @see #jsonSearch(Expression, boolean, String)
	 * @param jsonDoc jsonDoc
	 * @param trueAsAll 'one' or 'all', true is 'all'.
	 * @param text text
	 * @param escapeChar escapeChar
	 * @param paths paths
	 * @return StringOperation
	 */
	public static StringOperation jsonSearch(Expression<String> jsonDoc, boolean trueAsAll, String text, char escapeChar, String... paths) {
		Expression<String> oneOrAll = Expressions.asString(trueAsAll ? "all" : "one");
		if (paths == null || paths.length == 0) {
			return Expressions.stringOperation(JsonOps.JSON_SEARCH, jsonDoc, oneOrAll, ConstantImpl.create(text), ConstantImpl.create(escapeChar));
		} else {
			return Expressions.stringOperation(JsonOps.JSON_SEARCH_WITH_PATH, jsonDoc, oneOrAll, ConstantImpl.create(text), ConstantImpl.create(escapeChar), ConstantImpl.create(paths[0]));
		}
	}

	/**
	 * JSON_VALUE(json_doc, path) Extracts a value from a JSON document at the path
	 * given in the specified document, and returns the extracted value, optionally
	 * converting it to a desired type. The complete syntax is shown here:
	 *
	 * @param jsonDoc jsonDoc
	 * @param path path
	 * @return StringOperation
	 */
	public static StringOperation jsonValue(Expression<String> jsonDoc, String path) {
		return Expressions.stringOperation(JsonOps.JSON_VALUE, jsonDoc, ConstantImpl.create(path));
	}

	/**
	 * JSON_ARRAY_APPEND(json_doc, path, val[, path, val] ...)
	 *
	 * @see #jsonArrayAppend(Expression, List)
	 * @param jsonDoc jsonDoc
	 * @param pathAndValues pathAndValues
	 * @return StringOperation
	 */
	public static StringOperation jsonArrayAppend(Expression<String> jsonDoc, String... pathAndValues) {
		return Expressions.stringOperation(JsonOps.JSON_ARRAY_APPEND, jsonDoc, ConstantImpl.create(pathAndValues));
	}

	/**
	 * JSON_ARRAY_APPEND(json_doc, path, val[, path, val] ...)
	 *
	 * Appends values to the end of the indicated arrays within a JSON document and
	 * returns the result. Returns NULL if any argument is NULL. An error occurs if
	 * the json_doc argument is not a valid JSON document or any path argument is
	 * not a valid path expression or contains a * or ** wildcard.
	 *
	 * The path-value pairs are evaluated left to right. The document produced by
	 * evaluating one pair becomes the new value against which the next pair is
	 * evaluated.
	 *
	 * If a path selects a scalar or object value, that value is autowrapped within
	 * an array and the new value is added to that array. Pairs for which the path
	 * does not identify any value in the JSON document are ignored.
	 * @param jsonDoc jsonDoc
	 * @param pathAndValues pathAndValues
	 * @return StringOperation
	 */
	public static StringOperation jsonArrayAppend(Expression<String> jsonDoc, List<Pair<String, String>> pathAndValues) {
		if (pathAndValues == null || pathAndValues.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Object[] arrays = new Object[pathAndValues.size() * 2];
		for (int i = 0; i < pathAndValues.size(); i++) {
			Pair<String, String> pair = pathAndValues.get(i);
			int index = i * 2;
			arrays[index] = pair.getFirst();
			arrays[index + 1] = pair.getSecond();
		}
		return Expressions.stringOperation(JsonOps.JSON_ARRAY_APPEND, jsonDoc, ConstantImpl.create(arrays));
	}

	/**
	 * JSON_SET(json_doc, path, val[, path, val] ...)
	 * <p>
	 * Inserts or updates data in a JSON document and returns the result. Returns
	 * NULL if any argument is NULL or path, if given, does not locate an object. An
	 * error occurs if the json_doc argument is not a valid JSON document or any
	 * path argument is not a valid path expression or contains a * or ** wildcard.
	 * <p>
	 * The path-value pairs are evaluated left to right. The document produced by
	 * evaluating one pair becomes the new value against which the next pair is
	 * evaluated.
	 * <p>
	 * A path-value pair for an existing path in the document overwrites the
	 * existing document value with the new value. A path-value pair for a
	 * nonexisting path in the document adds the value to the document if the path
	 * identifies one of these types of values:
	 * <ul><li>A member not present in an existing object. The member is added to the
	 * object and associated with the new value.</li>
	 * <li>A position past the end of an existing array. The array is extended with
	 * the new value. If the existing value is not an array, it is autowrapped as an
	 * array, then extended with the new value.</li>
	 * </ul>
	 *
	 * @param jsonDoc jsonDoc
	 * @param pathAndValues pathAndValues
	 * @return SimpleOperation
	 * @param <T> The type of target object.
	 */
	public static <T> SimpleOperation<T> jsonSet(Expression<T> jsonDoc, List<Pair<String, String>> pathAndValues) {
		if (pathAndValues == null || pathAndValues.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Expression<?>[] arrays = new Expression[pathAndValues.size() * 2];
		for (int i = 0; i < pathAndValues.size(); i++) {
			Pair<String, String> pair = pathAndValues.get(i);
			int index = i * 2;
			arrays[index] = ConstantImpl.create(pair.getFirst());
			arrays[index + 1] = Expressions.simpleTemplate(jsonDoc.getType(), "CAST({0} AS JSON)", pair.getSecond());
		}
		return Expressions.simpleOperation(jsonDoc.getType(), JsonOps.JSON_SET, jsonDoc, ConstantImpl.create(arrays));
	}

	/**
	 * JSON_SET(json_doc, path, val[, path, val] ...)
	 *
	 * @param jsonDoc jsonDoc
	 * @param pathAndValues pathAndValues
	 * @return StringOperation
	 */
	public static StringOperation jsonSet(Expression<String> jsonDoc, String... pathAndValues) {
		Expression<?>[] exps = convertJSONparams(pathAndValues, jsonDoc.getType());
		return Expressions.stringOperation(JsonOps.JSON_SET, jsonDoc, ConstantImpl.create(exps));
	}

	private static Expression<?>[] convertJSONparams(String[] pathAndValues, Class<?> valueType) {
		if (pathAndValues == null || pathAndValues.length == 0 || Primitives.isOdd(pathAndValues.length)) {
			throw new IllegalArgumentException("Input path is null or size is odd-number!");
		}
		Expression<?>[] result = new Expression<?>[pathAndValues.length];
		for (int i = 0; i < pathAndValues.length; i++) {
			String path = pathAndValues[i];
			result[i] = ConstantImpl.create(path);
			String value = pathAndValues[++i];
			result[i] = Expressions.simpleTemplate(valueType, "CAST({0} AS JSON)", value);
		}
		return result;
	}

	/**
	 * JSON_TYPE(json_val) <br>
	 * Returns a utf8mb4 string indicating the type of a JSON value. This can be an
	 * object, an array, or a scalar type, as shown here:
	 * Purely JSON types:
	 * <ul><li>OBJECT: JSON objects</li>
	 * <li>ARRAY: JSON arrays</li>
	 * <li>BOOLEAN: The JSON true and false literals</li>
	 * <li>NULL: The JSON null literal</li>
	 * </ul>
	 * Numeric types:
	 * <ul><li>INTEGER: MySQL TINYINT, SMALLINT, MEDIUMINT and INT and BIGINT
	 * scalars</li>
	 * <li>DOUBLE: MySQL DOUBLE FLOAT scalars</li>
	 * <li>DECIMAL: MySQL DECIMAL and NUMERIC scalars</li>
	 * </ul>
	 * Temporal types:
	 * <ul>
	 * <li>DATETIME: MySQL DATETIME and TIMESTAMP scalars</li>
	 * <li>DATE: MySQL DATE scalars</li>
	 * <li>TIME: MySQL TIME scalars</li>
	 * </ul>
	 * String types:
	 * <ul>
	 * <li>STRING: MySQL utf8 character type scalars: CHAR, VARCHAR, TEXT, ENUM, and
	 * SET</li>
	 * </ul>
	 * <p>
	 * Binary types:
	 * <ul>
	 * <li>BLOB: MySQL binary type scalars including BINARY, VARBINARY, BLOB, and
	 * BIT</li>
	 * </ul>
	 * All other types:
	 * <ul>
	 * <li>OPAQUE (raw bits)</li>
	 * </ul>
	 *
	 * @param jsonDoc jsonDoc
	 * @return StringOperation
	 */
	public static StringOperation jsonType(Expression<String> jsonDoc) {
		return Expressions.stringOperation(JsonOps.JSON_TYPE, jsonDoc);
	}

	/**
	 * get length of the json. 
	 * The length of a document is determined as follows:
	 * <ul>
	 * <li>The length of a scalar is 1.</li>
	 * <li>The length of an array is the number of array elements.</li>
	 * <li>The length of an object is the number of object members.</li>
	 * </ul>
	 * The length does not count the length of nested arrays or objects.
	 *
	 * @param jsonDoc jsonDoc
	 * @param path path
	 * @return NumberOperation
	 */
	public static NumberOperation<Integer> jsonLength(Expression<String> jsonDoc, String path) {
		path = path == null || path.isEmpty() ? "$" : path;
		return Expressions.numberOperation(Integer.class,JsonOps.JSON_LENGTH, jsonDoc, ConstantImpl.create(path));
	}

	/**
	 * value MEMBER OF(json_array)
	 * <p>
	 *
	 * Returns true (1) if value is an element of json_array, otherwise returns
	 * false (0). value must be a scalar or a JSON document; if it is a scalar, the
	 * operator attempts to treat it as an element of a JSON array.
	 * 
	 * @param keyword keyword
	 * @param jsDoc jsDoc
	 * @return BooleanOperation
	 */
	public static BooleanOperation memberOf(Expression<String> keyword, Expression<String> jsDoc) {
		return Expressions.booleanOperation(JsonOps.MEMBER_OF, keyword, jsDoc);
	}

	/**
	 * value MEMBER OF(json_array)
	 *
	 * @see #memberOf(Expression, Expression)
	 * @param keyword keyword
	 * @param jsDoc jsDoc
	 * @return BooleanOperation
	 */
	public static BooleanOperation memberOf(String keyword, Expression<String> jsDoc) {
		return memberOf(ConstantImpl.create(keyword), jsDoc);
	}
}
