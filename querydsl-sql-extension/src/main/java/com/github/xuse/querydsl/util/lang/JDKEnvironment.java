package com.github.xuse.querydsl.util.lang;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class JDKEnvironment {

	public static final Unsafe UNSAFE;
    public static final int JVM_VERSION;
	public static final boolean ANDROID;
	public static final boolean GRAAL_NATIVE;
    public static final boolean OPENJ9;
	public static final int ANDROID_SDK_INT;
	public static boolean DISABLE_ASM;

    static final MethodHandles.Lookup IMPL_LOOKUP;
    static volatile MethodHandle CONSTRUCTOR_LOOKUP;
    static volatile boolean CONSTRUCTOR_LOOKUP_ERROR;

    static {
        // 获取 Unsafe 实例
        Unsafe unsafe = null;
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);
        } catch (Throwable ignored) {
        }
        UNSAFE = unsafe;

        // 检测 JVM 版本和运行时类型
		int jvmVersion = -1, android_sdk_int = -1;
        boolean openj9 = false, android = false, graal = false;
        try {
            String vmName = System.getProperty("java.vm.name");
            openj9 = vmName.contains("OpenJ9");
            android = "Dalvik".equals(vmName);
    		graal = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

            String javaSpecVer = System.getProperty("java.specification.version");
            if (javaSpecVer.startsWith("1.")) {
                javaSpecVer = javaSpecVer.substring(2);
            }
            if (javaSpecVer.indexOf('.') == -1) {
                jvmVersion = Integer.parseInt(javaSpecVer);
            }
			if (android) {
				android_sdk_int = Class.forName("android.os.Build$VERSION").getField("SDK_INT").getInt(null);
			}
        } catch (Throwable ignored) {
        }
        JVM_VERSION = jvmVersion;
        OPENJ9 = openj9;
		ANDROID = android;
		GRAAL_NATIVE = graal;
		ANDROID_SDK_INT = android_sdk_int;

        // 获取可信 Lookup
        MethodHandles.Lookup trustedLookup = null;
        try {
            Class<?> lookupClass = MethodHandles.Lookup.class;
            Field implLookup = lookupClass.getDeclaredField("IMPL_LOOKUP");
            // 优先尝试反射方式（JDK 8~11 下可行）
            try {
                implLookup.setAccessible(true);
                trustedLookup = (MethodHandles.Lookup) implLookup.get(null);
            } catch (Throwable ignored) {
            }
            // 如果反射失败（JDK 16+ 模块限制），尝试 Unsafe 方式
            if (trustedLookup == null && unsafe != null) {
                long fieldOffset = unsafe.staticFieldOffset(implLookup);
                trustedLookup = (MethodHandles.Lookup) unsafe.getObject(lookupClass, fieldOffset);
            }
        } catch (Throwable ignored) {
        }
        if (trustedLookup == null) {
            trustedLookup = MethodHandles.lookup();
        }
        IMPL_LOOKUP = trustedLookup;
    }

    /**
     * 获取针对指定类的可信 Lookup，可以访问该类的 private 成员。
     * <p>
     * JDK 8~14：通过 Lookup(Class, int) 构造函数<br>
     * JDK 15+：通过 Lookup(Class, Class, int) 构造函数<br>
     * 降级：IMPL_LOOKUP.in(targetClass)
     * </p>
     */
    public static MethodHandles.Lookup trustedLookup(Class<?> objectClass) {
        if (!CONSTRUCTOR_LOOKUP_ERROR) {
            try {
                int TRUSTED = -1;
                MethodHandle constructor = CONSTRUCTOR_LOOKUP;
                if (JVM_VERSION < 15) {
                    if (constructor == null) {
                        constructor = IMPL_LOOKUP.findConstructor(
                                MethodHandles.Lookup.class,
                                methodType(void.class, Class.class, int.class)
                        );
                        CONSTRUCTOR_LOOKUP = constructor;
                    }
                    int FULL_ACCESS_MASK = 31; // for IBM OpenJ9
                    return (MethodHandles.Lookup) constructor.invoke(
                            objectClass,
                            OPENJ9 ? FULL_ACCESS_MASK : TRUSTED
                    );
                } else {
                    if (constructor == null) {
                        constructor = IMPL_LOOKUP.findConstructor(
                                MethodHandles.Lookup.class,
                                methodType(void.class, Class.class, Class.class, int.class)
                        );
                        CONSTRUCTOR_LOOKUP = constructor;
                    }
                    return (MethodHandles.Lookup) constructor.invoke(objectClass, null, TRUSTED);
                }
            } catch (Throwable ignored) {
                CONSTRUCTOR_LOOKUP_ERROR = true;
            }
        }
        return IMPL_LOOKUP.in(objectClass);
    }
}
