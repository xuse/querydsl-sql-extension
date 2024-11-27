package com.github.xuse.querydsl.mock;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterPartitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTablePartitionOptionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionDefinitionOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionDefinitionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionKeyAlgorithmContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionLessThanValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionTypeDefContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.sql.partitions.PartitionDef;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.collection.CollectionUtils;

import lombok.Data;

public class PartitionDataEmu {
	
	private static final PartitionDataEmu INSTANCE=new PartitionDataEmu();
	public static PartitionDataEmu getInstance() {
		return INSTANCE;
	}
	
	final Map<String,PartitionData> data=new HashMap<>();
	
	public void put(String table, PartitionData pb) {
		//System.out.println("Save:" + table+" :" +pb.partitions.size());
		this.data.put(table, pb); 
	}
	
	public ResultSet get(Map<String, Object> map) {
		String schema=StringUtils.toString(map.get("schema"));
		String table=StringUtils.toString(map.get("table"));
		PartitionData pb=data.get(table);
		if(pb==null) {
			return MockResultSet.EMPTY;
		}
		List<String> columns=Arrays.asList("TABLE_CATALOG","TABLE_SCHEMA","TABLE_NAME",
				"PARTITION_NAME","PARTITION_METHOD","CREATE_TIME",
				"PARTITION_EXPRESSION","PARTITION_ORDINAL_POSITION","PARTITION_DESCRIPTION");
		List<Object[]> rows=new ArrayList<>();
		List<PartitionDef> partitions=pb.partitions;
		if(partitions.isEmpty() && pb.count>0) {
			for(int i=0;i<pb.count;i++) {
				Object[] row=new Object[9];
				rows.add(row);
				row[0]="def";
				row[1] = schema;
				row[2] = table;
				row[3]="p"+i;
				row[4]=pb.getMethod().name().replace('_', ' ');
				row[5]=new java.sql.Timestamp(System.currentTimeMillis()-60000);
				row[6]=pb.getMethodExpr();
				row[7]=i+1;
				row[8]=null;
			}
		}else{
			for(int i=0;i<partitions.size();i++) {
				Partition p=partitions.get(i);
				Object[] row=new Object[9];
				rows.add(row);
				row[0] = "def";
				row[1] = schema;
				row[2] = table;
				row[3] = p.name();
				row[4] = pb.getMethod().name().replace('_', ' ');
				row[5] = new java.sql.Timestamp(System.currentTimeMillis() - 60000);
				row[6] = pb.getMethodExpr();
				row[7] = i + 1;
				row[8] = p.value();
			}
		}
		return MockResultSet.of(rows, columns);
	}

	public void processCreate(CreateTableContext ctx) {
		ctx.accept(new PartitionProcessVisitor());
	}

	//PARTITION BY RANGE COLUMNS(created) (PARTITION p202401 VALUES LESS THAN ('2024-02-01') , PARTITION p202402 VALUES LESS THAN ('2024-03-01'))
	//ctx.accept(null)
	public void processAlter(AlterTableContext ctx) {
		//分区问题
		ctx.accept(new PartitionProcessVisitor());
		//表结构修改 CHANGE问题
		// online DDL问题
		ctx.accept(new OnlineDDLVisitor());
	}
	
	static class OnlineDDLVisitor extends MySQLStatementBaseVisitor<String>{
		@Override
		public String visitAlterList(AlterListContext ctx) {
//			for(Iterator<ParseTree> iter=ctx.children.iterator();iter.hasNext();) {
//				ParseTree tree=iter.next();
//				if(tree instanceof AlterCommandsModifierContext) {
//					iter.remove();
//					iter.next();//删除掉后面的逗号Token;
//					iter.remove();
//					continue;
//				}
//			}
			ASTUtils.removeToParent(ctx, AlterTableContext.class);
			return null;
		}
//
//		@Override
//		public String visitAlterAlgorithmOption(AlterAlgorithmOptionContext ctx) {
//			ParseTree delete=ctx.getParent();
//			List<ParseTree> list=ctx.getParent().getParent().children;
//			for(Iterator<ParseTree> iter=list.iterator();iter.hasNext();) {
//				if(delete==iter.next()) {
//					iter.remove();
//					iter.next();//删除掉后面的逗号Token;
//					iter.remove();
//				}
//			}
//			return super.visitAlterAlgorithmOption(ctx);
//		}
//		
//		@Override
//		public String visitAlterLockOption(AlterLockOptionContext ctx) {
//			ParseTree delete=ctx.getParent();
//			List<ParseTree> list=ctx.getParent().getParent().children;
//			for(Iterator<ParseTree> iter=list.iterator();iter.hasNext();) {
//				if(delete==iter.next()) {
//					iter.remove();
//					iter.next();//删除掉后面的逗号Token;
//					iter.remove();
//				}
//			}
//			return super.visitAlterLockOption(ctx);
//		}
		
//		@Override
//		public String visitAlgorithmOptionAndLockOption(AlgorithmOptionAndLockOptionContext ctx) {
//			super.visitAlgorithmOptionAndLockOption(ctx);
//			ctx.getParent().children.remove(ctx);
//			return null;
//		}
//		ALGORITHM = INPLACE,
//				LOCK = SHARED,
	}
	
