package com.github.xuse.querydsl.sql.expression;

import static com.github.xuse.querydsl.util.ASMUtils.doUnwrap;
import static com.github.xuse.querydsl.util.ASMUtils.getDesc;
import static com.github.xuse.querydsl.util.ASMUtils.getMethodDesc;
import static com.github.xuse.querydsl.util.ASMUtils.getType;
import static com.github.xuse.querydsl.util.ASMUtils.iconst;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.asm.ClassWriter;
import com.github.xuse.querydsl.asm.MethodVisitor;
import com.github.xuse.querydsl.asm.Opcodes;
import com.github.xuse.querydsl.util.ASMUtils;
import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.Primitives;

/**
 * To generate {@link BeanCodec} class with ASM.
 * 
 * @author jiyi
 *
 */
public class CodecClassGenerator implements Opcodes {
	protected ClassLoaderAccessor cl;

	public CodecClassGenerator(ClassLoaderAccessor cl) {
		this.cl = cl;
	}

	private static final String PARENT_CLASS = getType(BeanCodec.class);
	private static final Logger log = LoggerFactory.getLogger(CodecClassGenerator.class);
	private boolean debug = false;

	public Class<?> generate(Class<?> beanType, List<FieldProperty> methods, String clzName) {
		clzName = clzName.replace('.', '_');
		try {
			byte[] data = generate0(beanType, methods, clzName);
			if (debug) {
				File file = new File(System.getProperty("user.dir"), clzName + ".class");
				IOUtils.saveAsFile(file, data);
				log.info("The codec class {} was generate for debug.", file.getAbsolutePath());
			}
			Class<?> clz = cl.defineClz(clzName, data);
			log.info("The codec class {} was load.", clzName);
			return clz;
		} catch (Exception ex) {
			log.error("ASM generation error for class {}", clzName, ex);
			return null;
		}
	}

	private byte[] generate0(Class<?> beanType, List<FieldProperty> methods, String clzName) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER + ACC_FINAL, clzName, null, PARENT_CLASS, new String[] {});
		
		// Contructor
		{
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mw.visitVarInsn(ALOAD, 0);
			mw.visitMethodInsn(INVOKESPECIAL, PARENT_CLASS, "<init>", "()V", false);
			mw.visitInsn(RETURN);
			mw.visitMaxs(1, 1);
			mw.visitEnd();
		}
		{
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "newInstance", getMethodDesc(Object.class, Object[].class),
					null, null);
			mw.visitTypeInsn(NEW, getType(beanType));// S1
			try {
				// 运行空构造方法
				if (beanType.getDeclaredConstructor() != null) {
					mw.visitInsn(DUP);
					mw.visitMethodInsn(INVOKESPECIAL, getType(beanType), "<init>", "()V", false);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			mw.visitVarInsn(ASTORE, 2);// 创建对象并存入 S0

			int index = 0;
			for (FieldProperty property : methods) {
				Method setter = property.getSetter();
				if (setter == null) {
					// 轮空
					index++;
					continue;
				}
				mw.visitVarInsn(ALOAD, 2);// S1
				mw.visitVarInsn(ALOAD, 1);// S2
				iconst(mw, index++);// S3
				mw.visitInsn(AALOAD);// S2
				// 类型转换
				Class<?> target = setter.getParameterTypes()[0];
				if (target.isPrimitive()) {
					Class<?> wrapped = Primitives.toWrapperClass(target);
					mw.visitTypeInsn(CHECKCAST, getType(wrapped));// 类型转换
					doUnwrap(mw, target, wrapped); // 拆箱
				} else {
					mw.visitTypeInsn(CHECKCAST, getType(target));// 类型转换
				}
				mw.visitMethodInsn(INVOKEVIRTUAL, getType(beanType), setter.getName(), getDesc(setter), false);
			}

			mw.visitVarInsn(ALOAD, 2);// 创建对象并存入 S0
			mw.visitInsn(ARETURN);
			mw.visitMaxs(3, 3);
			mw.visitEnd();
		}
		{
			// 生成values方法
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "values", getMethodDesc(Object[].class, Object.class), null,
					null);

			mw.visitVarInsn(ALOAD, 1);// s1
			mw.visitTypeInsn(CHECKCAST, getType(beanType));// 类型转换
			mw.visitVarInsn(ASTORE, 2);// s0 转换后的Bean写入

			iconst(mw, methods.size());
			mw.visitTypeInsn(ANEWARRAY, getType(Object.class));// 创建数组
			mw.visitVarInsn(ASTORE, 3);// s0

			int index = 0;
			for (FieldProperty property : methods) {
				Method getter = property.getGetter();
				if (getter == null) {
					// 轮空
					index++;
					continue;
				}
				mw.visitVarInsn(ALOAD, 3);// S1
				iconst(mw, index++);// S2
				mw.visitVarInsn(ALOAD, 2);// S3 获得转换后的Bean对象
				mw.visitMethodInsn(INVOKEVIRTUAL, getType(beanType), getter.getName(), getDesc(getter), false);// S3

				Class<?> target = getter.getReturnType();
				if (target.isPrimitive()) {
					ASMUtils.doWrap(mw, target);
				}
				mw.visitInsn(AASTORE); // 写入数组,S0
			}

			mw.visitVarInsn(ALOAD, 3);//
			mw.visitInsn(ARETURN);
			mw.visitMaxs(3, 4);
			mw.visitEnd();

		}
		cw.visitEnd();
		return cw.toByteArray();
	}
}
