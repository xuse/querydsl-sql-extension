package com.github.xuse.querydsl.datatype.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * Jackson AnnotationIntrospector，识别自定义 @JSONField 注解。
 * <p>
 * 支持：
 * <ul>
 *   <li>name → Jackson 主字段名</li>
 *   <li>alternateNames → Jackson 别名（反序列化时识别多个名称）</li>
 *   <li>serialize=false → 序列化时忽略该字段</li>
 *   <li>deserialize=false → 反序列化时忽略该字段</li>
 *   <li>format → Date 字段的序列化/反序列化格式</li>
 * </ul>
 */
public class FastjsonAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final long serialVersionUID = 1L;

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    // ==================== name ====================

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null) {
            // serialize=false 时，序列化方向不暴露该属性
            if (!ann.serialize()) {
                return null;
            }
            if (!ann.name().isEmpty()) {
                return PropertyName.construct(ann.name());
            }
        }
        return null;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null) {
            // deserialize=false 时，反序列化方向不暴露该属性
            if (!ann.deserialize()) {
                return null;
            }
            if (!ann.name().isEmpty()) {
                return PropertyName.construct(ann.name());
            }
        }
        return null;
    }

    // ==================== alternateNames ====================

    @Override
    public List<PropertyName> findPropertyAliases(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann == null || !ann.deserialize()) {
            return null;
        }
        String[] alternates = ann.alternateNames();
        if (alternates.length == 0) {
            return null;
        }
        List<PropertyName> aliases = new ArrayList<>(alternates.length);
        for (String alt : alternates) {
            if (alt != null && !alt.isEmpty()) {
                aliases.add(PropertyName.construct(alt));
            }
        }
        return aliases.isEmpty() ? null : aliases;
    }

    // ==================== serialize/deserialize 控制 ====================

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        JSONField ann = m.getAnnotation(JSONField.class);
        if (ann == null) {
            return false;
        }
        // 两者都为 false → 完全忽略（序列化+反序列化都不参与）
        return !ann.serialize() && !ann.deserialize();
    }

    /**
     * serialize=false 时，序列化方向视为 ignored。
     * Jackson 在序列化时会调用此方法检查该 property 是否应被忽略。
     */
    @Override
    public Boolean hasAsValue(Annotated a) {
        // 不覆盖此方法，保持默认
        return null;
    }

    // ==================== format ====================

    @Override
    public JsonFormat.Value findFormat(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null && !ann.format().isEmpty()) {
            // 与 fastjson 一致：format 总是使用 JVM 默认时区
            return JsonFormat.Value.forPattern(ann.format())
                    .withTimeZone(java.util.TimeZone.getDefault());
        }
        return null;
    }

    // ==================== 内部工具 ====================

    private static JSONField findAnnotation(Annotated a) {
        if (a instanceof AnnotatedField) {
            return a.getAnnotation(JSONField.class);
        }
        if (a instanceof AnnotatedMethod) {
            return a.getAnnotation(JSONField.class);
        }
        return null;
    }
}
