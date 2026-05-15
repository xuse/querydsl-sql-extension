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
 * Generates {@link BeanCodec} subclasses at runtime using ASM bytecode manipulation.
 * <p>
 * The generated codec class provides high-performance, reflection-free access to bean properties
 * for reading (values), writing (sets/newInstance), and copying (copy) operations.
 * This approach eliminates the overhead of Java reflection by generating direct method invocations
 * in bytecode.
 * </p>
 * <p>
 * Supports both regular JavaBeans (with getter/setter) and Java Records (constructor-based instantiation).
 * </p>
 * <p>
 * <b>Limitations / 限制:</b> Does not support Android (no dynamic class loading) or GraalVM Native Image
 * (no runtime bytecode generation).
 * </p>
 * <hr>
 * 使用 ASM 字节码技术在运行时动态生成 {@link BeanCodec} 子类。
 * <p>
 * 生成的编解码器类提供高性能、无反射的 Bean 属性访问能力，包括读取（values）、写入（sets/newInstance）
 * 和复制（copy）操作。通过在字节码中直接生成方法调用指令，消除了 Java 反射带来的性能开销。
 * </p>
 * <p>
 * 同时支持普通 JavaBean（基于 getter/setter）和 Java Record（基于构造器实例化）。
 * </p>
 * <p>
 * <b>限制：</b>不支持 Android（无法动态加载类）和 GraalVM Native Image（无法在运行时生成字节码）。
 * </p>
 *
 * @author Joey
 * @see BeanCodec
 * @see FieldProperty
 */
public class CodecClassGenerator implements Opcodes {
	protected ClassLoaderAccessor cl;

	/**
	 * Creates a new CodecClassGenerator with the specified class loader accessor.
	 * <p>
	 * 使用指定的类加载器访问器创建 CodecClassGenerator 实例。
	 *
	 * @param cl the class loader accessor used to define generated classes at runtime.
	 *           用于在运行时定义生成类的类加载器访问器。
	 */
	public CodecClassGenerator(ClassLoaderAccessor cl) {
		this.cl = cl;
	}

	private static final String PARENT_CLASS = getType(BeanCodec.class);
	private static final Logger log = LoggerFactory.getLogger(CodecClassGenerator.class);
	private final boolean debug = false;

	/**
	 * Generates a {@link BeanCodec} class for the given bean type using ASM bytecode generation,
	 * then loads it into the JVM via the configured class loader.
	 * <p>
	 * For regular beans, the generated class implements: {@code newInstance}, {@code sets},
	 * {@code values}, and {@code copy}. For records, only {@code newInstance} and {@code values}
	 * are generated (calling {@code sets} or {@code copy} on a record codec will throw
	 * {@link AbstractMethodError}).
	 * </p>
	 * <hr>
	 * 使用 ASM 字节码生成技术为指定的 Bean 类型生成 {@link BeanCodec} 类，并通过配置的类加载器加载到 JVM 中。
	 * <p>
	 * 对于普通 Bean，生成的类实现了 {@code newInstance}、{@code sets}、{@code values} 和 {@code copy} 方法。
	 * 对于 Record 类型，仅生成 {@code newInstance} 和 {@code values}（在 Record 编解码器上调用
	 * {@code sets} 或 {@code copy} 将抛出 {@link AbstractMethodError}）。
	 * </p>
	 *
	 * @param beanType the target bean class to generate a codec for. 目标 Bean 类。
	 * @param methods  the list of field properties describing getter/setter pairs. 描述 getter/setter 对的字段属性列表。
	 * @param clzName  the fully qualified name for the generated class (dots will be replaced with underscores).
	 *                 生成类的全限定名（点号将被替换为下划线）。
	 * @param record   {@code true} if the bean type is a Java Record. 如果 Bean 类型是 Java Record 则为 {@code true}。
	 * @return the generated and loaded {@link BeanCodec} class, or {@code null} if generation failed.
	 *         生成并加载的 {@link BeanCodec} 类，如果生成失败则返回 {@code null}。
	 */
	public Class<?> generate(Class<?> beanType, List<FieldProperty> methods, String clzName, boolean record) {
		// Preserve the package of the target class so that the generated codec class
		// resides in the same runtime package. This is required to access package-private
		// bean classes (e.g., non-public inner classes).
		int lastDot = clzName.lastIndexOf('.');
		String binaryName;
		String internalName;
		if (lastDot >= 0) {
			String pkg = clzName.substring(0, lastDot);
			String simpleName = clzName.substring(lastDot + 1).replace('.', '_');
			binaryName = pkg + "." + simpleName;
			internalName = pkg.replace('.', '/') + "/" + simpleName;
		} else {
			binaryName = clzName.replace('.', '_');
			internalName = binaryName;
		}
		try {
			byte[] data = generate0(beanType, methods, internalName, record);
			if (debug) {
				File file = new File(System.getProperty("user.dir"), binaryName + ".class");
				IOUtils.saveAsFile(file, data);
				log.info("The codec class {} was generate for debug.", file.getAbsolutePath());
			}
			Class<?> clz = cl.defineClz(binaryName, data);
			log.info("The codec class {} was load.", binaryName);
			return clz;
		} catch (Throwable ex) {
			log.error("ASM generation error for class {}", clzName, ex);
			return null;
		}
	}
	

