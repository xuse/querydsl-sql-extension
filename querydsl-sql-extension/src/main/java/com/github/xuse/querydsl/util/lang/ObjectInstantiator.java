package com.github.xuse.querydsl.util.lang;

import sun.misc.Unsafe;

/**
 * 无构造函数对象实例化器，类似 objenesis 的核心功能。
 * <p>
 * 适配 JDK 8~22，主路径使用 {@code Unsafe.allocateInstance}。
 * </p>
 *
 * <pre>
 * ObjectInstantiator&lt;Foo&gt; instantiator = new ObjectInstantiator&lt;&gt;(Foo.class);
 * Foo obj = instantiator.newInstance();
 * </pre>
 *
 * @param <T> 目标类型
 */
public class ObjectInstantiator<T> {

    private static final Unsafe unsafe = JDKEnvironment.UNSAFE;

    private final Class<T> type;

    public ObjectInstantiator(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        this.type = type;
    }

    /**
     * 不调用任何构造函数，直接创建对象实例。
     * 对象的所有字段为默认值（0/null/false）。
     */
    @SuppressWarnings("unchecked")
    public T newInstance() {
        try {
            return (T) unsafe.allocateInstance(type);
        } catch (InstantiationException e) {
            throw new UnsafeOperationException("Cannot instantiate " + type.getName(), e);
        }
    }

    /**
     * 静态便捷方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> type) {
        try {
            return (T) unsafe.allocateInstance(type);
        } catch (InstantiationException e) {
            throw new UnsafeOperationException("Cannot instantiate " + type.getName(), e);
        }
    }

    public Class<T> getType() {
        return type;
    }
}
