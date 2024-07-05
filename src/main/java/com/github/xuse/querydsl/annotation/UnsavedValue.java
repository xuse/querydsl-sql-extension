package com.github.xuse.querydsl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 
 * 当数据类型为int, long, short, char等基础类型时，如果被判定为UnsavedValue，那么视为null一样处理。
 * 带来的效果是——
 * <li>Insert时：populate(bean)方法不会主动向数据库插入该列(使用数据库的默认值)</li>
 * <li>update时：populate(bean)方法自动判定认为这个字段不需要更新。</li>
 * <li>查询时：正常的条件拼装不影响，但基于ConditionBean或者findByExample等查询时不认为是查询条件之一。</li>
 * 因此，如果定义Primitive类型的数据，建议尽量设置此属性，以免业务理解发生歧义。
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface UnsavedValue {
	String value() default Default;
	
	/**
	 * 仅有null值被视作null。
	 * 对于Primitive字段来说，必然有0/false等数值存在，因此当配置为Null时，意味着primitive字段永远不会被当作null值处理。
	 */
	String Null = "null";
	
	/**
	 * 空字符串当作null。适用于字符串。
	 */
	String NullOrEmpty = "null+";

	/**
	 * 负数视作null
	 */
	String MinusNumber = "<0";

	/**
	 * 零和负数视作null
	 */
	String ZeroAndMinus = "<=0";
	
	/**
	 * 零视作null
	 */
	String Zero = "0";
	
	/**
	 * 根据字段类型视为null。比如 数值0被视为null， false被视为boolean的null。
	 * 注意，如果primitive字段上不配置@UnsavedValue注解，也视同default. 
	 */
	String Default ="default";
}
