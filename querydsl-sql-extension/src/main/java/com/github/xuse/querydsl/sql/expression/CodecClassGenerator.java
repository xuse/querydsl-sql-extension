package com.github.xuse.querydsl.sql.expression;

import static com.github.xuse.querydsl.asm.ASMUtils.doUnwrap;
import static com.github.xuse.querydsl.asm.ASMUtils.getDesc;
import static com.github.xuse.querydsl.asm.ASMUtils.getMethodDesc;
import static com.github.xuse.querydsl.asm.ASMUtils.getType;
import static com.github.xuse.querydsl.asm.ASMUtils.iconst;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.asm.ASMUtils;
import com.github.xuse.querydsl.asm.ClassWriter;
import com.github.xuse.querydsl.asm.MethodVisitor;
import com.github.xuse.querydsl.asm.Opcodes;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.TypeUtils;
import com.github.xuse.querydsl.util.lang.Primitives;
import com.mysema.commons.lang.Pair;

import lombok.SneakyThrows;

/**
 * To generate {@link BeanCodec} class with ASM.
 * 
 * Do not support Android and GraalVM.
 * 
 * @author Joey
 *
 */
public class CodecClassGenerator implements Opcodes {
	protected ClassLoaderAccessor cl;

	public CodecClassGenerator(ClassLoaderAccessor cl) {
		this.cl = cl;
	}

	private static final String PARENT_CLASS = getType(BeanCodec.class);
	private static final Logger log = LoggerFactory.getLogger(CodecClassGenerator.class);
	private final boolean debug = false;

	public Class<?> generate(Class<?> beanType, List<FieldProperty> methods, String clzName, boolean record) {
		clzName = clzName.replace('.', '_');
		try {
			byte[] data = generate0(beanType, methods, clzName,record);
			if (debug) {
				File file = new File(System.getProperty("user.dir"), clzName + ".class");
				IOUtils.saveAsFile(file, data);
				log.info("The codec class {} was generate for debug.", file.getAbsolutePath());
			}
			Class<?> clz = cl.defineClz(clzName, data);
			log.info("The codec class {} was load.", clzName);
			return clz;
		} catch (Throwable ex) {
			log.error("ASM generation error for class {}", clzName, ex);
			return null;
		}
	}
	

