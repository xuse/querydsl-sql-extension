package com.github.xuse.querydsl.mock;

import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionClauseContext;

public class SQLRewriter {
	public void process(CreateTableContext e) {
		PartitionClauseContext partition = e.getChild(PartitionClauseContext.class, 0);
		if (partition != null) {
			PartitionDataEmu.getInstance().processCreate(e);
			e.children.remove(partition);
		}
	}

	private boolean processed(AlterTableContext ctx) {
		PartitionDataEmu.getInstance().processAlter(ctx);
		return ctx.isEmpty();
	}

	public void process(AlterStatementContext e) {
		AlterTableContext alterTable = e.getChild(AlterTableContext.class, 0);
		if(alterTable!=null) {
			if(processed(alterTable)) {
				e.children.clear();
			};
		}
	}
}
