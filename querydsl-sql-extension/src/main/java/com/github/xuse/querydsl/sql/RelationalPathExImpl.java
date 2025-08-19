package com.github.xuse.querydsl.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.sql.column.AccessibleElement;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.PathMapping;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.TypeUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.Column;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;

/**
 * @param <T> the type of entity
 */
public class RelationalPathExImpl<T> extends RelationalPathBaseEx<T> implements TablePath<T> {

	private static final long serialVersionUID = 1L;

	public RelationalPathExImpl(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata, schema, table);
	}

	public Constraint createCheck(String name, Expression<Boolean> checkExpression) {
		return super.createCheck(name, checkExpression);
	}

	/**
	 * Create a normal constraint or index.
	 * 
	 * @param name    constraint name, can be null
	 * @param type    The type of index/constraint
	 * @param columns columns that in the constraint/index
	 * @return Constraint
	 */
	public Constraint createConstraint(String name, ConstraintType type, Path<?>... columns) {
		return super.createConstraint(name, type, false, columns);
	}

	/**
	 * Create an index, typically a B-Tree index. to create more types, use
	 * {@link #createConstraint(String, ConstraintType, Path...)}
	 * 
	 * @param name    name of index.
	 * @param columns columns in the index.
	 * @return Constraint
	 */
	public Constraint createIndex(String name, Path<?>... columns) {
		return super.createConstraint(name, ConstraintType.KEY, false, columns);
	}

	public PrimaryKey<T> createPrimaryKey(Path<?>... columns) {
		return super.createPrimaryKey(columns);
	}

	public void setComment(String comment) {
		super.setComment(comment);
	}

	public void setCollate(Collate collate) {
		super.setCollate(collate);
	}

	public void setPartitionBy(PartitionBy partitionBy) {
		super.setPartitionBy(partitionBy);
	}

	public Constraint createCheck(String name, String checkExpression) {
		return super.createCheck(name, checkExpression);
	}

	/*
	 * This method is for internal use, do not call it from your application.
	 */
	public <P> PathMapping addMetadataDynamic(Path<P> path, ColumnMetadata cMetadata) {
		PathMapping metadata = new PathMapping(path, new DynamicField(path), cMetadata);
		addMetadata(path, metadata);
		clearColumnsCache();
		return metadata;
	}
	
	public ColumnMapping removeColumn(Path<?> path){
		ColumnMapping mapping=columnMetadata.remove(path);
		if (mapping != null) {
			bindingsMap.remove(path.getMetadata().getName());
			clearColumnsCache();
		}
		return mapping;
	}

	static class DynamicField implements AccessibleElement {
		private final Path<?> path;

		DynamicField(Path<?> path) {
			this.path = path;
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> clz) {
			return null;
		}

		@Override
		public Class<?> getType() {
			return path.getType();
		}

		@Override
		public String getName() {
			return path.getMetadata().getName();
		}

		@Override
		public void set(Object bean, Object value) {
		}

		@Override
		public Type getGenericType() {
			return path.getType();
		}
	}

	@SuppressWarnings("unchecked")
	public <A> Path<A> createPath(String property, Class<? super A> type) {
		return (Path<A>) TypeUtils.createPathByType(type, property, this);
	}

	public StringPath createString(String property) {
		return super.createString(property);
	}

	@Override
	public Collection<Constraint> getConstraints() {
		return constraints;
	}

	/*
	 * 内部使用，用于对齐原生querydsl的表模型和扩展后的表模型。
	 */
	public static <T> RelationalPathEx<T> toRelationPathEx(RelationalPath<T> path) {
		if (path instanceof RelationalPathEx) {
			return (RelationalPathEx<T>) path;
		}
		return generateForOriginal(path);
	}

	public static <T> RelationalPathExImpl<T> valueOf(RelationalPath<T> path) {
		if (path instanceof RelationalPathExImpl) {
			return (RelationalPathExImpl<T>) path;
		}
		if (path instanceof RelationalPathBaseEx) {
			return ((RelationalPathBaseEx<T>) path).clone();
		}
		return generateForOriginal(path);
	}

	public static <T> RelationalPathExImpl<T> valueOf(Class<T> beanType, String variable) {
		if (variable == null || variable.isEmpty()) {
			variable = beanType.getSimpleName().toLowerCase();
		}
		PathMetadata pm = PathMetadataFactory.forVariable(variable);
		RelationalPathExImpl<T> t = new RelationalPathExImpl<>(beanType, pm, null, null);
		t.scanClassMetadata(() -> {
			List<Path<?>> paths = new ArrayList<>();
			for (Field field : TypeUtils.getAllDeclaredFields(beanType)) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				Column column = field.getAnnotation(Column.class);
				ColumnSpec anno = field.getAnnotation(ColumnSpec.class);
				if (column == null && anno == null) {
					continue;
				}
				Path<?> path = TypeUtils.createPathByType(field.getType(), field.getName(), t);
				paths.add(path);
			}
			if (paths.isEmpty()) {
				throw Exceptions.illegalArgument("Invalid entity bean {}, please add @ColumnSpec annotation to the fields in this class.", beanType.getName());
			}
			return paths;
		});
		return t;
	}

	private static <T> RelationalPathExImpl<T> generateForOriginal(RelationalPath<T> path) {
		// Use table cache to accelerate.
		String variable = path.getMetadata().getName();
		RelationalPathEx<T> relation = PathCache.compute(path.getType(), variable, () -> {
			Class<? extends T> beanType = path.getType();
			RelationalPathExImpl<T> t = new RelationalPathExImpl<>(beanType, path.getMetadata(), path.getSchemaName(), path.getTableName());
			t.primaryKey = path.getPrimaryKey();
			for (Path<?> p : path.getColumns()) {
				ColumnMetadata meta = path.getMetadata(p);
				Assert.notNull(meta);
				t.addMetadata(p, meta);
			}
			t.foreignKeys.addAll(path.getForeignKeys());
			t.inverseForeignKeys.addAll(path.getInverseForeignKeys());
			t.initByClassAnnotation(beanType, null);
			return t;
		});
		if (relation instanceof RelationalPathExImpl) {
			return (RelationalPathExImpl<T>) relation;
		}
		return ((RelationalPathBaseEx<T>) path).clone();
	}

	@Override
	public StringPath get(StringLambdaColumn<T> column) {
		Pair<Class<?>, String> pair = PathCache.analysis(column);
		if (pair.getFirst() == this.getType()) {
			// 应当相同
			Path<?> p = this.getColumn(pair.getSecond());
			return (StringPath) p;
		} else {
			throw Exceptions.illegalArgument("Lambda value is {}, this type is{}", pair, getType());
		}
	}
	
	public StringPath getString(String pathName) {
		Path<?> p = this.getColumn(pathName);
		return (StringPath) p;
	}
	
	public NumberPath<?> getNumber(String pathName){
		Path<?> p = this.getColumn(pathName);
		return (NumberPath<?>) p;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Number & Comparable<C>> NumberPath<C> get(NumberLambdaColumn<T, C> column) {
		Pair<Class<?>, String> pair = PathCache.analysis(column);
		if (pair.getFirst() == this.getType()) {
			// 应当相同
			Path<?> p = this.getColumn(pair.getSecond());
			return (NumberPath<C>) p;
		} else {
			throw Exceptions.illegalArgument("Lambda value is {}, this type is{}", pair, getType());
		}
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Comparable<C>> ComparableExpression<C> get(LambdaColumn<T, C> column) {
		Pair<Class<?>, String> pair = PathCache.analysis(column);
		if (pair.getFirst() == this.getType()) {
			// 应当相同
			Path<?> p = this.getColumn(pair.getSecond());
			return (ComparableExpression<C>) p;
		} else {
			throw Exceptions.illegalArgument("Lambda value is {}, this type is{}", pair, getType());
		}
	}
}
