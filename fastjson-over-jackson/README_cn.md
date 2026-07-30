# fastjson-over-jackson

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Jackson](https://img.shields.io/badge/Jackson-2.13.5-green.svg)](https://github.com/FasterXML/jackson)

以 Jackson 为底层实现的 Fastjson API 兼容层。用于把既有项目从 fastjson 迁移到
Jackson，业务代码只需替换 import，无需改写调用逻辑。

[English](README.md)

## 目录

- [背景](#背景)
- [快速开始](#快速开始)
- [已支持的 API](#已支持的-api)
- [对 Jackson 默认行为的改动](#对-jackson-默认行为的改动)
- [刻意保留的差异](#刻意保留的差异)
- [迁移指南](#迁移指南)
- [高级用法](#高级用法)
- [构建与测试](#构建与测试)
- [设计原则](#设计原则)
- [参与贡献](#参与贡献)
- [License](#license)

## 背景

fastjson 1.x 历史上多次出现反序列化安全问题，许多团队因此需要迁移到 Jackson。
但两者 API 差异较大，逐个改写调用点成本高、易出错。

本项目提供一套与 fastjson 同名同签名的 API，内部委托给 Jackson，让迁移动作
收敛为替换 import。同时对若干 Jackson 与 fastjson 的行为差异做了对齐，
避免"编译通过但运行时行为变了"的隐性故障。

本项目**选择性**模拟 fastjson 行为，不追求完整复刻。取舍原则见
[设计原则](#设计原则)。

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>com.github.xuse</groupId>
    <artifactId>fastjson-over-jackson</artifactId>
    <version>1.0.4</version>
</dependency>
```

需要 JDK 8 或更高版本。传递依赖仅 `jackson-databind`，java.time 支持已内置，
无需额外引入 `jackson-datatype-jsr310`。

### 替换 import

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

### 用法示例

```java
// 序列化
String json = JSON.toJSONString(user);

// 反序列化为 POJO
User user = JSON.parseObject(json, User.class);

// 反序列化为 JSONObject 并链式取值
JSONObject obj = JSON.parseObject(responseBody);
String iccid = obj.getJSONObject("data").getString("iccid");
int total = obj.getJSONObject("data").getJSONObject("usage").getIntValue("total");

// 数组
List<User> users = JSON.parseArray(jsonArray, User.class);

// 泛型（注意：使用 Jackson 的 TypeReference）
Map<String, String> map = JSON.parseObject(
        json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
```

## 已支持的 API

### JSON

| 方法 | 说明 |
|---|---|
| `toJSONString(Object)` | 序列化为字符串 |
| `toJSONString(Object, boolean)` | 支持格式化输出 |
| `toJSONStringWithDateFormat(Object, String)` | 指定日期格式序列化 |
| `toJSONBytes(Object)` | 序列化为字节数组 |
| `writeJSONString(OutputStream/Writer, Object)` | 写入流 |
| `parseObject(String)` | 解析为 `JSONObject` |
| `parseObject(String, Class)` | 解析为指定类型 |
| `parseObject(String, Type)` | 按 `Type` 解析 |
| `parseObject(String, TypeReference)` | 泛型解析 |
| `parseObject(InputStream/byte[], Class/Type)` | 从流或字节数组解析 |
| `parseArray(String)` | 解析为 `JSONArray` |
| `parseArray(String, Class)` | 解析为 `List<T>` |
| `parse(String)` | 解析为 `JSONObject` / `JSONArray` / 标量 |
| `toJavaObject(JSONObject, Class)` | `JSONObject` 转 POJO |
| `setDefaultDateFormat(String)` | 设置全局日期格式 |
| `getObjectMapper()` | 取出内部 `ObjectMapper` |

### JSONObject

取值方法族 `get`、`getString`、`getInteger` / `getIntValue`、`getLong` /
`getLongValue`、`getDouble` / `getDoubleValue`、`getFloat` / `getFloatValue`、
`getBoolean` / `getBooleanValue`、`getBigDecimal`、`getBigInteger`、
`getJSONObject`、`getJSONArray`；

容器方法 `put`、`remove`、`containsKey`、`size`、`isEmpty`、`keySet`、
`entrySet`、`getInnerMap`、`toJavaObject`。

### JSONArray

`get`、`getString`、`getInteger` / `getIntValue`、`getLong` / `getLongValue`、
`getDouble` / `getDoubleValue`、`getFloat` / `getFloatValue`、`getBoolean` /
`getBooleanValue`、`getBigDecimal`、`getBigInteger`、`getJSONObject`、
`getJSONArray`、`add`、`remove`、`size`、`isEmpty`、`toJavaList`，
并实现 `Iterable<Object>`。

### @JSONField

支持 `name`、`alternateNames`、`format`、`serialize`、`deserialize`。
若 classpath 中仍存在原生 `com.alibaba.fastjson.annotation.JSONField`，
也会被一并识别，便于渐进式迁移。

## 对 Jackson 默认行为的改动

改动集中在 `JSON.createMapper()` 与 `FastjsonCompatModule`。

### 解析（反序列化）

| # | 改动 | Jackson 默认 | 本框架行为 | 对应 fastjson 开关 |
|---|---|---|---|---|
| 1 | 浮点数类型 | `Double` | `BigDecimal` | `Feature.UseBigDecimal` |
| 2 | 超出 long 范围的整数 | `String`（走 `Object` 时） | `BigInteger` | 同上 |
| 3 | 单引号 | 拒绝 | 接受 `{'a':'b'}` | `Feature.AllowSingleQuotes` |
| 4 | 无引号字段名 | 拒绝 | 接受 `{a:1}` | `Feature.AllowUnQuotedFieldNames` |
| 5 | 未知字段 | 抛异常 | 忽略 | `Feature.IgnoreNotMatch` |
| 6 | 尾部残留字符 | 静默忽略 | 抛 `JSONException` | fastjson 报错 |
| 7 | 无类型容器 | `LinkedHashMap` / `ArrayList` | `JSONObject` / `JSONArray` | fastjson 同此 |

对应 Jackson 开关依次为 `USE_BIG_DECIMAL_FOR_FLOATS`、`ALLOW_SINGLE_QUOTES`、
`ALLOW_UNQUOTED_FIELD_NAMES`、`FAIL_ON_UNKNOWN_PROPERTIES`、
`FAIL_ON_TRAILING_TOKENS`。

第 7 项指目标类型为 `Object`、裸 `List`、裸 `Map`（无泛型参数）的场景，由
`UntypedJSONDeserializer` 实现。这样迁移前常见的
`(JSONObject) rawList.get(0)` 强转才不会在运行时抛 `ClassCastException`。
**声明了泛型的集合**（如 `List<Map<String, Object>>`）按声明类型还原，
与 Jackson 一致；标量值不受影响。

### 输出（序列化）

| # | 改动 | Jackson 默认 | 本框架行为 |
|---|---|---|---|
| 8 | `null` 字段 | 输出 | 省略（`Include.NON_NULL`） |
| 9 | `transient` 字段 | 有 getter 时仍输出 | 一律不输出 |
| 10 | 空 bean | 抛异常 | 输出 `{}` |
| 11 | `JSONObject` / `JSONArray` 作为值 | 按 bean 内省，泄漏 `inner` / `innerMap` / `empty` | 输出真实 JSON 内容 |

第 9 项对应 fastjson 的 `SkipTransientField`，靠 `PROPAGATE_TRANSIENT_MARKER`
实现。第 11 项若不处理，`JSONObject` 当字段值会输出
`{"empty":false,"innerMap":{...},"inner":{...}}`，由 `JSONContainerSerializer`
修正。

### 日期与时间

| # | 类型 | 改动 |
|---|---|---|
| 12 | `java.util.Date` | 默认输出时间戳；反序列化宽松接受多种格式 |
| 13 | java.time 类型 | 无需 `jackson-datatype-jsr310`，内置 fastjson 兼容编解码 |

`Date` 的宽松解析见 `FlexibleDateDeserializer`，接受 epoch 秒/毫秒、
`yyyyMMdd`、`yyyy-MM-dd`、`yyyyMMddHHmm`、`yyyyMMddHHmmss`、
`yyyy-MM-dd HH:mm`、`yyyy-MM-dd HH:mm:ss`、ISO-8601（含毫秒与时区偏移）等形式。

java.time 支持见 `JavaTimeCodec`，覆盖 `LocalDate`、`LocalDateTime`、
`LocalTime`、`Instant`、`ZonedDateTime`、`OffsetDateTime`。序列化默认输出
ISO-8601 字符串，其中 `LocalDateTime` 按纳秒精度选择
`yyyy-MM-dd'T'HH:mm:ss`、`...SSS` 或 `...SSSSSSSSS`，与 fastjson
`Jdk8DateCodec` 一致。反序列化同样宽松。

`JSON.setDefaultDateFormat(String)` 只影响 `java.util.Date`，不改变 java.time
类型的 ISO 默认输出，此点与 fastjson 相同。

### 注解

| # | 改动 |
|---|---|
| 14 | 识别 `@JSONField` 的 `name`、`alternateNames`、`format`、`serialize`、`deserialize` |
| 15 | 同时识别原生 `com.alibaba.fastjson.annotation.JSONField`（若 classpath 存在） |

由 `FastjsonAnnotationIntrospector` 与 `NativeJSONFieldAdapter` 实现。
Jackson 原生注解优先级更高，两者共存时以 Jackson 注解为准。

### 取值方法的类型转换

`JSONObject` / `JSONArray` 的 `getXxx` 走 `TypeCast`，复刻 fastjson `TypeUtils`
语义，而非 Jackson 的 `JsonNode.asInt()` / `asBoolean()`：

| # | 场景 | Jackson `asXxx()` | 本框架行为 |
|---|---|---|---|
| 16 | `getBoolean("1")` | `false` | `true` |
| 17 | 布尔字符串 | 仅识别 `true` | `1`/`Y`/`T`/`true` → true，`0`/`F`/`N`/`false` → false |
| 18 | 数字转布尔 | — | `1` → true，其余 → false |
| 19 | `""`、`"null"`、`"NULL"` | `0` / `false` | `null` |
| 20 | 千分位如 `"1,234"` | `0` | `1234` |
| 21 | 无法转换如 `"abc"` | 静默返回 `0` / `false` | 抛异常 |

第 21 项是差异中较关键的一处：Jackson 会把脏数据静默转成零值，
本框架保持 fastjson 的抛错行为，避免问题被吞掉。

### 异常类型

| # | 场景 | 改动 |
|---|---|---|
| 22 | `parseObject("[1,2]")`、`parseArray("{...}")` | 抛 `JSONException` 而非 `ClassCastException` |

`parseObject(String)` / `parseArray(String)` 的返回类型固定为 `JSONObject` /
`JSONArray`，根节点类型不符时原先硬转会抛出未声明的 `ClassCastException`。

## 刻意保留的差异

以下 fastjson 行为**未**模拟。

| 项 | fastjson | 本框架 | 原因 |
|---|---|---|---|
| 字段顺序（`SortField`） | 字段名字典序 | Jackson 声明顺序 | JSON 对象成员无顺序语义 |
| 非 String 的 Map key | 不加引号，如 `{2:"two"}` | 加引号 `{"2":"two"}` | fastjson 输出非合规 JSON，无法被标准解析器读回 |
| `JSONObject` / `JSONArray` 类型 | 实现 `Map` / `List` | 不实现 | 属编译期差异，迁移时开发者可直接发现 |
| `TypeReference` | `com.alibaba.fastjson.TypeReference` | 用 Jackson 的 `TypeReference` | 同上 |
| 单值转集合 | `"a"` → `["a"]` | 抛异常 | 特例，不予支持 |
| 空串转集合 | `""` → `null` | 抛异常 | 同上 |
| Map 接口成员的实现类 | `HashMap` | `LinkedHashMap` | 保留插入顺序更可用 |
| `DEFFAULT_DATE_FORMAT` 直接赋值 | 生效 | 不生效，需调用 `setDefaultDateFormat()` | `ObjectMapper` 在类加载时已创建 |
| `@JSONType`、`SerializerFeature` 参数、`ParserConfig`、AutoType | 支持 | 不支持 | 超出兼容范围；AutoType 本身即安全风险来源 |

## 迁移指南

### 需要留意的改动点

替换 import 后仍需人工确认的地方：

1. **泛型解析**。原 `com.alibaba.fastjson.TypeReference` 需改为
   `com.fasterxml.jackson.core.type.TypeReference`，编译器会提示。
2. **`JSONObject` 当 `Map` 用**。形如
   `Map<String, Object> m = JSON.parseObject(s);` 会编译失败，需显式调用
   `getInnerMap()`。
3. **字段顺序**。若有依赖序列化字段顺序的快照测试、报文比对或签名计算，
   需重新生成基线。
4. **浮点数类型**。`JSON.parseObject(s).get("x")` 现在返回 `BigDecimal`，
   若原代码强转 `Double` 会抛 `ClassCastException`。
5. **全局日期格式**。`JSON.DEFFAULT_DATE_FORMAT = "..."` 改为
   `JSON.setDefaultDateFormat("...")`。

### 渐进式迁移

原生 `@JSONField` 会被一并识别，因此注解可以暂不替换，先只改
`JSON` / `JSONObject` / `JSONArray` 的 import，分批推进。

## 高级用法

需要 Jackson 原生能力时可取出内部 `ObjectMapper`：

```java
ObjectMapper mapper = JSON.getObjectMapper();
```

该实例全局共享且线程安全，**请勿修改其配置**，否则会影响所有调用方。
若需不同配置，请自行 `copy()` 后使用。

也可以把兼容能力挂到自己的 `ObjectMapper` 上：

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new FastjsonCompatModule());
```

## 构建与测试

```bash
# 构建
mvn clean package

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=JavaTimeCompatibilityTest
```

测试策略是与原生 fastjson **逐项对比**：同一份输入分别喂给 fastjson 和本兼容层，
断言两者结果一致，而非只断言预期值。因此测试范围（test scope）依赖 fastjson。

测试类分布：

| 测试类 | 覆盖范围 |
|---|---|
| `JsonCompatibilityTest` | 核心 API 等效性、`@JSONField` |
| `JavaTimeCompatibilityTest` | java.time 类型的序列化、反序列化、`format` |
| `DefaultBehaviorCompatibilityTest` | fastjson 默认开关、`TypeCast` 转换语义 |
| `RawCollectionElementTypeTest` | 裸集合的元素类型、回写序列化 |
| `NativeJSONFieldAnnotationTest` | 原生 `@JSONField` 识别 |
| `MissingCoverageTest` | 边界与补充覆盖 |

## 设计原则

1. **不追求完整复刻**。只模拟迁移中真正会踩到的行为，逐项评估后决定。
2. **优先防范隐性故障**。编译期就能暴露的差异（如缺失的 `Map` 接口）交由开发者
   处理；重点对齐"编译通过但运行时行为变化"的场景。
3. **不牺牲 JSON 规范性**。fastjson 的非合规输出（如无引号 Map key）不予模拟。
4. **不引入 AutoType**。该特性是 fastjson 历史漏洞的主要来源，本项目不提供。
5. **改动均有测试固定**。包括刻意不对齐的决定，也用反向断言写成用例，
   避免日后被误当作缺陷"修复"。

## 参与贡献

欢迎提交 issue 和 pull request。提交 PR 前请确认：

- `mvn test` 全部通过；
- 新增行为对齐时，附上与原生 fastjson 的对比测试；
- 若某项 fastjson 行为经评估决定**不**对齐，请写反向断言用例并说明理由。

## License

本项目基于 [Apache License 2.0](LICENSE) 发布。

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
