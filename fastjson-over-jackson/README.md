# fastjson-over-jackson

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Jackson](https://img.shields.io/badge/Jackson-2.13.5-green.svg)](https://github.com/FasterXML/jackson)

A Fastjson API compatibility layer built on Jackson. It lets existing projects
migrate from fastjson to Jackson by changing imports only, without rewriting
call sites.

[中文文档](README_cn.md)

## Table of Contents

- [Background](#background)
- [Getting Started](#getting-started)
- [Supported API](#supported-api)
- [Changes to Jackson Defaults](#changes-to-jackson-defaults)
- [Intentional Differences](#intentional-differences)
- [Migration Guide](#migration-guide)
- [Advanced Usage](#advanced-usage)
- [Build and Test](#build-and-test)
- [Design Principles](#design-principles)
- [Contributing](#contributing)
- [License](#license)

## Background

Fastjson 1.x has had repeated deserialization security issues, pushing many
teams to migrate to Jackson. The two APIs differ substantially, so rewriting
every call site is costly and error-prone.

This project provides an API that mirrors fastjson's names and signatures while
delegating to Jackson underneath, reducing migration to an import swap. It also
aligns a number of behavioral differences between Jackson and fastjson to avoid
the failure mode where code compiles cleanly but behaves differently at runtime.

Compatibility is **selective**, not exhaustive. See
[Design Principles](#design-principles) for the reasoning behind what is and
isn't emulated.

## Getting Started

### Add the dependency

```xml
<dependency>
    <groupId>com.github.xuse</groupId>
    <artifactId>fastjson-over-jackson</artifactId>
    <version>1.0.4</version>
</dependency>
```

Requires JDK 8 or later. The only transitive dependency is `jackson-databind`;
java.time support is built in, so `jackson-datatype-jsr310` is not needed.

### Swap imports

```java
// Before
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;

// After
import io.github.xuse.fastjson.adapter.JSON;
import io.github.xuse.fastjson.adapter.JSONObject;
import io.github.xuse.fastjson.adapter.JSONArray;
import io.github.xuse.fastjson.adapter.JSONException;
import io.github.xuse.fastjson.adapter.JSONField;
```

### Examples

```java
// Serialize
String json = JSON.toJSONString(user);

// Deserialize into a POJO
User user = JSON.parseObject(json, User.class);

// Deserialize into JSONObject and navigate
JSONObject obj = JSON.parseObject(responseBody);
String iccid = obj.getJSONObject("data").getString("iccid");
int total = obj.getJSONObject("data").getJSONObject("usage").getIntValue("total");

// Arrays
List<User> users = JSON.parseArray(jsonArray, User.class);

// Generics (note: use Jackson's TypeReference)
Map<String, String> map = JSON.parseObject(
        json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
```

## Supported API

### JSON

| Method | Description |
|---|---|
| `toJSONString(Object)` | Serialize to string |
| `toJSONString(Object, boolean)` | Pretty-print support |
| `toJSONStringWithDateFormat(Object, String)` | Serialize with a date pattern |
| `toJSONBytes(Object)` | Serialize to a byte array |
| `writeJSONString(OutputStream/Writer, Object)` | Write to a stream |
| `parseObject(String)` | Parse into `JSONObject` |
| `parseObject(String, Class)` | Parse into a given type |
| `parseObject(String, Type)` | Parse by `Type` |
| `parseObject(String, TypeReference)` | Generic parse |
| `parseObject(InputStream/byte[], Class/Type)` | Parse from stream or bytes |
| `parseArray(String)` | Parse into `JSONArray` |
| `parseArray(String, Class)` | Parse into `List<T>` |
| `parse(String)` | Parse into `JSONObject` / `JSONArray` / scalar |
| `toJavaObject(JSONObject, Class)` | Convert `JSONObject` to a POJO |
| `setDefaultDateFormat(String)` | Set the global date format |
| `getObjectMapper()` | Access the internal `ObjectMapper` |

### JSONObject

Accessors: `get`, `getString`, `getInteger` / `getIntValue`, `getLong` /
`getLongValue`, `getDouble` / `getDoubleValue`, `getFloat` / `getFloatValue`,
`getBoolean` / `getBooleanValue`, `getBigDecimal`, `getBigInteger`,
`getJSONObject`, `getJSONArray`.

Container methods: `put`, `remove`, `containsKey`, `size`, `isEmpty`, `keySet`,
`entrySet`, `getInnerMap`, `toJavaObject`.

### JSONArray

`get`, `getString`, `getInteger` / `getIntValue`, `getLong` / `getLongValue`,
`getDouble` / `getDoubleValue`, `getFloat` / `getFloatValue`, `getBoolean` /
`getBooleanValue`, `getBigDecimal`, `getBigInteger`, `getJSONObject`,
`getJSONArray`, `add`, `remove`, `size`, `isEmpty`, `toJavaList`. Implements
`Iterable<Object>`.

### @JSONField

Supports `name`, `alternateNames`, `format`, `serialize`, and `deserialize`.
If the original `com.alibaba.fastjson.annotation.JSONField` is still on the
classpath, it is recognized as well, which enables incremental migration.

## Changes to Jackson Defaults

All changes live in `JSON.createMapper()` and `FastjsonCompatModule`.

### Parsing (deserialization)

| # | Change | Jackson default | This library | fastjson feature |
|---|---|---|---|---|
| 1 | Floating-point type | `Double` | `BigDecimal` | `Feature.UseBigDecimal` |
| 2 | Integers beyond `long` | `String` (via `Object`) | `BigInteger` | same |
| 3 | Single quotes | rejected | accepts `{'a':'b'}` | `Feature.AllowSingleQuotes` |
| 4 | Unquoted field names | rejected | accepts `{a:1}` | `Feature.AllowUnQuotedFieldNames` |
| 5 | Unknown fields | throws | ignored | `Feature.IgnoreNotMatch` |
| 6 | Trailing garbage | silently ignored | throws `JSONException` | fastjson throws |
| 7 | Untyped containers | `LinkedHashMap` / `ArrayList` | `JSONObject` / `JSONArray` | fastjson does the same |

The corresponding Jackson switches are `USE_BIG_DECIMAL_FOR_FLOATS`,
`ALLOW_SINGLE_QUOTES`, `ALLOW_UNQUOTED_FIELD_NAMES`,
`FAIL_ON_UNKNOWN_PROPERTIES`, and `FAIL_ON_TRAILING_TOKENS`.

Item 7 applies when the target type is `Object`, a raw `List`, or a raw `Map`
(no type parameters), and is implemented by `UntypedJSONDeserializer`. Without
it, the common pre-migration cast `(JSONObject) rawList.get(0)` compiles fine
and throws `ClassCastException` at runtime. Collections **with** type parameters
(for example `List<Map<String, Object>>`) resolve to the declared types, matching
Jackson. Scalar values are unaffected.

### Output (serialization)

| # | Change | Jackson default | This library |
|---|---|---|---|
| 8 | `null` fields | written | omitted (`Include.NON_NULL`) |
| 9 | `transient` fields | still written when a getter exists | never written |
| 10 | Empty beans | throws | writes `{}` |
| 11 | `JSONObject` / `JSONArray` as a value | bean introspection leaks `inner` / `innerMap` / `empty` | writes the actual JSON content |

Item 9 corresponds to fastjson's `SkipTransientField` and is implemented via
`PROPAGATE_TRANSIENT_MARKER`. Without item 11, a `JSONObject` used as a field
value serializes as `{"empty":false,"innerMap":{...},"inner":{...}}`; this is
corrected by `JSONContainerSerializer`.

### Date and time

| # | Type | Change |
|---|---|---|
| 12 | `java.util.Date` | writes timestamps by default; lenient parsing on read |
| 13 | java.time types | built-in fastjson-compatible codecs, no `jackson-datatype-jsr310` |

Lenient `Date` parsing lives in `FlexibleDateDeserializer` and accepts epoch
seconds and milliseconds, `yyyyMMdd`, `yyyy-MM-dd`, `yyyyMMddHHmm`,
`yyyyMMddHHmmss`, `yyyy-MM-dd HH:mm`, `yyyy-MM-dd HH:mm:ss`, and ISO-8601
(including milliseconds and zone offsets).

java.time support lives in `JavaTimeCodec` and covers `LocalDate`,
`LocalDateTime`, `LocalTime`, `Instant`, `ZonedDateTime`, and `OffsetDateTime`.
Serialization emits ISO-8601 strings by default; `LocalDateTime` selects
`yyyy-MM-dd'T'HH:mm:ss`, `...SSS`, or `...SSSSSSSSS` based on nanosecond
precision, matching fastjson's `Jdk8DateCodec`. Parsing is equally lenient.

`JSON.setDefaultDateFormat(String)` affects `java.util.Date` only and does not
change the ISO defaults for java.time types, which matches fastjson.

### Annotations

| # | Change |
|---|---|
| 14 | Recognizes `@JSONField` attributes `name`, `alternateNames`, `format`, `serialize`, `deserialize` |
| 15 | Also recognizes the original `com.alibaba.fastjson.annotation.JSONField` when present |

Implemented by `FastjsonAnnotationIntrospector` and `NativeJSONFieldAdapter`.
Native Jackson annotations take precedence when both are present.

### Accessor type coercion

The `getXxx` methods on `JSONObject` / `JSONArray` route through `TypeCast`,
which reproduces fastjson's `TypeUtils` semantics rather than Jackson's
`JsonNode.asInt()` / `asBoolean()`:

| # | Case | Jackson `asXxx()` | This library |
|---|---|---|---|
| 16 | `getBoolean("1")` | `false` | `true` |
| 17 | Boolean strings | only `true` | `1`/`Y`/`T`/`true` → true; `0`/`F`/`N`/`false` → false |
| 18 | Number to boolean | — | `1` → true, otherwise false |
| 19 | `""`, `"null"`, `"NULL"` | `0` / `false` | `null` |
| 20 | Grouping separators, e.g. `"1,234"` | `0` | `1234` |
| 21 | Unconvertible input, e.g. `"abc"` | silently returns `0` / `false` | throws |

Item 21 is among the more consequential differences: Jackson quietly coerces bad
data to zero values, whereas this library preserves fastjson's throwing behavior
so problems are not swallowed.

### Exception types

| # | Case | Change |
|---|---|---|
| 22 | `parseObject("[1,2]")`, `parseArray("{...}")` | throws `JSONException` instead of `ClassCastException` |

`parseObject(String)` and `parseArray(String)` have fixed return types of
`JSONObject` and `JSONArray`. When the root node type does not match, the
previous hard cast raised an undeclared `ClassCastException`.

## Intentional Differences

The following fastjson behaviors are **not** emulated.

| Item | fastjson | This library | Rationale |
|---|---|---|---|
| Field order (`SortField`) | alphabetical by field name | Jackson declaration order | JSON object members carry no ordering semantics |
| Non-String map keys | unquoted, e.g. `{2:"two"}` | quoted, `{"2":"two"}` | fastjson emits non-conforming JSON that standard parsers cannot read back |
| `JSONObject` / `JSONArray` types | implement `Map` / `List` | do not | compile-time difference; developers hit it immediately during migration |
| `TypeReference` | `com.alibaba.fastjson.TypeReference` | use Jackson's `TypeReference` | same as above |
| Single value to collection | `"a"` → `["a"]` | throws | edge case, out of scope |
| Empty string to collection | `""` → `null` | throws | same as above |
| Implementation for `Map`-typed members | `HashMap` | `LinkedHashMap` | preserving insertion order is more useful |
| Assigning `DEFFAULT_DATE_FORMAT` directly | takes effect | no effect; call `setDefaultDateFormat()` | the `ObjectMapper` is built at class-load time |
| `@JSONType`, `SerializerFeature` arguments, `ParserConfig`, AutoType | supported | not supported | out of scope; AutoType is itself a security liability |

## Migration Guide

### Points to review

After swapping imports, these still need attention:

1. **Generic parsing.** Replace `com.alibaba.fastjson.TypeReference` with
   `com.fasterxml.jackson.core.type.TypeReference`. The compiler will flag this.
2. **`JSONObject` used as a `Map`.** Code such as
   `Map<String, Object> m = JSON.parseObject(s);` will not compile; call
   `getInnerMap()` explicitly.
3. **Field order.** Regenerate baselines if you have snapshot tests, payload
   comparisons, or signature computations that depend on serialized field order.
4. **Floating-point types.** `JSON.parseObject(s).get("x")` now returns
   `BigDecimal`; an existing cast to `Double` will throw `ClassCastException`.
5. **Global date format.** Replace `JSON.DEFFAULT_DATE_FORMAT = "..."` with
   `JSON.setDefaultDateFormat("...")`.

### Incremental migration

The original `@JSONField` is still recognized, so annotations can stay as they
are. Start by swapping only the `JSON` / `JSONObject` / `JSONArray` imports and
proceed in batches.

## Advanced Usage

To reach Jackson's native capabilities, retrieve the internal `ObjectMapper`:

```java
ObjectMapper mapper = JSON.getObjectMapper();
```

This instance is shared globally and thread-safe. **Do not modify its
configuration**, as that affects every caller. If you need different settings,
call `copy()` first.

You can also attach the compatibility behavior to your own `ObjectMapper`:

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new FastjsonCompatModule());
```

## Build and Test

```bash
# Build
mvn clean package

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=JavaTimeCompatibilityTest
```

The testing approach is **item-by-item comparison** against native fastjson: the
same input goes through both fastjson and this compatibility layer, and the
assertions require matching results rather than merely checking expected values.
Fastjson is therefore a test-scope dependency.

| Test class | Coverage |
|---|---|
| `JsonCompatibilityTest` | core API equivalence, `@JSONField` |
| `JavaTimeCompatibilityTest` | java.time serialization, parsing, `format` |
| `DefaultBehaviorCompatibilityTest` | fastjson default features, `TypeCast` semantics |
| `RawCollectionElementTypeTest` | raw collection element types, round-trip serialization |
| `NativeJSONFieldAnnotationTest` | native `@JSONField` recognition |
| `MissingCoverageTest` | edge cases and supplementary coverage |

## Design Principles

1. **Compatibility is selective.** Only behaviors that migrations actually hit
   are emulated, each evaluated on its own merits.
2. **Prioritize silent failures.** Differences the compiler surfaces (such as the
   missing `Map` interface) are left to developers; the focus is on cases that
   compile cleanly but change runtime behavior.
3. **Do not sacrifice JSON conformance.** Non-conforming fastjson output, such as
   unquoted map keys, is not reproduced.
4. **No AutoType.** That feature is the main source of fastjson's historical
   vulnerabilities and is not provided.
5. **Every decision is pinned by a test.** Deliberate non-alignment is covered by
   inverted assertions so it is not later "fixed" as a defect.

## Contributing

Issues and pull requests are welcome. Before opening a PR, please confirm:

- `mvn test` passes;
- new alignments include comparison tests against native fastjson;
- if a fastjson behavior is evaluated and deliberately **not** aligned, add an
  inverted assertion test and document the reasoning.

## License

Released under the [Apache License 2.0](LICENSE).

```
Copyright 2026 Joey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
