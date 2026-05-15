package com.github.xuse.querydsl.util.lang;

import sun.misc.Unsafe;

/**
 * 统一的 Unsafe 操作门面，封装对象创建、字段读写、CAS、内存操作等。 适配 JDK 8~22。
 */
@SuppressWarnings("restriction")
public final class UnsafeAccess {

	private static final Unsafe unsafe = JDKEnvironment.UNSAFE;

	// 数组基础偏移量与位移量（用位移替代乘法，参考 ConcurrentHashMap 实现）
	private static final int AOBJECT_BASE;
	private static final int AOBJECT_SHIFT;
	private static final int AINT_BASE;
	private static final int AINT_SHIFT;
	private static final int ALONG_BASE;
	private static final int ALONG_SHIFT;

	static {
		AOBJECT_BASE = unsafe.arrayBaseOffset(Object[].class);
		AOBJECT_SHIFT = checkAndComputeShift(unsafe.arrayIndexScale(Object[].class), "Object[]");

		AINT_BASE = unsafe.arrayBaseOffset(int[].class);
		AINT_SHIFT = checkAndComputeShift(unsafe.arrayIndexScale(int[].class), "int[]");

		ALONG_BASE = unsafe.arrayBaseOffset(long[].class);
		ALONG_SHIFT = checkAndComputeShift(unsafe.arrayIndexScale(long[].class), "long[]");
	}

	/**
	 * 校验 scale 为 2 的幂次并计算位移量（参考 ConcurrentHashMap 实现）
	 */
	private static int checkAndComputeShift(int scale, String arrayType) {
		if ((scale & (scale - 1)) != 0) {
			throw new ExceptionInInitializerError("Array index scale not a power of two for " + arrayType);
		}
		return 31 - Integer.numberOfLeadingZeros(scale);
	}

	private UnsafeAccess() {
	}

	/**
	 * 获取 Unsafe 实例
	 */
	public static Unsafe getUnsafe() {
		return unsafe;
	}

	// ==================== 对象实例化 ====================

	/**
	 * 不调用构造函数创建对象实例
	 */
	@SuppressWarnings("unchecked")
	public static <T> T allocateInstance(Class<T> clazz) {
		try {
			return (T) unsafe.allocateInstance(clazz);
		} catch (InstantiationException e) {
			throw new UnsafeOperationException("Failed to allocate instance of " + clazz.getName(), e);
		}
	}

	// ==================== 字段偏移量 ====================

	/**
	 * 获取实例字段偏移量
	 */
	public static long objectFieldOffset(Class<?> clazz, String fieldName) {
		try {
			return unsafe.objectFieldOffset(clazz.getDeclaredField(fieldName));
		} catch (NoSuchFieldException e) {
			throw new UnsafeOperationException("Field not found: " + clazz.getName() + "." + fieldName, e);
		}
	}

	/**
	 * 获取静态字段偏移量
	 */
	public static long staticFieldOffset(Class<?> clazz, String fieldName) {
		try {
			return unsafe.staticFieldOffset(clazz.getDeclaredField(fieldName));
		} catch (NoSuchFieldException e) {
			throw new UnsafeOperationException("Field not found: " + clazz.getName() + "." + fieldName, e);
		}
	}

	// ==================== 实例字段读写 ====================

	public static Object getObject(Object obj, long offset) {
		return unsafe.getObject(obj, offset);
	}

	public static void putObject(Object obj, long offset, Object value) {
		unsafe.putObject(obj, offset, value);
	}

	public static int getInt(Object obj, long offset) {
		return unsafe.getInt(obj, offset);
	}

	public static void putInt(Object obj, long offset, int value) {
		unsafe.putInt(obj, offset, value);
	}

	public static long getLong(Object obj, long offset) {
		return unsafe.getLong(obj, offset);
	}

	public static void putLong(Object obj, long offset, long value) {
		unsafe.putLong(obj, offset, value);
	}

