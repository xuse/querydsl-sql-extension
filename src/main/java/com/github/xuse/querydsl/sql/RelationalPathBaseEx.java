package com.github.xuse.querydsl.sql;

import static com.google.common.collect.ImmutableList.copyOf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.xuse.querydsl.sql.column.ColumnMetadataExt;
import com.github.xuse.querydsl.sql.column.MetadataBuilder;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.QBeanEx;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * 覆盖QueryDSL生成的表元数据的一些默认行为，建议让生成类继承本类.
 * 
 * @author jiyi
 * 
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class RelationalPathBaseEx<T> extends BeanPath<T> implements RelationalPath<T>, IRelationPathEx {
	private static final long serialVersionUID = -3351359519644416084L;
	@Nullable
	private PrimaryKey<T> primaryKey;

	private final Map<Path<?>, ColumnMetadataExt> columnMetadata = new LinkedHashMap<>();

	private final List<ForeignKey<?>> foreignKeys = Lists.newArrayList();

	private final List<ForeignKey<?>> inverseForeignKeys = Lists.newArrayList();

	private final String schema, table;

	private final SchemaAndTable schemaAndTable;

	private transient volatile QBeanEx<T> projection;

	private transient NumberExpression<Long> count, countDistinct;

	public RelationalPathBaseEx(Class<? extends T> type, String variable, String schema, String table) {
		this(type, PathMetadataFactory.forVariable(variable), schema, table);
	}

	public RelationalPathBaseEx(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata);
		this.schema = schema;
		this.table = table;
		this.schemaAndTable = new SchemaAndTable(schema, table);
	}

	protected PrimaryKey<T> createPrimaryKey(Path<?>... columns) {
		primaryKey = new PrimaryKey<T>(this, columns);
		return primaryKey;
	}

	protected <F> ForeignKey<F> createForeignKey(Path<?> local, String foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<F>(this, local, foreign);
		foreignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createForeignKey(List<? extends Path<?>> local, List<String> foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<F>(this, copyOf(local), copyOf(foreign));
		foreignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createInvForeignKey(Path<?> local, String foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<F>(this, local, foreign);
		inverseForeignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createInvForeignKey(List<? extends Path<?>> local, List<String> foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<F>(this, copyOf(local), copyOf(foreign));
		inverseForeignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <P extends Path<?>> ColumnMetadataExt addMetadata(P expr, ColumnMetadata metadata) {
		Class<?> beanType = super.getType();
		Field field = null;
		while (!beanType.equals(Object.class)) {
			try {
				field = beanType.getDeclaredField(expr.getMetadata().getName());
				field.setAccessible(true);
				if (!isAssignableFrom(field.getType(), expr.getType())) {
					typeMismatch(field.getType(), expr);
				}
				break;
			} catch (SecurityException e) {
				// do nothing
			} catch (NoSuchFieldException e) {
				beanType = beanType.getSuperclass();
			}
		}
		Assert.notNull(field, "Can't find field " + expr.getMetadata().getName() + " in class " + super.getType().getName());
		ColumnMetadataExt metadataExt=new ColumnMetadataExt(field, metadata);
		columnMetadata.put(expr, metadataExt);
		return metadataExt;
	}

	protected <P extends Path<?>> P addMetadata(P path, ColumnMetadataExt metadata) {
		columnMetadata.put(path, metadata);
		return path;
	}

	@Override
	public NumberExpression<Long> count() {
		if (count == null) {
			if (primaryKey != null) {
				count = Expressions.numberOperation(Long.class, Ops.AggOps.COUNT_AGG, primaryKey.getLocalColumns().get(0));
			} else {
				throw new IllegalStateException("No count expression can be created");
			}
		}
		return count;
	}

	@Override
	public NumberExpression<Long> countDistinct() {
		if (countDistinct == null) {
			if (primaryKey != null) {
				// TODO handle multiple column primary keys properly
				countDistinct = Expressions.numberOperation(Long.class, Ops.AggOps.COUNT_DISTINCT_AGG, primaryKey.getLocalColumns().get(0));
			} else {
				throw new IllegalStateException("No count distinct expression can be created");
			}
		}
		return countDistinct;
	}

	/**
	 * Compares the two relational paths using primary key columns
	 *
	 * @param right rhs of the comparison
	 * @return this == right
	 */
	@Override
	public BooleanExpression eq(T right) {
		if (right instanceof RelationalPath) {
			return primaryKeyOperation(Ops.EQ, primaryKey, ((RelationalPath) right).getPrimaryKey());
		} else {
			return super.eq(right);
		}
	}

	/**
	 * Compares the two relational paths using primary key columns
	 *
	 * @param right rhs of the comparison
	 * @return this == right
	 */
	@Override
	public BooleanExpression eq(Expression<? super T> right) {
		if (right instanceof RelationalPath) {
			return primaryKeyOperation(Ops.EQ, primaryKey, ((RelationalPath) right).getPrimaryKey());
		} else {
			return super.eq(right);
		}
	}

	/**
	 * Compares the two relational paths using primary key columns
	 *
	 * @param right rhs of the comparison
	 * @return this != right
	 */
	@Override
	public BooleanExpression ne(T right) {
		if (right instanceof RelationalPath) {
			return primaryKeyOperation(Ops.NE, primaryKey, ((RelationalPath) right).getPrimaryKey());
		} else {
			return super.ne(right);
		}
	}

	/**
	 * Compares the two relational paths using primary key columns
	 *
	 * @param right rhs of the comparison
	 * @return this != right
	 */
	@Override
	public BooleanExpression ne(Expression<? super T> right) {
		if (right instanceof RelationalPath) {
			return primaryKeyOperation(Ops.NE, primaryKey, ((RelationalPath) right).getPrimaryKey());
		} else {
			return super.ne(right);
		}
	}

	private BooleanExpression primaryKeyOperation(Operator op, PrimaryKey<?> pk1, PrimaryKey<?> pk2) {
		if (pk1 == null || pk2 == null) {
			throw new UnsupportedOperationException("No primary keys available");
		}
		if (pk1.getLocalColumns().size() != pk2.getLocalColumns().size()) {
			throw new UnsupportedOperationException("Size mismatch for primary key columns");
		}
		BooleanExpression rv = null;
		for (int i = 0; i < pk1.getLocalColumns().size(); i++) {
			BooleanExpression pred = Expressions.booleanOperation(op, pk1.getLocalColumns().get(i), pk2.getLocalColumns().get(i));
			rv = rv != null ? rv.and(pred) : pred;
		}
		return rv;
	}

	@Override
	public QBeanEx<T> getProjection() {
		if (projection == null) {
			projection = ProjectionsAlter.createBeanProjection(this);
		}
		return projection;
	}

	@Override
	public BeanCodec getBeanCodec() {
		return getProjection().getBeanCodec();
	}

	public Path<?>[] all() {
		return columnMetadata.keySet().toArray(new Path<?>[columnMetadata.size()]);
	}

	@Override
	protected <P extends Path<?>> P add(P path) {
		return path;
	}

	@Override
	public List<Path<?>> getColumns() {
		return Lists.newArrayList(this.columnMetadata.keySet());
	}

	@Override
	public Collection<ForeignKey<?>> getForeignKeys() {
		return foreignKeys;
	}

	@Override
	public Collection<ForeignKey<?>> getInverseForeignKeys() {
		return inverseForeignKeys;
	}

	@Override
	public PrimaryKey<T> getPrimaryKey() {
		return primaryKey;
	}

	@Override
	public SchemaAndTable getSchemaAndTable() {
		return schemaAndTable;
	}

	@Override
	public String getSchemaName() {
		return schema;
	}

	@Override
	public String getTableName() {
		return table;
	}

	@Override
	public ColumnMetadata getMetadata(Path<?> column) {
		ColumnMetadataExt metadata = columnMetadata.get(column);
		return metadata == null ? null : metadata.get();
	}

	/**
	 * 读取Entity类上的JPA注解，生成元数据
	 */
	protected void scanClassMetadata() {
		Class<? extends T> beanType = super.getType();
		Class<?> QClz = this.getClass();
		try {
			for (Field field : QClz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				Path<?> path = (Path<?>) field.get(this);
				MetadataBuilder<?> metadata = getMetadataBuilder(beanType, path, field);
				this.addMetadata(path, metadata.build());
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private MetadataBuilder<?> getMetadataBuilder(Class<? extends T> beanClz, Path<?> expr, Field metadataField) {
		Class<?> beanType=beanClz;
		while (!beanType.equals(Object.class)) {
			try {
				Field field = beanType.getDeclaredField(expr.getMetadata().getName());
				field.setAccessible(true);
				if (!isAssignableFrom(field.getType(), expr.getType())) {
					typeMismatch(field.getType(), expr);
				}
				return new MetadataBuilder(field, expr, metadataField);
			} catch (SecurityException e) {
				// do nothing
			} catch (NoSuchFieldException e) {
				beanType = beanType.getSuperclass();
			}
		}
		throw new IllegalArgumentException("Not found field [" + expr.getMetadata().getName() + "] in bean " + beanType.getName());
	}
	
	
//	public ColumnTypeBuilder(Column col, java.lang.reflect.Field field, Class<?> treatJavaType, FieldAnnotationProvider fieldProvider) {
//		this.field = field;
//		this.javaType = treatJavaType;
//		this.fieldProvider = fieldProvider;
//		init(col);
//	}
//
//	private void init(Column col) {
//		generatedValue = GenerateTypeDef.create(fieldProvider.getAnnotation(javax.persistence.GeneratedValue.class));
//		version = fieldProvider.getAnnotation(javax.persistence.Version.class) != null;
//		lob = fieldProvider.getAnnotation(Lob.class) != null;
//		if (col != null) {
//			length = col.length();
//			precision = col.precision();
//			scale = col.scale();
//			nullable = col.nullable();
//			unique = col.unique();
//			if (col.columnDefinition().length() > 0) {
//				parseColumnDef(col.columnDefinition());
//			}
//		} else {
//			nullable = !javaType.isPrimitive();
//		}
//	}
	

	private static boolean isAssignableFrom(Class<?> cl1, Class<?> cl2) {
		return normalize(cl1).isAssignableFrom(normalize(cl2));
	}

	private static Class<?> normalize(Class<?> cl) {
		return cl.isPrimitive() ? Primitives.wrap(cl) : cl;
	}

	protected void typeMismatch(Class<?> type, Expression<?> expr) {
		final String msg = expr.getType().getName() + " is not compatible with " + type.getName();
		throw new IllegalArgumentException(msg);
	}

	@Override
	public ColumnMetadataExt getColumnMetadata(Path<?> path) {
		return columnMetadata.get(path);
	}
}
