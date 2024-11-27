package com.github.xuse.querydsl.annotation.dbdef;

import com.github.xuse.querydsl.sql.ddl.ConstraintType;

/**
 * <h2>Chinese:</h2> 注解，用于定义一个数据库索引或者UNIQUE约束。 可定义的具体类型参见 {@link ConstraintType}.
 * 其中外键、REF、READ_ONLY等约束不会实际生效（更新到数据库）。 如果要定义Check约束。请使用{@link Check}注解。
 * <h2>English:</h2> Annotation used to define a database index or a UNIQUE
 * constraint. For specific types that can be defined, refer to
 * {@link ConstraintType}. Constraints such as foreign keys, REF, READ_ONLY,
 * etc., will not actually take effect (be updated to the database). To define
 * a Check constraint, please use the {@link Check} annotation.
 * 
 * @see ConstraintType
 */
public @interface Key {
	/**
	 * @return 名称 <p> the name of index or constraint.
	 */
	String name() default "";
	/**
	 * @return 索引类型 <p> the type of index of constraint.
	 */
	ConstraintType type() default ConstraintType.KEY;
	
	/**
	 * @return 索引或约束中的字段
	 * <p>
	 *     Fields in the index or constraint. Note: here is the name of java fields, not the name of database columns.
	 */
	String[] path();
	
	/**
	 * @return 建表时如果数据库不支持该类索引/约束，忽略该索引/约束.
	 * <p>
	 * If the database does not support the type of index/constraint during table creation, ignore this index/constraint.
	 */
	boolean allowIgnore() default false;
}
