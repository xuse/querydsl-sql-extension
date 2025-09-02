package com.github.xuse.querydsl.sql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.xuse.querydsl.annotation.InitializeData;
import com.github.xuse.querydsl.annotation.dbdef.Check;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.annotation.partition.HashPartition;
import com.github.xuse.querydsl.annotation.partition.ListPartition;
import com.github.xuse.querydsl.annotation.partition.RangePartition;
import com.github.xuse.querydsl.spring.core.resource.Util;
import com.github.xuse.querydsl.sql.column.ColumnBuilder;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.PathMapping;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.QBeanEx;
import com.github.xuse.querydsl.sql.partitions.HashPartitionBy;
import com.github.xuse.querydsl.sql.partitions.ListPartitionBy;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.github.xuse.querydsl.sql.partitions.RangePartitionBy;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.TypeUtils;
import com.github.xuse.querydsl.util.lang.Primitives;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.util.ReflectionUtils;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * 覆盖QueryDSL生成的表元数据的一些默认行为，建议让生成类继承本类.
 * @param <T> type of entity
 */
@SuppressWarnings("rawtypes")
public abstract class RelationalPathBaseEx<T> extends BeanPath<T> implements RelationalPathEx<T> {

	private static final long serialVersionUID = -3351359519644416084L;

	protected PrimaryKey<T> primaryKey;

	final Map<Path<?>, ColumnMapping> columnMetadata = new LinkedHashMap<>();

	private volatile transient List<Path<?>> columns;
	
	private volatile transient List<ColumnMapping> autoColumns;

	/**
	 * path name &lt;-&gt; path
	 */
	final Map<String, Path<?>> bindingsMap = new HashMap<>();

	protected final List<ForeignKey<?>> foreignKeys = new ArrayList<>();

	protected final List<ForeignKey<?>> inverseForeignKeys = new ArrayList<>();

	protected final List<Constraint> constraints = new ArrayList<>();

	private final String schema, table;

	private final SchemaAndTable schemaAndTable;

	private transient volatile QBeanEx<T> projection;

	private transient NumberExpression<Long> count, countDistinct;

	private String comment;

	private Collate collate;

	private PartitionBy partitionBy;

	private InitializeData initializeData;

	public RelationalPathBaseEx(Class<? extends T> type, String variable) {
		this(type, PathMetadataFactory.forVariable(variable), null, null);
	}

	public RelationalPathBaseEx(Class<? extends T> type, String variable, String schema, String table) {
		this(type, PathMetadataFactory.forVariable(variable), schema, table);
	}

	public RelationalPathBaseEx(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
		super(type, metadata);
		TableSpec spec = type.getAnnotation(TableSpec.class);
		if (spec != null) {
			if (StringUtils.isNotEmpty(spec.name())) {
				table = spec.name();
			}
			if (StringUtils.isNotEmpty(spec.schema())) {
				schema = spec.schema();
			}
		}
		this.schema = schema;
		this.table = table;
		if (StringUtils.isEmpty(table)) {
			throw new IllegalArgumentException("Table name is empty. class is " + type.getName());
		}
		this.schemaAndTable = new SchemaAndTable(schema, table);
		InitializeData initializeData = this.getClass().getAnnotation(InitializeData.class);
		if (initializeData == null) {
			initializeData = type.getAnnotation(InitializeData.class);
		}
		this.initializeData = initializeData;
		{
			HashPartition hashPartition;
			if ((hashPartition = type.getAnnotation(HashPartition.class)) != null) {
				setPartitionBy(new HashPartitionBy(this, hashPartition.columns(), hashPartition.expr(), hashPartition.type(), hashPartition.count()));
			}
		}
		{
			ListPartition anno;
			if ((anno = type.getAnnotation(ListPartition.class)) != null) {
				setPartitionBy(new ListPartitionBy(this, anno.columns(), anno.expr(), anno.value()));
			}
		}
		{
			RangePartition anno;
			if ((anno = type.getAnnotation(RangePartition.class)) != null) {
				setPartitionBy(new RangePartitionBy(this, anno.columns(), anno.expr(), anno.value(), anno.auto()));
			}
		}
	}

