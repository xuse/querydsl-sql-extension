package com.querydsl.sql;

/**
 * 为了访问SQLTemplates的包私有属性。无法移动到其他包
 *
 */
public class TemplatesAccessor {
	public static void setAutoIncrement(SQLTemplates templates, String str) {
		templates.setAutoIncrement(str);
	}
}
