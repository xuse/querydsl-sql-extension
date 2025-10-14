package com.github.xuse.querydsl.util;


import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithIdentifier;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import io.github.xuse.querydsl.sql.code.generate.LambdaFieldsGenerator;
import io.github.xuse.querydsl.sql.code.generate.OutputDir;
import io.github.xuse.querydsl.sql.code.generate.QCalssGenerator;

public class TypeTests {
	JavaParser p=new JavaParser();
	
	
	@Test
	public void testParserAst() {
		ParseResult<ClassOrInterfaceType> result=p.parseClassOrInterfaceType("String<List<Aaa<? super Object>[]>>");
		if(result.isSuccessful()) {
			ClassOrInterfaceType value=result.getResult().get();
			print(value,0);	
		}else {
			System.err.println(result.getProblems());
		}
	}
	
	@Test
	public void testClassAST() throws IOException {
		URL url=this.getClass().getResource("/Foo.txt");
		ParseResult<CompilationUnit> result=p.parse(url.openStream());
		if(result.isSuccessful()) {
			CompilationUnit value=result.getResult().get();
			print(value,0);	
			value.accept(new GenericVisitorAdapter<Void,Void>(){

				@Override
				public Void visit(LambdaExpr n, Void arg) {
					return super.visit(n, arg);
				}
				@Override
				public Void visit(VariableDeclarator n, Void arg) {
					return super.visit(n, arg);
				}
				
			}, null);
		}else {
			System.err.println(result.getProblems());
		}
	}
	
	@Test
	public void testGenerateQclz() {
		QCalssGenerator g=new QCalssGenerator();
		g.setOutputDir(OutputDir.DIR_TEST);
		File file=g.generate(Foo.class);
		System.out.println(file.getAbsolutePath());
	}
	
	@Test
	public void testGenerateLambda() {
		LambdaFieldsGenerator g=new LambdaFieldsGenerator();
		g.setOutputDir(OutputDir.DIR_TEST);
		File file=g.generate(Foo.class);
		System.out.println(file.getAbsolutePath());
	}

	private void print(Node value, int i) {
		System.out.print(StringUtils.repeat("  ",i));
		System.out.print(" - ");
		String nodeClass=value.getClass().getSimpleName();
		String v;
		if(value instanceof NodeWithIdentifier) {
			v = ((NodeWithIdentifier<?>) value).getIdentifier();
		}else {
			v=value.toString();
		}
		System.out.println(nodeClass+" -- "+v.replace("\r\n", ""));
		for(Node node:value.getChildNodes()) {
			if(node instanceof MethodDeclaration) {
			}else {
				print(node, i+1);
			}
		}
	}
}
