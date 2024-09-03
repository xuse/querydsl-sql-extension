package com.github.xuse.querydsl.spring.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.util.Date;

import com.github.xuse.querydsl.spring.enums.Status;
import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QTestEntity extends RelationalPathBaseEx<TestEntity> {
	public static final QTestEntity testEntity = new QTestEntity("ta");

	public final NumberPath<Long> id = super.createNumber("id", Long.class);
	public final StringPath name = super.createString("name");
	public final EnumPath<Status> gender = super.createEnum("status", Status.class);
	public final DateTimePath<Date> created = super.createDateTime("created", Date.class);
	public final NumberPath<Integer> version = super.createNumber("version", Integer.class);

	public QTestEntity(String variable) {
		super(TestEntity.class, forVariable(variable), null, null);
		addMetadata();
	}

	public QTestEntity(String variable, String schema, String table) {
		super(TestEntity.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QTestEntity(String variable, String schema) {
		super(TestEntity.class, forVariable(variable), schema, "AAA");
		addMetadata();
	}

	public QTestEntity(Path<? extends TestEntity> path) {
		super(path.getType(), path.getMetadata(), "null", "AAA");
		addMetadata();
	}

	public QTestEntity(PathMetadata metadata) {
		super(TestEntity.class, metadata, "null", "AAA");
		addMetadata();
	}

	public void addMetadata() {
		scanClassMetadata();
	}
}
