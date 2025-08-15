package io.github.xuse.querydsl.sql.extension.code.generate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.querydsl.core.types.dsl.Expressions;

public class PropertyPathCreater {
	
	static interface PathGenerator{
		ClassOrInterfaceType pathType(Class<?> type); 
		MethodCallExpr pathValue(Class<?> type);
	}
	
	//	public final ArrayPath<String[],String> ext111 = createArray("ext", String[].class);
	private static final PathGenerator ArrayCreator=new PathGenerator(){
		@Override
		public ClassOrInterfaceType pathType(Class<?> type) {
			
			
			ClassOrInterfaceType type=new ClassOrInterfaceType()
			
			return new ClassOrInterfaceType(null,new SimpleName("ArrayPath"),new NodeList<Type>());
		}
		@Override
		public MethodCallExpr pathValue(Class<?> type) {
			// TODO Auto-generated method stub
			return null;
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

		PathCreators.put(Long.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Short.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Integer.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Float.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Double.TYPE, PrimitiveNumberCreator);

		PathCreators.put(java.sql.Date.class, DateCreator);
		PathCreators.put(LocalDate.class, (a, b) -> Expressions.datePath(a.asSubclass(LocalDate.class), b));

		PathCreators.put(java.sql.Time.class, TimeCreator);
		PathCreators.put(LocalTime.class, (a, b) ->Expressions.timePath(a.asSubclass(LocalTime.class), b));

		
		PathCreators.put(Instant.class, (a, b) -> Expressions.dateTimePath(a.asSubclass(Instant.class), b));
		PathCreators.put(java.util.Date.class, DateTimeCreator);
		PathCreators.put(java.sql.Timestamp.class, DateTimeCreator);
		PathCreators.put(ZonedDateTime.class, (a, b) -> Expressions.dateTimePath(a.asSubclass(ZonedDateTime.class), b));
		PathCreators.put(LocalDateTime.class, (a, b) -> Expressions.dateTimePath(a.asSubclass(LocalDateTime.class), b));

		PathCreators.put(Boolean.class, BooleanCreator);
		PathCreators.put(Boolean.TYPE, BooleanCreator);
	}

	public static PathGenerator getGenerator(Class<?> type) {
		// TODO Auto-generated method stub
		return null;
	}
}
