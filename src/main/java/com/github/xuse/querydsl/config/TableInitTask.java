package com.github.xuse.querydsl.config;

import com.github.xuse.querydsl.init.ScanOptions;
import com.github.xuse.querydsl.sql.RelationalPathEx;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class TableInitTask {
	public final RelationalPathEx<?> table;
	public final ScanOptions option;

	public static TableInitTask pollFrom(ConfigurationEx configuration) {
		return configuration.initTasks.poll();
	}
}
