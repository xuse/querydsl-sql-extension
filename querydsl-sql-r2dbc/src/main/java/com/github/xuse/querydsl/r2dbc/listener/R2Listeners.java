package com.github.xuse.querydsl.r2dbc.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLDetailedListener;
import com.querydsl.sql.SQLListeners;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLUpdateBatch;

public class R2Listeners implements R2BaseListener{
	private final List<SQLDetailedListener> sqls= new ArrayList<>();
	
	private final List<R2BaseListener> listeners = new ArrayList<>();
	
	public R2Listeners(SQLDetailedListener sqls) {
		this.sqls.add(sqls);
	}
	
	public static R2BaseListener wrap(SQLListeners listeners2) {
		return new R2Listeners(listeners2);
	}

	@Override
	public void notifyQuery(QueryMetadata md) {
		for(SQLDetailedListener e:sqls) {
			e.notifyQuery(md);
		}
		for(R2BaseListener e:listeners) {
			e.notifyQuery(md);
		}
	}

	@Override
	public void notifyDelete(RelationalPath<?> entity, QueryMetadata md) {
		for(SQLDetailedListener e:sqls) {
			e.notifyDelete(entity,md);
		}
		for(R2BaseListener e:listeners) {
			e.notifyDelete(entity,md);
		}
	}

	@Override
	public void notifyDeletes(RelationalPath<?> entity, List<QueryMetadata> batches) {
		for(SQLDetailedListener e:sqls) {
			e.notifyDeletes(entity,batches);
		}
		for(R2BaseListener e:listeners) {
			e.notifyDeletes(entity,batches);
		}
	}

	@Override
	public void notifyMerge(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> keys, List<Path<?>> columns,
			List<Expression<?>> values, SubQueryExpression<?> subQuery) {
		for (SQLDetailedListener e : sqls) {
			e.notifyMerge(entity, md, keys, columns, values, subQuery);
		}
		for (R2BaseListener e : listeners) {
			e.notifyMerge(entity, md, keys, columns, values, subQuery);
		}
	}

	@Override
	public void notifyMerges(RelationalPath<?> entity, QueryMetadata md, List<SQLMergeBatch> batches) {
		for (SQLDetailedListener e : sqls) {
			e.notifyMerges(entity, md, batches);
		}
		for (R2BaseListener e : listeners) {
			e.notifyMerges(entity, md, batches);
		}
	}

	@Override
	public void notifyInsert(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> columns,
			List<Expression<?>> values, SubQueryExpression<?> subQuery) {
		for (SQLDetailedListener e : sqls) {
			e.notifyInsert(entity, md, columns, values, subQuery);
		}
		for (R2BaseListener e : listeners) {
			e.notifyInsert(entity, md, columns, values, subQuery);
		}
	}

	@Override
	public void notifyInserts(RelationalPath<?> entity, QueryMetadata md, List<SQLInsertBatch> batches) {
		for (SQLDetailedListener e : sqls) {
			e.notifyInserts(entity, md, batches);
		}
		for (R2BaseListener e : listeners) {
			e.notifyInserts(entity, md, batches);
		}
	}

	@Override
	public void notifyUpdate(RelationalPath<?> entity, QueryMetadata md, Map<Path<?>, Expression<?>> updates) {
		for (SQLDetailedListener e : sqls) {
			e.notifyUpdate(entity, md, updates);
		}
		for (R2BaseListener e : listeners) {
			e.notifyUpdate(entity, md, updates);
		}
	}

	@Override
	public void notifyUpdates(RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
		for (SQLDetailedListener e : sqls) {
			e.notifyUpdates(entity, batches);
		}
		for (R2BaseListener e : listeners) {
			e.notifyUpdates(entity, batches);
		}
	}

	@Override
	public void start(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.start(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.start(context);
		}
	}

	@Override
	public void preRender(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.preRender(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.preRender(context);
		}
	}

	@Override
	public void rendered(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.rendered(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.rendered(context);
		}
	}

	@Override
	public void prePrepare(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.prePrepare(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.prePrepare(context);
		}
	}

	@Override
	public void prepared(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.prepared(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.prepared(context);
		}
	}

	@Override
	public void preExecute(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.preExecute(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.preExecute(context);
		}
	}

	@Override
	public void executed(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.executed(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.executed(context);
		}
	}

	@Override
	public void exception(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.exception(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.exception(context);
		}
	}

	@Override
	public void end(R2ListenerContext context) {
		if(!sqls.isEmpty() && context instanceof R2ListenerContextImpl) {
			R2ListenerContextImpl c=(R2ListenerContextImpl)context;
			for (SQLDetailedListener e : sqls) {
				e.end(c);
			}	
		}
		for (R2BaseListener e : listeners) {
			e.end(context);
		}
	}
}
