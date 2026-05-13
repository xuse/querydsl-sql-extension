# unsafe-support

A lightweight Java library that provides unified access to `sun.misc.Unsafe` and `MethodHandles.Lookup` across JDK 8~25.

Similar to [objenesis](https://github.com/easymock/objenesis), this library is designed to be used as a dependency by other libraries that need low-level JVM operations without worrying about cross-version compatibility.

## Features

- **ObjectInstantiator** — Create object instances without calling constructors (like objenesis)
- **FieldAccessor** — Read/write private and final fields, including inherited and static fields
- **UnsafeAccess** — Unified facade for Unsafe operations: field access, CAS, array element access, off-heap memory, thread parking, memory fences
- **LookupFactory** — Obtain trusted `MethodHandles.Lookup` across JDK versions, find constructors/methods/fields as MethodHandles
- **ReflectionSupport** — Invoke private methods/constructors, bypass module access restrictions on JDK 16+

## Compatibility

| JDK Version | Status |
|-------------|--------|
| 8           | ✅ Tested |
| 11          | ✅ Tested |
| 17          | ✅ Tested |
| 21          | ✅ Tested |
| 22          | ✅ Tested |
| 25          | ✅ Tested |

Works on HotSpot, OpenJ9, and GraalVM.

## Usage

### Maven

```xml
<dependency>
    <groupId>io.github.xuse</groupId>
    <artifactId>unsafe-support</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Quick Start

```java
import io.github.xuse.unsafe.support.*;

// 1. Create instance without constructor
MyClass obj = ObjectInstantiator.newInstance(MyClass.class);

// 2. Access private/final fields
FieldAccessor accessor = FieldAccessor.of(MyClass.class, "secretField");
accessor.set(obj, "newValue");
String value = (String) accessor.get(obj);

// 3. Array element CAS
Object[] array = new Object[10];
UnsafeAccess.compareAndSetArrayObject(array, 0, null, "hello");

// 4. Get trusted Lookup and find private methods
MethodHandle handle = LookupFactory.findVirtual(
    MyClass.class, "privateMethod", String.class, int.class);
String result = (String) handle.invoke(obj, 42);

// 5. Invoke private methods directly
Object ret = ReflectionSupport.invokeMethod(obj, "privateMethod",
    new Class[]{int.class}, 42);
```

## How It Works

The library uses a multi-strategy approach to obtain privileged access:

1. **JDK 8~11**: Reflection with `setAccessible(true)` to get `IMPL_LOOKUP`
2. **JDK 12~15**: `Unsafe.staticFieldOffset` + `Unsafe.getObject` to read `IMPL_LOOKUP`
3. **JDK 16+**: Same Unsafe approach (reflection is blocked by module system)
4. **Trusted Lookup construction**: Version-aware constructor invocation (2-arg on JDK <15, 3-arg on JDK 15+)

## License

Licensed under the [Apache License 2.0](LICENSE).
