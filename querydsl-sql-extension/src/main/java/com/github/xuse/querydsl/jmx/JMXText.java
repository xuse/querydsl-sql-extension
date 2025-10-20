package com.github.xuse.querydsl.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a description to the feature being annotation. Note that when applied to getter/setter ({@link JMXAttribute}
 * ) methods the description should describe the attribute, not the getter/setter method, and therefore the
 * {@link JMXText} annotation should be added to only one, since a description annotation added to both getter and
 * setter methods would cause competing attribute descriptions which may result in indeterminate behavior or cause an
 * error during introspection.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
public @interface JMXText {
    /**
     * 属性、操作、参数的注释。
     */
	String description() default "";
	/**
	 * @return 可以覆盖属性、操作、参数的默认名称
	 */
	String value() default "";
}
