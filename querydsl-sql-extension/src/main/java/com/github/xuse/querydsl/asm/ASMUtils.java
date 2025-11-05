package com.github.xuse.querydsl.asm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.github.xuse.querydsl.util.lang.Primitives;

public class ASMUtils {

	public static boolean isAndroid(String vmName) {
		if (vmName == null) {
			// default is false
			return false;
		}
		String lowerVMName = vmName.toLowerCase();
		return // 
		lowerVMName.contains("dalvik") || // aliyun-vm name
		lowerVMName.contains("lemur");
	}

	public static boolean isAndroid() {
		return isAndroid(System.getProperty("java.vm.name"));
	}

	public static String getDesc(Method method) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		java.lang.Class<?>[] types = method.getParameterTypes();
		for (int i = 0; i < types.length; ++i) {
			buf.append(getDesc(types[i]));
		}
		buf.append(")");
		buf.append(getDesc(method.getReturnType()));
		return buf.toString();
	}

	public static String getDesc(Class<?> returnType) {
		if (returnType.isPrimitive()) {
			return getPrimitiveLetter(returnType);
		} else if (returnType.isArray()) {
			return "[" + getDesc(returnType.getComponentType());
		} else {
			return "L" + getType(returnType) + ";";
		}
	}

	public static String getType(Class<?> parameterType) {
		if (parameterType.isArray()) {
			return "[" + getDesc(parameterType.getComponentType());
		} else {
			if (parameterType.isPrimitive()) {
				return getPrimitiveLetter(parameterType);
			} else {
				return parameterType.getName().replace('.', '/');
			}
		}
	}

	public static String getPrimitiveLetter(Class<?> type) {
		if (Integer.TYPE.equals(type)) {
			return "I";
		} else if (Void.TYPE.equals(type)) {
			return "V";
		} else if (Boolean.TYPE.equals(type)) {
			return "Z";
		} else if (Character.TYPE.equals(type)) {
			return "C";
		} else if (Byte.TYPE.equals(type)) {
			return "B";
		} else if (Short.TYPE.equals(type)) {
			return "S";
		} else if (Float.TYPE.equals(type)) {
			return "F";
		} else if (Long.TYPE.equals(type)) {
			return "J";
		} else if (Double.TYPE.equals(type)) {
			return "D";
		}
		throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
	}

	public static boolean checkName(String name) {
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (c < '\001' || c > '\177') {
				return false;
			}
		}
		return true;
	}

	/**
	 * get the class object of primitive type
	 *
	 * @param mw mw
	 * @param rawType rawType
	 */
	public static void getPrimitiveType(MethodVisitor mw, Class<?> rawType) {
		Class<?> wrapClz = Primitives.toWrapperClass(rawType);
		mw.visitFieldInsn(Opcodes.GETSTATIC, getType(wrapClz), "TYPE", "Ljava/lang/Class;");
	}

	/**
	 * 生成拆箱方法
	 *
	 * @param mw mw
	 * @param primitive primitive
	 *            原生类型
	 * @param wrapped 包装类型
	 */
	public static void doUnwrap(MethodVisitor mw, Class<?> primitive, Class<?> wrapped) {
		String name = primitive.getName() + "Value";
		mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, getType(wrapped), name, getMethodDesc(primitive), false);
	}

	/**
	 * 生成装箱方法
	 *
	 * @param mw mw
	 * @param primitive primitive
	 *            原生类型
	 * @param wrapped wrapped
	 */
	public static void doWrap(MethodVisitor mw, Class<?> primitive, Class<?> wrapped) {
		mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(wrapped), "valueOf", getMethodDesc(wrapped, primitive), false);
	}

	public static void doWrap(MethodVisitor mw, com.github.xuse.querydsl.asm.Type paramType) {
		Class<?> w;
		switch(paramType.getSort()) {
			case com.github.xuse.querydsl.asm.Type.BOOLEAN:
				w = Boolean.class;
				break;
			case com.github.xuse.querydsl.asm.Type.BYTE:
				w = Byte.class;
				break;
			case com.github.xuse.querydsl.asm.Type.CHAR:
				w = Character.class;
				break;
			case com.github.xuse.querydsl.asm.Type.DOUBLE:
				w = Double.class;
				break;
			case com.github.xuse.querydsl.asm.Type.FLOAT:
				w = Float.class;
				break;
			case com.github.xuse.querydsl.asm.Type.INT:
				w = Integer.class;
				break;
			case com.github.xuse.querydsl.asm.Type.LONG:
				w = Long.class;
				break;
			case com.github.xuse.querydsl.asm.Type.SHORT:
				w = Short.class;
				break;
			default:
				throw new IllegalArgumentException();
		}
		mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(w), "valueOf", getMethodDesc(w, Primitives.toPrimitiveClass(w)), false);
	}

	/**
	 * 获得加载指令。要注LLOAD和DLOAD都是64位操作
	 * @param paramType paramType
	 * @return loadIns
	 */
	public static int getLoadIns(com.github.xuse.querydsl.asm.Type paramType) {
		switch(paramType.getSort()) {
			case com.github.xuse.querydsl.asm.Type.BOOLEAN:
				return Opcodes.ILOAD;
			case com.github.xuse.querydsl.asm.Type.BYTE:
				return Opcodes.ILOAD;
			case com.github.xuse.querydsl.asm.Type.CHAR:
				return Opcodes.ILOAD;
			case com.github.xuse.querydsl.asm.Type.DOUBLE:
				return Opcodes.DLOAD;
			case com.github.xuse.querydsl.asm.Type.FLOAT:
				return Opcodes.FLOAD;
			case com.github.xuse.querydsl.asm.Type.INT:
				return Opcodes.ILOAD;
			case com.github.xuse.querydsl.asm.Type.LONG:
				return Opcodes.LLOAD;
			case com.github.xuse.querydsl.asm.Type.SHORT:
				return Opcodes.ILOAD;
			default:
				return Opcodes.ALOAD;
		}
	}

	/**
	 * 插入一条常量入栈指令
	 * @param mw mw
	 * @param s s
	 */
	public static void iconst(MethodVisitor mw, int s) {
		switch(s) {
			case -1:
				mw.visitInsn(Opcodes.ICONST_M1);
				break;
			case 0:
				mw.visitInsn(Opcodes.ICONST_0);
				break;
			case 1:
				mw.visitInsn(Opcodes.ICONST_1);
				break;
			case 2:
				mw.visitInsn(Opcodes.ICONST_2);
				break;
			case 3:
				mw.visitInsn(Opcodes.ICONST_3);
				break;
			case 4:
				mw.visitInsn(Opcodes.ICONST_4);
				break;
			case 5:
				mw.visitInsn(Opcodes.ICONST_5);
				break;
			default:
				mw.visitIntInsn(Opcodes.BIPUSH, s);
		}
	}

	/**
	 * 生成方法签名
	 *
	 * @param returnType returnType
	 * @param params params
	 * @return 方法签名
	 */
	public static String getMethodDesc(Class<?> returnType, Class<?>... params) {
		StringBuilder sb = new StringBuilder("(");
		for (Class<?> c : params) {
			sb.append(getDesc(c));
		}
		sb.append(')');
		sb.append(getDesc(returnType));
		return sb.toString();
	}

	/**
	 * 获得Java类名
	 * @param cr cr
	 * @return className
	 */
	public static String getJavaClassName(ClassReader cr) {
		return cr.getClassName().replace('/', '.');
	}

	/**
	 * 获得父类名
	 * @param cr cr
	 * @return 父类名
	 */
	public static String getSuperClassName(ClassReader cr) {
		return cr.getSuperName().replace('/', '.');
	}
	
	public static class ClassAnnotationExtracter extends ClassVisitor {
		private Set<String> annotations = new HashSet<String>();

		public ClassAnnotationExtracter() {
			super(Opcodes.ASM9);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			annotations.add(desc);
			return null;
		}

		public boolean hasAnnotation(Class<? extends Annotation> clzName) {
			return annotations.contains(ASMUtils.getDesc(clzName));
		}
	}
}
