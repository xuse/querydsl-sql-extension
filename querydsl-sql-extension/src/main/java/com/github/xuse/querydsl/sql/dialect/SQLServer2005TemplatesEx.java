package com.github.xuse.querydsl.sql.dialect;

import java.sql.Types;

import com.querydsl.sql.SQLTemplates;

/**
 * SQL Server 2005 dialect extension.
 * <p>
 * Inherits from {@link SQLServer2012TemplatesEx} with the following differences:
 * <ul>
 *   <li>No DATE/TIME types (use DATETIME instead)</li>
 *   <li>No DATETIME2 (use DATETIME with fixed precision)</li>
 *   <li>No OFFSET/FETCH pagination (uses TOP or ROW_NUMBER)</li>
 * </ul>
 * 
 * @author Joey
 */
public class SQLServer2005TemplatesEx extends SQLServer2012TemplatesEx {

	public SQLServer2005TemplatesEx(SQLTemplates template) {
		super(template);
		// SQL Server 2005 does not have DATE, TIME, DATETIME2 types
		// Override with DATETIME (fixed precision, ~3.33ms accuracy)
		typeNames.put(Types.DATE, "datetime").type(Types.TIMESTAMP).noSize();
		typeNames.put(Types.TIME, "datetime").type(Types.TIMESTAMP).noSize();
		typeNames.put(Types.TIMESTAMP, "datetime").noSize();
	}
}
