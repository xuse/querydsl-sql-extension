package io.github.xuse.fastjson.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 将原生 {@code com.alibaba.fastjson.annotation.JSONField} 适配为本项目的 {@link JSONField}。
 * <p>
 * 适配后，其余代码只需面对 {@link JSONField} 一种注解。
 * 若类路径上不存在原生注解，本适配器静默失效。
 */
class NativeJSONFieldAdapter {

    /** 原生注解类型，类路径上不存在时为 null */
    private static final Class<? extends Annotation> NATIVE_CLASS = loadNativeClass();

    private NativeJSONFieldAdapter() {
    }

    /**
     * 原生注解类型，类路径上不存在时返回 null。
     */
    static Class<? extends Annotation> nativeClass() {
        return NATIVE_CLASS;
    }

    /**
     * 将原生注解实例适配为 {@link JSONField}。
     *
     * @param native0 原生注解实例，可为 null
     * @return 适配后的注解，入参为 null 时返回 null
     */
    static JSONField adapt(Annotation native0) {
        if (native0 == null) {
            return null;
        }
        return new NativeJSONField(native0);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> loadNativeClass() {
        try {
            return (Class<? extends Annotation>) Class.forName("com.alibaba.fastjson.annotation.JSONField");
        } catch (ClassNotFoundException e) {
            // fastjson 不在类路径上
            return null;
        }
    }

    /**
     * 基于原生注解取值的 {@link JSONField} 实现。
     * <p>
     * 原生注解缺失某属性时（版本差异）回退到 {@link JSONField} 的默认值。
     */
    private static class NativeJSONField implements JSONField {

        private static final Method M_NAME = method("name");
        private static final Method M_ALTERNATE_NAMES = method("alternateNames");
        private static final Method M_FORMAT = method("format");
        private static final Method M_SERIALIZE = method("serialize");
        private static final Method M_DESERIALIZE = method("deserialize");

        private final Annotation native0;

        NativeJSONField(Annotation native0) {
            this.native0 = native0;
        }

        @Override
        public String name() {
            String v = invoke(M_NAME);
            return v == null ? "" : v;
        }

        @Override
        public String[] alternateNames() {
            String[] v = invoke(M_ALTERNATE_NAMES);
            return v == null ? new String[0] : v;
        }

        @Override
        public String format() {
            String v = invoke(M_FORMAT);
            return v == null ? "" : v;
        }

        @Override
        public boolean serialize() {
            Boolean v = invoke(M_SERIALIZE);
            return v == null || v;
        }

        @Override
        public boolean deserialize() {
            Boolean v = invoke(M_DESERIALIZE);
            return v == null || v;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return JSONField.class;
        }

        @SuppressWarnings("unchecked")
        private <T> T invoke(Method method) {
            if (method == null) {
                return null;
            }
            try {
                return (T) method.invoke(native0);
            } catch (Exception e) {
                return null;
            }
        }

        private static Method method(String name) {
            if (NATIVE_CLASS == null) {
                return null;
            }
            try {
                return NATIVE_CLASS.getMethod(name);
            } catch (NoSuchMethodException e) {
                // 原生注解版本不含该属性，回退默认值
                return null;
            }
        }
    }
}
