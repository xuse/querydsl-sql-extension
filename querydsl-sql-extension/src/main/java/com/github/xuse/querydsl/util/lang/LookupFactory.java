package com.github.xuse.querydsl.util.lang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * MethodHandles.Lookup 工厂，跨 JDK 版本获取可信 Lookup。
 * <p>
 * 适配 JDK 8~22，提供多种策略获取具有完全访问权限的 Lookup：
 * <ol>
 *   <li>通过 Unsafe 获取 IMPL_LOOKUP（最优路径）</li>
 *   <li>通过构造 Lookup 实例（JDK 8~14 / JDK 15+）</li>
 *   <li>降级为 MethodHandles.lookup()（最低权限）</li>
 * </ol>
 * </p>
 */
public final class LookupFactory {

    private LookupFactory() {
    }

    /**
     * 获取全局可信 Lookup（IMPL_LOOKUP）。
     * 该 Lookup 拥有对所有类的完全访问权限。
     */
    public static Lookup trustedLookup() {
        return JDKEnvironment.IMPL_LOOKUP;
    }

    /**
     * 获取针对指定类的可信 Lookup。
     * 可以访问该类的 private 成员，包括跨模块。
     *
     * @param targetClass 目标类
     * @return 具有完全访问权限的 Lookup
     */
    public static Lookup trustedLookup(Class<?> targetClass) {
        return JDKEnvironment.trustedLookup(targetClass);
    }

    /**
     * 获取普通 Lookup（调用者权限）
     */
    public static Lookup lookup() {
        return MethodHandles.lookup();
    }

    /**
     * 获取公共 Lookup（仅能访问 public 成员）
     */
    public static Lookup publicLookup() {
        return MethodHandles.publicLookup();
    }

    // ==================== MethodHandle 便捷方法 ====================

    /**
     * 查找构造函数 MethodHandle
     *
     * @param clazz      目标类
     * @param paramTypes 构造函数参数类型
     * @return MethodHandle
     */
    public static MethodHandle findConstructor(Class<?> clazz, Class<?>... paramTypes) {
        try {
            Lookup lookup = trustedLookup(clazz);
            return lookup.findConstructor(clazz, MethodType.methodType(void.class, paramTypes));
        } catch (Throwable e) {
            // fallback: unreflect constructor with setAccessible
            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor(paramTypes);
                ctor.setAccessible(true);
                return MethodHandles.lookup().unreflectConstructor(ctor);
            } catch (Throwable e2) {
                throw new UnsafeOperationException("Cannot find constructor of " + clazz.getName(), e);
            }
        }
    }

    /**
     * 查找实例方法 MethodHandle
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param returnType 返回类型
     * @param paramTypes 参数类型
     * @return MethodHandle
     */
    public static MethodHandle findVirtual(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... paramTypes) {
        try {
            Lookup lookup = trustedLookup(clazz);
            return lookup.findVirtual(clazz, methodName, MethodType.methodType(returnType, paramTypes));
        } catch (Throwable e) {
            // fallback: unreflect with setAccessible
            try {
                Method method = clazz.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            } catch (Throwable e2) {
                throw new UnsafeOperationException(
                        "Cannot find method " + clazz.getName() + "." + methodName, e);
            }
        }
    }

    /**
     * 查找静态方法 MethodHandle
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param returnType 返回类型
     * @param paramTypes 参数类型
     * @return MethodHandle
     */
    public static MethodHandle findStatic(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... paramTypes) {
        try {
            Lookup lookup = trustedLookup(clazz);
            return lookup.findStatic(clazz, methodName, MethodType.methodType(returnType, paramTypes));
        } catch (Throwable e) {
            // fallback: unreflect with setAccessible
            try {
                Method method = clazz.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            } catch (Throwable e2) {
                throw new UnsafeOperationException(
                        "Cannot find static method " + clazz.getName() + "." + methodName, e);
            }
        }
    }

    /**
     * 查找 Getter MethodHandle
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @param fieldType 字段类型
     * @return MethodHandle
     */
    public static MethodHandle findGetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
        try {
            Lookup lookup = trustedLookup(clazz);
            return lookup.findGetter(clazz, fieldName, fieldType);
        } catch (Throwable e) {
            throw new UnsafeOperationException(
                    "Cannot find getter for " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 查找 Setter MethodHandle
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @param fieldType 字段类型
     * @return MethodHandle
     */
    public static MethodHandle findSetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
        try {
            Lookup lookup = trustedLookup(clazz);
            return lookup.findSetter(clazz, fieldName, fieldType);
        } catch (Throwable e) {
            throw new UnsafeOperationException(
                    "Cannot find setter for " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 查找静态 Getter MethodHandle
     */
    public static MethodHandle findStaticGetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
        try {
            Lookup lookup = trustedLookup(clazz);
            return lookup.findStaticGetter(clazz, fieldName, fieldType);
        } catch (Throwable e) {
            throw new UnsafeOperationException(
                    "Cannot find static getter for " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 查找静态 Setter MethodHandle
     */
    public static MethodHandle findStaticSetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
        try {
            Lookup lookup = trustedLookup(clazz);
            return lookup.findStaticSetter(clazz, fieldName, fieldType);
        } catch (Throwable e) {
            throw new UnsafeOperationException(
                    "Cannot find static setter for " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 将 Method 转为 MethodHandle（unreflect），绕过访问检查
     */
    public static MethodHandle unreflect(Method method) {
        try {
            Lookup lookup = trustedLookup(method.getDeclaringClass());
            return lookup.unreflect(method);
        } catch (Throwable e) {
            // fallback: setAccessible + publicLookup
            try {
                method.setAccessible(true);
                return MethodHandles.lookup().unreflect(method);
            } catch (Throwable e2) {
                throw new UnsafeOperationException(
                        "Cannot unreflect method " + method.getDeclaringClass().getName() + "." + method.getName(), e);
            }
        }
    }

    /**
     * 将 Constructor 转为 MethodHandle（unreflectConstructor），绕过访问检查
     */
    public static MethodHandle unreflectConstructor(Constructor<?> constructor) {
        try {
            Lookup lookup = trustedLookup(constructor.getDeclaringClass());
            return lookup.unreflectConstructor(constructor);
        } catch (Throwable e) {
            // fallback: setAccessible + lookup
            try {
                constructor.setAccessible(true);
                return MethodHandles.lookup().unreflectConstructor(constructor);
            } catch (Throwable e2) {
                throw new UnsafeOperationException(
                        "Cannot unreflect constructor of " + constructor.getDeclaringClass().getName(), e);
            }
        }
    }
}
