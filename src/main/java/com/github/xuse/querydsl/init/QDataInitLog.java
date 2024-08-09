package com.github.xuse.querydsl.init;


import java.util.Date;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QDataInitLog extends RelationalPathBaseEx<DataInitLog>{
	public static final QDataInitLog dataInitLog = new QDataInitLog("tnt");
	
	public QDataInitLog(String variable) {
		super(DataInitLog.class, variable);
		super.scanClassMetadata();
	}
	
	public final StringPath tableName = createString("tableName");
	
	public final NumberPath<Integer> disabled = createNumber("disabled",int.class);
	
	public final NumberPath<Integer> records = createNumber("records", int.class);
	
	public final DateTimePath<Date> lastInitTime = createDateTime("lastInitTime", Date.class);
	
	public final StringPath lastInitUser = createString("lastInitUser");
	
	public final StringPath lastInitResult = createString("lastInitResult");
	
	private static final long serialVersionUID = -3898613275156482519L;

}