	public static boolean getBoolean(Object obj, long offset) {
		return unsafe.getBoolean(obj, offset);
	}

	public static void putBoolean(Object obj, long offset, boolean value) {
		unsafe.putBoolean(obj, offset, value);
	}

	public static byte getByte(Object obj, long offset) {
		return unsafe.getByte(obj, offset);
	}

	public static void putByte(Object obj, long offset, byte value) {
		unsafe.putByte(obj, offset, value);
	}

	public static short getShort(Object obj, long offset) {
		return unsafe.getShort(obj, offset);
	}

	public static void putShort(Object obj, long offset, short value) {
		unsafe.putShort(obj, offset, value);
	}

	public static char getChar(Object obj, long offset) {
		return unsafe.getChar(obj, offset);
	}

	public static void putChar(Object obj, long offset, char value) {
		unsafe.putChar(obj, offset, value);
	}

	public static float getFloat(Object obj, long offset) {
		return unsafe.getFloat(obj, offset);
	}

	public static void putFloat(Object obj, long offset, float value) {
		unsafe.putFloat(obj, offset, value);
	}

	public static double getDouble(Object obj, long offset) {
		return unsafe.getDouble(obj, offset);
	}

	public static void putDouble(Object obj, long offset, double value) {
		unsafe.putDouble(obj, offset, value);
	}

	// ==================== 静态字段读写 ====================

	public static Object getStaticObject(Class<?> clazz, long offset) {
		return unsafe.getObject(clazz, offset);
	}

	public static void putStaticObject(Class<?> clazz, long offset, Object value) {
		unsafe.putObject(clazz, offset, value);
	}

	public static int getStaticInt(Class<?> clazz, long offset) {
		return unsafe.getInt(clazz, offset);
	}

	public static void putStaticInt(Class<?> clazz, long offset, int value) {
		unsafe.putInt(clazz, offset, value);
	}

	public static boolean getStaticBoolean(Class<?> clazz, long offset) {
		return unsafe.getBoolean(clazz, offset);
	}

	public static void putStaticBoolean(Class<?> clazz, long offset, boolean value) {
		unsafe.putBoolean(clazz, offset, value);
	}

	// ==================== Volatile / Reference 读写 ====================

	/**
	 * 以 volatile 语义读取引用字段（等同于 JDK 9+ 的 getReferenceVolatile）
	 */
	public static Object getReferenceVolatile(Object obj, long offset) {
		return unsafe.getObjectVolatile(obj, offset);
	}

	/**
	 * 以 volatile 语义写入引用字段（等同于 JDK 9+ 的 putReferenceVolatile）
	 */
	public static void putReferenceVolatile(Object obj, long offset, Object value) {
		unsafe.putObjectVolatile(obj, offset, value);
	}

	/**
	 * 引用字段 CAS（等同于 JDK 9+ 的 compareAndSetReference）
	 */
	public static boolean compareAndSetReference(Object obj, long offset, Object expected, Object update) {
		return unsafe.compareAndSwapObject(obj, offset, expected, update);
	}

	public static Object getObjectVolatile(Object obj, long offset) {
		return unsafe.getObjectVolatile(obj, offset);
	}

	public static void putObjectVolatile(Object obj, long offset, Object value) {
		unsafe.putObjectVolatile(obj, offset, value);
	}

	public static int getIntVolatile(Object obj, long offset) {
		return unsafe.getIntVolatile(obj, offset);
	}

	public static void putIntVolatile(Object obj, long offset, int value) {
		unsafe.putIntVolatile(obj, offset, value);
	}

	public static long getLongVolatile(Object obj, long offset) {
		return unsafe.getLongVolatile(obj, offset);
	}

	public static void putLongVolatile(Object obj, long offset, long value) {
		unsafe.putLongVolatile(obj, offset, value);
	}

	// ==================== CAS 操作 ====================

	public static boolean compareAndSwapObject(Object obj, long offset, Object expected, Object update) {
		return unsafe.compareAndSwapObject(obj, offset, expected, update);
	}

