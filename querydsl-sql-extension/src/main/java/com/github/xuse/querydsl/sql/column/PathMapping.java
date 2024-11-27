package com.github.xuse.querydsl.sql.column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Predicate;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.CustomType;
import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.DateUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.types.Type;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class implements the path metadata extension. path means a column metadata within java side, not the database side.
 */
@Slf4j
@Getter
@Setter
public class PathMapping extends AbstractColumnMetadataEx implements ColumnMapping {

	/**
	 *  Java字段（反射）
	 */
	private final AccessibleElement field;

	protected final Path<?> path;

	/**
	 *  无效值判断器
	 */
	private Predicate<Object> unsavedValue;

	/**
	 *  不插入
	 */
	private boolean notInsert;

	/**
	 *  不更新，这里是指基于自动方式生成Update语句时不更新，手动指定更新字段是不受影响的。
	 */
	private boolean notUpdate;

	/**
	 *  自动生成。这个是java语法层面的自动生成，不是用于数据库层面的
	 */
	private AutoGenerated generated;

	/**
	 *  自定义类型
	 */
	private Type<?> customType;
	
	private PathMapping(Path<?> path, AccessibleElement field, ColumnMetadata column,Type<?> customType) {
		super(column);
		this.path=path;
		this.field=field;
		this.customType=customType;
	}

	public PathMapping(Path<?> path, Field field, ColumnMetadata column) {
		this(path, AccessibleElement.ofField(field), column);
	}

	public PathMapping(Path<?> path, AccessibleElement field, ColumnMetadata column) {
		super(column);
		this.field = field;
		this.path = path;
		this.generated = field.getAnnotation(AutoGenerated.class);
		CustomType anno = field.getAnnotation(CustomType.class);
		UnsavedValue unsaved = field.getAnnotation(UnsavedValue.class);
		if (field.getType().isPrimitive()) {
			// 检查用户映射，提醒用户正确配置Primitive映射
			if (column.isNullable() && anno == null && !ConfigurationEx.FREE_PRIMITIVE) {
				throw Exceptions.illegalArgument("The column [{}] in database is nullable, please use a wrapped type (not primitive) to mapping it. field = {}", column.getName(), field);
			} else if(unsaved==null){
				log.info("Field {} using primitive type to mapping a column in database. Please using {@code @UnsavedValue} or withUnsavePredicate() method to describe it. Otherwise the value like '0' or 'false' will be regarded as a null value.", field);
			}
		}
		this.unsavedValue = UnsavedValuePredicateFactory.create(field.getType(), unsaved == null ? null : unsaved.value());
		ColumnSpec spec = field.getAnnotation(ColumnSpec.class);
		if (spec != null) {
			if (StringUtils.isNotEmpty(spec.defaultValue())) {
				this.setDefaultExpression(Expressions.template(Object.class, spec.defaultValue()));
			}
			if (spec.autoIncrement()) {
				this.features = new ColumnFeature[] { ColumnFeature.AUTO_INCREMENT };
			}
			this.setUnsigned(spec.unsigned());
			this.notUpdate = !spec.updatable();
			this.notInsert = !spec.insertable();
		}
		Comment comment = field.getAnnotation(Comment.class);
		if (comment != null) {
			this.comment = comment.value();
		}
		if (anno != null) {
			@SuppressWarnings("rawtypes")
			Class<? extends Type> clz = anno.value();
			try {
				customType = SQLTypeUtils.createInstance(clz, anno.parameters(), path.getType());
				setCustomType(customType);
			} catch (Exception e) {
				log.error("customType on {} error", path, e);
			}
		}
	}

	@Override
	public boolean isUnsavedValue(Object value) {
		return unsavedValue.test(value);
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public String fieldName() {
		return field.getName();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> clz) {
		return field.getAnnotation(clz);
	}

	/**
	 * 回写即使失败也不报错?
	 * @param bean Object
	 * @param value Object
	 */
	@Override
	public void writeback(Object bean, Object value) {
		try {
			if (value instanceof DateTimeExpression) {
				// 采用了数据库时间的方式，此时正常无法支持回写，而是需要使用数据库Insert返回字段的功能，较为复杂，目前临时先用当前系统时间代替看看。
				value = localTemporal(value, getType());
			}
			if (value instanceof SimpleExpression) {
				// 无法回写
				log.warn("the field {} will not writeback since value is a SimpleExpression.", this);
			}
			if (customType != null) {
				//将自动生成值转换回用户自定义的Java数据类型。
				value = customType.getValue(new DummyResultSet(value), 1);
			}
			field.set(bean, value);
		} catch (SQLException e) {
			throw Exceptions.toRuntime(e);
		}
	}

	protected Object localTemporal(Object value, Class<?> type) {
		Date d = null;
		if (value == DateTimeExpression.currentDate()) {
			d = DateUtils.truncateToDay(new Date());
		} else if (value == DateTimeExpression.currentTimestamp()) {
			d = new Date();
		}
		if (d != null) {
			if (type == java.sql.Date.class) {
				value = new java.sql.Date(d.getTime());
			} else if (type == java.sql.Timestamp.class) {
				value = new java.sql.Timestamp(d.getTime());
			}else {
				value = d;
			}
		}
		return value;
	}

	@Override
	public String toString() {
		return path + "(" + this.getColumn().getName() + ")";
	}
	
	public PathMapping copyForPath(Path<?> newPath) {
		PathMapping p = new PathMapping(newPath, field, super.getColumn(), customType);
		p.unsavedValue = this.unsavedValue;
		p.unsigned = this.unsigned;
		p.defaultExpression = this.defaultExpression;
		p.features = this.features;
		p.comment = this.comment;
		p.notInsert = this.notInsert;
		p.notUpdate = this.notUpdate;
		p.generated = this.generated;
		return p;
	}
}
