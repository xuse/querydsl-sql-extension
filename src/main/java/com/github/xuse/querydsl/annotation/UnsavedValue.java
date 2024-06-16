package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 当数据类型为int, long, short, char等基础类型时。如果Entity的修改记录了该值是设置过的，
 * 那么认为是有效值。<br>
 * 如果无记录，那么不等于UnsavedValue的值认为是有效值。
 * @author jiyi
 *
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface UnsavedValue {
	String value();
	
	/**
	 * 空字符串当作null。
	 * <p>
	 * 此外对于primitive类型的字段，使用这个配置可以让false/0 之类的值变为有效值。
	 * 如果没有定义UnsavedValue, int(0), boolean(false)都被认为是null值。
	 * </p>
	 */
	String NullOrEmpty = "null+";

	String MinusNumber = "<0";

	String ZeroAndMinus = "<=0";
}