	public static boolean compareAndSwapInt(Object obj, long offset, int expected, int update) {
		return unsafe.compareAndSwapInt(obj, offset, expected, update);
	}

	public static boolean compareAndSwapLong(Object obj, long offset, long expected, long update) {
		return unsafe.compareAndSwapLong(obj, offset, expected, update);
	}

	// ==================== 数组元素访问 ====================

	/**
	 * 获取数组基础偏移量
	 */
	public static int arrayBaseOffset(Class<?> arrayClass) {
		return unsafe.arrayBaseOffset(arrayClass);
	}

	/**
	 * 获取数组元素间距（字节数）
	 */
	public static int arrayIndexScale(Class<?> arrayClass) {
		return unsafe.arrayIndexScale(arrayClass);
	}

	/**
	 * 计算数组中指定索引的偏移量
	 *
	 * @param arrayClass 数组类型（如 Object[].class, int[].class）
	 * @param index      数组索引
	 * @return 该索引对应的内存偏移量
	 */
	public static long arrayElementOffset(Class<?> arrayClass, int index) {
		int scale = unsafe.arrayIndexScale(arrayClass);
		int shift = 31 - Integer.numberOfLeadingZeros(scale);
		return (long) unsafe.arrayBaseOffset(arrayClass) + ((long) index << shift);
	}

	/**
	 * 通过偏移量获取 Object 数组元素
	 */
	public static Object getArrayObject(Object[] array, int index) {
		return unsafe.getObject(array, ((long) index << AOBJECT_SHIFT) + AOBJECT_BASE);
	}

	/**
	 * 通过偏移量设置 Object 数组元素
	 */
	public static void putArrayObject(Object[] array, int index, Object value) {
		unsafe.putObject(array, ((long) index << AOBJECT_SHIFT) + AOBJECT_BASE, value);
	}

	/**
	 * 以 volatile 语义获取 Object 数组元素（getReferenceVolatile on array）
	 */
	public static Object getArrayObjectVolatile(Object[] array, int index) {
		long offset = ((long) index << AOBJECT_SHIFT) + AOBJECT_BASE;
		return unsafe.getObjectVolatile(array, offset);
	}

	/**
	 * 以 volatile 语义设置 Object 数组元素（putReferenceVolatile on array）
	 */
	public static void putArrayObjectVolatile(Object[] array, int index, Object value) {
		long offset = AOBJECT_BASE + ((long) index << AOBJECT_SHIFT);
		unsafe.putObjectVolatile(array, offset, value);
	}

	/**
	 * Object 数组元素 CAS（compareAndSetReference on array）
	 */
	public static boolean compareAndSetArrayObject(Object[] array, int index, Object expected, Object update) {
		long offset = AOBJECT_BASE + ((long) index << AOBJECT_SHIFT);
		return unsafe.compareAndSwapObject(array, offset, expected, update);
	}

	/**
	 * 通过偏移量获取 int 数组元素
	 */
	public static int getArrayInt(int[] array, int index) {
		long offset = AINT_BASE + ((long) index << AINT_SHIFT);
		return unsafe.getInt(array, offset);
	}

	/**
	 * 通过偏移量设置 int 数组元素
	 */
	public static void putArrayInt(int[] array, int index, int value) {
		long offset = AINT_BASE + ((long) index << AINT_SHIFT);
		unsafe.putInt(array, offset, value);
	}

	/**
	 * 以 volatile 语义获取 int 数组元素
	 */
	public static int getArrayIntVolatile(int[] array, int index) {
		long offset = AINT_BASE + ((long) index << AINT_SHIFT);
		return unsafe.getIntVolatile(array, offset);
	}

	/**
	 * 以 volatile 语义设置 int 数组元素
	 */
	public static void putArrayIntVolatile(int[] array, int index, int value) {
		long offset = AINT_BASE + ((long) index << AINT_SHIFT);
		unsafe.putIntVolatile(array, offset, value);
	}

