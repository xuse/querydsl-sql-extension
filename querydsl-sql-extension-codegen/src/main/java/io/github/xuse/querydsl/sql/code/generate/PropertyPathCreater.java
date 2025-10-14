package io.github.xuse.querydsl.sql.code.generate;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.xuse.querydsl.lambda.BooleanLambdaColumn;
import com.github.xuse.querydsl.lambda.DateLambdaColumn;
import com.github.xuse.querydsl.lambda.DateTimeLambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.SimpleLambdaColumn;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.lambda.TimeLambdaColumn;
import com.github.xuse.querydsl.util.lang.Primitives;
import com.querydsl.core.types.dsl.ArrayPath;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TimePath;

import io.github.xuse.querydsl.sql.code.generate.util.GenericTypes;

public class PropertyPathCreater {
	static interface PathGenerator{
		ClassOrInterfaceType pathType(Type fieldType,CompilationUnitBuilder builder); 
		ClassOrInterfaceType lambdaType(Type type,ClassOrInterfaceType bean,CompilationUnitBuilder builder);
		MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder);
	}
	
	private static final PathGenerator ArrayCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			return builder.createType(ArrayPath.class, type, ((Class<?>)type).getComponentType());
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			MethodCallExpr createCall = new MethodCallExpr("createArray", new StringLiteralExpr(name),
					new ClassExpr(builder.createType(type)));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			return builder.createType(SimpleLambdaColumn.class, bean,builder.createType(type));
		}
	};
	
	private static final PathGenerator StringCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			return builder.createClassType(StringPath.class);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			MethodCallExpr createCall = new MethodCallExpr("createString", new StringLiteralExpr(name));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type fieldType,ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			return builder.createType(StringLambdaColumn.class, bean);
		}
	};
	
	private static final PathGenerator NumberCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			if(type instanceof Class) {
				type=Primitives.toWrapperClass((Class<?>)type);
			}
			return builder.createType(NumberPath.class, type);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			MethodCallExpr createCall = new MethodCallExpr("createNumber", new StringLiteralExpr(name),
					new ClassExpr(builder.createType(type)));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			if(type instanceof Class) {
				type=Primitives.toWrapperClass((Class<?>)type);
			}
			return builder.createType(NumberLambdaColumn.class, bean, builder.createType(type));
		}
	};
	
	private static final PathGenerator DateTimeCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			return builder.createType(DateTimePath.class, type);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			MethodCallExpr createCall = new MethodCallExpr("createDateTime", new StringLiteralExpr(name),
					new ClassExpr(builder.createType(type)));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			return builder.createType(DateTimeLambdaColumn.class, bean, builder.createType(type));
		}
	};
	
	private static final PathGenerator DateCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			return builder.createType(DatePath.class, type);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			MethodCallExpr createCall = new MethodCallExpr("createDate", new StringLiteralExpr(name),
					new ClassExpr(builder.createType(type)));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			return builder.createType(DateLambdaColumn.class, bean, builder.createType(type));
		}
	};
	
	private static final PathGenerator TimeCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			return builder.createType(TimePath.class, type);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			MethodCallExpr createCall = new MethodCallExpr("createTime", new StringLiteralExpr(name),
					new ClassExpr(builder.createType(type)));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			return builder.createType(TimeLambdaColumn.class, bean, builder.createType(type));
		}
	};
	
	private static final PathGenerator BooleanCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			if(type instanceof Class) {
				type=Primitives.toWrapperClass((Class<?>)type);
			}
			return builder.createClassType(BooleanPath.class);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			return new MethodCallExpr("createBoolean", new StringLiteralExpr(name));
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			if(type instanceof Class) {
				type=Primitives.toWrapperClass((Class<?>)type);
			}
			return builder.createType(BooleanLambdaColumn.class, bean);
		}
	};
	
	private static final PathGenerator EnumCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			return builder.createType(EnumPath.class, type);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			MethodCallExpr createCall = new MethodCallExpr("createEnum", new StringLiteralExpr(name),
					new ClassExpr(builder.createType(type)));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			return builder.createType(LambdaColumn.class, bean, builder.createType(type));
		}
	};
	
	private static final PathGenerator SimpleCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Type type, CompilationUnitBuilder builder) {
			return builder.createType(SimplePath.class, type);
		}
		@Override
		public MethodCallExpr pathValue(Type type, String name, CompilationUnitBuilder builder) {
			Class<?> raw=GenericTypes.getRawClass(type);
			MethodCallExpr createCall = new MethodCallExpr("createSimple", new StringLiteralExpr(name),
					new ClassExpr(builder.createType(raw)));
			return createCall;
		}
		@Override
		public ClassOrInterfaceType lambdaType(Type type, ClassOrInterfaceType bean, CompilationUnitBuilder builder) {
			Class<?> clz=GenericTypes.getRawClass(type);
			if(Comparable.class.isAssignableFrom(clz)) {
				return builder.createType(LambdaColumn.class, bean,builder.createType(type));
			}else {
				return builder.createType(SimpleLambdaColumn.class, bean,builder.createType(type));
			}
		}
	};
	
	private static final Map<Class<?>,PathGenerator> PathCreators = new HashMap<>();
	
	static {
		PathCreators.put(byte[].class, ArrayCreator);
		PathCreators.put(Byte[].class, ArrayCreator);
		PathCreators.put(long[].class, ArrayCreator);
		PathCreators.put(Long[].class, ArrayCreator);
		PathCreators.put(float[].class, ArrayCreator);
		PathCreators.put(Float[].class, ArrayCreator);
		PathCreators.put(double[].class, ArrayCreator);
		PathCreators.put(Double[].class, ArrayCreator);
		PathCreators.put(short[].class, ArrayCreator);
		PathCreators.put(Short[].class, ArrayCreator);
		PathCreators.put(char[].class, ArrayCreator);
		PathCreators.put(Character[].class, ArrayCreator);
		PathCreators.put(boolean[].class, ArrayCreator);
		PathCreators.put(Boolean[].class, ArrayCreator);
		PathCreators.put(int[].class, ArrayCreator);
		PathCreators.put(Integer[].class, ArrayCreator);
		
		PathCreators.put(String.class, StringCreator);
		PathCreators.put(CharSequence.class, StringCreator);

		PathCreators.put(Long.class, NumberCreator);
		PathCreators.put(Short.class, NumberCreator);
		PathCreators.put(Integer.class, NumberCreator);
		PathCreators.put(Float.class, NumberCreator);
		PathCreators.put(Double.class, NumberCreator);
		
		PathCreators.put(BigInteger.class, NumberCreator);
		PathCreators.put(BigDecimal.class, NumberCreator);

		PathCreators.put(Long.TYPE, NumberCreator);
		PathCreators.put(Short.TYPE, NumberCreator);
		PathCreators.put(Integer.TYPE, NumberCreator);
		PathCreators.put(Float.TYPE, NumberCreator);
		PathCreators.put(Double.TYPE, NumberCreator);

		PathCreators.put(java.sql.Date.class, DateCreator);
		PathCreators.put(LocalDate.class,DateCreator);

		PathCreators.put(java.sql.Time.class, TimeCreator);
		PathCreators.put(LocalTime.class, TimeCreator);
		
		PathCreators.put(Instant.class, DateTimeCreator);
		PathCreators.put(java.util.Date.class, DateTimeCreator);
		PathCreators.put(java.sql.Timestamp.class, DateTimeCreator);
		PathCreators.put(ZonedDateTime.class, DateTimeCreator);
		PathCreators.put(LocalDateTime.class, DateTimeCreator);

		PathCreators.put(Boolean.class, BooleanCreator);
		PathCreators.put(Boolean.TYPE, BooleanCreator);
	}

	public static PathGenerator getGenerator(Class<?> type) {
		PathGenerator g=PathCreators.get(type);
		if(g!=null) {
			return g;
		}
		if(type.isEnum()) {
			return EnumCreator;
		}
		return SimpleCreator;
	}
}
