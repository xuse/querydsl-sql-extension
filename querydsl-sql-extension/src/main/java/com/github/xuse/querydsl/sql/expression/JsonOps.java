package com.github.xuse.querydsl.sql.expression;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Operator;

/**
 * <h2>English:</h2>
 * Extends QueryDSL to implement JSON functions.
 * <h2>Chinese:</h2>
 * 扩展QueryDSL，实现JSON函数
 * 
 * @author Joey
 */
public enum JsonOps implements Operator {

	/**
	 * 判断 JSON 文档是否包含指定的值。
	 * <p>MySQL syntax: {@code JSON_CONTAINS(target, candidate[, path])}
	 * <p>Example: {@code JSON_CONTAINS('{"a": 1, "b": 2}', '1', '$.a')} → 1
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-contains">MySQL Reference</a>
	 */
	JSON_CONTAINS(Boolean.class),

	/**
	 * 判断 JSON 文档在指定路径下是否包含指定的值（带路径参数的 JSON_CONTAINS）。
	 * <p>MySQL syntax: {@code JSON_CONTAINS(target, candidate, path)}
	 * <p>Example: {@code JSON_CONTAINS('{"a": {"b": 1}}', '1', '$.a.b')} → 1
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-contains">MySQL Reference</a>
	 */
	JSON_CONTAINS_UNDER_PATH(Boolean.class),

	/**
	 * 判断 JSON 文档中是否存在指定路径。
	 * <p>MySQL syntax: {@code JSON_CONTAINS_PATH(json_doc, one_or_all, path[, path] ...)}
	 * <p>Example: {@code JSON_CONTAINS_PATH('{"a": 1}', 'one', '$.a', '$.b')} → 1
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-contains-path">MySQL Reference</a>
	 */
	JSON_CONTAINS_PATH(Boolean.class),

	/**
	 * 在 JSON 文档中搜索匹配给定字符串的值，返回其路径。
	 * <p>MySQL syntax: {@code JSON_SEARCH(json_doc, one_or_all, search_str[, escape_char[, path] ...])}
	 * <p>Example: {@code JSON_SEARCH('["abc", "def"]', 'one', 'abc')} → "$[0]"
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-search">MySQL Reference</a>
	 */
	JSON_SEARCH(String.class),

	/**
	 * 在 JSON 文档中搜索匹配给定字符串的值（带路径限定），返回其路径。
	 * <p>MySQL syntax: {@code JSON_SEARCH(json_doc, one_or_all, search_str, escape_char, path)}
	 * <p>Example: {@code JSON_SEARCH('{"a": "hello"}', 'one', 'hello', NULL, '$.a')} → "$.a"
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-search">MySQL Reference</a>
	 */
	JSON_SEARCH_WITH_PATH(String.class),

	/**
	 * 从 JSON 文档中提取指定路径的数据。
	 * <p>MySQL syntax: {@code JSON_EXTRACT(json_doc, path[, path] ...)}
	 * <p>Example: {@code JSON_EXTRACT('{"id": 1, "name": "a"}', '$.name')} → "a"
	 * <p>Shorthand: {@code column->'$.path'} (equivalent to JSON_EXTRACT)
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-extract">MySQL Reference</a>
	 */
	JSON_EXTRACT(String.class),

	/**
	 * 返回 JSON 对象顶层的所有键名，或指定路径下对象的键名。
	 * <p>MySQL syntax: {@code JSON_KEYS(json_doc[, path])}
	 * <p>Example: {@code JSON_KEYS('{"a": 1, "b": 2}')} → ["a", "b"]
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-keys">MySQL Reference</a>
	 */
	JSON_KEYS(String.class),

	/**
	 * 合并多个 JSON 文档（已废弃，推荐使用 JSON_MERGE_PRESERVE 或 JSON_MERGE_PATCH）。
	 * <p>MySQL syntax: {@code JSON_MERGE(json_doc, json_doc[, json_doc] ...)}
	 * <p>Example: {@code JSON_MERGE('[1]', '[2]')} → [1, 2]
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-merge">MySQL Reference</a>
	 * @deprecated Use JSON_MERGE_PRESERVE or JSON_MERGE_PATCH instead since MySQL 8.0.3
	 */
	JSON_MERGE(String.class),

	/**
	 * 在 JSON 文档中设置指定路径的值（已存在则覆盖，不存在则插入）。
	 * <p>MySQL syntax: {@code JSON_SET(json_doc, path, val[, path, val] ...)}
	 * <p>Example: {@code JSON_SET('{"a": 1}', '$.b', 2)} → {"a": 1, "b": 2}
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-set">MySQL Reference</a>
	 */
	JSON_SET(String.class),

	/**
	 * 在 JSON 文档中插入新值（仅当路径不存在时才插入，已存在则忽略）。
	 * <p>MySQL syntax: {@code JSON_INSERT(json_doc, path, val[, path, val] ...)}
	 * <p>Example: {@code JSON_INSERT('{"a": 1}', '$.b', 2)} → {"a": 1, "b": 2}
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-insert">MySQL Reference</a>
	 */
	JSON_INSERT(String.class),

