package com.github.xuse.querydsl.enums;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;
import java.util.Date;

import javax.annotation.Generated;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.github.xuse.querydsl.sql.column.ColumnFeature;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QAaa is a Querydsl query type for Aaa
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAaa2 extends RelationalPathBaseEx<Aaa> {

	private static final long serialVersionUID = -1389588466;

	public static final QAaa2 aaa = new QAaa2("AAA");

	public final DateTimePath<Date> created = createDateTime("created", Date.class);

	public final NumberPath<Integer> id = createNumber("id", int.class);
	
	public final NumberPath<Integer> version = createNumber("version", Integer.class);

	public final StringPath name = super.createString("name");

	public final EnumPath<Gender> gender = super.createEnum("gender", Gender.class);
	
	public final EnumPath<TaskStatus> taskStatus = super.createEnum("taskStatus", TaskStatus.class);

	public final com.querydsl.sql.PrimaryKey<Aaa> PK_Aaa = createPrimaryKey(id,version);

	public QAaa2(String variable) {
		super(Aaa.class, forVariable(variable), "null", "AAA");
		addMetadata();
	}

	public QAaa2(String variable, String schema, String table) {
		super(Aaa.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QAaa2(String variable, String schema) {
		super(Aaa.class, forVariable(variable), schema, "AAA");
		addMetadata();
	}

	public QAaa2(Path<? extends Aaa> path) {
		super(path.getType(), path.getMetadata(), "null", "AAA");
		addMetadata();
	}

	public QAaa2(PathMetadata metadata) {
		super(Aaa.class, metadata, "null", "AAA");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull()).with(ColumnFeature.AUTO_INCREMENT).comment("ID");
		addMetadata(created,ColumnMetadata.named("CREATED").withIndex(3).ofType(Types.TIMESTAMP)).comment("创建时间");
		addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(56)).comment("名称");
		addMetadata(gender, ColumnMetadata.named("GENDER").withIndex(4).ofType(Types.TINYINT).notNull()).unsigned().defaultExpression("0").comment("性别");
		addMetadata(taskStatus, ColumnMetadata.named("TASK_STATUS").withIndex(5).ofType(Types.SMALLINT).notNull()).unsigned().defaultExpression("1").comment("任务状态");
		addMetadata(version, ColumnMetadata.named("VERSION").withIndex(6).ofType(Types.INTEGER).notNull()).defaultValue(2).comment("版本");
		createConstraint(null,ConstraintType.UNIQUE,name, version);
		createConstraint(null,ConstraintType.KEY,taskStatus);
		setComment("测试表");
		setCollate(Collate.utf8mb4_general_ci);
	}
}
