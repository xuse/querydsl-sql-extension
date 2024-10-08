package com.github.xuse.querydsl.spring.entity;

import java.sql.Types;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;

import lombok.Data;

@TableSpec(name = "SP_CAASSET", primaryKeys = "id")
@Data
public class SpringCaAsset {
	@ColumnSpec(autoIncrement = true, type = Types.BIGINT, nullable = false)
	private long id;

	@ColumnSpec(size = 64)
	private String name;
}
