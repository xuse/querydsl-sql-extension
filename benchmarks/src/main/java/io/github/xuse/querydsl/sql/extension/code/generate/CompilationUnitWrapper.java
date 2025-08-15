package io.github.xuse.querydsl.sql.extension.code.generate;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class CompilationUnitWrapper {
	private List<Class> imports=new ArrayList<>();
	
	private final CompilationUnit unit;
	
	public CompilationUnitWrapper(CompilationUnit unit){
		this.unit=unit;
	};
	
	
	
	
	public ClassOrInterfaceType type(Class<?> mainE) {
		
	}
}