	private byte[] generate0(Class<?> beanType, List<FieldProperty> methods, String clzName, boolean record) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER + ACC_FINAL, clzName, null, PARENT_CLASS, new String[] {});
		
		{
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mw.visitVarInsn(ALOAD, 0);
			mw.visitMethodInsn(INVOKESPECIAL, PARENT_CLASS, "<init>", "()V", false);
			mw.visitInsn(RETURN);
			mw.visitMaxs(1, 1);
			mw.visitEnd();
		}
		if(record){
			generateRecordInstance(beanType,cw,methods);
			// There will be no method copy/sets in the generated class. If user call these methods
			// on a record object, will receive a AbstractMethodError. This is expected case.
		}else{
			generateInstance(beanType,cw,methods);
			generateSet(beanType,cw,methods);
			generateCopy(beanType,cw,methods);
		}
		{
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "values", getMethodDesc(Object[].class, Object.class), null,
					null);

			mw.visitVarInsn(ALOAD, 1);// s1 入参1上栈（Bean）
			mw.visitTypeInsn(CHECKCAST, getType(beanType));// 类型转换
			mw.visitVarInsn(ASTORE, 2);// s0 转换后的Bean写入变量2.

			iconst(mw, methods.size());
			mw.visitTypeInsn(ANEWARRAY, getType(Object.class));// 创建数组
			mw.visitVarInsn(ASTORE, 3);// s0 结果数组写入变量3

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
					ASMUtils.doWrap(mw, target, Primitives.toWrapperClass(target));
				}
				mw.visitInsn(AASTORE); // 写入数组,S0
			}
			mw.visitVarInsn(ALOAD, 3);
			mw.visitInsn(ARETURN);
			mw.visitMaxs(3, 4);
			mw.visitEnd();

		}
		cw.visitEnd();
		return cw.toByteArray();
	}

	private void generateSet(Class<?> beanType, ClassWriter cw, List<FieldProperty> methods) {
		String type=getType(beanType);
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "sets", getMethodDesc(void.class, Object[].class, Object.class), null,null);
		//Stack:0
		//Locals:[0-this, 1-values, 2-bean]
		mw.visitVarInsn(ALOAD, 2);// S1
		mw.visitTypeInsn(CHECKCAST, type);// 类型转换
		mw.visitVarInsn(ASTORE, 2);// S0 正常java代码变量类型是固定的，无法编译出这种数据。但直接操作字节码可以。
		generateValueSet(type,mw,methods);
		mw.visitInsn(RETURN);
		mw.visitMaxs(2, 3);
		mw.visitEnd();
	}

	private void generateValueSet(String type, MethodVisitor mw, List<FieldProperty> methods) {
		int index = 0;
		for (FieldProperty property : methods) {
			Method setter = property.getSetter();
			if (setter == null) {
				index++;
				continue;
			}
			mw.visitVarInsn(ALOAD, 2);// S1
			mw.visitVarInsn(ALOAD, 1);// S2, 读取V1，即数组
			iconst(mw, index++);// S3
			mw.visitInsn(AALOAD);// S2
			// 类型转换
			Class<?> target = setter.getParameterTypes()[0];
			if (target.isPrimitive()) {
				Class<?> wrapped = Primitives.toWrapperClass(target);
				boolean primitive = tryTypeConvert(mw, wrapped, property.getBindingType());
				if (!primitive) {
					doUnwrap(mw, target, wrapped); // 拆箱
				}
			} else {
				boolean primitive = tryTypeConvert(mw, target, property.getBindingType());
				if (primitive) {
					ASMUtils.doWrap(mw, Primitives.toPrimitiveClass(target), target);
				}
			}
			mw.visitMethodInsn(INVOKEVIRTUAL, type, setter.getName(), getDesc(setter), false);
		}
	}

	@SneakyThrows
	private void generateRecordInstance(Class<?> beanType, ClassWriter cw, List<FieldProperty> methods) {
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "newInstance", getMethodDesc(Object.class, Object[].class),
				null, null);
		mw.visitTypeInsn(NEW, getType(beanType));// S1 用于返回的栈底
		mw.visitInsn(DUP);//S2 用于调用构造器
		
		//按构造器顺序重新计算属性位置
		String[] names = TypeUtils.getRecordFieldNames(beanType);
		Map<String,Pair<Integer,FieldProperty>> map=new HashMap<>();
		for(int i=0;i<methods.size();i++) {
			FieldProperty f=methods.get(i);
			Pair<Integer,FieldProperty> entry=new Pair<>(i,f);
			map.put(f.getName(),entry);
		}
		
		List<Pair<Integer,FieldProperty>> exprs=new ArrayList<>();
		for (String name:names) {
			Pair<Integer,FieldProperty> entry=map.get(name);
			if(entry==null) {
				//本次没有查询该字段，需要生成一个默认值入栈
				Field field=beanType.getDeclaredField(name);
				exprs.add(new Pair<>(-1, new FieldProperty(null, null, field)));
			}else {
				exprs.add(entry);
			}
		}
		//正式开始
		StringBuilder sb=new StringBuilder();
		
		for(Pair<Integer,FieldProperty> entry:exprs) {
			int index=entry.getFirst();
			FieldProperty property=entry.getSecond();
			Class<?> target=property.getField().getType();
			//生成构造器签名
			sb.append(getDesc(target));
			
			//准备栈上参数
			if(index<0) {
				pushDefaultValueOnStack(mw, entry.getSecond().getField());
			}else {
				//目标类型
				mw.visitVarInsn(ALOAD, 1);//读取V1，即数组
				iconst(mw, index);
				mw.visitInsn(AALOAD);// 读出数据
				
				// 类型转换
				if (target.isPrimitive()) {
					Class<?> wrapped = Primitives.toWrapperClass(target);
					boolean primitive = tryTypeConvert(mw, wrapped, property.getBindingType());
					if(!primitive) {
						doUnwrap(mw, target, wrapped); // 拆箱
					}
				} else {
					boolean primitive=tryTypeConvert(mw,target, property.getBindingType());
					if(primitive) {
						ASMUtils.doWrap(mw, Primitives.toPrimitiveClass(target), target);
					}
				}
				//数据丢在栈上，最后调用构造器的时候用
			}
		}
		
		//栈数据准备完成，调用构造器
		mw.visitMethodInsn(INVOKESPECIAL, getType(beanType), "<init>", "("+sb+")V", false);
		
		//最初构造的对象还在栈底，直接return即可
		mw.visitInsn(ARETURN);
		mw.visitMaxs(exprs.size() + 2, 1); //栈长度计算，2（this） + 最大参数个数
		mw.visitEnd();
	}


	private void pushDefaultValueOnStack(MethodVisitor mw,Field declaredField) {
		Class<?> type=declaredField.getType();
		if(type.isPrimitive()) {
			String s=declaredField.getName();
			switch(s.length()+s.charAt(0)) {
				case 112://long
					mw.visitInsn(Opcodes.LCONST_0);
					break;
				case 106://double
					mw.visitInsn(Opcodes.DCONST_0);
					break;
				case 107://float
					mw.visitInsn(Opcodes.FCONST_0);
					break;
				case 108://int
				case 105://boolean
				case 103://char
				case 102://byte
				default://short
					mw.visitInsn(Opcodes.ICONST_0);
			}
		}else {
			//在栈上塞一个null
			mw.visitInsn(Opcodes.ACONST_NULL);
		}
	}


	private void generateInstance(Class<?> beanType, ClassWriter cw, List<FieldProperty> methods) {
		String type=getType(beanType);
		
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "newInstance", getMethodDesc(Object.class, Object[].class), null,
				null);
		mw.visitTypeInsn(NEW, type);// S1
		// 运行空构造方法
		if (TypeUtils.getDeclaredConstructor(beanType) != null) {
			mw.visitInsn(DUP);
			mw.visitMethodInsn(INVOKESPECIAL, type, "<init>", "()V", false);
		}
		mw.visitVarInsn(ASTORE, 2);// 存入 V2
		generateValueSet(type,mw,methods);
		mw.visitVarInsn(ALOAD, 2);// 创建对象并存入 S0
		mw.visitInsn(ARETURN);
		mw.visitMaxs(3, 3);
		mw.visitEnd();
	}

	private void generateCopy(Class<?> beanType, ClassWriter cw, List<FieldProperty> methods) {
		String type=getType(beanType);
		//L0=this,L1=source,L2=target,L3=casted source,L4=casted target
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "copy", getMethodDesc(void.class, Object.class,Object.class),null,null);
		mw.visitVarInsn(ALOAD, 1);// stack1,  
		mw.visitTypeInsn(CHECKCAST, type);// 类型转换
		mw.visitVarInsn(ASTORE, 3);// stack1,
		
		mw.visitVarInsn(ALOAD, 2);// stack1,  
		mw.visitTypeInsn(CHECKCAST, type);// 类型转换
		mw.visitVarInsn(ASTORE, 4);// stack1,
		
		for (FieldProperty property : methods) {
			Method setter = property.getSetter();
			if (setter == null) {
				// 轮空
				continue;
			}
			Method getter = property.getGetter();
			if(getter==null) {
				continue;
			}
			
			mw.visitVarInsn(ALOAD, 4);// S1,
			mw.visitVarInsn(ALOAD, 3);// S2,
			mw.visitMethodInsn(INVOKEVIRTUAL, type, getter.getName(), getDesc(getter), false);//S2
			mw.visitMethodInsn(INVOKEVIRTUAL, type, setter.getName(), getDesc(setter), false);//S0
		}
		mw.visitInsn(RETURN);
		mw.visitMaxs(1, 4);
		mw.visitEnd();
	}


	private boolean tryTypeConvert(MethodVisitor mw, Class<?> target, Class<?> bindingType) {
		if(bindingType == null || target.isAssignableFrom(bindingType)) {
			mw.visitTypeInsn(CHECKCAST, getType(target));// 类型转换	
			return false;
		}
		if(Number.class.isAssignableFrom(bindingType) && Number.class.isAssignableFrom(target)) {
			//基于Number的方式进行特殊拆箱
			mw.visitTypeInsn(CHECKCAST, getType(Number.class));
			switch(target.getName()) {
			case "java.lang.Integer":
				mw.visitMethodInsn(INVOKEVIRTUAL, getType(Number.class), "intValue", "()I", false);
				return true;
			case "java.lang.Double":
				mw.visitMethodInsn(INVOKEVIRTUAL, getType(Number.class), "doubleValue", "()D", false);
				return true;
			case "java.lang.Float":
				mw.visitMethodInsn(INVOKEVIRTUAL, getType(Number.class), "floatValue", "()F", false);
				return true;
			case "java.lang.Long":
				mw.visitMethodInsn(INVOKEVIRTUAL, getType(Number.class), "longValue", "()J", false);
				return true;
			case "java.lang.Short":
				mw.visitMethodInsn(INVOKEVIRTUAL, getType(Number.class), "shortValue", "()S", false);
				return true;
			default:
				throw Exceptions.illegalState("Unable to generate unbox method for type {} -> {}", bindingType,target);
			}
		}
		mw.visitTypeInsn(CHECKCAST, getType(target));// 类型转换
		return false;
	}
}
