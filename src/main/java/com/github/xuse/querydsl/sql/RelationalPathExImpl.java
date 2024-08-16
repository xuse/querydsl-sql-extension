package com.github.xuse.querydsl.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.lambda.PathCache;
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
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.util.ReflectionUtils;
import com.querydsl.sql.Column;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;

/**
 * @param <T> the type of entity
 */
public class RelationalPathExImpl<T> extends RelationalPathBaseEx<T> {

	private static final long serialVersionUID = 1L;

	public RelationalPathExImpl(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata, schema, table);
	}

	public Constraint createCheck(String name, Expression<Boolean> checkExpression) {
		return super.createCheck(name, checkExpression);
	}

	/**
	 *  Create a normal constraint or index.
	 *  @param name constraint name, can be null
	 *  @param type The type of index/constraint
	 *  @param columns columns that in the constraint/index
	 *  @return Constraint
	 */
	public Constraint createConstraint(String name, ConstraintType type, Path<?>... columns) {
		return super.createConstraint(name, type, columns);
	}

	/**
	 *  Create an index, typically a B-Tree index. to create more types, use {@link #createConstraint(String, ConstraintType, Path...)}
	 *  @param name name of index.
	 *  @param columns columns in the index.
	 *  @return Constraint
	 */
	public Constraint createIndex(String name, Path<?>... columns) {
		return super.createConstraint(name, ConstraintType.KEY, columns);
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

	public <P extends Path<?>> P addMetadata(P path, ColumnMapping metadata) {
		return super.addMetadata(path, metadata);
	}

	public <P> PathMapping addMetadataDynamic(Path<P> expr, ColumnMetadata metadata) {
		PathMapping metadataExt = new PathMapping(expr, new DynamicField(expr), metadata);
		super.columnMetadata.put(expr, metadataExt);
		pathUpdate(expr, metadata);
		super.bindingsMap.put(expr.getMetadata().getName(), expr);
		return metadataExt;
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
		if(path instanceof RelationalPathEx) {
			return (RelationalPathEx<T>)path;
		}
		return generateForOriginal(path);
	}
	
	public static <T> RelationalPathExImpl<T> valueOf(RelationalPath<T> path) {
		if(path instanceof RelationalPathExImpl) {
			return (RelationalPathExImpl<T>)path;
		}
		if(path instanceof RelationalPathBaseEx) {
			return ((RelationalPathBaseEx<T>)path).clone();
		}
		return generateForOriginal(path);
	}

	public static <T> RelationalPathExImpl<T> valueOf(Class<T> beanType) {
		PathMetadata pm=PathMetadataFactory.forVariable(beanType.getSimpleName().toLowerCase());
		RelationalPathExImpl<T> t = new RelationalPathExImpl<>(beanType, pm, null, null);
		t.scanClassMetadata(()->{
			List<Path<?>> paths = new ArrayList<>();
			for(Field field:ReflectionUtils.getFields(beanType)) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				Column column=field.getAnnotation(Column.class);
				ColumnSpec anno=field.getAnnotation(ColumnSpec.class);
				if(column == null && anno==null) {
					continue;
				}
				Path<?> path=TypeUtils.createPathByType(field.getType(),field.getName(),t);
				paths.add(path);
			}
			if(paths.isEmpty()) {
				throw Exceptions.illegalArgument("Invalid entity bean {}, please add @ColumnSpec annotation to the fields in this class.", beanType.getName());
			}
			return paths;
		});
		return t;
	}

	private static <T> RelationalPathExImpl<T> generateForOriginal(RelationalPath<T> path) {
		//Use table cache to accelerate.
		return PathCache.compute(path.getType(), () -> {
			Class<? extends T> beanType = path.getType();
			RelationalPathExImpl<T> t = new RelationalPathExImpl<>(beanType, path.getMetadata(), path.getSchemaName(),
					path.getTableName());
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
	}	
}
