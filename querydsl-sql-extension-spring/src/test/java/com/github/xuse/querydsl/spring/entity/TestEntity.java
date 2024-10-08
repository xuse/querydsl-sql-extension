package com.github.xuse.querydsl.spring.entity;

import java.sql.Types;
import java.util.Date;

import com.github.xuse.querydsl.annotation.CustomType;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.spring.enums.Status;
import com.github.xuse.querydsl.types.EnumByCodeType;

import lombok.Data;

@TableSpec(name="test_entity",primaryKeys = "id")
@Data
public class TestEntity {
	@ColumnSpec(autoIncrement = true, type = Types.BIGINT, nullable = false)
	private long id;
	
	@ColumnSpec(size = 64)
	private String name;

	@ColumnSpec(nullable = false, defaultValue = "0")
	private int version;

	@CustomType(EnumByCodeType.class)
	@ColumnSpec(type = Types.SMALLINT)
	private Status status;

	@ColumnSpec
	private Date created;

}
