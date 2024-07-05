package com.github.xuse.querydsl.sql;

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.github.xuse.querydsl.sql.column.Accessor;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.PathMapping;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.querydsl.core.types.ConstraintType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;

/**
 * @param <T>
 */
public class ClonedRelationalPathBaseEx<T> extends RelationalPathBaseEx<T> {
	private static final long serialVersionUID = 1L;

	public ClonedRelationalPathBaseEx(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata, schema, table);
	}

	public Constraint createCheck(String name, Expression<Boolean> checkExpression) {
		return super.createCheck(name, checkExpression);
	}

	/**
	 * Create a normal constraint or index.
	 * @param name
	 * @param type The type of a index/constraint
	 * @param columns
	 * @return Constraint
	 */
	public Constraint createConstraint(String name, ConstraintType type, Path<?>... columns) {
		return super.createConstraint(name, type, columns);
	}
	
	/**
	 * Create a index, typically a B-Tree index. to create more types, use {@link #createConstraint(String, ConstraintType, Path...)}
	 * @param name
	 * @param columns
	 * @return Constraint
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
	
	public Constraint createCheck(String name, String checkExpression)  {
		return super.createCheck(name, checkExpression);
	}
	
	public <P extends Path<?>> P addMetadata(P path, ColumnMapping metadata) {
		return super.addMetadata(path, metadata);
	}
	
	public <P> PathMapping addMetadataDynamic(Path<P> expr, ColumnMetadata metadata){
		PathMapping metadataExt=new PathMapping(expr, new DynamicAccessor(expr),metadata);
		super.columnMetadata.put(expr, metadataExt);
		pathUpdate(expr, metadata);
		super.bindingsMap.put(expr.getMetadata().getName(), expr);
		return metadataExt;
	}
	
	static class DynamicAccessor implements Accessor{
		private final Path<?> path;
		DynamicAccessor(Path<?> path){
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
	
	public <A> SimplePath<A> createSimple(String property, Class<? super A> type){
		return super.createSimple(property, type);
	}
    
	public StringPath createString(String property) {
        return super.createString(property);
    }

	@Override
	public Collection<Constraint> getConstraints() {
		return constraints;
	}
}