	class PartitionProcessVisitor extends MySQLStatementBaseVisitor<String>{
		private String table;

		@Override
		public String visitTableName(TableNameContext ctx) {
			return table = ctx.accept(ASTUtils.NAME);
		}

		@Override
		public String visitAlterTablePartitionOptions(AlterTablePartitionOptionsContext ctx) {
			if(ctx.REMOVE()!=null) {
				//移除分区配置
				PartitionDataEmu.this.put(table, null);
			}else {
				PartitionDataParser v=new PartitionDataParser();
				ctx.accept(v);
				PartitionData pb= v.get();
				PartitionDataEmu.this.put(table, pb);	
			}
			super.visitAlterTablePartitionOptions(ctx);
			//语句相关部分移除
			ASTUtils.removeToParent(ctx, AlterTableContext.class);
			return null;
		}

		@Override
		public String visitAlterPartition(AlterPartitionContext ctx) {
			PartitionData old=PartitionDataEmu.this.data.get(table);
			Assert.notNull(old);
			if(ctx.REBUILD()!=null) {
				//不模拟
			}else if(ctx.DROP()!=null) {
				old.processDrop(ctx.accept(ASTUtils.getIdentfiers()));
			}else if(ctx.REORGANIZE()!=null) {
				PartitionDataParser v=new PartitionDataParser();
				ctx.accept(v);
				old.processReorg(v.get().partitions);
			}else if(ctx.ADD()!=null) {
				if(ctx.NUMBER_()!=null) {
					old.adjust(Integer.parseInt(ctx.NUMBER_().getText()));	
				}else {
					PartitionDataParser v=new PartitionDataParser();
					ctx.accept(v);
					old.processAdd(v.get().partitions);	
				}
			}else if(ctx.COALESCE()!=null) {
				old.adjust(-Integer.parseInt(ctx.NUMBER_().getText()) );
			}else if(ctx.DISCARD()!=null) {
				//不模拟
			}else if(ctx.REPAIR()!=null) {
				//不模拟
			}else if(ctx.TRUNCATE()!=null) {
				//不模拟
			}
			
			
			ASTUtils.removeToParent(ctx, AlterTableContext.class);
			return super.visitAlterPartition(ctx);
		}

		@Override
		public String visitPartitionClause(PartitionClauseContext ctx) {
			PartitionDataParser v=new PartitionDataParser();
			ctx.accept(v);
			PartitionData pb= v.get();
			PartitionDataEmu.this.put(table, pb);	
			return super.visitPartitionClause(ctx);
		}
	}
	
	
	static class PartitionDataParser extends MySQLStatementBaseVisitor<Void>{
		
		private PartitionData result=new PartitionData();
		
		public PartitionData get() {
			return result;
		}

		//ADD PARTITION (PARTITION p202403 VALUES LESS THAN ('2024-04-01'))
		@Override
		public Void visitAlterPartition(AlterPartitionContext ctx) {
			return super.visitAlterPartition(ctx);
		}
		@Override
		public Void visitPartitionDefinitionOption(PartitionDefinitionOptionContext ctx) {
			ASTUtils.show("DEBUG2d", ctx);
			return super.visitPartitionDefinitionOption(ctx);
		}

		@Override
		public Void visitPartitionDefinitions(PartitionDefinitionsContext ctx) {
			return super.visitPartitionDefinitions(ctx);
		}
		@Override
		public Void visitPartitionKeyAlgorithm(PartitionKeyAlgorithmContext ctx) {
			return super.visitPartitionKeyAlgorithm(ctx);
		}
		@Override
		public Void visitPartitionList(PartitionListContext ctx) {
			return super.visitPartitionList(ctx);
		}
		
