package com.github.xuse.querydsl.r2dbc.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.util.Date;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QSchool extends RelationalPathBaseEx<School> {
	public static final QSchool school = new QSchool("school");

	public final NumberPath<Integer> id = createNumber("id", int.class);

	public final StringPath code = createString("code");
	
	public final StringPath name = createString("name");
	
	public final StringPath content = createString("content");

	public final DateTimePath<Date> created = createDateTime("created", Date.class);
	
	public final DateTimePath<Date> updated = createDateTime("updated", Date.class);
	
	public final NumberPath<Integer> volume = createNumber("volume",Integer.class);

	public QSchool(Class<? extends School> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata, schema, table);
	}

	public QSchool(String variable) {
		super(School.class, forVariable(variable), "null", "foo");
		super.scanClassMetadata();
	}

	public QSchool(String variable, String schema, String table) {
		super(School.class, forVariable(variable), schema, table);
		super.scanClassMetadata();
	}

	public QSchool(String variable, String schema) {
		super(School.class, forVariable(variable), schema, "foo");
		super.scanClassMetadata();
	}
}
