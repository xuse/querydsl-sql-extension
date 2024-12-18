## 修订记录 / ChangeLog

**版本编号方式：** 版本号由两部分组成，前一个数字是对应的querydsl库版本号，后一个是此扩展框架的修订号。从1开始向上，每个修订都向下兼容。

```
v{querydsl 版本号} - r(extension version)
```

**v5.0.0-r130**
2024-11-27
 * New module for r2dbc and r2dbc-spring transaction
 * Supports DDL on H2DB.
 * Upgerade JUnit to Junit.jupiter

**v5.0.0-r120**

2024-10-01

* 工程拆分，将核心不使用的类拆分到其他工程中。拆分出querydsl-sql-extension-spring，用于Spring下集成。querydsl-sql-extension不再依赖任何Spring Framework和FastJSON。
* 构建期自动执行单元测试并输出报告，目前行覆盖65%，下个版本继续改进。
* 测试代码增加了MySQL Mock驱动，用于单元测试时模拟MySQL数据库行为。
* 对工具类的单元测试，修复几处边界数值下的小错误。
* AlterTableQuery功能增强，支持列修改、更名等操作。

**v5.0.0-r110**

2024-09-01

* PostgreSQL DDL支持。基于PostgreSQL 10.3测试。常规DDL操作已经支持完成。
* PostgreSQL表分区功能支持，但PostgreSQL分区机制与MySQL差异很大，目前仅支持创建分区表，添加/删除分区。
  不支持Hash分区，
  不支持为已有的表添加/删除分区配置（机制上无法支持，Postgresql分区表在创建时就已明确是是否分区，之后无法修改）
  不支持重组织分区
  上述由数据库机制差异造成的特性差异，暂无支持计划。
* Postgresql的支持比预想更为复杂，为此重构了Schema获取和DDL部分生成的机制。 
* 优化BatchInsert下的数据插入性能。尤其对MySQL下的批量写入，默认从JDBC Batch更换为Bulk SQL语句，某些场景下性能提升约65倍。
* QueryDSL Insert Batch功能调整，增加参数一致化处理。
* 数据初始化增加配置，setPrimaryKeys用于控制主键列是否要写入到数据库。

**v5.0.0-r104**

2024-08-08

* 支持GraalVM的Native模式使用
* 支持在java 16以上环境使用record类型作为实体。

**v5.0.0-r102**

2024-07-24

* 按包整理javadoc，部分类更换包位置。补充重要类的双语javadoc。
* 支持外部扩展自行实现Facade。通过自行实现ExtensionQueryFactory接口，扩展各种查询API。
* 提供GenericRepository类，在Spring下可以继承该类后无需编写任何代码实现常用repository的功能。支持多种API风格。
* 【实验性】支持无Query Class的纯POJO映射使用。使用Bean Class代替QueryClass。使用方法引用Lambda代替模型字段。

**v5.0.0-r101**

2024-06-28

* 增加了一个供上层业务调整表名的机制，以支持业务层分表场景。
* 支持MySQL Partition管理。支持RANGE/LIST/HASH/KEY等分区类型，支持分区的创建，调整，删除，重组等操作。
* 补充表约束和索引的缺失接口。部分操作MySQL支持不锁表的Online操作。

**v5.0.0-r100**

2024-06-11

* 完成DDL相关表语法支持。部分类名重构调整。
* 版本号100开始，保持三位数。QueryDSL官方版本5.1.0是需要Java 11以上的，目前还不打算跟进支持。 

**v5.0.0-r8 **

2018年

* 升级支持Querydsl v5.0.0
* 添加MySQL方言：com.querydsl.core.types.dsl.MySQLWithJSONTemplates、JsonExpressions等，支持JSON字段操作
* 注解@AutoGenerated，支持字段内容自动生成(在update/insert时)。如使用populate()方法则自动写入，如使用set()方法，可调用populateAutoGeneratedColumns()方法生成。
* 增加GUID、SnowFlake ID等生成规则

**v4.1.1-r4**

* 增加更适合于linux下的简洁日志。
* 结果集拼接性能优化。提供额外的RelationalPathBaseEx用于继承。
* API增加——增加DDL语法框架（尚未实现）。

**v4.2.1-r1 **

2017年

1. 日志扩展
2. 关于Connection is not transactional异常修复，还有一些其他方面的个性化要求，因此通过本项目进行扩展。后续随功能更新。