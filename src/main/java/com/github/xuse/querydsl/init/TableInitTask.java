package com.github.xuse.querydsl.init;

import java.util.HashMap;
import java.util.Map;

import com.github.xuse.querydsl.sql.RelationalPathEx;

public class TableInitTask {
	public final RelationalPathEx<?> table;
	public final Map<String, Object> option = new HashMap<>();

	public TableInitTask(RelationalPathEx<?> table) {
		this.table = table;
	}
}
