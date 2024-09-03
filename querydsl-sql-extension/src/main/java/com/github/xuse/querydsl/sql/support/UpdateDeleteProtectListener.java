package com.github.xuse.querydsl.sql.support;

import java.util.List;
import java.util.Map;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBaseListener;
import com.querydsl.sql.dml.SQLUpdateBatch;


/**
 * 为了防止开发者不小心遗漏where条件，造成全表记录被误写或者全表被删除的严重后果，一般业务代码开发中可以添加这个监听器。
 * 一旦发现where条件为空的删除或更新，将阻止操作。
 * @author Joey
 *
 */
public class UpdateDeleteProtectListener  extends SQLBaseListener {

	@Override
	public void notifyDelete(RelationalPath<?> entity, QueryMetadata md) {
		if(md.getWhere()==null) {
			throw new UnsupportedOperationException("The deletion was rejected to prevent operation on all records.");
		}
	}

	@Override
	public void notifyDeletes(RelationalPath<?> entity, List<QueryMetadata> batches) {
		for(QueryMetadata md: batches) {
			if(md.getWhere()==null) {
				throw new UnsupportedOperationException("The deletion was rejected to prevent operation on all records.");
			}	
		}
	}

	@Override
	public void notifyUpdate(RelationalPath<?> entity, QueryMetadata md, Map<Path<?>, Expression<?>> updates) {
		if(md.getWhere()==null) {
			throw new UnsupportedOperationException("The update was rejected to prevent operation on all records.");
		}
	}

	@Override
	public void notifyUpdates(RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
		for(SQLUpdateBatch md: batches) {
			if(md.getMetadata().getWhere()==null) {
				throw new UnsupportedOperationException("The update was rejected to prevent operation on all records.");
			}	
		}
	}

}
