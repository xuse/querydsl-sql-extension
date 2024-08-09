package com.github.xuse.querydsl.annotation.dbdef;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 
 * <h2>Chinese:</h2>
 * 注解，可以用于类或字段，用来定义数据库中的注释。当位于实体类上时定义数据表的注释；位于字段上时定义数据库列的注释。
 * <h2>English:</h2>
 * Annotation can be used for classes or fields to define comments in the
 * database. When placed on an entity class, it defines the comment for the data
 * table; when placed on a field, it defines the comment for the database
 * column.
 * 
 * @author Joey
 */
@Target({ TYPE,FIELD })
@Retention(RUNTIME)
public @interface Comment {
	/**
	 * @return 数据库注释内容 / The comment content.
	 */
	String value();
}
