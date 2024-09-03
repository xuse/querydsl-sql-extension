package com.github.xuse.querydsl.sql.dbmeta;

import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.Column;

/**
 * 描述外键信息
 * 
 * @author Administrator
 * 
 */
public class ForeignKeyItem {
	// 删除被引用的记录引发的策略，默认为禁止删除，不同数据库的返回值常量不同
	@Column("FKTABLE_SCHEM")
	private String fromSchema;
	@Column("FKTABLE_NAME")
	private String fromTable;
	@Column("FKCOLUMN_NAME")
	private String fromColumn;
	@Column("PKTABLE_SCHEM")
	private String referenceSchema;
	@Column("PKTABLE_NAME")
	private String referenceTable;
	@Column("PKCOLUMN_NAME")
	private String referenceColumn;
	@Column("KEY_SEQ")
	private int keySeq;

	/**
	 * 删除规则：当被引用的数据删除时：<br>
	 * 默认： 禁止删除/on　delete　cascade: 删除那些引用此记录的记录/ on delete set null:
	 * 清空那些引用此记录的字段
	 * <ul>
	 * <li>importedKeyNoAction - do not allow delete of primary key if it has
	 * been imported</li>
	 * <li>importedKeyCascade - delete rows that import a deleted key</li>
	 * <li>importedKeySetNull - change imported key to NULL if its primary key
	 * has been deleted</li>
	 * <li>importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x
	 * compatibility)</li>
	 * <li>importedKeySetDefault - change imported key to default if its primary
	 * key has been deleted</li>
	 * </ul>
	 */
	@Column("DELETE_RULE")
	private int deleteRule;//

	/**
	 * 更新规则：当被引用的记录键值更新时<br>
	 * 默认：禁止更新 /on　update　cascade: 删除那些引用此记录的记录<br>
	 * 注意：Oracle不支持此操作，因此Oracle驱动返回的值总是0
	 * <ul>
	 * <li>importedNoAction - do not allow update of primary key if it has been
	 * imported</li>
	 * <li>importedKeyCascade - change imported key to agree with primary key
	 * update</li>
	 * <li>importedKeySetNull - change imported key to NULL if its primary key
	 * has been updated</li>
	 * <li>importedKeySetDefault - change imported key to default values if its
	 * primary key has been updated</li>
	 * <li>importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x
	 * compatibility)</li>
	 * </ul>
	 */
	@Column("UPDATE_RULE")
	private int updateRule;

	/**
	 * 外键名称
	 */
	@Column("FK_NAME")
	private String name;

	/**
	 * 主键名称
	 */
	@Column("PK_NAME")
	private String pkName;// ??

	/**
	 * Initially immediate(default) - constraint validated at statement level
	 * Initially deferred - constraint validated at commit level
	 */
	@Column("DEFERRABILITY")
	private int deferrAbility;

	public ForeignKeyItem() {
	}

	public ForeignKeyItem(String table, String column, String refTable,	String refColumn) {
		this.fromTable = table;
		this.fromColumn = column;
		this.referenceTable = refTable;
		this.referenceColumn = refColumn;
	}

	public String getFromTable() {
		return fromTable;
	}

	public void setFromTable(String fromTable) {
		this.fromTable = fromTable;
	}

	public String getFromColumn() {
		return fromColumn;
	}

	public void setFromColumn(String fromColumn) {
		this.fromColumn = fromColumn;
	}

	public String getReferenceTable() {
		return referenceTable;
	}

	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	public String getReferenceColumn() {
		return referenceColumn;
	}

	public void setReferenceColumn(String referenceColumn) {
		this.referenceColumn = referenceColumn;
	}

	public int getKeySeq() {
		return keySeq;
	}

	public void setKeySeq(int keySeq) {
		this.keySeq = keySeq;
	}

	public int getDeleteRule() {
		return deleteRule;
	}

	public void setDeleteRule(int deleteRule) {
		this.deleteRule = deleteRule;
	}

	public int getUpdateRule() {
		return updateRule;
	}

	public void setUpdateRule(int updateRule) {
		this.updateRule = updateRule;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPkName() {
		return pkName;
	}

	public void setPkName(String pkName) {
		this.pkName = pkName;
	}

	public int getDeferrAbility() {
		return deferrAbility;
	}

	public void setDeferrAbility(int deferrAbility) {
		this.deferrAbility = deferrAbility;
	}

	public String getFromSchema() {
		return fromSchema;
	}

	public void setFromSchema(String fromSchema) {
		this.fromSchema = fromSchema;
	}

	public String getReferenceSchema() {
		return referenceSchema;
	}

	public void setReferenceSchema(String referenceSchema) {
		this.referenceSchema = referenceSchema;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("(");
		sb.append(fromTable).append('.').append(fromColumn);
		sb.append("->").append(referenceTable).append('.')
				.append(referenceColumn).append(')');
		sb.append("D:").append(this.deleteRule);
		sb.append("U:").append(this.updateRule);
		sb.append("F:").append(this.deferrAbility);
		return sb.toString();
	}

	/**
	 * 生成删除外键的SQL语句
	 * 
	 * @return Drop sql
	 */
	public String toDropSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("alter table ").append(fromTable).append(" drop constraint ")
				.append(name);
		return sb.toString();
	}

	public String generateName() {
		StringBuilder sb=new StringBuilder("FK_");
		sb.append(StringUtils.upperCase(fromTable)).append('_');
		sb.append(StringUtils.upperCase(fromColumn));
		return sb.toString();
	}

	public String name() {
		return this.name;
	}

	public String foreignKeyDefinition() {
		return this.toString();
	}
}
