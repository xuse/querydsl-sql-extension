package io.github.xuse.fastjson.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy;

/**
 * 非 JavaBean 规范字段名的属性名推导策略。
 * <p>
 * Jackson 默认按 JavaBean 规范对 getter 去前缀后做首字母小写（bean mangling），
 * 遇到全大写、含下划线、首字母大写的字段名时会得到与声明名不一致的结果，
 * 且与 fastjson 的规则也不同：
 * <pre>
 * 声明字段            Jackson 默认        fastjson
 * TOTAL_BYTES_CNT     total_BYTES_CNT    tOTAL_BYTES_CNT
 * ICCID               iccid              iCCID
 * Response            response           response
 * </pre>
 * 本策略的处理：getter/setter 去掉 get/set/is 前缀后，若剩余部分与类中声明的字段名
 * 完全一致，则直接采用该声明名；否则回退到 Jackson 默认规则。
 * <p>
 * 因此 {@code getICCID()} 得到 {@code ICCID}（类中有同名字段），
 * 而 {@code getUrl()} 仍得到 {@code url}（类中没有名为 {@code Url} 的字段），
 * 常规命名的行为不受影响。
 * <p>
 * 历史数据中可能存在 fastjson 产出的 mangle 名（如 {@code iCCID}），
 * 由 {@code MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES} 在反序列化时兜住。
 */
public class FastjsonAccessorNaming extends AccessorNamingStrategy {

    private final AccessorNamingStrategy delegate;

    /** 目标类及其父类中声明的实例字段名 */
    private final Set<String> declaredFields;

    FastjsonAccessorNaming(AccessorNamingStrategy delegate, AnnotatedClass forClass) {
        this.delegate = delegate;
        this.declaredFields = collectFieldNames(forClass.getRawType());
    }

    private static Set<String> collectFieldNames(Class<?> type) {
        if (type == null) {
            return Collections.emptySet();
        }
        Set<String> names = new HashSet<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!f.isSynthetic() && !Modifier.isStatic(f.getModifiers())) {
                    names.add(f.getName());
                }
            }
        }
        return names;
    }

    /** 方法名去掉前缀后若与声明字段同名则返回该名字，否则返回 null 交由默认规则处理 */
    private String declaredName(String methodName, String prefix) {
        if (!methodName.startsWith(prefix) || methodName.length() == prefix.length()) {
            return null;
        }
        String candidate = methodName.substring(prefix.length());
        return declaredFields.contains(candidate) ? candidate : null;
    }

    @Override
    public String findNameForIsGetter(AnnotatedMethod method, String name) {
        String declared = declaredName(name, "is");
        return declared != null ? declared : delegate.findNameForIsGetter(method, name);
    }

    @Override
    public String findNameForRegularGetter(AnnotatedMethod method, String name) {
        String declared = declaredName(name, "get");
        return declared != null ? declared : delegate.findNameForRegularGetter(method, name);
    }

    @Override
    public String findNameForMutator(AnnotatedMethod method, String name) {
        String declared = declaredName(name, "set");
        return declared != null ? declared : delegate.findNameForMutator(method, name);
    }

    @Override
    public String modifyFieldName(com.fasterxml.jackson.databind.introspect.AnnotatedField field, String name) {
        return delegate.modifyFieldName(field, name);
    }

    /** 只介入普通 POJO，record / builder 沿用 Jackson 默认策略 */
    public static class Provider extends DefaultAccessorNamingStrategy.Provider {

        private static final long serialVersionUID = 1L;

        @Override
        public AccessorNamingStrategy forPOJO(MapperConfig<?> config, AnnotatedClass targetClass) {
            return new FastjsonAccessorNaming(super.forPOJO(config, targetClass), targetClass);
        }
    }
}