	/**
	 * int 数组元素 CAS
	 */
	public static boolean compareAndSetArrayInt(int[] array, int index, int expected, int update) {
		long offset = AINT_BASE + ((long) index << AINT_SHIFT);
		return unsafe.compareAndSwapInt(array, offset, expected, update);
	}

	/**
	 * 通过偏移量获取 long 数组元素
	 */
	public static long getArrayLong(long[] array, int index) {
		long offset = ALONG_BASE + ((long) index << ALONG_SHIFT);
		return unsafe.getLong(array, offset);
	}

	/**
	 * 通过偏移量设置 long 数组元素
	 */
	public static void putArrayLong(long[] array, int index, long value) {
		long offset = ALONG_BASE + ((long) index << ALONG_SHIFT);
		unsafe.putLong(array, offset, value);
	}

	/**
	 * 以 volatile 语义获取 long 数组元素
	 */
	public static long getArrayLongVolatile(long[] array, int index) {
		long offset = ALONG_BASE + ((long) index << ALONG_SHIFT);
		return unsafe.getLongVolatile(array, offset);
	}

	/**
	 * 以 volatile 语义设置 long 数组元素
	 */
	public static void putArrayLongVolatile(long[] array, int index, long value) {
		long offset = ALONG_BASE + ((long) index << ALONG_SHIFT);
		unsafe.putLongVolatile(array, offset, value);
	}

	/**
	 * long 数组元素 CAS
	 */
	public static boolean compareAndSetArrayLong(long[] array, int index, long expected, long update) {
		long offset = ALONG_BASE + ((long) index << ALONG_SHIFT);
		return unsafe.compareAndSwapLong(array, offset, expected, update);
	}

	// ==================== 堆外内存操作 ====================

	/**
	 * 分配堆外内存
	 *
	 * @param bytes 字节数
	 * @return 内存地址
	 */
	public static long allocateMemory(long bytes) {
		return unsafe.allocateMemory(bytes);
	}

	/**
	 * 重新分配堆外内存
	 */
	public static long reallocateMemory(long address, long bytes) {
		return unsafe.reallocateMemory(address, bytes);
	}

	/**
	 * 释放堆外内存
	 */
	public static void freeMemory(long address) {
		unsafe.freeMemory(address);
	}

	/**
	 * 设置内存值
	 */
	public static void setMemory(long address, long bytes, byte value) {
		unsafe.setMemory(address, bytes, value);
	}

	/**
	 * 内存拷贝
	 */
	public static void copyMemory(long srcAddress, long destAddress, long bytes) {
		unsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	/**
	 * 直接内存读写
	 */
	public static byte getByte(long address) {
		return unsafe.getByte(address);
	}

	public static void putByte(long address, byte value) {
		unsafe.putByte(address, value);
	}

	public static int getInt(long address) {
		return unsafe.getInt(address);
	}

	public static void putInt(long address, int value) {
		unsafe.putInt(address, value);
	}

	public static long getLong(long address) {
		return unsafe.getLong(address);
	}

	public static void putLong(long address, long value) {
		unsafe.putLong(address, value);
	}

	// ==================== 线程操作 ====================

	public static void park(boolean isAbsolute, long time) {
		unsafe.park(isAbsolute, time);
	}

	public static void unpark(Object thread) {
		unsafe.unpark(thread);
	}

	// ==================== 内存屏障 ====================

	public static void loadFence() {
		unsafe.loadFence();
	}

	public static void storeFence() {
		unsafe.storeFence();
	}

	public static void fullFence() {
		unsafe.fullFence();
	}

	// ==================== 类操作 ====================

	/**
	 * 判断是否需要初始化类
	 */
	public static boolean shouldBeInitialized(Class<?> clazz) {
		return unsafe.shouldBeInitialized(clazz);
	}

	/**
	 * 确保类已初始化
	 */
	public static void ensureClassInitialized(Class<?> clazz) {
		unsafe.ensureClassInitialized(clazz);
	}
}
