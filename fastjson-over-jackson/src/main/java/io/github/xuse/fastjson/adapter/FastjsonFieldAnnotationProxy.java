package io.github.xuse.fastjson.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 通过反射读取 com.alibaba.fastjson.annotation.JSONField 注解的属性值。
 * <p>
 * 当 classpath 中存在原生 fastjson 的 JSONField 注解时，此代理可将其属性映射为
 * 与本项目自定义 {@link JSONField} 相同的语义，从而实现透明兼容。
 * <p>
 * 如果 classpath 中不存在原生注解，则此代理不会生效。
 */
class FastjsonFieldAnnotationProxy {

    private static final Class<? extends Annotation> NATIVE_CLASS = loadNativeClass();
    private static final Method METHOD_NAME;
    private static final Method METHOD_FORMAT;
    private static final Method METHOD_SERIALIZE;
    private static final Method METHOD_DESERIALIZE;
    private static final Method METHOD_ALTERNATE_NAMES;

    static {
        if (NATIVE_CLASS != null) {
            METHOD_NAME = getMethod("name");
            METHOD_FORMAT = getMethod("format");
            METHOD_SERIALIZE = getMethod("serialize");
            METHOD_DESERIALIZE = getMethod("deserialize");
            METHOD_ALTERNATE_NAMES = getMethod("alternateNames");
        } else {
            METHOD_NAME = null;
            METHOD_FORMAT = null;
            METHOD_SERIALIZE = null;
            METHOD_DESERIALIZE = null;
            METHOD_ALTERNATE_NAMES = null;
        }
    }

    /**
     * 判断是否在 classpath 中找到了原生 fastjson JSONField 注解
     */
    static boolean isAvailable() {
        return NATIVE_CLASS != null;
    }

    /**
     * 原生注解类型，classpath 中不存在时返回 null。
     */
    static Class<? extends Annotation> nativeClass() {
        return NATIVE_CLASS;
    }

    /**
     * 将原生注解实例转换为属性值对象。
     *
     * @param ann 原生 JSONField 注解实例，可为 null
     * @return 属性值对象，入参为 null 时返回 null
     */
    static FieldValues valuesOf(Annotation ann) {
        if (ann == null || NATIVE_CLASS == null || !NATIVE_CLASS.isInstance(ann)) {
            return null;
        }
        return extractValues(ann);
    }

    /**
     * 查找指定成员（Field/Method）上的原生 JSONField 注解。
     *
     * @param member 反射成员
     * @return 属性值对象，未找到返回 null
     */
    static FieldValues findNativeAnnotation(java.lang.reflect.AnnotatedElement member) {
        if (NATIVE_CLASS == null || member == null) {
            return null;
        }
        Annotation ann = member.getAnnotation(NATIVE_CLASS);
        return ann == null ? null : extractValues(ann);
    }

    private static FieldValues extractValues(Annotation ann) {
        try {
            String name = METHOD_NAME != null ? (String) METHOD_NAME.invoke(ann) : "";
            String format = METHOD_FORMAT != null ? (String) METHOD_FORMAT.invoke(ann) : "";
            boolean serialize = METHOD_SERIALIZE != null ? (Boolean) METHOD_SERIALIZE.invoke(ann) : true;
            boolean deserialize = METHOD_DESERIALIZE != null ? (Boolean) METHOD_DESERIALIZE.invoke(ann) : true;
            String[] alternateNames;
            if (METHOD_ALTERNATE_NAMES != null) {
                alternateNames = (String[]) METHOD_ALTERNATE_NAMES.invoke(ann);
            } else {
                alternateNames = new String[0];
            }
            return new FieldValues(name, alternateNames, format, serialize, deserialize);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> loadNativeClass() {
        try {
            return (Class<? extends Annotation>) Class.forName("com.alibaba.fastjson.annotation.JSONField");
        } catch (ClassNotFoundException e) {
            // fastjson not on classpath — ignore
            return null;
        }
    }

    private static Method getMethod(String name) {
        try {
            if (NATIVE_CLASS == null) {
                return null;
            }
            return NATIVE_CLASS.getMethod(name);
        } catch (NoSuchMethodException e) {
            // older version may not have this method
            return null;
        }
    }

    /**
     * 从原生 JSONField 注解中提取的属性值
     */
    static class FieldValues {
        final String name;
        final String[] alternateNames;
        final String format;
        final boolean serialize;
        final boolean deserialize;

        FieldValues(String name, String[] alternateNames, String format, boolean serialize, boolean deserialize) {
            this.name = name;
            this.alternateNames = alternateNames;
            this.format = format;
            this.serialize = serialize;
            this.deserialize = deserialize;
        }
    }
}
