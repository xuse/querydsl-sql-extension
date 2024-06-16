package com.github.xuse.querydsl.sql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.annotation.dbdef.Check;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.sql.column.AbstractColumnMetadataEx;
import com.github.xuse.querydsl.sql.column.ColumnBuilder;
import com.github.xuse.querydsl.sql.column.PathMapping;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.QBeanEx;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.Primitives;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.ConstraintType;
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
public class RelationalPathBaseEx<T> extends BeanPath<T> implements RelationalPathEx<T> {
	private static final long serialVersionUID = -3351359519644416084L;

	private PrimaryKey<T> primaryKey;

	private final Map<Path<?>, PathMapping> columnMetadata = new LinkedHashMap<>();
	
	/**
	 * path name <-> path
	 */
	private final Map<String, Path<?>> bindingsMap = new HashMap<>();

	private final List<ForeignKey<?>> foreignKeys = new ArrayList<>();

	private final List<ForeignKey<?>> inverseForeignKeys = new ArrayList<>();

	private final List<Constraint> constraints = new ArrayList<>();

	private final String schema, table;

	private final SchemaAndTable schemaAndTable;

	private transient volatile QBeanEx<T> projection;

	private transient NumberExpression<Long> count, countDistinct;
	
	private String comment;
	
	private Collate collate;

	public RelationalPathBaseEx(Class<? extends T> type, String variable, String schema, String table) {
		this(type, PathMetadataFactory.forVariable(variable), schema, table);
	}

