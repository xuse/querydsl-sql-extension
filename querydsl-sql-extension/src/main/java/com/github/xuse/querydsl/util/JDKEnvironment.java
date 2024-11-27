package com.github.xuse.querydsl.util;

import java.nio.ByteOrder;

public class JDKEnvironment {
	public static final int JVM_VERSION;
	public static final boolean ANDROID;
	public static final boolean GRAAL_NATIVE;
	public static final boolean OPENJ9;
	public static final int ANDROID_SDK_INT;
	// Android not support
	public static final boolean BIG_ENDIAN;

	public static boolean DISABLE_ASM;

	static {
		int jvmVersion = -1, android_sdk_int = -1;
		boolean openj9 = false, android = false, graal = false;

		String jmvName = System.getProperty("java.vm.name");
		openj9 = jmvName.contains("OpenJ9");
		android = "Dalvik".equals(jmvName);
		graal = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

		String javaSpecVer = System.getProperty("java.specification.version");
		// android is 0.9
		if (javaSpecVer.startsWith("1.")) {
			javaSpecVer = javaSpecVer.substring(2);
		}
		if (javaSpecVer.indexOf('.') == -1) {
			jvmVersion = Integer.parseInt(javaSpecVer);
		}
		try {
			if (android) {
				android_sdk_int = Class.forName("android.os.Build$VERSION").getField("SDK_INT").getInt(null);
			}
		} catch (Throwable e) {
			//do nothing.
		}

		OPENJ9 = openj9;
		ANDROID = android;
		GRAAL_NATIVE = graal;
		ANDROID_SDK_INT = android_sdk_int;
		JVM_VERSION = jvmVersion;
		BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	}
}
