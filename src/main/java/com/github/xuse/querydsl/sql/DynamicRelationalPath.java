package com.github.xuse.querydsl.sql;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.sql.column.ColumnBuilderHandler;
import com.github.xuse.querydsl.sql.column.PathMapping;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QTuple;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.ColumnMetadata;

public class DynamicRelationalPath extends RelationalPathExImpl<Tuple>{
	private static final long serialVersionUID = -8971780600361004636L;
	
	public DynamicRelationalPath(String schema, String table) {
		super(Tuple.class, PathMetadataFactory.forVariable(table), schema, table);
	}

	public DynamicRelationalPath(String abbreviation,String schema, String table) {
		super(Tuple.class, PathMetadataFactory.forVariable(abbreviation), schema, table);
	}
	
	/**
	 * 在表模型中增加一列
	 * @param <A> the class of the column type
	 * @param column metadata of the column
	 * @param type class of type
	 * @return ColumnBuilderHandler
	 */
	@SuppressWarnings("unchecked")
	public <A> ColumnBuilderHandler<A,Path<A>> addColumn(Class<A> type,ColumnMetadata column) {
		String name=column.getName();
		Path<A> path;
		if(String.class==type){
			path = (Path<A>) createString(name);
		} else if (Number.class.isAssignableFrom(type)) {
			path = (Path<A>) createNumber0(name,type);
		} else if (Date.class.isAssignableFrom(type)) {
			path = (Path<A>) createDateTime0(name,type);
		}else {
			path = createSimple(name, type);
		}
		PathMapping cb= addMetadataDynamic(path, column);
		return new ColumnBuilderHandler<>(cb,path);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	 private DateTimePath<? extends Date> createDateTime0(String name, Class type) {
		return createDateTime(name, type);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private NumberPath<? extends Number> createNumber0(String property, Class type){
		return createNumber(property, type);
	 }
	 

	@Override
	public QTuple getProjection() {
		List<Path<?>> list=this.getColumns();
		return Projections.tuple(list.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
	}

	/**
	 * 从数组构造一个实体对象。
	 * @param objects values
	 * @return Tuple
	 */
	public Tuple newTuple(Object... objects) {
		return getProjection().newInstance(objects);
	}

	/**
	 * 从String Map创造一个实体对象。
	 * @param map Map的key是字段名，value是数值。
	 * @return Tuple
	 */
	public Tuple newTuple(Map<String,Object> map) {
		List<Path<?>> list=this.getColumns();
		int len=list.size();
		Object[] values=new Object[len];
		for(int i=0;i<len;i++) {
			Path<?> p=list.get(i);
			Object value=map.get(p.getMetadata().getName());
			values[i]=value;
		}
		return newTuple(values); 
	}

	@SuppressWarnings("unchecked")
	public <T> SimpleExpression<T> path(String string, Class<T> ignoredClass1) {
		Path<?> path=getColumn(string);
		return (SimpleExpression<T>) path;
	}
}