	public RelationalPathBaseEx(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata);
		TableSpec spec=type.getAnnotation(TableSpec.class);
		if(spec!=null) {
			if(StringUtils.isNotEmpty(spec.name())) {
				table=spec.name();
			}
			if(StringUtils.isNotEmpty(spec.schema())) {
				schema=spec.schema();
			}
		}
		this.schema = schema;
		this.table = table;
		this.schemaAndTable = new SchemaAndTable(schema, table);
	}
	

	/**
	 * 创建check类型的约束
	 * @param name 名称。在某些不支持指定CHECK名称的数据库（如MySQL 5.x）上会被忽略。
	 * @param checkExpression 表达式。两端无需加上括号。
	 * @return
	 */
	protected Constraint createCheck(String name, Expression<Boolean> checkExpression) {
		Constraint constraint=new Constraint();
		constraint.setName(name);
		constraint.setConstraintType(ConstraintType.CHECK);
		constraint.setCheckClause(checkExpression);
		this.constraints.add(constraint);
		return constraint;
	}

	/**
	 * Create a check constraint.
	 * @param name 名称。在某些不支持指定CHECK名称的数据库（如MySQL 5.x）上会被忽略。
	 * @param checkExpression 表达式。两端无需加上括号。
	 * @return Constraint
	 */
	protected Constraint createCheck(String name, String checkExpression) {
		return createCheck(name, Expressions.template(Boolean.class, checkExpression));
	}
	
	/**
	 * Create a normal constraint or index.
	 * @param name
	 * @param type
	 * @param columns
	 * @return Constraint
	 */
	protected Constraint createConstraint(String name, ConstraintType type, Path<?>... columns) {
		if(type==ConstraintType.PRIMARY_KEY) {
			throw Exceptions.unsupportedOperation("please use #createPrimaryKey() method for columns",Arrays.toString(columns));
		}
		Constraint constraint=new Constraint();
		constraint.setName(name);
		constraint.setConstraintType(type);
		constraint.setPaths(Arrays.asList(columns));
		this.constraints.add(constraint);
		return constraint;
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
		ForeignKey<F> foreignKey = new ForeignKey<F>(this, new ArrayList<>(local), new ArrayList<>(foreign));
		foreignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createInvForeignKey(Path<?> local, String foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<F>(this, local, foreign);
		inverseForeignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createInvForeignKey(List<? extends Path<?>> local, List<String> foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<F>(this, new ArrayList<>(local), new ArrayList<>(foreign));
		inverseForeignKeys.add(foreignKey);
		return foreignKey;
	}

	/**
	 * 定义一个数据库列。可以指定列的各个属性。
	 * 
	 * @implNote
	 * 以下是一些使用的注意点
	 *           <ul>
	 *           <li>对于Timestamp数据类型(包括MySQL的datetime)，其小数位数精度使用 {@link ColumnMetadata#withSize(int)}, 而不是withDigits()。</li>
	 *           </ul>
	 * 
	 * @param <P>
	 * @param expr
	 * @param metadata
	 * @return
	 */
	protected <P> ColumnBuilder<P> addMetadata(Path<P> expr, ColumnMetadata metadata) {
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
//		boolean isPk=this.getPrimaryKey()!=null && this.getPrimaryKey().getLocalColumns().contains(expr);
		PathMapping metadataExt=new PathMapping(expr, field, metadata);
		columnMetadata.put(expr, metadataExt);
		bindingsMap.put(expr.getMetadata().getName(), expr);
		return new ColumnBuilder<P>(metadataExt);
	}

	protected <P extends Path<?>> P addMetadata(P path, PathMapping metadata) {
		columnMetadata.put(path, metadata);
		bindingsMap.put(path.getMetadata().getName(), path);
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
		return new ArrayList<>(this.columnMetadata.keySet());
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
		AbstractColumnMetadataEx metadata = columnMetadata.get(column);
		return metadata == null ? null : metadata.getColumn();
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
				if(!Path.class.isAssignableFrom(field.getType())) {
					continue;
				}
				Path<?> path = (Path<?>) field.get(this);
				this.addMetadata(path, builderMetadata(beanType, path, field));
			}
			initByClassAnnotation(beanType,QClz);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private PathMapping builderMetadata(Class<? extends T> beanType, Path<?> path, Field metadataField) {
		while (!beanType.equals(Object.class)) {
			try {
				Field field = beanType.getDeclaredField(path.getMetadata().getName());
				field.setAccessible(true);
				if (!isAssignableFrom(field.getType(), path.getType())) {
					typeMismatch(field.getType(), path);
				}
				
				String columnName = path.getMetadata().getName().toUpperCase();
				//解析QueryDSL的名称注解 
				com.querydsl.sql.Column c = field.getAnnotation(com.querydsl.sql.Column.class);
				{
					if (c!=null && StringUtils.isNotEmpty(c.value())) {
						columnName = c.value();
					}
				}
				ColumnSpec columnSpec=field.getAnnotation(ColumnSpec.class);
				if (columnSpec!=null) {
					if(StringUtils.isNotEmpty(columnSpec.name())) {
						columnName = columnSpec.name();	
					}
				}
				ColumnMetadata column=ColumnMetadata.named(columnName);
				if(columnSpec!=null) {
					column=column.ofType(calcJdbcType(columnSpec.type(),field)).withSize(columnSpec.size()).withDigits(columnSpec.digits());
					if(!columnSpec.nullable()) {
						column=column.notNull();
					}
				}
				PathMapping columnMapping = new PathMapping(path, field, column);
				return columnMapping;
			} catch (SecurityException e) {
				throw Exceptions.toRuntime(e);
			} catch (NoSuchFieldException e) {
				throw Exceptions.toRuntime(e);
			}
		}
		throw new IllegalArgumentException("Not found field [" + path.getMetadata().getName() + "] in bean " + beanType.getName());
	}

	private int calcJdbcType(int sqlType, Field field) {
		if (sqlType != Types.NULL) {
			return sqlType;
		}
		Class<?> type=field.getType();
		switch(type.getName()) {
		case "int":
		case "java.lang.Integer":
			return Types.INTEGER;
		case "java.lang.Long":
			return Types.BIGINT;
		case "java.lang.Double":
			return Types.DOUBLE;
		case "java.lang.Float":
			return Types.FLOAT;
		case "java.lang.String":
			return Types.VARCHAR;
		case "java.sql.Date":
		case "java.time.LocalDate":
			return Types.DATE; 
		case "java.util.Date":
		case "java.sql.Timestamp":
			return Types.TIMESTAMP;
		case "java.sql.Time":
			return Types.TIME;
		case "java.time.LocalTime":
			return Types.TIME_WITH_TIMEZONE;
		case "java.time.LocalDateTime":
			return Types.TIME_WITH_TIMEZONE;
		}
		throw Exceptions.illegalArgument("Please assign the jdbc data type of field {}", field);
	}

	private void initByClassAnnotation(Class<? extends T> beanType, Class<?> qClz) {
		TableSpec spec=qClz.getAnnotation(TableSpec.class);
		if(spec==null) {
			spec=beanType.getAnnotation(TableSpec.class);
		}
		if(spec!=null) {
			if(StringUtils.isNotEmpty(spec.collate())) {
				this.setCollate(Collate.findValueOf(spec.collate()));
			}
			if(spec.primaryKeyPath().length>0) {
				List<Path<?>> paths=new ArrayList<>();
				for(String n:spec.primaryKeyPath()) {
					Path<?> path=findPathByName(n);
					if(path==null) {
						throw Exceptions.illegalArgument("Invalid primary key column {} on class {}", n, beanType);
					}
					paths.add(path);
				}
				this.createPrimaryKey(paths.toArray(new Path[paths.size()]));
			}
			for(com.github.xuse.querydsl.annotation.dbdef.Key k:spec.keys()) {
				List<Path<?>> paths=new ArrayList<>();
				for(String n:k.path()) {
					Path<?> path=findPathByName(n);
					if(path==null) {
						throw Exceptions.illegalArgument("Invalid key=[{}] column {} on class {}",k.name(), n, beanType);
					}
					paths.add(path);
				}
				this.createConstraint(k.name(), k.type(), paths.toArray(new Path[paths.size()]));
			}
			for(Check c:spec.checks()) {
				this.createCheck(c.name(), c.value());
			}
		}
		Comment comment=qClz.getAnnotation(Comment.class);
		if(comment==null) {
			comment=beanType.getAnnotation(Comment.class);
		}
		if(comment!=null) {
			this.setComment(comment.value());
		}
	}

	private Path<?> findPathByName(String n) {
		for(Path<?> p:getColumns()) {
			if(p.getMetadata().getName().equalsIgnoreCase(n)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Path<?> getColumn(String name) {
		return bindingsMap.get(name);
	}

	private static boolean isAssignableFrom(Class<?> cl1, Class<?> cl2) {
		return normalize(cl1).isAssignableFrom(normalize(cl2));
	}

	private static Class<?> normalize(Class<?> cl) {
		return cl.isPrimitive() ? Primitives.toWrapperClass(cl) : cl;
	}

	protected void typeMismatch(Class<?> type, Expression<?> expr) {
		final String msg = expr.getType().getName() + " is not compatible with " + type.getName();
		throw new IllegalArgumentException(msg);
	}

	@Override
	public PathMapping getColumnMetadata(Path<?> path) {
		return columnMetadata.get(path);
	}

	@Override
	public Collection<Constraint> getConstraints() {
		return Collections.unmodifiableCollection(constraints);
	}
	
	protected void setComment(String comment) {
		this.comment = comment;
	}

	protected void setCollate(Collate collate) {
		this.collate=collate;
	}

	public String getComment() {
		return comment;
	}

	public Collate getCollate() {
		return collate;
	}
}
