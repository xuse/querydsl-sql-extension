package com.github.xuse.querydsl.entity;

import java.sql.Types;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.CustomType;
import com.github.xuse.querydsl.annotation.GeneratedType;
import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.annotation.dbdef.Key;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.entity.type.TestForJSONObjectType;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;

import lombok.Data;

/**
 * 这是一个没有QueryClass的数据库对象
 */
@Data
@TableSpec(name="ca_foo",primaryKeys="id",collate = "utf8mb4_general_ci",
keys = {
		@Key(path= {"code"},type=ConstraintType.UNIQUE),
		@Key(path= {"content"},type=ConstraintType.FULLTEXT),
	}
)
@Comment("Test table for Foo.")
public class Foo {
	
	@ColumnSpec(autoIncrement = true,type = Types.INTEGER,unsigned = true,nullable = false)
	@Comment("primary key，auto increment.")
	private int id;
	
	@ColumnSpec(type = Types.VARCHAR,size=64,nullable = false,defaultValue = "''")
	@Comment("The code of asset. unique.")
	private String code;
	
	@ColumnSpec(name="asset_name",size=128,nullable = false)
	@Comment("The name of the asset.")
	private String name;
	
	@ColumnSpec(size=16384)
	@Comment("Asset's comments.")
	private String content;
	
	@ColumnSpec()
	@AutoGenerated(GeneratedType.CREATED_TIMESTAMP)
	@Comment("create time of the record.")
	private Instant created;
	
	@ColumnSpec()
	@AutoGenerated(GeneratedType.UPDATED_TIMESTAMP)
	@Comment("update time of the record.")
	private Date updated;
	
	@ColumnSpec(type=Types.VARCHAR,size=16)
	@Comment("Another extension info.")
	private Gender gender;
	
	@CustomType(TestForJSONObjectType.class)
	@ColumnSpec(type=Types.VARCHAR,size=256)
	@Comment("extension info.")
	private Aaa ext;
	
	@CustomType(TestForJSONObjectType.class)
	@ColumnSpec(type=Types.VARCHAR,size=256)
	@Comment("Some custom attributes.")
	private Map<String,String> map;
	
	@ColumnSpec(type=Types.INTEGER,nullable = false,unsigned = true)
	@UnsavedValue(UnsavedValue.MinusNumber)
	private int volume;

}