package com.github.xuse.querydsl.entity.partition;

import java.sql.Types;
import java.util.Date;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.GeneratedType;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.annotation.dbdef.Key;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.querydsl.core.types.ConstraintType;

import lombok.Data;

@Data
@TableSpec(name="partition_test5",collate = "utf8mb4_general_ci",
	keys = {
		@Key(path= {"name"},type=ConstraintType.KEY)
	}
)
@Comment("Test table for partition-5.")
public class PartitionFoo5 {
	@ColumnSpec(autoIncrement = true,type = Types.INTEGER,unsigned = true,nullable = false)
	@Comment("primary key，auto increment.")
	private int id;
	
	@ColumnSpec(type = Types.VARCHAR,size=64,nullable = false,defaultValue = "''")
	@Comment("The code of asset. unique.")
	private String code;
	
	@ColumnSpec(name="asset_name",size=128,nullable = false)
	@Comment("The name of the asset.")
	private String name;
	
	@ColumnSpec(nullable = false)
	@AutoGenerated(GeneratedType.CREATED_TIMESTAMP)
	@Comment("create time of the record.")
	private Date created;
	
	@ColumnSpec(nullable = false)
	@AutoGenerated(GeneratedType.UPDATED_TIMESTAMP)
	@Comment("update time of the record.")
	private Date updated;
}