	/**
	 *   创建check类型的约束
	 *   @param name 名称。在某些不支持指定CHECK名称的数据库（如MySQL 5.x）上会被忽略。
	 *   @param checkExpression 表达式。两端无需加上括号。
	 *   @return Constraint
	 */
	protected Constraint createCheck(String name, Expression<Boolean> checkExpression) {
		Constraint constraint = new Constraint();
		constraint.setName(name);
		constraint.setConstraintType(ConstraintType.CHECK);
		constraint.setCheckClause(checkExpression);
		this.constraints.add(constraint);
		return constraint;
	}

	/**
	 *   Create a check constraint.
	 *   @param name 名称。在某些不支持指定CHECK名称的数据库（如MySQL 5.x）上会被忽略。
	 *   @param checkExpression 表达式。两端无需加上括号。
	 *   @return Constraint
	 */
	protected Constraint createCheck(String name, String checkExpression) {
		return createCheck(name, Expressions.template(Boolean.class, checkExpression));
	}

	/**
	 *   Create a normal constraint or index.
	 *   @param name name of constraint
	 *   @param type type of constraint
	 *   @param columns columns in the constraint
	 *   @return Constraint
	 */
	protected Constraint createConstraint(String name, ConstraintType type, Path<?>... columns) {
		return createConstraint(name, type, false, columns);
	}
	
	/**
	 * Create a normal constraint or index.
	 * @param name  name of constraint
	 * @param type  type of constraint
	 * @param ignore true to ignore error when the database do not support this constraint/index.
	 * @param columns columns in the constraint
	 * @return Constraint
	 */
	protected Constraint createConstraint(String name, ConstraintType type, boolean ignore, Path<?>... columns) {
		if (type == ConstraintType.PRIMARY_KEY) {
			throw Exceptions.unsupportedOperation("please use #createPrimaryKey() method for columns", Arrays.toString(columns));
		}
		Constraint constraint = new Constraint();
		constraint.setName(name);
		constraint.setConstraintType(type);
		constraint.setPaths(Arrays.asList(columns));
		constraint.setAllowIgnore(ignore);
		this.constraints.add(constraint);
		return constraint;
	}

	protected PrimaryKey<T> createPrimaryKey(Path<?>... columns) {
		primaryKey = new PrimaryKey<>(this, columns);
		return primaryKey;
	}

