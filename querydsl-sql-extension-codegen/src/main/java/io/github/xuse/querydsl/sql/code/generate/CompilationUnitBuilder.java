package io.github.xuse.querydsl.sql.code.generate;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.xuse.querydsl.util.Exceptions;

public class CompilationUnitBuilder {
//	private List<Class<?>> imports = new LinkedHashSet<>();
	private Map<String,Class<?>> simpleNames = new HashMap<>();

	private final JavaParser parser;
	private final CompilationUnit unit;
	
	
	public static CompilationUnitBuilder create() {
		return new CompilationUnitBuilder(new CompilationUnit());
	}

	public CompilationUnitBuilder(CompilationUnit unit) {
		this.unit = unit;
		this.parser = new JavaParser();
	};

	public ClassOrInterfaceType createType(Class<?> raw, Type... args) {
		addImport(raw);
		com.github.javaparser.ast.type.Type[] types = new com.github.javaparser.ast.type.Type[args.length];
		for (int i = 0; i < args.length; i++) {
			types[i] = createType(args[i]);
		}
		return new ClassOrInterfaceType(null, new SimpleName(raw.getSimpleName()), NodeList.nodeList(types));
	}

	public com.github.javaparser.ast.type.Type createType(Type t) {
		if (t instanceof Class<?>) {
			return createClassType((Class<?>) t);
		} else if (t instanceof ParameterizedType) {
			com.github.javaparser.ast.type.Type type = createParameterized((ParameterizedType) t);
			return type;
		} else if (t instanceof GenericArrayType) {
			com.github.javaparser.ast.type.Type type = createGenericArray((GenericArrayType) t);
			return type;
		} else if (t instanceof WildcardType) {
			com.github.javaparser.ast.type.Type type = createWildcard((WildcardType) t);
			return type;
		} else {
			throw new UnsupportedOperationException(t.getTypeName());
		}
	}

	private com.github.javaparser.ast.type.Type createWildcard(WildcardType t) {
		Type[] upper = t.getUpperBounds();
		Type[] lower = t.getLowerBounds();

		com.github.javaparser.ast.type.ReferenceType exts = null;
		com.github.javaparser.ast.type.ReferenceType supers = null;

		if (upper.length > 0) {
			exts = (ReferenceType) createType(upper[0]);
		}
		if (lower.length > 0) {
			supers = (ReferenceType) createType(lower[0]);
		}
		return new com.github.javaparser.ast.type.WildcardType(exts, supers, new NodeList<>());
	}

	private com.github.javaparser.ast.type.Type createGenericArray(GenericArrayType t) {
		Type component = t.getGenericComponentType();
		com.github.javaparser.ast.type.Type astType = createType(component);
		return new ArrayType(astType);
	}

	private com.github.javaparser.ast.type.Type createParameterized(ParameterizedType t) {
		return createType((Class<?>) t.getRawType(), t.getActualTypeArguments());
	}

	public ClassOrInterfaceType createClassType(Class<?> t) {
		addImport(t);
		return new ClassOrInterfaceType(null, t.getSimpleName());
	}

	public void addImport(Class<?> t) {
		if(t.isPrimitive()) {
			return;
		}
		Class<?> old = simpleNames.put(t.getSimpleName(), t);
		if (old!=null && old != t) {
			throw new UnsupportedOperationException(
					"not support duplicate simple name of classes." + t.getName() + " - " + old.getName());
		}
	}
	
	public CompilationUnit build() {
		for(Class<?> c:simpleNames.values()) {
			unit.addImport(c);
		}
		return unit;
	}

	public void setPackageDeclaration(String pkg) {
		unit.setPackageDeclaration(pkg);
	}

	public ClassOrInterfaceDeclaration addClass(String className) {
		return unit.addClass(className);
	}

	public Expression getExpr(String code) {
		ParseResult<? extends Expression> result = parser.parseExpression(code);
		if(result.isSuccessful()) {
			return result.getResult().get();
		}else {
			throw Exceptions.illegalArgument("Invalid express code: [{}]\nProblem:", code, result.getProblems());
		}
	}
}
