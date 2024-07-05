package com.github.xuse.querydsl.init;

import lombok.Getter;

@Getter
public class ScanOptions {
	
	public static final ScanOptions DEFAULT = new ScanOptions();
	
	//是否创建不存在的表
	private boolean createMissingTable = true;
	
	//是否更改已有表的结构
	private boolean alterExistTable = true;

	//是否允许删除已有表的列
	private boolean allowDropColumn;
	
	//是否允许删除已有表的索引
	private boolean allowDropIndex;
	
	//是否允许删除已有表的约束
	private boolean allowDropConstraint;
	
	//数据初始化行为
	private DataInitBehavior dataInitBehavior = DataInitBehavior.FOR_CREATED_TABLE_ONLY;
	
	/**
	 * 数据初始化功能：数据文件后缀
	 */
	private String dataInitFileSuffix=".csv";
	/**
	 * 数据初始化功能：用一张记录表记录初始化状态。
	 * 数据记录表有以下字段
	 * <ol>
	 * </ol>
	 */
	private boolean useDataInitTable = false;
	
	public static ScanOptions getDefault() {
		return DEFAULT;
	}

	public ScanOptions setCreateMissingTable(boolean createMissingTable) {
		this.createMissingTable = createMissingTable;
		return this;
	}
	public ScanOptions setAlterExistTable(boolean alterExistTable) {
		this.alterExistTable = alterExistTable;
		return this;
	}
	public ScanOptions setAllowDropColumn(boolean allowDropColumn) {
		this.allowDropColumn = allowDropColumn;
		return this;
	}
	public ScanOptions setAllowDropIndex(boolean allowDropIndex) {
		this.allowDropIndex = allowDropIndex;
		return this;
	}
	public ScanOptions setAllowDropConstraint(boolean allowDropConstraint) {
		this.allowDropConstraint = allowDropConstraint;
		return this;
	}
	public ScanOptions setDataInitBehavior(DataInitBehavior dataInitBehavior) {
		this.dataInitBehavior = dataInitBehavior;
		return this;
	}
	public ScanOptions setDataInitFileSuffix(String dataInitFileSuffix) {
		this.dataInitFileSuffix = dataInitFileSuffix;
		return this;
	}
	public ScanOptions setUseDataInitTable(boolean useDataInitTable) {
		this.useDataInitTable = useDataInitTable;
		return this;
	}
	public ScanOptions allowDrops() {
		this.allowDropColumn = true;
		this.allowDropIndex = true;
		this.allowDropConstraint = true;
		return this;
	}
	public ScanOptions useDataInitTable() {
		this.useDataInitTable=true;
		return this;
	}


	@Override
	public String toString() {
		return "ScanOptions [createMissingTable=" + createMissingTable + ", alterExistTable=" + alterExistTable
				+ ", allowDropColumn=" + allowDropColumn + ", allowDropIndex=" + allowDropIndex
				+ ", allowDropConstraint=" + allowDropConstraint + ", dataInitBehavior=" + dataInitBehavior
				+ ", dataInitFileSuffix=" + dataInitFileSuffix + ", useDataInitTable=" + useDataInitTable + "]";
	}
}