	/**
	 * 替换 JSON 文档中已存在路径的值（路径不存在则忽略）。
	 * <p>MySQL syntax: {@code JSON_REPLACE(json_doc, path, val[, path, val] ...)}
	 * <p>Example: {@code JSON_REPLACE('{"a": 1}', '$.a', 2)} → {"a": 2}
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-replace">MySQL Reference</a>
	 */
	JSON_REPLACE(String.class),

	/**
	 * 创建一个 JSON 数组。
	 * <p>MySQL syntax: {@code JSON_ARRAY([val[, val] ...])}
	 * <p>Example: {@code JSON_ARRAY(1, "abc", NULL, TRUE)} → [1, "abc", null, true]
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-creation-functions.html#function_json-array">MySQL Reference</a>
	 */
	JSON_ARRAY(String.class),

	/**
	 * 创建一个 JSON 对象。
	 * <p>MySQL syntax: {@code JSON_OBJECT([key, val[, key, val] ...])}
	 * <p>Example: {@code JSON_OBJECT('name', 'John', 'age', 30)} → {"name": "John", "age": 30}
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-creation-functions.html#function_json-object">MySQL Reference</a>
	 */
	JSON_OBJECT(String.class),

	/**
	 * 将字符串用双引号包裹为 JSON 字符串值，并转义内部特殊字符。
	 * <p>MySQL syntax: {@code JSON_QUOTE(string)}
	 * <p>Example: {@code JSON_QUOTE('hello')} → "\"hello\""
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-creation-functions.html#function_json-quote">MySQL Reference</a>
	 */
	JSON_QUOTE(String.class),

	/**
	 * 向 JSON 数组末尾追加值。
	 * <p>MySQL syntax: {@code JSON_ARRAY_APPEND(json_doc, path, val[, path, val] ...)}
	 * <p>Example: {@code JSON_ARRAY_APPEND('["a"]', '$', 'b')} → ["a", "b"]
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-array-append">MySQL Reference</a>
	 */
	JSON_ARRAY_APPEND(String.class),

	/**
	 * 在 JSON 数组的指定位置插入值。
	 * <p>MySQL syntax: {@code JSON_ARRAY_INSERT(json_doc, path, val[, path, val] ...)}
	 * <p>Example: {@code JSON_ARRAY_INSERT('["a", "b"]', '$[1]', 'x')} → ["a", "x", "b"]
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-array-insert">MySQL Reference</a>
	 */
	JSON_ARRAY_INSERT(String.class),

	/**
	 * 从 JSON 文档中移除指定路径的数据。
	 * <p>MySQL syntax: {@code JSON_REMOVE(json_doc, path[, path] ...)}
	 * <p>Example: {@code JSON_REMOVE('["a", "b", "c"]', '$[1]')} → ["a", "c"]
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-remove">MySQL Reference</a>
	 */
	JSON_REMOVE(String.class),

	/**
	 * 判断两个 JSON 文档是否有重叠部分（MySQL 8.0.17+）。
	 * <p>MySQL syntax: {@code JSON_OVERLAPS(json_doc1, json_doc2)}
	 * <p>Example: {@code JSON_OVERLAPS('[1, 3]', '[3, 5]')} → 1
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-overlaps">MySQL Reference</a>
	 */
	JSON_OVERLAPS(Boolean.class),

	/**
	 * 提取 JSON 文档中指定路径的标量值并转换为指定类型（MySQL 8.0.21+）。
	 * <p>MySQL syntax: {@code JSON_VALUE(json_doc, path [RETURNING type] [on_empty] [on_error])}
	 * <p>Example: {@code JSON_VALUE('{"a": 123}', '$.a')} → '123'
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#function_json-value">MySQL Reference</a>
	 */
	JSON_VALUE(String.class),

	/**
	 * 去除 JSON 值的引号，返回 utf8mb4 字符串。
	 * <p>MySQL syntax: {@code JSON_UNQUOTE(json_val)}
	 * <p>Example: {@code JSON_UNQUOTE('"hello"')} → hello
	 * <p>Shorthand: {@code column->>'$.path'} (equivalent to JSON_UNQUOTE(JSON_EXTRACT(...)))
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-unquote">MySQL Reference</a>
	 */
	JSON_UNQUOTE(String.class),

	/**
	 * 返回 JSON 文档的最大嵌套深度。
	 * <p>MySQL syntax: {@code JSON_DEPTH(json_doc)}
	 * <p>Example: {@code JSON_DEPTH('{"a": {"b": 1}}')} → 3
	 * <p>空数组/对象深度为1，标量深度为1。
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-attribute-functions.html#function_json-depth">MySQL Reference</a>
	 */
	JSON_DEPTH(Integer.class),