		@Override
		public Void visitPartitionTypeDef(PartitionTypeDefContext ctx) {
			//RANGE COLUMNS(created)
			boolean columns=ctx.COLUMNS()!=null;
			boolean liner=ctx.LINEAR()!=null;
			if(ctx.RANGE()!=null) {
				result.method = columns? PartitionMethod.RANGE_COLUMNS:PartitionMethod.RANGE;
			}else if(ctx.LIST()!=null) {
				result.method = columns? PartitionMethod.LIST_COLUMNS:PartitionMethod.LIST;
			}else if(ctx.HASH()!=null) {
				result.method = liner? PartitionMethod.LINEAR_HASH:PartitionMethod.HASH;
				calcCount(ctx);
			}else if(ctx.KEY()!=null) {
				result.method=PartitionMethod.KEY;
				calcCount(ctx);
			}
			if(ctx.columnNames()!=null) {
				result.methodExpr = ctx.columnNames().accept(ASTUtils.NAME);
			}else {
				result.methodExpr = ctx.bitExpr().accept(ASTUtils.getDDLFormatter());
			}
			return super.visitPartitionTypeDef(ctx);
		}
		
		private void calcCount(PartitionTypeDefContext ctx) {
			for(int i=0;i<ctx.getParent().children.size();i++) {
				ParseTree t = ctx.getParent().children.get(i);
				if(ctx==t) {
					//String s1=ctx.getParent().children.get(i+1).getText();
					String s2=ctx.getParent().children.get(i+2).getText();
					result.count=Integer.parseInt(s2);
					break;
				}
			}
		}

		@Override
		public Void visitPartitionDefinition(PartitionDefinitionContext ctx) {
			PartitionNameContext nameCtx=ctx.getChild(PartitionNameContext.class, 0);
			String name=nameCtx.accept(ASTUtils.NAME);
			
			String value = null;
			PartitionLessThanValueContext values = ctx.getChild(PartitionLessThanValueContext.class, 0);
			if(values!=null) {
				value = values.getChild(ExprContext.class, 0).accept(ASTUtils.getDDLFormatter());
			}
			
			if(ctx.partitionValueList()!=null) {
				value= ctx.partitionValueList().accept(ASTUtils.getDDLFormatter());
			}
			result.partitions.add(new PartitionDef(name, null, value));
			return super.visitPartitionDefinition(ctx);
		}

		@Override
		public Void visitPartitionNames(PartitionNamesContext ctx) {
			ASTUtils.show("DEBUG2h", ctx);
			return super.visitPartitionNames(ctx);
		}
		
		
		/////////////// 无需访问的层级 //////////////
		@Override
		public Void visitPartitionClause(PartitionClauseContext ctx) {
			//整个分区语句，粒度太粗，无需访问
			//PARTITION BY RANGE COLUMNS(created) (PARTITION p202401 VALUES LESS THAN ('2024-02-01') , PARTITION p202402 VALUES LESS THAN ('2024-03-01'))
			return super.visitPartitionClause(ctx);
		}
	}
	@Data
	static final class PartitionData{
		private PartitionMethod method;
		
		private String methodExpr;
		
		private List<PartitionDef> partitions=new ArrayList<>();
		
		private int count;

		public void processAdd(List<PartitionDef> partitions2) {
			for(PartitionDef comming:partitions2) {
				PartitionDef hasDef=null;
				for(PartitionDef def:partitions) {
					if(comming.getName().equals(def.getName())) {
						hasDef = def;
						break;
					}
				}	
				if(hasDef==null) {
					partitions.add(comming);
				}else {
					hasDef.setFrom(comming.from());
					hasDef.setValue(comming.value());
				}
			}
			System.out.println("Save: ADD:" +partitions.size());
		}

		public void adjust(int i) {
			count+=i;
		}

		public void processReorg(List<PartitionDef> partitions2) {
			processAdd(partitions2);
		}

		public void processDrop(List<String> partitions2) {
			for(String name:partitions2) {
				CollectionUtils.removeFirst(partitions, e->e.getName().equals(name));
			}
			
			System.out.println("Save: DROP:" +partitions.size());
		}
	}
}
