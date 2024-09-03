package com.github.xuse.querydsl.entity;

import java.util.Date;
import java.util.Map;

import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;

public class QCaAsset extends RelationalPathBaseEx<CaAsset> {
	private static final long serialVersionUID = -1972906214968601009L;

	public static final QCaAsset caAsset = new QCaAsset("as");
	
	public final NumberPath<Integer> id = createNumber("id", int.class);

	public final StringPath code = createString("code");
	
	public final StringPath name = createString("name");
	
	public final StringPath content = createString("content");

	public final DateTimePath<Date> created = createDateTime("created", Date.class);
	
	public final DateTimePath<Date> updated = createDateTime("updated", Date.class);
	
	public final EnumPath<Gender> gender = createEnum("gender", Gender.class);
	
	public final SimplePath<Aaa> ext = createSimple("ext", Aaa.class);
	
	public final SimplePath<Map<String,String>> map = createSimple("map", Map.class);
	

	public QCaAsset(String variable) {
		super(CaAsset.class, PathMetadataFactory.forVariable(variable), "null", "CA_ASSET");
		super.scanClassMetadata();
	}
}