	protected <F> ForeignKey<F> createForeignKey(Path<?> local, String foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<>(this, local, foreign);
		foreignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createForeignKey(List<? extends Path<?>> local, List<String> foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<>(this, new ArrayList<>(local), new ArrayList<>(foreign));
		foreignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createInvForeignKey(Path<?> local, String foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<>(this, local, foreign);
		inverseForeignKeys.add(foreignKey);
		return foreignKey;
	}

	protected <F> ForeignKey<F> createInvForeignKey(List<? extends Path<?>> local, List<String> foreign) {
		ForeignKey<F> foreignKey = new ForeignKey<>(this, new ArrayList<>(local), new ArrayList<>(foreign));
		inverseForeignKeys.add(foreignKey);
		return foreignKey;
	}

	/**
	 *   定义一个数据库列。可以指定列的各个属性。
	 *
	 *   @implNote
	 *   以下是一些使用的注意点
	 *             <ul>
	 *             <li>对于Timestamp数据类型(包括MySQL的datetime)，其小数位数精度使用 {@link ColumnMetadata#withSize(int)}, 而不是withDigits()。</li>
	 *             </ul>
	 *
	 *   @param <P> type of field.
	 *   @param expr path of column
	 *   @param metadata metadata of the column.
	 *   @return ColumnBuilder
	 */
	protected <P> ColumnBuilder<P> addMetadata(Path<P> expr, ColumnMetadata metadata) {
		Class<?> beanType = super.getType();
		Field field = ReflectionUtils.getFieldOrNull(beanType, expr.getMetadata().getName());;
		Assert.notNull(field, "Unable to find field " + expr.getMetadata().getName() + " in class " + super.getType().getName());

		field.setAccessible(true);
		if (isNotAssignableFrom(field.getType(), expr.getType())) {
			typeMismatch(field, expr);
		}
		checkColumnLength(metadata);
		
		PathMapping metadataExt = new PathMapping(expr, field, metadata);
		addMetadata(expr, metadataExt);
		return new ColumnBuilder<>(metadataExt);
	}

	private void checkColumnLength(ColumnMetadata metadata) {
		if (SQLTypeUtils.isCharBinary(metadata.getJdbcType())) {
			if (metadata.getSize() <= 0) {
				throw Exceptions.illegalArgument("column {}.{}'s size must greater than 0.", this.getClass().getName(), metadata);
			}
		}
	}

	protected void initColumns(){
		List<Entry<Path<?>, ColumnMetadata>> list = new ArrayList<>();
		for (Map.Entry<Path<?>, ColumnMapping> e : columnMetadata.entrySet()) {
			list.add(new Entry<>(e.getKey(), e.getValue().getColumn()));
		}
		list.sort((a, b) -> {
			ColumnMetadata ma = a.getValue();
			ColumnMetadata mb = b.getValue();
			int indexA = 0, indexB = 0;
			try {
				indexA = ma.getIndex();
			} catch (NullPointerException e) {
				// There is a NPE by unbox an Integer suspect in ColumnMetadata. do nothing.
			}
			try {
				indexB = mb.getIndex();
			} catch (NullPointerException e) {
				// There is a NPE by unbox an Integer suspect in ColumnMetadata. do nothing.
			}
			return Integer.compare(indexA, indexB);
		});
		int size=list.size();
		Path<?>[] columns=new Path<?>[size];
		List<ColumnMapping> autoColumns=new ArrayList<>();
		for(int i=0;i<size;i++) {
			Entry<Path<?>, ColumnMetadata> e=list.get(i);
			ColumnMapping column=getColumnMetadata(columns[i]=e.getKey());
			if(column.getGenerated()!=null) {
				autoColumns.add(column);
			}
		}
		this.autoColumns = autoColumns.isEmpty() ? Collections.emptyList() : autoColumns;
		this.columns = Arrays.asList(columns);
	}

	protected <P extends Path<?>> P addMetadata(P path, ColumnMapping metadata) {
		columnMetadata.put(path, metadata);
		bindingsMap.put(path.getMetadata().getName(), path);
		return path;
	}
	
	protected void clearColumnsCache() {
		this.columns=null;
		this.autoColumns=null;
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
	 *   Compares the two relational paths using primary key columns
	 *
	 *   @param right rhs of the comparison
	 *   @return this == right
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
	 *   Compares the two relational paths using primary key columns
	 *
	 *   @param right rhs of the comparison
	 *   @return this == right
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
	 *   Compares the two relational paths using primary key columns
	 *
	 *   @param right rhs of the comparison
	 *   @return this != right
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
	 *   Compares the two relational paths using primary key columns
	 *
	 *   @param right rhs of the comparison
	 *   @return this != right
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
			BooleanExpression predicate = Expressions.booleanOperation(op, pk1.getLocalColumns().get(i), pk2.getLocalColumns().get(i));
			rv = rv != null ? rv.and(predicate) : predicate;
		}
		return rv;
	}

	@Override
	public FactoryExpression<T> getProjection() {
		if (projection == null) {
			this.projection = ProjectionsAlter.createBeanProjection(this);
		}
		return projection;
	}

	@Override
	public BeanCodec getBeanCodec() {
		return ((QBeanEx<T>) getProjection()).getBeanCodec();
	}

	@Override
	public List<Path<?>> getColumns() {
		if(columns==null) {
			initColumns();
		}
		return columns;
	}

	@Override
	public List<ColumnMapping> getAutoColumns() {
		if (autoColumns == null) {
			initColumns();
		}
		return autoColumns;
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
		ColumnMapping metadata = columnMetadata.get(column);
		return metadata != null ? metadata.getColumn():null;
	}

	/**
	 *   读取Entity类上的JPA注解，生成元数据
	 */
	protected void scanClassMetadata() {
		Class<? extends T> beanType = super.getType();
		Class<?> QClz = this.getClass();
		try {
			int count = 1;
			for (Field field : Util.getDeclaredFields(QClz)) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (!Path.class.isAssignableFrom(field.getType())) {
					continue;
				}
				Path<?> path = (Path<?>) field.get(this);
				this.addMetadata(path, builderMetadata(beanType, path, field, count++));
			}
			initByClassAnnotation(beanType, QClz);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void scanClassMetadata(Supplier<List<Path<?>>> pathProvider) {
		Class<? extends T> beanType = super.getType();
		int count = 1;
		List<Path<?>> list=pathProvider.get();
		for (Path<?> path : list) {
			this.addMetadata(path, builderMetadata(beanType, path, null, count++));
		}
		initByClassAnnotation(beanType, null);
	}

	private PathMapping builderMetadata(Class<? extends T> beanType, Path<?> path, Field metadataField, int index) {
		Field field = ReflectionUtils.getFieldOrNull(beanType, path.getMetadata().getName());
		if (field == null) {
			throw new IllegalArgumentException("Not found field [" + path.getMetadata().getName() + "] in bean " + beanType.getName());
		}
		field.setAccessible(true);
		if (isNotAssignableFrom(field.getType(), path.getType())) {
			typeMismatch(field, path);
		}
		String columnName = path.getMetadata().getName().toUpperCase();
		// 解析QueryDSL的名称注解
		com.querydsl.sql.Column c = field.getAnnotation(com.querydsl.sql.Column.class);
		{
			if (c != null && StringUtils.isNotEmpty(c.value())) {
				columnName = c.value();
			}
		}
		ColumnSpec columnSpec = field.getAnnotation(ColumnSpec.class);
		if (columnSpec == null && metadataField != null) {
			columnSpec = metadataField.getAnnotation(ColumnSpec.class);
		}
		if (columnSpec != null) {
			if (StringUtils.isNotEmpty(columnSpec.name())) {
				columnName = columnSpec.name();
			}
		}
		ColumnMetadata column = ColumnMetadata.named(columnName).withIndex(index);
		if (columnSpec != null) {
			int type = SQLTypeUtils.calcJdbcType(columnSpec.type(), field);
			int size=SQLTypeUtils.getDefaultSize(type, columnSpec.size());
			column = column.ofType(type).withSize(size).withDigits(columnSpec.digits());
			if (!columnSpec.nullable()) {
				column = column.notNull();
			}
		}
		return new PathMapping(path, field, column);
	}

	protected void initByClassAnnotation(Class<? extends T> beanType, Class<?> qClz) {
		TableSpec spec = qClz==null? null: qClz.getAnnotation(TableSpec.class);
		if (spec == null) {
			spec = beanType.getAnnotation(TableSpec.class);
		}
		if (spec != null) {
			if (StringUtils.isNotEmpty(spec.collate())) {
				this.setCollate(Collate.findValueOf(spec.collate()));
			}
			if (spec.primaryKeys().length > 0) {
				List<Path<?>> paths = new ArrayList<>();
				for (String n : spec.primaryKeys()) {
					Path<?> path = findPathByName(n);
					if (path == null) {
						throw Exceptions.illegalArgument("Invalid primary key column {} on class {}", n, beanType);
					}
					paths.add(path);
				}
				this.createPrimaryKey(paths.toArray(new Path[0]));
			}
			for (com.github.xuse.querydsl.annotation.dbdef.Key k : spec.keys()) {
				List<Path<?>> paths = new ArrayList<>();
				for (String n : k.path()) {
					Path<?> path = findPathByName(n);
					if (path == null) {
						throw Exceptions.illegalArgument("Invalid key=[{}] column {} on class {}", k.name(), n, beanType);
					}
					paths.add(path);
				}
				this.createConstraint(k.name(), k.type(), k.allowIgnore(), paths.toArray(new Path[0]));
			}
			for (Check c : spec.checks()) {
				this.createCheck(c.name(), c.value());
			}
		}
		Comment comment = qClz==null? null: qClz.getAnnotation(Comment.class);
		if (comment == null) {
			comment = beanType.getAnnotation(Comment.class);
		}
		if (comment != null) {
			this.setComment(comment.value());
		}
	}

	private Path<?> findPathByName(String n) {
		for (Path<?> p : getColumns()) {
			if (p.getMetadata().getName().equalsIgnoreCase(n)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Path<?> getColumn(String name) {
		return bindingsMap.get(name);
	}

	private static boolean isNotAssignableFrom(Class<?> cl1, Class<?> cl2) {
		return !normalize(cl1).isAssignableFrom(normalize(cl2));
	}

	private static Class<?> normalize(Class<?> cl) {
		return cl.isPrimitive() ? Primitives.toWrapperClass(cl) : cl;
	}

	protected void typeMismatch(Field field, Expression<?> expr) {
		final String msg = expr.getType().getName() + " is not compatible with " + field.getType().getName() + " on field " + field;
		throw new IllegalArgumentException(msg);
	}

	public ColumnMapping getColumnMetadata(Path<?> path) {
		return columnMetadata.get(path);
	}

	public Collection<Constraint> getConstraints() {
		return Collections.unmodifiableCollection(constraints);
	}

	protected void setPartitionBy(PartitionBy partitionBy) {
		if (this.partitionBy != null) {
			throw new IllegalArgumentException("One table has one partition rule only." + this.table);
		}
		this.partitionBy = partitionBy;
	}

	protected void setComment(String comment) {
		this.comment = comment;
	}

	protected void setCollate(Collate collate) {
		this.collate = collate;
	}

	public String getComment() {
		return comment;
	}

	public Collate getCollate() {
		return collate;
	}

	public PartitionBy getPartitionBy() {
		return partitionBy;
	}

	public InitializeData getInitializeData() {
		return initializeData;
	}

	protected void setInitializeData(InitializeData initializeData) {
		this.initializeData = initializeData;
	}

	@Override
	public RelationalPathExImpl<T> clone() {
		RelationalPathBaseEx<T> t = new RelationalPathExImpl<T>(this.getType(), this.getMetadata(), schema, table);
		t.collate = this.collate;
		t.comment = this.comment;
		t.partitionBy = this.partitionBy;
		t.primaryKey = this.primaryKey;
		t.columnMetadata.putAll(this.columnMetadata);
		t.columns = new ArrayList<>(getColumns());
		t.autoColumns=new ArrayList<>(getAutoColumns());
		t.bindingsMap.putAll(this.bindingsMap);
		t.constraints.addAll(this.constraints);
		t.foreignKeys.addAll(this.foreignKeys);
		t.inverseForeignKeys.addAll(this.inverseForeignKeys);
		t.initializeData=this.initializeData;
		return (RelationalPathExImpl<T>) t;
	}
	
	public RelationalPathExImpl<T> copyTo(String variable) {
		RelationalPathBaseEx<T> t = new RelationalPathExImpl<T>(this.getType(),PathMetadataFactory.forVariable(variable) , schema, table);
		t.collate = this.collate;
		t.comment = this.comment;
		t.partitionBy = this.partitionBy;
		
		Map<Path<?>,Path<?>> pathMapping=new HashMap<>();
		List<Path<?>> columns=getColumns();
		int size=columns.size();
		Path<?>[] newColumns=new Path<?>[size];
		t.columns = Arrays.asList(newColumns);
		List<ColumnMapping> autoColumns=new ArrayList<>();
		for (int i = 0; i < size; i++) {
			// 重建Path
			Path<?> p = columns.get(i);
			String name=p.getMetadata().getName();
			Path<?> newPath = SQLTypeUtils.createPathByType(p.getType(), name, t);
			newColumns[i] = newPath;
			pathMapping.put(p, newPath);
			t.bindingsMap.put(name, newPath);
			
			PathMapping newMapping = ((PathMapping) this.columnMetadata.get(p)).copyForPath(newPath);
			t.columnMetadata.put(newPath, newMapping);
			if(newMapping.getGenerated()!=null) {
				autoColumns.add(newMapping);
			}
		}
		t.autoColumns = autoColumns.isEmpty() ? Collections.emptyList() : autoColumns;
		t.primaryKey = t.createPrimaryKey(replace(pathMapping,primaryKey.getLocalColumns()).toArray(new Path<?>[0]));
		//约束是在DDL中用的，用旧的Path没有问题，不重建了
		t.constraints.addAll(this.constraints);
		t.foreignKeys.addAll(this.foreignKeys);
		t.inverseForeignKeys.addAll(this.inverseForeignKeys);
		t.initializeData=this.initializeData;
		return (RelationalPathExImpl<T>) t;
	}

	private List<Path<?>> replace(Map<Path<?>, Path<?>> pathMapping, List<? extends Path<?>> localColumns) {
		List<Path<?>> newPaths = new ArrayList<>(localColumns.size());
		for (Path<?> p : localColumns) {
			Path<?> newPath = pathMapping.get(p);
			newPaths.add(newPath);
		}
		return newPaths;
	}
}
