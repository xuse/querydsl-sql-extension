package com.github.xuse.querydsl.r2dbc.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.util.Date;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QUser extends RelationalPathBaseEx<User> {
	public static final QUser user = new QUser("user");

	public final NumberPath<Long> id = createNumber("id", long.class);

	public final StringPath name = createString("name");
	
	public final StringPath uid = createString("uid");


	public final DateTimePath<Date> created = createDateTime("created", Date.class);
	
	public final DateTimePath<Date> modified = createDateTime("modified", Date.class);
	
	public final StringPath email = createString("email");

	public QUser(Class<? extends User> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata, schema, table);
	}

	public QUser(String variable) {
		super(User.class, forVariable(variable), "null", "user");
		super.scanClassMetadata();
	}

	public QUser(String variable, String schema, String table) {
		super(User.class, forVariable(variable), schema, table);
		super.scanClassMetadata();
	}

	public QUser(String variable, String schema) {
		super(User.class, forVariable(variable), schema, "user");
		super.scanClassMetadata();
	}
}
