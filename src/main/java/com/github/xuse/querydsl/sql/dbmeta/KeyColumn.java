package com.github.xuse.querydsl.sql.dbmeta;

import com.querydsl.sql.Column;

import lombok.Data;

@Data
public final class KeyColumn{
	@Column("TABLE_CAT")
	private String tableCat;
	
	@Column("TABLE_SCHEM")
	private String tableSchema;
	
	@Column("TABLE_NAME")
	private String tableName;
	
	@Column("INDEX_NAME")
	 String keyName;
	
	@Column("COLUMN_NAME")
	private String columnName;
	
	/**
	 * 	tableIndexStatistic - this identifies table statistics that arereturned in conjuction with a table's index descriptions 
	tableIndexClustered - this is a clustered index 
	tableIndexHashed - this is a hashed index 
	tableIndexOther - this is some other style of index 
	 */
	@Column("TYPE")
	private int type;
	
	@Column("NON_UNIQUE")
	private boolean nonUnique;
	
	@Column("INDEX_QUALIFIER")
	private String indexQualifier;
	
	@Column("ASC_OR_DESC")
	private String ascDesc;
	
	@Column("CARDINALITY")
	private long cardinality;
	
	@Column("PAGES")
	private long pages;
	
	@Column("ORDINAL_POSITION")
	public int seq;
	
	@Column("FILTER_CONDITION")
	private String filterCondition;
}
