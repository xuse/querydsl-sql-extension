package com.github.xuse.querydsl.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Types;
import java.util.Date;

import javax.annotation.Generated;

import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.github.xuse.querydsl.sql.column.ColumnFeature;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.querydsl.core.types.ConstraintType;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QAaa is a Querydsl query type for Aaa
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAaa extends RelationalPathBaseEx<Aaa> {

	private static final long serialVersionUID = -1389588466;

	public static final QAaa aaa = new QAaa("t1");

	public final DateTimePath<Date> created = createDateTime("created", Date.class);

	public final NumberPath<Integer> id = createNumber("id", int.class);
	
	public final NumberPath<Integer> version = createNumber("version", Integer.class);

	public final StringPath name = super.createString("name");

	public final EnumPath<Gender> gender = super.createEnum("gender", Gender.class);
	
	public final EnumPath<TaskStatus> taskStatus = super.createEnum("taskStatus", TaskStatus.class);
	
	public final EnumPath<Gender> genderWithChar = createEnum("genderWithChar", Gender.class);
	
	private final NumberPath<Integer> dataInt = createNumber("dataInt",Integer.class);
	
	private final NumberPath<Float> dataFloat =createNumber("dataFloat",Float.class);
	
	private final NumberPath<Double> dataDouble = createNumber("dataDouble",Double.class);
	
	private final NumberPath<Short> dataShort = createNumber("dataShort",Short.class);
	
	private final NumberPath<Long> dataBigint = createNumber("dataBigint",Long.class);
	
	private final NumberPath<BigDecimal> dataDecimal = createNumber("dataDecimal",BigDecimal.class);
	
	private final BooleanPath dataBit = createBoolean("dataBit");
	
	private final BooleanPath dataBool = createBoolean("dataBool");
	
	private final DateTimePath<Date> dataDate = createDateTime("dataDate",Date.class);;
	
	private final DateTimePath<Time> dataTime = createDateTime("dataTime",Time.class);;
	
	private final DateTimePath<Date> dateTimestamp =  createDateTime("dateTimestamp",Date.class);
	
	private final StringPath dataText =  super.createString("dataText");;
	
	private final StringPath dataLongText = super.createString("dataLongText");
	
	private final SimplePath<byte[]> dateBinary = super.createSimple("dateBinary", byte[].class);
	
	private final SimplePath<byte[]> dateVarBinary = super.createSimple("dateVarBinary", byte[].class);
	
	
	

	public final com.querydsl.sql.PrimaryKey<Aaa> PK_Aaa = createPrimaryKey(id);

	public QAaa(String variable) {
		super(Aaa.class, forVariable(variable), "null", "AAA");
		addMetadata();
	}

	public QAaa(String variable, String schema, String table) {
		super(Aaa.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QAaa(String variable, String schema) {
		super(Aaa.class, forVariable(variable), schema, "AAA");
		addMetadata();
	}

	public QAaa(Path<? extends Aaa> path) {
		super(path.getType(), path.getMetadata(), "null", "AAA");
		addMetadata();
	}

	public QAaa(PathMetadata metadata) {
		super(Aaa.class, metadata, "null", "AAA");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull()).with(ColumnFeature.AUTO_INCREMENT).comment("ID");
		addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(64)).comment("名称");
		addMetadata(created,ColumnMetadata.named("CREATED").withIndex(3).ofType(Types.TIMESTAMP).withSize(3)).comment("创建时间");
		addMetadata(gender, ColumnMetadata.named("GENDER").withIndex(4).ofType(Types.TINYINT).notNull()).unsigned().defaultExpression("1").comment("性别");
		addMetadata(taskStatus, ColumnMetadata.named("TASK_STATUS").withIndex(5).ofType(Types.TINYINT).notNull()).unsigned().defaultExpression("1").comment("任务状态");
		addMetadata(version, ColumnMetadata.named("VERSION").withIndex(6).ofType(Types.INTEGER).notNull()).defaultValue(1).comment("版本");
		addMetadata(genderWithChar, ColumnMetadata.named("GENDER2").withIndex(7).ofType(Types.CHAR).withSize(4)).comment("性别2");
		addMetadata(dataInt, ColumnMetadata.named("C_INT").withIndex(8).ofType(Types.INTEGER).notNull()).defaultValue(0).comment("整型版本");
		addMetadata(dataFloat, ColumnMetadata.named("C_FLOAT").withIndex(9).ofType(Types.FLOAT).notNull()).defaultValue(1f).comment("浮点");
		addMetadata(dataDouble, ColumnMetadata.named("C_DOUBLE").withIndex(10).ofType(Types.DOUBLE).notNull()).defaultValue(1d).comment("DOUBLE");
		addMetadata(dataShort, ColumnMetadata.named("C_SHORT").withIndex(11).ofType(Types.SMALLINT).notNull()).defaultValue((short)1).comment("SMALL");
		addMetadata(dataBigint, ColumnMetadata.named("C_BIGINT").withIndex(12).ofType(Types.BIGINT).notNull()).defaultValue(1L).comment("长整型");
		addMetadata(dataDecimal, ColumnMetadata.named("C_DECIMAL").withIndex(13).ofType(Types.DECIMAL).withSize(12).withDigits(2).notNull())
			.defaultValue(new BigDecimal("1.99")).comment("版本");
		addMetadata(dataBool, ColumnMetadata.named("C_BOOL").withIndex(14).ofType(Types.BOOLEAN).notNull()).defaultValue(true).comment("BOOLEAN字段");
		addMetadata(dataBit, ColumnMetadata.named("C_BIT").withIndex(15).ofType(Types.BIT).notNull()).defaultValue(false).comment("BIT字段");
		addMetadata(dataDate, ColumnMetadata.named("C_DATE").withIndex(16).ofType(Types.DATE).notNull()).defaultValueInString("2023-01-01").comment("字段DATE");
		addMetadata(dataTime, ColumnMetadata.named("C_TIME").withIndex(17).ofType(Types.TIME).withSize(3).notNull()).defaultValueInString("12:00:00").comment("字段TIME");
		addMetadata(dateTimestamp, ColumnMetadata.named("C_TIMESTAMP").withIndex(18).ofType(Types.TIMESTAMP).notNull()).defaultExpression(Expressions.currentTimestamp());
		addMetadata(dataText, ColumnMetadata.named("C_TEXT").withIndex(19).ofType(Types.VARCHAR).withSize(2048)).defaultValue(" ");
		addMetadata(dataLongText, ColumnMetadata.named("C_LONGTEXT").withIndex(20).ofType(Types.VARCHAR).withSize(10000));
		addMetadata(dateBinary, ColumnMetadata.named("C_BIN").withIndex(21).ofType(Types.BINARY).withSize(512)).comment("测试二进制");
		addMetadata(dateVarBinary, ColumnMetadata.named("C_VARBIN").withIndex(22).ofType(Types.VARBINARY).withSize(1024)).comment("测试VARBIN123");
		
		createConstraint("unq_aaa_name_version",ConstraintType.UNIQUE,name, version);
		createConstraint("idx_aaa_taskstatus",ConstraintType.KEY,taskStatus);
		createCheck("cnt_check_int1", "TASK_STATUS < 10");
		createCheck("cnt_float_lt_double", dataFloat.loe(dataDouble));
		setComment("测试表新的备32");
		setCollate(Collate.utf8mb4_general_ci);
	}
}
