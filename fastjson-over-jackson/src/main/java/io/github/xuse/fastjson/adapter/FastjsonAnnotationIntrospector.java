package io.github.xuse.fastjson.adapter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * Jackson AnnotationIntrospector，识别 {@link JSONField} 注解。
 * <p>
 * 原生 {@code com.alibaba.fastjson.annotation.JSONField} 会被适配为 {@link JSONField}
 * 后一并处理，见 {@link NativeJSONFieldAdapter}。同一属性上两者都存在时，以 {@link JSONField} 为准。
 * <p>
 * Jackson 原生注解的优先级高于本 Introspector，由 {@code AnnotationIntrospectorPair} 保证。
 */
public class FastjsonAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final long serialVersionUID = 1L;

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null && ann.serialize() && !ann.name().isEmpty()) {
            return PropertyName.construct(ann.name());
        }
        return null;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null && ann.deserialize() && !ann.name().isEmpty()) {
            return PropertyName.construct(ann.name());
        }
        return null;
    }

    @Override
    public List<PropertyName> findPropertyAliases(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann == null || !ann.deserialize()) {
            return null;
        }
        List<PropertyName> aliases = new ArrayList<>();
        for (String alt : ann.alternateNames()) {
            if (alt != null && !alt.isEmpty()) {
                aliases.add(PropertyName.construct(alt));
            }
        }
        return aliases.isEmpty() ? null : aliases;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        JSONField ann = findAnnotation(m);
        // serialize=false && deserialize=false → 完全忽略
        return ann != null && !ann.serialize() && !ann.deserialize();
    }

    @Override
    public Boolean hasAsValue(Annotated a) {
        return null;
    }

    /**
     * 将 {@code serialize=false} / {@code deserialize=false} 映射为 Jackson 的单向可见性。
     * <p>
     * 走 Jackson 合并后的注解表，字段与 getter/setter 上的注解都能识别。
     */
    @Override
    public JsonProperty.Access findPropertyAccess(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann == null) {
            return null;
        }
        if (!ann.serialize() && !ann.deserialize()) {
            // 两者都 false 由 hasIgnoreMarker 整体忽略
            return null;
        }
        if (!ann.serialize()) {
            // 只写：不参与序列化输出
            return JsonProperty.Access.WRITE_ONLY;
        }
        if (!ann.deserialize()) {
            // 只读：不参与反序列化写入
            return JsonProperty.Access.READ_ONLY;
        }
        return null;
    }

    @Override
    public JsonFormat.Value findFormat(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null && !ann.format().isEmpty()) {
            return JsonFormat.Value.forPattern(ann.format())
                    .withTimeZone(java.util.TimeZone.getDefault());
        }
        return null;
    }

    /**
     * 查找 {@link JSONField}：自定义注解优先，未标注时回退到适配后的原生注解。
     * <p>
     * 两者都从 Jackson 合并后的注解表读取（涵盖 field/getter/setter），
     * 不能直接查单个反射成员，否则内省到 getter 时读不到字段上的注解。
     */
    static JSONField findAnnotation(Annotated a) {
        if (!(a instanceof AnnotatedField) && !(a instanceof AnnotatedMethod)) {
            return null;
        }
        JSONField ann = a.getAnnotation(JSONField.class);
        if (ann != null) {
            return ann;
        }
        Class<? extends Annotation> nativeClass = NativeJSONFieldAdapter.nativeClass();
        if (nativeClass == null) {
            return null;
        }
        return NativeJSONFieldAdapter.adapt(a.getAnnotation(nativeClass));
    }
}
