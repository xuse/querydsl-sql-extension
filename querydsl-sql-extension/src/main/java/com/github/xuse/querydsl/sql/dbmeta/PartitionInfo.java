package com.github.xuse.querydsl.sql.dbmeta;

import java.util.Date;

import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Generated
public class PartitionInfo {
	private String tableCat;
	private String tableSchema;
	private String tableName;
	private String name;
	private int partitionOrdinal;
	private PartitionMethod method;
	private String partitionExpression;
	private String partitionDescription;
	private Date createTime;
	
	private final boolean hasRangeDesc;
	
	public PartitionInfo(){
		this(false);
	}
	
	public PartitionInfo(boolean hasRangeDesc){
		this.hasRangeDesc = hasRangeDesc;
	}

	@Override
	public String toString() {
		String s = " ";
		if (method != null && !hasRangeDesc) {
			switch (method) {
			case KEY:
			case HASH:
			case LINEAR_HASH:
				s = " (";
				break;
			case LIST:
			case LIST_COLUMNS:
				s = " VALUES IN (";
				break;
			case RANGE:
			case RANGE_COLUMNS:
				s = " VALUES LESS THAN (";
				break;
			default:
			}
		}

		return "PARTITION BY " + method + " (" + partitionExpression + ") PARTITION " + name + s + partitionDescription
				+ ")";
	}
}
