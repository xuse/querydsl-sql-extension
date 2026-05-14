package com.github.xuse.querydsl.sql.dialect;

import com.querydsl.sql.SQLTemplates;

/**
 * SQL Server 2008 dialect extension.
 * <p>
 * Inherits from {@link SQLServer2012TemplatesEx}. SQL Server 2008 introduced:
 * <ul>
 *   <li>DATE, TIME, DATETIME2, DATETIMEOFFSET types</li>
 *   <li>Same type mappings as 2012</li>
 * </ul>
 * The only difference from 2012 is pagination (no OFFSET/FETCH, uses ROW_NUMBER),
 * which is handled by the base QueryDSL SQLServer2008Templates.
 * 
 * @author Joey
 */
public class SQLServer2008TemplatesEx extends SQLServer2012TemplatesEx {

	public SQLServer2008TemplatesEx(SQLTemplates template) {
		super(template);
	}
}
