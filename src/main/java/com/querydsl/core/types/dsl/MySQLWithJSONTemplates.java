package com.querydsl.core.types.dsl;

import com.github.xuse.querydsl.sql.json.JsonOps;
import com.querydsl.sql.MySQLTemplates;

/**
 * 扩展的MySQL方言，支持了MySQL的JSON操作
 * @author jiyi
 *
 */
public class MySQLWithJSONTemplates extends MySQLTemplates {
	public MySQLWithJSONTemplates() {
		super();
		super.newLineToSingleSpace();
		initJson();
	}

	
	private void initJson() {
		//入参必须是集合，当传入为集合时，集合自带一对小括号，所以函数小括号可以省去
		add(JsonOps.JSON_ARRAY,"JSON_ARRAY{0}");
		add(JsonOps.JSON_OBJECT,"JSON_OBJECT{0}");
		add(JsonOps.JSON_QUOTE,"JSON_QUOTE({0})");
		
		add(JsonOps.JSON_CONTAINS,"JSON_CONTAINS({0},{1})");
		add(JsonOps.JSON_CONTAINS_UNDER_PATH,"JSON_CONTAINS({0},{1},{2})");
		//
		add(JsonOps.JSON_CONTAINS_PATH,"JSON_CONTAINS_PATH({0},{1},{2})");
		add(JsonOps.JSON_EXTRACT,"JSON_EXTRACT({0},{1})");
		add(JsonOps.JSON_KEYS,"JSON_KEYS({0},{1})");
		
		add(JsonOps.JSON_OVERLAPS,"JSON_OVERLAPS({0},{1})");
		
		//JSON_SEARCH(json_doc, one_or_all, search_str[, escape_char[, path] ...])
		add(JsonOps.JSON_SEARCH,"JSON_SEARCH({0},{1},{2},{3})");
		add(JsonOps.JSON_SEARCH_WITH_PATH,"JSON_SEARCH({0},{1},{2},{3},{4}})");
		add(JsonOps.JSON_VALUE,"JSON_VALUE({0},{1})");
		
		add(JsonOps.JSON_ARRAY_APPEND,"JSON_ARRAY_APPEND({0},{1})");
		add(JsonOps.JSON_ARRAY_INSERT,"JSON_ARRAY_INSERT({0},{1})");
		add(JsonOps.JSON_INSERT,"JSON_INSERT({0},{1})");
		add(JsonOps.JSON_MERGE,"JSON_MERGE({0},{1})");
		add(JsonOps.JSON_REMOVE,"JSON_REMOVE({0},{1})");
		add(JsonOps.JSON_REPLACE,"JSON_REPLACE({0},{1})");
		add(JsonOps.JSON_SET,"JSON_SET({0},{1})");
		
		add(JsonOps.JSON_UNQUOTE,"JSON_UNQUOTE({0})");
		add(JsonOps.JSON_DEPTH,"JSON_DEPTH({0})");
		add(JsonOps.JSON_LENGTH,"JSON_LENGTH({0},{1})");
		
		add(JsonOps.JSON_TYPE,"JSON_TYPE({0})");
		add(JsonOps.JSON_VALID,"JSON_VALID({0})");
		//这个函数太复杂了，先不具体包装
		add(JsonOps.JSON_TABLE,"JSON_TABLE({0})");
		add(JsonOps.JSON_SCHEMA_VALID,"JSON_SCHEMA_VALID({0},{1})");
		add(JsonOps.JSON_SCHEMA_VALIDATION_REPORT,"JSON_SCHEMA_VALIDATION_REPORT({0},{1})");
		
		add(JsonOps.JSON_PRETTY,"JSON_PRETTY({0})");
		add(JsonOps.MEMBER_OF,"{0} MEMBER OF({1})");
	}
}
