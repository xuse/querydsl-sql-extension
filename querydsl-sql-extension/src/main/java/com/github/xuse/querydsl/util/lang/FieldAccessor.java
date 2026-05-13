package com.github.xuse.querydsl.util.lang;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 字段访问器，支持 private/final 字段的读写。
 * <p>
 * 适配 JDK 8~22。主路径使用 Unsafe 的 objectFieldOffset + getObject/putObject，
 * 绕过 JDK 16+ 的模块访问限制。
 * </p>
 *
 * <pre>
 * FieldAccessor accessor = FieldAccessor.of(Foo.class, "name");
 * String name = (String) accessor.get(fooInstance);
 * accessor.set(fooInstance, "newName");
 * </pre>
 */
public class FieldAccessor {

    private static final Unsafe unsafe = JDKEnvironment.UNSAFE;

    private final Field field;
    private final long offset;
    private final boolean isStatic;
    private final Class<?> declaringClass;
    private final Class<?> fieldType;

    private FieldAccessor(Field field) {
        this.field = field;
        this.declaringClass = field.getDeclaringClass();
        this.fieldType = field.getType();
        this.isStatic = Modifier.isStatic(field.getModifiers());
        if (isStatic) {
            this.offset = unsafe.staticFieldOffset(field);
        } else {
            this.offset = unsafe.objectFieldOffset(field);
        }
    }

    /**
     * 创建字段访问器
     *
     * @param clazz     声明字段的类
     * @param fieldName 字段名
     * @return FieldAccessor
     */
    public static FieldAccessor of(Class<?> clazz, String fieldName) {
        Field field = findField(clazz, fieldName);
        if (field == null) {
            throw new UnsafeOperationException("Field not found: " + clazz.getName() + "." + fieldName);
        }
        return new FieldAccessor(field);
    }

    /**
     * 从 Field 对象创建访问器
     */
    public static FieldAccessor of(Field field) {
        if (field == null) {
            throw new IllegalArgumentException("field must not be null");
        }
        return new FieldAccessor(field);
    }

    // ==================== 通用读写 ====================

    /**
     * 读取字段值（自动装箱）
     */
    public Object get(Object obj) {
        Object base = isStatic ? declaringClass : obj;
        if (fieldType == int.class) {
            return unsafe.getInt(base, offset);
        } else if (fieldType == long.class) {
            return unsafe.getLong(base, offset);
        } else if (fieldType == boolean.class) {
            return unsafe.getBoolean(base, offset);
        } else if (fieldType == byte.class) {
            return unsafe.getByte(base, offset);
        } else if (fieldType == short.class) {
            return unsafe.getShort(base, offset);
        } else if (fieldType == char.class) {
            return unsafe.getChar(base, offset);
        } else if (fieldType == float.class) {
            return unsafe.getFloat(base, offset);
        } else if (fieldType == double.class) {
            return unsafe.getDouble(base, offset);
        } else {
            return unsafe.getObject(base, offset);
        }
    }

    /**
     * 写入字段值（支持 final 字段）
     */
    public void set(Object obj, Object value) {
        Object base = isStatic ? declaringClass : obj;
        if (fieldType == int.class) {
            unsafe.putInt(base, offset, (Integer) value);
        } else if (fieldType == long.class) {
            unsafe.putLong(base, offset, (Long) value);
        } else if (fieldType == boolean.class) {
            unsafe.putBoolean(base, offset, (Boolean) value);
        } else if (fieldType == byte.class) {
            unsafe.putByte(base, offset, (Byte) value);
        } else if (fieldType == short.class) {
            unsafe.putShort(base, offset, (Short) value);
        } else if (fieldType == char.class) {
            unsafe.putChar(base, offset, (Character) value);
        } else if (fieldType == float.class) {
            unsafe.putFloat(base, offset, (Float) value);
        } else if (fieldType == double.class) {
            unsafe.putDouble(base, offset, (Double) value);
        } else {
            unsafe.putObject(base, offset, value);
        }
    }

