package io.github.xuse.fastjson.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 兼容 com.alibaba.fastjson.annotation.JSONField 的注解。
 * <p>
 * 迁移：将 import com.alibaba.fastjson.annotation.JSONField 替换为本注解。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface JSONField {

    /** JSON 字段名（序列化和反序列化都使用） */
    String name() default "";

    /** 反序列化时可识别的别名列表 */
    String[] alternateNames() default {};

    /** 序列化时的日期格式 */
    String format() default "";

    /** 是否序列化该字段 */
    boolean serialize() default true;

    /** 是否反序列化该字段 */
    boolean deserialize() default true;
}
