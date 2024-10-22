package com.github.xuse.querydsl.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;

public class ASTUtils {
	static Properties propDDL = new Properties();

	static {
		propDDL.setProperty("parameterized", "false");
	}
	public static void show(String prefix,RuleContext e) {
		String sql = e.accept(getDDLFormatter());
		System.err.println(prefix);
		System.err.println(sql);
	}
	
	private static final String SEP=" > ";
	
	public static void path(String prefix,RuleContext e) {
		String sep= SEP;
		List<String> types=new ArrayList<>();
		while(e!=null) {
			types.add(e.getClass().getSimpleName());
			e=e.getParent();
		}
		StringBuilder sb=new StringBuilder(prefix);
		if(!types.isEmpty()) {
			for(int i=types.size()-1;i>=0;i--) {
				sb.append(types.get(i));
				sb.append(sep);
			}
			sb.setLength(sb.length()-sep.length());	
		}
		System.err.println(sb);
	}
	
	public static MySQLStatementBaseVisitor<String> NAME=new MySQLStatementBaseVisitor<String>() {
		@Override
		public String visitTerminal(TerminalNode node) {
			super.visitTerminal(node);
			return node.getText();
		}
	};
	
	public static MySQLStatementBaseVisitor<List<String>> getIdentfiers() {
		return new MySQLStatementBaseVisitor<List<String>>() {
			List<String> result=new ArrayList<>();
			@Override
			public List<String> visitIdentifier(MySQLStatementParser.IdentifierContext node) {
				result.add(node.getText());
				return result;
			}
		};
	}
	
	public static MySQLFormatVisitor getDDLFormatter() {
		MySQLFormatVisitor v=new MySQLFormatVisitor();
		v.init(propDDL);
		return v;
	}

	public static boolean removeToParent(ParserRuleContext ctx, Class<? extends RuleContext> class1) {
		ParserRuleContext child=ctx;
		ParserRuleContext target=ctx.getParent();
		while(target!=null && target.getClass()!=class1 ) {
			child = target;
			target=target.getParent();
		}
		if(target==null) {
			return false;
		}
		target.children.remove(child);
		int effective=0;
		for(ParseTree c:target.children){
			if(c instanceof TerminalNode) {
				continue;
			}
			if(c instanceof TableNameContext) {
				continue;
			}
			effective++;
		}
		if(effective==0) {
			target.getParent().children.remove(target);
		}
		return true;
	}
}