	/**
	 * 返回 JSON 文档的长度。
	 * <p>MySQL syntax: {@code JSON_LENGTH(json_doc[, path])}
	 * <p>Example: {@code JSON_LENGTH('{"a": 1, "b": 2}')} → 2
	 * <p>
	 * 长度定义：标量长度为1，数组长度为元素数，对象长度为成员数。不递归计算嵌套结构。
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-attribute-functions.html#function_json-length">MySQL Reference</a>
	 */
	JSON_LENGTH(Integer.class),

	/**
	 * 返回 JSON 值的类型名称字符串。
	 * <p>MySQL syntax: {@code JSON_TYPE(json_val)}
	 * <p>Example: {@code JSON_TYPE('[1, 2]')} → "ARRAY"
	 * <p>可能的返回值：OBJECT, ARRAY, STRING, INTEGER, DOUBLE, BOOLEAN, NULL 等。
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-attribute-functions.html#function_json-type">MySQL Reference</a>
	 */
	JSON_TYPE(String.class),

	/**
	 * 判断字符串是否为有效的 JSON 文档。
	 * <p>MySQL syntax: {@code JSON_VALID(val)}
	 * <p>Example: {@code JSON_VALID('{"a": 1}')} → 1; {@code JSON_VALID('hello')} → 0
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-attribute-functions.html#function_json-valid">MySQL Reference</a>
	 */
	JSON_VALID(Boolean.class),
	
	/**
	 * 将 JSON 数据映射为关系表（虚拟表），可在 FROM 子句中使用（MySQL 8.0.4+）。
	 * <p>MySQL syntax: {@code JSON_TABLE(expr, path COLUMNS (column_list) [AS] alias)}
	 * <p>Example:
	 * <pre>{@code
	 * SELECT * FROM JSON_TABLE(
	 *   '[{"a": 1}, {"a": 2}]', '$[*]'
	 *   COLUMNS (a INT PATH '$.a')
	 * ) AS jt;
	 * }</pre>
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-table-functions.html#function_json-table">MySQL Reference</a>
	 */
	JSON_TABLE(Tuple.class),

	/**
	 * 验证 JSON 文档是否符合指定的 JSON Schema（MySQL 8.0.17+）。
	 * <p>MySQL syntax: {@code JSON_SCHEMA_VALID(schema, document)}
	 * <p>Example: {@code JSON_SCHEMA_VALID('{"type": "object"}', '{"a": 1}')} → 1
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-validation-functions.html#function_json-schema-valid">MySQL Reference</a>
	 */
	JSON_SCHEMA_VALID(Boolean.class),

	/**
	 * 验证 JSON 文档是否符合 JSON Schema，并返回详细的验证报告（MySQL 8.0.17+）。
	 * <p>MySQL syntax: {@code JSON_SCHEMA_VALIDATION_REPORT(schema, document)}
	 * <p>Example: {@code JSON_SCHEMA_VALIDATION_REPORT('{"type":"string"}', '123')} → {"valid": false, "reason": "...", ...}
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-validation-functions.html#function_json-schema-validation-report">MySQL Reference</a>
	 */
	JSON_SCHEMA_VALIDATION_REPORT(String.class),

	/**
	 * 格式化 JSON 文档，使其更易于阅读（带缩进和换行）。
	 * <p>MySQL syntax: {@code JSON_PRETTY(json_val)}
	 * <p>Example: {@code JSON_PRETTY('{"a":1}')} → "{\n  \"a\": 1\n}"
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-utility-functions.html#function_json-pretty">MySQL Reference</a>
	 */
	JSON_PRETTY(String.class),

	/**
	 * 返回 JSON 二进制值在部分更新后释放的存储空间字节数（MySQL 8.0.2+）。
	 * <p>MySQL syntax: {@code JSON_STORAGE_FREE(json_val)}
	 * <p>Example: 对列执行部分更新后，{@code JSON_STORAGE_FREE(column)} 返回释放的字节数。
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-utility-functions.html#function_json-storage-free">MySQL Reference</a>
	 */
	JSON_STORAGE_FREE(Integer.class),

	/**
	 * 返回 JSON 文档的二进制存储大小（字节数）（MySQL 8.0.2+）。
	 * <p>MySQL syntax: {@code JSON_STORAGE_SIZE(json_val)}
	 * <p>Example: {@code JSON_STORAGE_SIZE('{"a": 1}')} → 13
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-utility-functions.html#function_json-storage-size">MySQL Reference</a>
	 */
	JSON_STORAGE_SIZE(Integer.class),

	/**
	 * 判断值是否为 JSON 数组的成员（MySQL 8.0.17+）。
	 * <p>MySQL syntax: {@code value MEMBER OF(json_array)}
	 * <p>Example: {@code 1 MEMBER OF('[1, 2, 3]')} → 1
	 * 
	 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#operator_member-of">MySQL Reference</a>
	 */
	MEMBER_OF(Boolean.class)
	;

	final Class<?> type;

	JsonOps(Class<?> type) {
		this.type = type;
	}

	@Override
	public Class<?> getType() {
		return type;
	}
}
