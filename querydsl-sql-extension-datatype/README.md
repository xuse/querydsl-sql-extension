# querydsl-sql-extension-datatype



这是一个功能扩展用模块。

## 功能

在QueryDSL中，允许自定义java类型到数据库类型的映射。为了更方便的使用这种映射，querydsl-sql-extension增加一个@CustomType的注解。

这个模块提供了若干常用的自定义映射策略，部分举例如下——

| 类名                            | 用途                                                         |
| ------------------------------- | ------------------------------------------------------------ |
| AbstractBase64EncryptStringType | 抽象类，继承后实现加密和解密方法，可对保存到数据库的信息进行加密。例如身份证或手机号等敏感信息。 |
| DateHoursAsShortType            | 低精度时间存储。使用SMAILLINT(2bytes) 可以表示UTC: 2023-12-31 20:00:00 to 2053-11-26 08:00:00的时间范围。 |
| DateSecondsAsIntegerType        | 低精度时间存储。使用INT(4bytes) 可以表示1970-01-01 00:00:00 to 2106-02-07 6:28:14中的每秒。 |
| DayAsSmallIntType               | 低精度时间存储。使用SMAILLINT(2bytes)表达日期                |
| IntegerASVarcharType            | java中Integer类型，数据库用varchar存储。                     |
| JSONObjectType                  | 最实用的映射类型。将一个复杂对象JSON序列化后存入数据库。(使用fastJson，如您不喜可自行用其他框架编写) |
| LongASDateTimeType              | java中long类型时间戳，数据库中使用TIMESTAMP类型              |
| LongASVarcharType               | java中的Long类型，转为字符串后存在数据库。                   |
| StringAsBigIntType              | 数据库中BIG INT类型，Java中用字符串表示。                    |
| StringAsDateTimeType            | 数据库中Timestamp类型，java中字符串表示。                    |
| StringAsDateType                | 数据库中Date类型，java中字符串表示。                         |
| StringAsIntegerType             | 数据库中Integer类型，Java中用String表示。                    |

其他——

* 一个轻量级的二进制数据序列化器。辅助开发者将复杂对象以二进制方式写入到数据库中。



