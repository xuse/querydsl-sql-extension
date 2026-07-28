package io.github.xuse.fastjson.adapter;

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
        return null;
    }

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

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        JSONField ann = m.getAnnotation(JSONField.class);
        if (ann == null) {
            return false;
        }
        return !ann.serialize() && !ann.deserialize();
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
}
