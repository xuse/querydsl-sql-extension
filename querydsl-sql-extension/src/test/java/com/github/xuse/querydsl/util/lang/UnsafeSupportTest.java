package com.github.xuse.querydsl.util.lang;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.lang.FieldAccessor;
import com.github.xuse.querydsl.util.lang.JDKEnvironment;
import com.github.xuse.querydsl.util.lang.LookupFactory;
import com.github.xuse.querydsl.util.lang.ObjectInstantiator;
import com.github.xuse.querydsl.util.lang.ReflectionSupport;
import com.github.xuse.querydsl.util.lang.UnsafeAccess;

/**
 * 跨 JDK 版本测试 unsafe-support 核心功能
 */
public class UnsafeSupportTest {

    // ==================== 测试用内部类 ====================

    static class Person {
        private String name;
        private int age;
        private final long id;
        private static String LABEL = "default";

        // 私有构造函数
        private Person(String name, int age, long id) {
            this.name = name;
            this.age = age;
            this.id = id;
        }

        // 无参构造
        public Person() {
            this.name = "empty";
            this.age = 0;
            this.id = -1;
        }

        private String greet(String greeting) {
            return greeting + ", " + name;
        }

        private static String staticMethod(int x) {
            return "value=" + x;
        }
    }

    static class Child extends Person {
        private String school;

        public Child() {
            super();
            this.school = "unknown";
        }
    }

    // ==================== ObjectInstantiator 测试 ====================

    @Test
    public void testAllocateInstance() {
        // 不调用构造函数创建对象
        Person p = ObjectInstantiator.newInstance(Person.class);
        assertNotNull(p);
        // 字段应为默认值（未调用构造函数）
        assertNull(p.name);
        assertEquals(0, p.age);
        assertEquals(0L, p.id);
    }

    @Test
    public void testAllocateInstanceWithInstantiator() {
        ObjectInstantiator<Person> instantiator = new ObjectInstantiator<>(Person.class);
        Person p1 = instantiator.newInstance();
        Person p2 = instantiator.newInstance();
        assertNotNull(p1);
        assertNotNull(p2);
        assertNotSame(p1, p2);
        assertEquals(Person.class, instantiator.getType());
    }

    // ==================== FieldAccessor 测试 ====================

    @Test
    public void testFieldAccessorReadWrite() {
        Person p = new Person();
        FieldAccessor nameAccessor = FieldAccessor.of(Person.class, "name");
        FieldAccessor ageAccessor = FieldAccessor.of(Person.class, "age");

        // 读取
        assertEquals("empty", nameAccessor.get(p));
        assertEquals(0, ageAccessor.getInt(p));

        // 写入 private 字段
        nameAccessor.set(p, "Alice");
        ageAccessor.setInt(p, 30);
        assertEquals("Alice", nameAccessor.get(p));
        assertEquals(30, ageAccessor.getInt(p));
    }

    @Test
    public void testFieldAccessorFinalField() {
        Person p = new Person();
        FieldAccessor idAccessor = FieldAccessor.of(Person.class, "id");

        // 修改 final 字段
        assertEquals(-1L, idAccessor.getLong(p));
        idAccessor.setLong(p, 12345L);
        assertEquals(12345L, idAccessor.getLong(p));
    }

    @Test
    public void testFieldAccessorStaticField() {
        FieldAccessor labelAccessor = FieldAccessor.of(Person.class, "LABEL");
        assertTrue(labelAccessor.isStatic());

        String original = (String) labelAccessor.get(null);
        labelAccessor.set(null, "modified");
        assertEquals("modified", labelAccessor.get(null));

        // 恢复
        labelAccessor.set(null, original);
    }

    @Test
    public void testFieldAccessorInheritedField() {
        // 从子类访问父类字段
        FieldAccessor nameAccessor = FieldAccessor.of(Child.class, "name");
        Child child = new Child();
        nameAccessor.set(child, "Bob");
        assertEquals("Bob", nameAccessor.get(child));
    }

    @Test
    public void testFieldAccessorAllTypes() {
        AllTypesBean bean = new AllTypesBean();
        FieldAccessor boolAccessor = FieldAccessor.of(AllTypesBean.class, "boolVal");
        FieldAccessor byteAccessor = FieldAccessor.of(AllTypesBean.class, "byteVal");
        FieldAccessor shortAccessor = FieldAccessor.of(AllTypesBean.class, "shortVal");
        FieldAccessor charAccessor = FieldAccessor.of(AllTypesBean.class, "charVal");
        FieldAccessor floatAccessor = FieldAccessor.of(AllTypesBean.class, "floatVal");
        FieldAccessor doubleAccessor = FieldAccessor.of(AllTypesBean.class, "doubleVal");

        boolAccessor.setBoolean(bean, true);
        assertTrue(boolAccessor.getBoolean(bean));

        byteAccessor.setByte(bean, (byte) 42);
        assertEquals((byte) 42, byteAccessor.getByte(bean));

        shortAccessor.setShort(bean, (short) 1000);
        assertEquals((short) 1000, shortAccessor.getShort(bean));

        charAccessor.setChar(bean, 'X');
        assertEquals('X', charAccessor.getChar(bean));

        floatAccessor.setFloat(bean, 3.14f);
        assertEquals(3.14f, floatAccessor.getFloat(bean), 0.001f);

        doubleAccessor.setDouble(bean, 2.718);
        assertEquals(2.718, doubleAccessor.getDouble(bean), 0.001);
    }