    // ==================== 类型化读写（避免装箱） ====================

    public int getInt(Object obj) {
        return unsafe.getInt(isStatic ? declaringClass : obj, offset);
    }

    public void setInt(Object obj, int value) {
        unsafe.putInt(isStatic ? declaringClass : obj, offset, value);
    }

    public long getLong(Object obj) {
        return unsafe.getLong(isStatic ? declaringClass : obj, offset);
    }

    public void setLong(Object obj, long value) {
        unsafe.putLong(isStatic ? declaringClass : obj, offset, value);
    }

    public boolean getBoolean(Object obj) {
        return unsafe.getBoolean(isStatic ? declaringClass : obj, offset);
    }

    public void setBoolean(Object obj, boolean value) {
        unsafe.putBoolean(isStatic ? declaringClass : obj, offset, value);
    }

    public byte getByte(Object obj) {
        return unsafe.getByte(isStatic ? declaringClass : obj, offset);
    }

    public void setByte(Object obj, byte value) {
        unsafe.putByte(isStatic ? declaringClass : obj, offset, value);
    }

    public short getShort(Object obj) {
        return unsafe.getShort(isStatic ? declaringClass : obj, offset);
    }

    public void setShort(Object obj, short value) {
        unsafe.putShort(isStatic ? declaringClass : obj, offset, value);
    }

    public char getChar(Object obj) {
        return unsafe.getChar(isStatic ? declaringClass : obj, offset);
    }

    public void setChar(Object obj, char value) {
        unsafe.putChar(isStatic ? declaringClass : obj, offset, value);
    }

    public float getFloat(Object obj) {
        return unsafe.getFloat(isStatic ? declaringClass : obj, offset);
    }

    public void setFloat(Object obj, float value) {
        unsafe.putFloat(isStatic ? declaringClass : obj, offset, value);
    }

    public double getDouble(Object obj) {
        return unsafe.getDouble(isStatic ? declaringClass : obj, offset);
    }

    public void setDouble(Object obj, double value) {
        unsafe.putDouble(isStatic ? declaringClass : obj, offset, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Object obj) {
        return (T) unsafe.getObject(isStatic ? declaringClass : obj, offset);
    }

    public void setObject(Object obj, Object value) {
        unsafe.putObject(isStatic ? declaringClass : obj, offset, value);
    }

    // ==================== Volatile 读写 ====================

    public Object getVolatile(Object obj) {
        Object base = isStatic ? declaringClass : obj;
        if (fieldType == int.class) {
            return unsafe.getIntVolatile(base, offset);
        } else if (fieldType == long.class) {
            return unsafe.getLongVolatile(base, offset);
        } else {
            return unsafe.getObjectVolatile(base, offset);
        }
    }

    public void setVolatile(Object obj, Object value) {
        Object base = isStatic ? declaringClass : obj;
        if (fieldType == int.class) {
            unsafe.putIntVolatile(base, offset, (Integer) value);
        } else if (fieldType == long.class) {
            unsafe.putLongVolatile(base, offset, (Long) value);
        } else {
            unsafe.putObjectVolatile(base, offset, value);
        }
    }

    // ==================== CAS ====================

    public boolean compareAndSwapObject(Object obj, Object expected, Object update) {
        return unsafe.compareAndSwapObject(isStatic ? declaringClass : obj, offset, expected, update);
    }

    public boolean compareAndSwapInt(Object obj, int expected, int update) {
        return unsafe.compareAndSwapInt(isStatic ? declaringClass : obj, offset, expected, update);
    }

    public boolean compareAndSwapLong(Object obj, long expected, long update) {
        return unsafe.compareAndSwapLong(isStatic ? declaringClass : obj, offset, expected, update);
    }

    // ==================== 元信息 ====================

    public Field getField() {
        return field;
    }

    public long getOffset() {
        return offset;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    // ==================== 内部方法 ====================

    /**
     * 在类继承链中查找字段
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
