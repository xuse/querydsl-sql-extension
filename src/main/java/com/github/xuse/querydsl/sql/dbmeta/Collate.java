package com.github.xuse.querydsl.sql.dbmeta;

import com.github.xuse.querydsl.util.Enums;

public enum Collate {
	utf8mb4_general_ci("utf8mb4"), 
	utf8mb4_unicode_ci("utf8mb4"), 
	utf8mb4_general_cs("utf8mb4"),
	utf8mb4_unicode_cs("utf8mb4"),
	ascii_bin("ascii"),
	ascii_general_ci("ascii"),
	ascii_general_cs("ascii"),
;
	public final String charset;

	Collate(String charset) {
		this.charset = charset;
	}

	public static Collate findValueOf(String collate) {
		return Enums.valueOf(Collate.class, collate, null);

	}
}