    static class AllTypesBean {
        boolean boolVal;
        byte byteVal;
        short shortVal;
        char charVal;
        float floatVal;
        double doubleVal;
    }

    // ==================== UnsafeAccess 测试 ====================

    @Test
    public void testUnsafeAccessBasic() {
        assertNotNull(UnsafeAccess.getUnsafe());

        // allocateInstance
        Person p = UnsafeAccess.allocateInstance(Person.class);
        assertNotNull(p);
        assertNull(p.name);
    }

    @Test
    public void testUnsafeAccessFieldOffset() {
        long offset = UnsafeAccess.objectFieldOffset(Person.class, "age");
        assertTrue(offset > 0);

        Person p = new Person();
        UnsafeAccess.putInt(p, offset, 99);
        assertEquals(99, UnsafeAccess.getInt(p, offset));
    }

    @Test
    public void testUnsafeAccessCAS() {
        long offset = UnsafeAccess.objectFieldOffset(Person.class, "age");
        Person p = new Person();
        // age 初始为 0
        assertTrue(UnsafeAccess.compareAndSwapInt(p, offset, 0, 42));
        assertEquals(42, UnsafeAccess.getInt(p, offset));
        // CAS 失败
        assertFalse(UnsafeAccess.compareAndSwapInt(p, offset, 0, 100));
        assertEquals(42, UnsafeAccess.getInt(p, offset));
    }

    @Test
    public void testReferenceVolatileAndCAS() {
        Person p = new Person();
        long nameOffset = UnsafeAccess.objectFieldOffset(Person.class, "name");

        // getReferenceVolatile
        assertEquals("empty", UnsafeAccess.getReferenceVolatile(p, nameOffset));

        // putReferenceVolatile
        UnsafeAccess.putReferenceVolatile(p, nameOffset, "volatile-write");
        assertEquals("volatile-write", UnsafeAccess.getReferenceVolatile(p, nameOffset));

        // compareAndSetReference
        assertTrue(UnsafeAccess.compareAndSetReference(p, nameOffset, "volatile-write", "cas-write"));
        assertEquals("cas-write", UnsafeAccess.getReferenceVolatile(p, nameOffset));
        assertFalse(UnsafeAccess.compareAndSetReference(p, nameOffset, "wrong", "should-fail"));
        assertEquals("cas-write", UnsafeAccess.getReferenceVolatile(p, nameOffset));
    }

    @Test
    public void testArrayObjectAccess() {
        String[] arr = {"a", "b", "c", "d"};

        // 普通读写
        assertEquals("b", UnsafeAccess.getArrayObject(arr, 1));
        UnsafeAccess.putArrayObject(arr, 1, "B");
        assertEquals("B", arr[1]);

        // volatile 读写
        assertEquals("c", UnsafeAccess.getArrayObjectVolatile(arr, 2));
        UnsafeAccess.putArrayObjectVolatile(arr, 2, "C");
        assertEquals("C", arr[2]);

        // CAS
        assertTrue(UnsafeAccess.compareAndSetArrayObject(arr, 3, "d", "D"));
        assertEquals("D", arr[3]);
        assertFalse(UnsafeAccess.compareAndSetArrayObject(arr, 3, "d", "X"));
        assertEquals("D", arr[3]);
    }

    @Test
    public void testArrayIntAccess() {
        int[] arr = {10, 20, 30, 40};

        // 普通读写
        assertEquals(20, UnsafeAccess.getArrayInt(arr, 1));
        UnsafeAccess.putArrayInt(arr, 1, 200);
        assertEquals(200, arr[1]);

        // volatile 读写
        assertEquals(30, UnsafeAccess.getArrayIntVolatile(arr, 2));
        UnsafeAccess.putArrayIntVolatile(arr, 2, 300);
        assertEquals(300, arr[2]);

        // CAS
        assertTrue(UnsafeAccess.compareAndSetArrayInt(arr, 3, 40, 400));
        assertEquals(400, arr[3]);
        assertFalse(UnsafeAccess.compareAndSetArrayInt(arr, 3, 40, 999));
        assertEquals(400, arr[3]);
    }

