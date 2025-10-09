package com.github.xuse.querydsl.util;


import java.io.File;

import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithIdentifier;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import lombok.SneakyThrows;

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
