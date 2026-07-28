package io.github.xuse.fastjson.adapter;

import java.lang.annotation.Annotation;
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
 * 同时通过反射支持原生 com.alibaba.fastjson.annotation.JSONField 注解，
 * 当 classpath 中存在 fastjson 依赖时自动生效。自定义注解优先级高于原生注解。
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
        if (ann != null) {
            if (!ann.serialize()) {
                return null;
            }
            if (!ann.name().isEmpty()) {
                return PropertyName.construct(ann.name());
            }
        }
        // fallback: 原生 fastjson 注解
        FastjsonFieldAnnotationProxy.FieldValues nv = findNativeAnnotation(a);
        if (nv != null) {
            if (!nv.serialize) {
                return null;
            }
            if (nv.name != null && !nv.name.isEmpty()) {
                return PropertyName.construct(nv.name);
            }
        }
        return null;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null) {
            if (!ann.deserialize()) {
                return null;
            }
            if (!ann.name().isEmpty()) {
                return PropertyName.construct(ann.name());
            }
        }
        // fallback: 原生 fastjson 注解
        FastjsonFieldAnnotationProxy.FieldValues nv = findNativeAnnotation(a);
        if (nv != null) {
            if (!nv.deserialize) {
                return null;
            }
            if (nv.name != null && !nv.name.isEmpty()) {
                return PropertyName.construct(nv.name);
            }
        }
        return null;
    }

    @Override
    public List<PropertyName> findPropertyAliases(Annotated a) {
        JSONField ann = findAnnotation(a);
        String[] alternates = null;
        boolean deserialize = true;
        if (ann != null) {
            deserialize = ann.deserialize();
            alternates = ann.alternateNames();
        } else {
            // fallback: 原生 fastjson 注解
            FastjsonFieldAnnotationProxy.FieldValues nv = findNativeAnnotation(a);
            if (nv != null) {
                deserialize = nv.deserialize;
                alternates = nv.alternateNames;
            }
        }
        if (!deserialize || alternates == null || alternates.length == 0) {
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

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        JSONField ann = m.getAnnotation(JSONField.class);
        if (ann != null) {
            return !ann.serialize() && !ann.deserialize();
        }
        // fallback: 原生 fastjson 注解
        FastjsonFieldAnnotationProxy.FieldValues nv = findNativeValues(m);
        if (nv != null) {
            return !nv.serialize && !nv.deserialize;
        }
        return false;
    }

    @Override
    public Boolean hasAsValue(Annotated a) {
        return null;
    }

    @Override
    public JsonFormat.Value findFormat(Annotated a) {
        JSONField ann = findAnnotation(a);
        if (ann != null && !ann.format().isEmpty()) {
            return JsonFormat.Value.forPattern(ann.format())
                    .withTimeZone(java.util.TimeZone.getDefault());
        }
        // fallback: 原生 fastjson 注解
        FastjsonFieldAnnotationProxy.FieldValues nv = findNativeAnnotation(a);
        if (nv != null && nv.format != null && !nv.format.isEmpty()) {
            return JsonFormat.Value.forPattern(nv.format)
                    .withTimeZone(java.util.TimeZone.getDefault());
        }
        return null;
    }

    private static JSONField findAnnotation(Annotated a) {
        if (a instanceof AnnotatedField) {
            return a.getAnnotation(JSONField.class);
        }
        if (a instanceof AnnotatedMethod) {
            return a.getAnnotation(JSONField.class);
        }
        return null;
    }

    /**
     * 查找原生 com.alibaba.fastjson.annotation.JSONField 注解。
     * <p>
     * 通过 Jackson 的合并注解表查询（涵盖 field/getter/setter），
     * 而非直接读取单个反射成员，否则 getter 上查不到字段注解。
     */
    private static FastjsonFieldAnnotationProxy.FieldValues findNativeAnnotation(Annotated a) {
        Class<? extends Annotation> type = FastjsonFieldAnnotationProxy.nativeClass();
        if (type == null || a == null) {
            return null;
        }
        return FastjsonFieldAnnotationProxy.valuesOf(a.getAnnotation(type));
    }

    /**
     * 查找 AnnotatedMember 上的原生注解
     */
    private static FastjsonFieldAnnotationProxy.FieldValues findNativeValues(AnnotatedMember m) {
        return findNativeAnnotation(m);
    }
}