    @Test
    public void testArrayLongAccess() {
        long[] arr = {100L, 200L, 300L, 400L};

        // 普通读写
        assertEquals(200L, UnsafeAccess.getArrayLong(arr, 1));
        UnsafeAccess.putArrayLong(arr, 1, 2000L);
        assertEquals(2000L, arr[1]);

        // volatile 读写
        assertEquals(300L, UnsafeAccess.getArrayLongVolatile(arr, 2));
        UnsafeAccess.putArrayLongVolatile(arr, 2, 3000L);
        assertEquals(3000L, arr[2]);

        // CAS
        assertTrue(UnsafeAccess.compareAndSetArrayLong(arr, 3, 400L, 4000L));
        assertEquals(4000L, arr[3]);
        assertFalse(UnsafeAccess.compareAndSetArrayLong(arr, 3, 400L, 9999L));
        assertEquals(4000L, arr[3]);
    }

    @Test
    public void testUnsafeAccessOffHeapMemory() {
        long addr = UnsafeAccess.allocateMemory(64);
        try {
            UnsafeAccess.setMemory(addr, 64, (byte) 0);
            UnsafeAccess.putInt(addr, 12345);
            assertEquals(12345, UnsafeAccess.getInt(addr));
            UnsafeAccess.putLong(addr + 8, Long.MAX_VALUE);
            assertEquals(Long.MAX_VALUE, UnsafeAccess.getLong(addr + 8));
        } finally {
            UnsafeAccess.freeMemory(addr);
        }
    }

    // ==================== LookupFactory 测试 ====================

    @Test
    public void testLookupFactoryTrusted() {
        MethodHandles.Lookup lookup = LookupFactory.trustedLookup();
        assertNotNull(lookup);
    }

    @Test
    public void testLookupFactoryFindConstructor() throws Throwable {
        // 查找私有构造函数
        MethodHandle handle = LookupFactory.findConstructor(Person.class,
                String.class, int.class, long.class);
        assertNotNull(handle);
        Person p = (Person) handle.invoke("test", 25, 100L);
        assertEquals("test", p.name);
        assertEquals(25, p.age);
        assertEquals(100L, p.id);
    }

    @Test
    public void testLookupFactoryFindVirtual() throws Throwable {
        MethodHandle handle = LookupFactory.findVirtual(Person.class, "greet",
                String.class, String.class);
        assertNotNull(handle);
        Person p = new Person("World", 20, 1L);
        String result = (String) handle.invoke(p, "Hello");
        assertEquals("Hello, World", result);
    }

    @Test
    public void testLookupFactoryFindStatic() throws Throwable {
        MethodHandle handle = LookupFactory.findStatic(Person.class, "staticMethod",
                String.class, int.class);
        assertNotNull(handle);
        String result = (String) handle.invoke(42);
        assertEquals("value=42", result);
    }

    // ==================== ReflectionSupport 测试 ====================

    @Test
    public void testReflectionSupportGetSetField() {
        Person p = new Person("Alice", 30, 1L);
        assertEquals("Alice", ReflectionSupport.getFieldValue(p, "name"));
        assertEquals(30, ReflectionSupport.getFieldValue(p, "age"));

        ReflectionSupport.setFieldValue(p, "name", "Bob");
        assertEquals("Bob", ReflectionSupport.getFieldValue(p, "name"));
    }

    @Test
    public void testReflectionSupportStaticField() {
        String original = (String) ReflectionSupport.getStaticFieldValue(Person.class, "LABEL");
        ReflectionSupport.setStaticFieldValue(Person.class, "LABEL", "test-label");
        assertEquals("test-label", ReflectionSupport.getStaticFieldValue(Person.class, "LABEL"));
        // 恢复
        ReflectionSupport.setStaticFieldValue(Person.class, "LABEL", original);
    }

    @Test
    public void testReflectionSupportInvokeMethod() {
        Person p = new Person("World", 20, 1L);
        Object result = ReflectionSupport.invokeMethod(p, "greet",
                new Class[]{String.class}, "Hi");
        assertEquals("Hi, World", result);
    }

    @Test
    public void testReflectionSupportInvokeStaticMethod() {
        Object result = ReflectionSupport.invokeStaticMethod(Person.class, "staticMethod",
                new Class[]{int.class}, 7);
        assertEquals("value=7", result);
    }

    @Test
    public void testReflectionSupportNewInstance() {
        // 通过私有构造函数创建
        Person p = ReflectionSupport.newInstance(Person.class,
                new Class[]{String.class, int.class, long.class},
                "Created", 50, 999L);
        assertEquals("Created", p.name);
        assertEquals(50, p.age);
        assertEquals(999L, p.id);
    }

    // ==================== JDK 环境信息 ====================

    @Test
    public void testJDKVersion() {
        int version = JDKEnvironment.JVM_VERSION;
        assertTrue(version >= 8,"JVM version should be >= 8, got: " + version);
        System.out.println("JVM_VERSION: " + version);
        System.out.println("UNSAFE available: " + (JDKEnvironment.UNSAFE != null));
        System.out.println("IMPL_LOOKUP: " + JDKEnvironment.IMPL_LOOKUP);
    }
}
