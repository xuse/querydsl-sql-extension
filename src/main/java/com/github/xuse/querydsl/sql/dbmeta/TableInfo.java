package com.github.xuse.querydsl.sql.dbmeta;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.github.xuse.querydsl.util.StringUtils;

/**
 * 一张数据库表或视图。<p>
 * Indicates a table/view in database.
 *
 * @author xuse
 */
public class TableInfo {

	private String catalog;

	private String schema;

	private String name;

	private String remarks;

	private String type;

	private String typeCat;

	private String typeSchema;

	private String typeName;

	private Map<String, Object> attributes;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotEmpty(schema)) {
			sb.append(schema).append('.');
		}
		sb.append(name);
		if (StringUtils.isNotEmpty(remarks)) {
			sb.append(':').append(remarks);
		}
		return sb.toString();
	}

	/**
	 *  数据库表所属catalog
	 *
	 *  @return catalog
	 */
	public String getCatalog() {
		return catalog;
	}

	/**
	 * 设置Catalog
	 * @param catalog catalog
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 *  获得表所在schema
	 *
	 *  @return the schema of table
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * 设置 schema
	 * @param schema schema
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 *  获得表/视图(等)的名称
	 *
	 *  @return 名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置名称
	 * @param name name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *  获得表的备注信息
	 *
	 *  @return 备注
	 */
	public String getRemarks() {
		return remarks;
	}

	/**
	 *  设置备注
	 *
	 *  @param remarks 备注
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	/**
	 *  获得表的类型
	 *
	 *  @return 类型
	 */
	public String getType() {
		return type;
	}

	/**
	 *  设置表类型
	 *
	 *  @param type 类型
	 */
	public void setType(String type) {
		this.type = type;
	}

	public String getTypeCat() {
		return typeCat;
	}

	public void setTypeCat(String typeCat) {
		this.typeCat = typeCat;
	}

	public String getTypeSchema() {
		return typeSchema;
	}

	public void setTypeSchema(String typeSchema) {
		this.typeSchema = typeSchema;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Date getAttributeDate(String at) {
		Object v = attributes == null ? null : attributes.get(at);
		if (v instanceof Date) {
			return (Date) v;
		}
		return null;
	}

	public Integer getAttributeInt(String at) {
		Object v = attributes == null ? null : attributes.get(at);
		if (v instanceof Number) {
			return ((Number) v).intValue();
		}
		return null;
	}

	public Long getAttributeLong(String at) {
		Object v = attributes == null ? null : attributes.get(at);
		if (v instanceof Number) {
			return ((Number) v).longValue();
		}
		return null;
	}

	public String getAttribute(String at) {
		Object v = attributes == null ? null : attributes.get(at);
		return v == null ? null : v.toString();
	}

	public void setAttribute(String at, Object obj) {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		attributes.put(at, obj);
	}
}
