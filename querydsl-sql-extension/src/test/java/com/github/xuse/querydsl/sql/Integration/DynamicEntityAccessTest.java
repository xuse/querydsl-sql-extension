package com.github.xuse.querydsl.sql.Integration;

import java.sql.Date;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.GeneratedType;
import com.github.xuse.querydsl.sql.DynamicRelationalPath;
import com.github.xuse.querydsl.sql.column.ColumnFeature;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.ColumnMetadata;

/**
 * 无类模型，纯使用动态方式操作数据库。
 * 作为一种特殊场景——表字段不确定情况下的补充手段
 */
public class DynamicEntityAccessTest extends AbstractTestBase{
	private final Map<String,DynamicRelationalPath> tableMetadata=new HashMap<>();
	
	@Test
	public void crateTable() {
		DynamicRelationalPath table=getModel("dyn_entity_apple");
		factory.getMetadataFactory().createTable(table).ifExists().execute();
	}
	
	@Test
	public void crudTest() {
		DynamicRelationalPath table=getModel("dyn_entity_apple");
		Tuple o = table.newTuple(null,"张三",2,null);
		//增
		factory.insert(table).populate(o).execute();
		//查
		List<Tuple> tuples=factory.selectFrom(table).fetch();
		for(Tuple t:tuples) {
			System.out.println(t);
		}
		//更新
		Map<String,Object> bean=new HashMap<>();
		bean.put("id", 3);
		bean.put("name", "李四");
		Tuple u = table.newTuple(bean);
		factory.update(table).populate(u, true).execute();
		tuples=factory.selectFrom(table).fetch();
		for(Tuple t:tuples) {
			System.out.println(t);
		}
		
		//删除
		factory.delete(table).populatePrimaryKey(u).execute();
		tuples=factory.selectFrom(table).fetch();
		for(Tuple t:tuples) {
			System.out.println(t);
		}
		
		//自定义查询
		SimpleExpression<String> name = table.path("name",String.class);
		SimpleExpression<Long> id = table.path("id",Long.class);
		SimpleExpression<Integer> status = table.path("status",Integer.class);
		tuples=factory.select(id,status).from(table).where(name.eq("张三")).fetch();
		for(Tuple t:tuples) {
			System.out.println(t);
		}
	}
	
	private DynamicRelationalPath getModel(String key) {
		return tableMetadata.computeIfAbsent(key, this::initModel);
	}
	@SuppressWarnings("unused")
	private DynamicRelationalPath initModel(String key) {
		DynamicRelationalPath table = new DynamicRelationalPath("t1", null, key);
		Path<Long> id=table.addColumn(Long.class, ColumnMetadata.named("id").ofType(Types.BIGINT).notNull())
			.with(ColumnFeature.AUTO_INCREMENT).unsigned().comment("主键ID")
			.build();
		
		Path<String> name=table.addColumn(String.class,ColumnMetadata.named("name").ofType(Types.VARCHAR).withSize(256).notNull())
			.defaultValue("")
			.build();
		
		Path<Integer> status=table.addColumn(Integer.class, ColumnMetadata.named("status").ofType(Types.INTEGER).notNull())
			.build();
			
		Path<Date> created=table.addColumn(Date.class,ColumnMetadata.named("create_time").ofType(Types.TIMESTAMP).notNull())
			.withAutoGenerate(GeneratedType.CREATED_TIMESTAMP)
			.build();
		
		table.createPrimaryKey(id);
		table.createIndex("idx_table_name_status", name, status);
		return table;
	}
}
