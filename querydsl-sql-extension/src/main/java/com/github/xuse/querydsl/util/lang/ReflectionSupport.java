package com.github.xuse.querydsl.util.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射增强工具，在 JDK 8~22 下绕过模块系统限制进行反射操作。
 * <p>
 * JDK 16+ 默认禁止跨模块 setAccessible，本类通过 Unsafe/trustedLookup 绕过限制。
 * </p>
 */
public final class ReflectionSupport {

    private ReflectionSupport() {
    }

    /**
     * 获取字段值，支持 private/final 字段，跨模块
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        FieldAccessor accessor = FieldAccessor.of(clazz, fieldName);
        return accessor.get(obj);
    }

    /**
     * 获取静态字段值
     */
    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) {
        FieldAccessor accessor = FieldAccessor.of(clazz, fieldName);
        return accessor.get(null);
    }

    /**
     * 设置字段值，支持 private/final 字段，跨模块
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Class<?> clazz = obj.getClass();
        FieldAccessor accessor = FieldAccessor.of(clazz, fieldName);
        accessor.set(obj, value);
    }

    /**
     * 设置静态字段值
     */
    public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) {
        FieldAccessor accessor = FieldAccessor.of(clazz, fieldName);
        accessor.set(null, value);
    }

    /**
     * 调用私有方法（实例方法）
     *
     * @param obj        目标对象
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @param args       参数值
     * @return 方法返回值
     */
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Class<?> clazz = obj.getClass();
            Method method = findMethod(clazz, methodName, paramTypes);
            if (method == null) {
                throw new UnsafeOperationException("Method not found: " + clazz.getName() + "." + methodName);
            }
            makeAccessible(method);
            return method.invoke(obj, args);
        } catch (UnsafeOperationException e) {
            throw e;
        } catch (Throwable e) {
            throw new UnsafeOperationException("Failed to invoke method " + methodName, e);
        }
    }

    /**
     * 调用静态私有方法
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = findMethod(clazz, methodName, paramTypes);
            if (method == null) {
                throw new UnsafeOperationException("Method not found: " + clazz.getName() + "." + methodName);
            }
            makeAccessible(method);
            return method.invoke(null, args);
        } catch (UnsafeOperationException e) {
            throw e;
        } catch (Throwable e) {
            throw new UnsafeOperationException("Failed to invoke static method " + clazz.getName() + "." + methodName, e);
        }
    }

    /**
     * 调用私有构造函数创建对象
     */
    public static <T> T newInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(paramTypes);
            makeAccessible(ctor);
            return ctor.newInstance(args);
        } catch (UnsafeOperationException e) {
            throw e;
        } catch (Throwable e) {
            throw new UnsafeOperationException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * 使字段可访问（兼容 JDK 8~22）。
     * JDK 16+ 对跨模块字段 setAccessible 会失败，此方法通过 Unsafe 绕过。
     */
    public static void makeAccessible(Field field) {
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            // JDK 16+ 跨模块时会抛 InaccessibleObjectException
            // 通过 Unsafe 修改 override 字段
            try {
                long overrideOffset = JDKEnvironment.UNSAFE.objectFieldOffset(
                        getAccessibleObjectOverrideField());
                JDKEnvironment.UNSAFE.putBoolean(field, overrideOffset, true);
            } catch (Exception ex) {
                throw new UnsafeOperationException("Cannot make field accessible: " + field, ex);
            }
        }
    }

    /**
     * 使方法可访问（兼容 JDK 8~22）
     */
    public static void makeAccessible(Method method) {
        try {
            method.setAccessible(true);
        } catch (Exception e) {
            try {
                long overrideOffset = JDKEnvironment.UNSAFE.objectFieldOffset(
                        getAccessibleObjectOverrideField());
                JDKEnvironment.UNSAFE.putBoolean(method, overrideOffset, true);
            } catch (Exception ex) {
                throw new UnsafeOperationException("Cannot make method accessible: " + method, ex);
            }
        }
    }

    /**
     * 使构造函数可访问（兼容 JDK 8~22）
     */
    public static void makeAccessible(Constructor<?> constructor) {
        try {
            constructor.setAccessible(true);
        } catch (Exception e) {
            try {
                long overrideOffset = JDKEnvironment.UNSAFE.objectFieldOffset(
                        getAccessibleObjectOverrideField());
                JDKEnvironment.UNSAFE.putBoolean(constructor, overrideOffset, true);
            } catch (Exception ex) {
                throw new UnsafeOperationException("Cannot make constructor accessible: " + constructor, ex);
            }
        }
    }

    // ==================== 内部方法 ====================

    private static volatile Field overrideField;

    private static Field getAccessibleObjectOverrideField() throws NoSuchFieldException {
        if (overrideField == null) {
            synchronized (ReflectionSupport.class) {
                if (overrideField == null) {
                    try {
                        overrideField = Class.forName("java.lang.reflect.AccessibleObject")
                                .getDeclaredField("override");
                    } catch (ClassNotFoundException e) {
                        throw new NoSuchFieldException("AccessibleObject.override");
                    }
                }
            }
        }
        return overrideField;
    }

    /**
     * 在类继承链中查找方法
     */
    private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
