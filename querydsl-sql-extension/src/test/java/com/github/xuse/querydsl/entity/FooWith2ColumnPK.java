package com.github.xuse.querydsl.entity;

import java.util.Date;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.GeneratedType;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;

import lombok.Data;

@Data
@TableSpec(name = "foo_with_2column_pk", primaryKeys = { "name", "value" })
public class FooWith2ColumnPK {
	@ColumnSpec(name = "f_name", nullable = false)
	private String name;

	@ColumnSpec(name = "f_value", nullable = false)
	private String value;

	@AutoGenerated(GeneratedType.CREATED_TIMESTAMP)
	@ColumnSpec
	private Date created;

	@AutoGenerated(GeneratedType.UPDATED_TIMESTAMP)
	@ColumnSpec
	private Date modified;
}