	/**
	 * Internal method that performs the actual bytecode generation.
	 * <p>
	 * Generates the class structure including constructor, newInstance, sets, copy, and values methods.
	 * </p>
	 * <hr>
	 * 执行实际字节码生成的内部方法。
	 * <p>
	 * 生成类结构，包括构造器、newInstance、sets、copy 和 values 方法。
	 * </p>
	 *
	 * @param beanType the target bean class. 目标 Bean 类。
	 * @param methods  the field properties list. 字段属性列表。
	 * @param clzName  the generated class name. 生成的类名。
	 * @param record   whether the bean is a record type. 是否为 Record 类型。
	 * @return the generated class bytecode. 生成的类字节码。
	 */
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

	/**
	 * Generates the {@code sets(Object[], Object)} method bytecode for regular beans.
	 * This method applies an array of values to the corresponding setters of the bean.
	 * <hr>
	 * 为普通 Bean 生成 {@code sets(Object[], Object)} 方法的字节码。
	 * 该方法将值数组中的元素依次通过 setter 写入 Bean 对象。
	 *
	 * @param beanType the target bean class. 目标 Bean 类。
	 * @param cw       the class writer. 类写入器。
	 * @param methods  the field properties list. 字段属性列表。
	 */
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

	/**
	 * Generates bytecode that reads values from an Object array and invokes the corresponding
	 * setter methods on the bean, with appropriate type casting and unboxing.
	 * <hr>
	 * 生成从 Object 数组中读取值并调用 Bean 对应 setter 方法的字节码，包含必要的类型转换和拆箱操作。
	 *
	 * @param type    the internal name of the bean type. Bean 类型的内部名称。
	 * @param mw      the method visitor for writing bytecode. 用于写入字节码的方法访问器。
	 * @param methods the field properties list. 字段属性列表。
	 */
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

	/**
	 * Generates the {@code newInstance(Object[])} method for Java Record types.
	 * <p>
	 * Since records are immutable, all field values must be passed to the canonical constructor.
	 * This method reorders the values array to match the record's component order and generates
	 * the appropriate constructor invocation bytecode.
	 * </p>
	 * <hr>
	 * 为 Java Record 类型生成 {@code newInstance(Object[])} 方法。
	 * <p>
	 * 由于 Record 是不可变的，所有字段值必须通过规范构造器传入。此方法将值数组重新排列为
	 * Record 组件的顺序，并生成相应的构造器调用字节码。
	 * </p>
	 *
	 * @param beanType the record class. Record 类。
	 * @param cw       the class writer. 类写入器。
	 * @param methods  the field properties list. 字段属性列表。
	 */
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


	/**
	 * Pushes the default value for a field's type onto the operand stack.
	 * For primitive types, pushes the corresponding zero constant (0, 0L, 0.0f, 0.0d).
	 * For reference types, pushes {@code null}.
	 * <hr>
	 * 将字段类型的默认值压入操作数栈。
	 * 对于基本类型，压入对应的零值常量（0、0L、0.0f、0.0d）。
	 * 对于引用类型，压入 {@code null}。
	 *
	 * @param mw            the method visitor. 方法访问器。
	 * @param declaredField the field whose default value should be pushed. 需要压入默认值的字段。
	 */
	private void pushDefaultValueOnStack(MethodVisitor mw,Field declaredField) {
		Class<?> type=declaredField.getType();
		if(type.isPrimitive()) {
			String s = type.getName();
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


	/**
	 * Generates the {@code newInstance(Object[])} method for regular (non-record) bean types.
	 * <p>
	 * Creates a new instance via the no-arg constructor, then applies values through setters.
	 * </p>
	 * <hr>
	 * 为普通（非 Record）Bean 类型生成 {@code newInstance(Object[])} 方法。
	 * <p>
	 * 通过无参构造器创建新实例，然后通过 setter 方法设置属性值。
	 * </p>
	 *
	 * @param beanType the target bean class. 目标 Bean 类。
	 * @param cw       the class writer. 类写入器。
	 * @param methods  the field properties list. 字段属性列表。
	 */
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

	/**
	 * Generates the {@code copy(Object, Object)} method that copies all property values
	 * from a source bean to a target bean by invoking getter on source and setter on target.
	 * <hr>
	 * 生成 {@code copy(Object, Object)} 方法，通过调用源对象的 getter 和目标对象的 setter，
	 * 将所有属性值从源 Bean 复制到目标 Bean。
	 *
	 * @param beanType the target bean class. 目标 Bean 类。
	 * @param cw       the class writer. 类写入器。
	 * @param methods  the field properties list. 字段属性列表。
	 */
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


	/**
	 * Attempts to generate a type conversion (cast or numeric unboxing) from the binding type
	 * to the target type. If the binding type is a numeric subclass and the target is also numeric,
	 * generates an appropriate {@code xxxValue()} call (e.g., {@code intValue()}, {@code longValue()}).
	 * <hr>
	 * 尝试生成从绑定类型到目标类型的类型转换（强制转换或数值拆箱）。如果绑定类型和目标类型都是
	 * Number 的子类，则生成相应的 {@code xxxValue()} 调用（如 {@code intValue()}、{@code longValue()}）。
	 *
	 * @param mw          the method visitor. 方法访问器。
	 * @param target      the desired target type after conversion. 转换后的目标类型。
	 * @param bindingType the actual type of the value on the stack (may be {@code null}).
	 *                    栈上值的实际类型（可能为 {@code null}）。
	 * @return {@code true} if the result on the stack is a primitive value (needs boxing for reference use);
	 *         {@code false} if the result is already a reference type.
	 *         如果栈上结果为基本类型值（需要装箱才能作为引用使用）则返回 {@code true}；
	 *         如果结果已经是引用类型则返回 {@code false}。
	 */
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
