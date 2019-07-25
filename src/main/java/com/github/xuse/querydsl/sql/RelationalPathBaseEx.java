package com.github.xuse.querydsl.sql;

import static com.google.common.collect.ImmutableList.copyOf;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.QBeanEx;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
public class RelationalPathBaseEx<T> extends BeanPath<T> implements RelationalPath<T>,IRelationPathEx {
	private static final long serialVersionUID = -3351359519644416084L;
	@Nullable
	private PrimaryKey<T> primaryKey;

	private final Map<Path<?>, ColumnMetadata> columnMetadata = Maps.newLinkedHashMap();

	private final List<ForeignKey<?>> foreignKeys = Lists.newArrayList();

	private final List<ForeignKey<?>> inverseForeignKeys = Lists.newArrayList();

	private final String schema, table;

	private final SchemaAndTable schemaAndTable;

	private transient QBeanEx<T> projection;

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

	protected <P extends Path<?>> P addMetadata(P path, ColumnMetadata metadata) {
		columnMetadata.put(path, metadata);
		return path;
	}

	@Override
	public NumberExpression<Long> count() {
		if (count == null) {
			if (primaryKey != null) {
				count = Expressions.numberOperation(Long.class, Ops.AggOps.COUNT_AGG,
						primaryKey.getLocalColumns().get(0));
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
				countDistinct = Expressions.numberOperation(Long.class, Ops.AggOps.COUNT_DISTINCT_AGG,
						primaryKey.getLocalColumns().get(0));
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
			BooleanExpression pred = Expressions.booleanOperation(op, pk1.getLocalColumns().get(i),
					pk2.getLocalColumns().get(i));
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
		return columnMetadata.get(column);
	}
}
